# Deployment Guide

This guide describes how to deploy the **Smart Manufacturing Order Processing System** across different environments, with a focus on **Cloudflare** and **Docker**.

---

## 1. Project Directory Structure Overview

The project is structured as a clean modular multi-module project:

```text
├── backend/                  # Spring Boot 3.2.5 Web Application (Thymeleaf + REST API)
│   ├── src/                  # Java source code and templates
│   ├── pom.xml               # Backend Maven dependencies and build plugin
│   ├── Dockerfile            # Multi-stage container build for backend
│   └── .env.example          # Backend-specific environment variables
├── desktop-client/           # Standalone Java Swing Production Terminal
│   ├── src/                  # Desktop terminal source code
│   └── pom.xml               # Desktop Maven build descriptor
├── database/                 # SQL Initialization scripts
│   ├── schema.sql            # Table definitions
│   └── seed.sql              # Initial demo data and credentials
├── deploy/                   # Deployment configurations
│   ├── README.md             # This deployment guide
│   └── cloudflare/           # Cloudflare Tunnel configs & Compose setup
├── docker-compose.yml        # Multi-container orchestration (MySQL + Backend + Cloudflare)
├── Dockerfile                # Root-level multi-stage container build
├── pom.xml                   # Root Maven Aggregator POM
└── .env.example              # Centralized environment template
```

---

## 2. Deploying on Render (Recommended: 24/7 Free Live Hosting)

To keep the application permanently live online 24/7 with zero local computer dependencies, deploy directly to **Render.com**:

### Option A: Instant 1-Click Deploy
1. Click the 1-click deployment link:
   **[Deploy to Render](https://render.com/deploy?repo=https://github.com/uditdev0523/OOTs-support-material-)**
2. Sign in to Render (via GitHub).
3. Render automatically parses `render.yaml`, configures the Docker container, memory constraints (`-Xmx384m`), health check (`/login`), and assigns a free permanent HTTPS domain:
   `https://smart-manufacturing-system.onrender.com`

### Option B: From Render Dashboard
1. Go to [Render Dashboard](https://dashboard.render.com).
2. Click **New +** -> **Blueprint**.
3. Select your repository: `uditdev0523/OOTs-support-material-`.
4. Click **Apply**. Render will automatically build the `Dockerfile` and keep it live 24/7!

---

## 3. Alternative: Deploying with Cloudflare

Because Spring Boot + MySQL is a full-stack, stateful server application, the most reliable and production-grade way to deploy it behind Cloudflare is via **Cloudflare Tunnel (`cloudflared`)**.

### Benefits of Cloudflare Tunnel:
- **Zero Inbound Ports:** No need to open ports 80/443 on your firewall or router.
- **DDoS Protection & SSL:** Cloudflare automatically provides free SSL/TLS certificates and edge protection.
- **Custom Domain:** Routes your custom domain (e.g., `manufacturing.yourdomain.com`) directly to your Docker container.

### Step-by-Step Cloudflare Tunnel Setup:

#### Option A: Cloudflare Zero Trust Dashboard (Recommended)
1. Go to [Cloudflare Zero Trust Dashboard](https://one.dash.cloudflare.com/).
2. Navigate to **Networks** -> **Tunnels** -> **Create a Tunnel**.
3. Choose **Cloudflared** as the connector.
4. Name your tunnel (e.g. `smart-manufacturing`).
5. Copy the **Tunnel Token** provided in the dashboard.
6. Under **Public Hostname**:
   - Subdomain/Domain: e.g. `mfg.yourdomain.com`
   - Service Type: `HTTP`
   - URL: `app:8081` (inside Docker network) or `localhost:8081` (if running natively).
7. In your project root, add your token to `.env`:
   ```bash
   CLOUDFLARE_TUNNEL_TOKEN=eyJhIjoi...your_token_here...
   ```
8. Start the stack with the Cloudflare profile:
   ```bash
   docker compose --profile cloudflare up -d
   ```
   Your application is now live on your Cloudflare domain!

#### Option B: Quick Free Tunnel (No Domain / Token Required)
If you want to immediately test public HTTPS access via Cloudflare without configuring a domain or token:
1. Start the backend:
   ```bash
   docker compose up -d
   ```
2. Run Cloudflare Quick Tunnel:
   ```bash
   docker run --rm -it --network host cloudflare/cloudflared:latest tunnel --url http://localhost:8081
   ```
   Cloudflare will output a temporary public HTTPS link (e.g. `https://random-words.trycloudflare.com`) accessible worldwide.

---

## 3. Standard Docker & Docker Compose Deployment

### Running with MySQL (Production Profile)
1. Copy the environment file:
   ```bash
   cp .env.example .env
   ```
2. Run the compose stack:
   ```bash
   docker compose up -d --build
   ```
3. Access the web app at `http://localhost:8081`.
4. Default credentials:
   - **Admin:** `admin` / `admin123`
   - **Operator:** `operator` / `operator123`
   - **Supervisor:** `supervisor` / `supervisor123`

### Building Single Container
To build just the Spring Boot container:
```bash
docker build -t smart-mfg-backend ./backend
```
Run with embedded H2 (zero database setup needed):
```bash
docker run -p 8081:8081 -e SPRING_PROFILES_ACTIVE=default smart-mfg-backend
```

---

## 4. Building with Maven

- **Build everything from root:**
  ```bash
  mvn clean package -DskipTests
  ```
  Generates:
  - `backend/target/smart-manufacturing-order-processing-system-1.0.0.jar`
  - `desktop-client/target/production-terminal.jar`

- **Build only the backend:**
  ```bash
  cd backend
  mvn clean package -DskipTests
  ```
