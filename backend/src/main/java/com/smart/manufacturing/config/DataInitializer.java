package com.smart.manufacturing.config;

import com.smart.manufacturing.entity.*;
import com.smart.manufacturing.enums.*;
import com.smart.manufacturing.repository.*;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.SchedulingService;
import com.smart.manufacturing.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ManufacturingOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ProductionScheduleRepository scheduleRepository;
    private final ProductionTaskRepository taskRepository;
    private final UserService userService;
    private final SchedulingService schedulingService;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository,
                           InventoryRepository inventoryRepository,
                           ManufacturingOrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           OrderStatusHistoryRepository statusHistoryRepository,
                           ProductionScheduleRepository scheduleRepository,
                           ProductionTaskRepository taskRepository,
                           UserService userService,
                           SchedulingService schedulingService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.scheduleRepository = scheduleRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.schedulingService = schedulingService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized with seed data. Skipping DataInitializer.");
            return;
        }

        log.info("Starting initial seed data population for Smart Manufacturing Order Processing System...");

        // 1. Roles
        Map<UserRole, Role> roleMap = new EnumMap<>(UserRole.class);
        for (UserRole roleName : UserRole.values()) {
            Role role = roleRepository.save(new Role(roleName, roleName.getDescription()));
            roleMap.put(roleName, role);
        }

        // 2. Demo Users (Admin, Manager, Staff, Inventory Coordinator, Supervisor)
        userService.registerUser("admin", "Mfg#Shield2026", "Rajesh Sharma", "admin@smartmfg.com", "Executive Leadership",
                Set.of(UserRole.ROLE_ADMIN));
        userService.registerUser("manager", "Mfg#Shield2026", "Anita Deshmukh", "manager@smartmfg.com", "Production Operations",
                Set.of(UserRole.ROLE_PRODUCTION_MANAGER));
        userService.registerUser("staff", "Mfg#Shield2026", "Vikram Malhotra", "staff@smartmfg.com", "Sales & Order Entry",
                Set.of(UserRole.ROLE_ORDER_STAFF));
        userService.registerUser("inventory", "Mfg#Shield2026", "Suresh Pillai", "inventory@smartmfg.com", "Warehouse & Inventory",
                Set.of(UserRole.ROLE_INVENTORY_COORDINATOR));
        userService.registerUser("supervisor", "Mfg#Shield2026", "Neha Kapoor", "supervisor@smartmfg.com", "Plant Quality & Oversight",
                Set.of(UserRole.ROLE_SUPERVISOR));

        User adminUser = userRepository.findByUsername("admin").orElseThrow();

        // 3. 12 Customers
        List<Customer> customers = new ArrayList<>();
        customers.add(new Customer("CUST-1001", "Acme Industrial Corp", "purchasing@acmeind.com", "+91 98200 11221", "Acme Industrial Ltd", "Plot 42, MIDC Industrial Area, Pune"));
        customers.add(new Customer("CUST-1002", "Titan Aerospace Ltd", "procure@titanaero.com", "+91 98101 22334", "Titan Aerospace", "Whitefield Tech Park, Bengaluru"));
        customers.add(new Customer("CUST-1003", "Apex Robotics Pvt Ltd", "orders@apexrobotics.in", "+91 98450 33445", "Apex Robotics", "Cyber City, Gurugram"));
        customers.add(new Customer("CUST-1004", "Bharat Heavy Motors", "supply@bhmotors.co.in", "+91 97660 44556", "Bharat Motors Group", "Pithampur Auto Cluster, Indore"));
        customers.add(new Customer("CUST-1005", "Dynamic Hydraulics", "contact@dynamichyd.com", "+91 98220 55667", "Dynamic Power & Hydraulics", "GIDC Estate, Vadodara"));
        customers.add(new Customer("CUST-1006", "Zenith Electronics", "purchase@zenithelec.com", "+91 99100 66778", "Zenith Systems Ltd", "Electronic City Phase 1, Bengaluru"));
        customers.add(new Customer("CUST-1007", "Precision Gears & Axles", "sales@precisiongears.in", "+91 98900 77889", "Precision Engineering", "Ambattur Industrial Estate, Chennai"));
        customers.add(new Customer("CUST-1008", "Matrix Automated Plants", "info@matrixplants.com", "+91 98300 88990", "Matrix Solutions", "Salt Lake Sector V, Kolkata"));
        customers.add(new Customer("CUST-1009", "Omni Automation Tools", "buy@omniauto.com", "+91 98720 99001", "Omni Automation", "Phase 8 Industrial Focal Point, Mohali"));
        customers.add(new Customer("CUST-1010", "Quantum Mechatronics", "orders@quantummech.in", "+91 98480 12345", "Quantum Technologies", "HITEC City, Hyderabad"));
        customers.add(new Customer("CUST-1011", "Vanguard Metal Works", "logistics@vanguardmetal.com", "+91 98660 23456", "Vanguard Industries", "Balanagar, Hyderabad"));
        customers.add(new Customer("CUST-1012", "Starlight Energy Grids", "procurement@starlightenergy.in", "+91 98230 34567", "Starlight Power Ltd", "Nariman Point, Mumbai"));

        for (Customer c : customers) {
            customerRepository.save(c);
        }

        // 4. 22 Products with Inventories
        Object[][] prodData = {
                {"PRD-R01", "6-Axis Industrial Robotic Arm", "Heavy duty 20kg payload articulation arm", "Robotics", 14500.00, 18, 5, 12},
                {"PRD-R02", "Delta High-Speed Pick Robot", "High-accuracy packaging delta mechanism", "Robotics", 9800.00, 12, 4, 8},
                {"PRD-R03", "Automated Guided Vehicle (AGV)", "Autonomous material delivery floor rover", "Robotics", 18500.00, 24, 3, 6},
                {"PRD-E01", "PLC Master Controller Board", "Industrial 32-channel I/O PLC logic processor", "Electronics", 750.00, 6, 25, 60},
                {"PRD-E02", "Optical Vision Inspection Sensor", "Sub-millimeter edge quality detection unit", "Electronics", 1250.00, 8, 15, 35},
                {"PRD-E03", "Digital Inverter Drive 15kW", "Variable frequency motor velocity controller", "Electronics", 890.00, 6, 20, 45},
                {"PRD-E04", "Solid State Industrial Relay", "High cycle switching surge protected relay", "Electronics", 85.00, 2, 100, 250},
                {"PRD-E05", "Thermal Infrared Line Scanner", "Continuous surface temperature monitor", "Electronics", 2100.00, 10, 10, 4}, // Low stock
                {"PRD-M01", "Precision Planetary Gearbox 1:10", "Zero-backlash robotic drive transmission", "Mechanical", 1120.00, 10, 15, 28},
                {"PRD-M02", "CNC Linear Ball Screw 1200mm", "Hardened steel precision positioning screw", "Mechanical", 480.00, 8, 20, 50},
                {"PRD-M03", "Pneumatic Gripper Module 80N", "Dual jaw workpiece handling clamp", "Mechanical", 320.00, 4, 30, 75},
                {"PRD-M04", "Vibration Dampened Base Mount", "Cast iron machine anchor shock absorber", "Mechanical", 210.00, 4, 40, 90},
                {"PRD-M05", "Tungsten Carbide Milling Head", "High durability 5-axis tooling spindle insert", "Mechanical", 540.00, 5, 25, 18}, // Low stock
                {"PRD-H01", "High-Pressure Hydraulic Cylinder", "250 bar double acting stamping cylinder", "Hydraulics", 1650.00, 14, 10, 22},
                {"PRD-H02", "Proportional Servo Valve Unit", "Digital flow metering proportional valve", "Hydraulics", 1420.00, 12, 12, 25},
                {"PRD-H03", "Hydraulic Power Pack 10HP", "Self-contained reservoir and pump station", "Hydraulics", 3800.00, 20, 5, 10},
                {"PRD-H04", "Rotary Actuator 180-Degree", "Heavy torque rotary positioner module", "Hydraulics", 890.00, 8, 15, 30},
                {"PRD-A01", "Braking Caliper Hydraulic Core", "Automotive heavy transit disc caliper", "Automotive", 620.00, 6, 25, 60},
                {"PRD-A02", "Forged Steel Crankshaft Unit", "4-cylinder balanced engine drive shaft", "Automotive", 1850.00, 16, 8, 15},
                {"PRD-A03", "Turbocharger Impeller Turbine", "Titanium alloy aero-thermo compression turbine", "Automotive", 940.00, 10, 12, 5}, // Low stock
                {"PRD-A04", "Electric Steering Rack 48V", "Assisted electronic rack & pinion assembly", "Automotive", 1280.00, 12, 10, 24},
                {"PRD-A05", "Exhaust Gas Recirculation Valve", "High temperature solenoid controlled valve", "Automotive", 310.00, 4, 30, 80}
        };

        List<Product> products = new ArrayList<>();
        for (Object[] p : prodData) {
            String code = (String) p[0];
            String name = (String) p[1];
            String desc = (String) p[2];
            String cat = (String) p[3];
            BigDecimal price = BigDecimal.valueOf((Double) p[4]);
            int duration = (Integer) p[5];
            int minStock = (Integer) p[6];
            int stock = (Integer) p[7];

            Product product = new Product(code, name, desc, cat, price, duration, minStock);
            Product savedProduct = productRepository.save(product);

            Inventory inventory = new Inventory(savedProduct, stock, minStock);
            inventoryRepository.save(inventory);
            savedProduct.setInventory(inventory);

            products.add(savedProduct);
        }

        // 5. 24 Orders with items, priorities, and statuses
        LocalDate today = LocalDate.now();

        // Template order configurations (customerIdx, priority, status, deliveryOffsetDays, items:[[prodIdx, qty]])
        Object[][] orderConfigs = {
                {0, Priority.URGENT, OrderStatus.IN_PROGRESS, 2, new int[][]{{0, 2}, {3, 4}}},
                {1, Priority.HIGH, OrderStatus.SCHEDULED, 4, new int[][]{{1, 1}, {8, 2}}},
                {2, Priority.URGENT, OrderStatus.APPROVED, 3, new int[][]{{2, 1}, {4, 3}}},
                {3, Priority.NORMAL, OrderStatus.VALIDATED, 7, new int[][]{{5, 5}, {6, 20}}},
                {4, Priority.LOW, OrderStatus.CREATED, 12, new int[][]{{10, 4}, {11, 8}}},
                {5, Priority.HIGH, OrderStatus.COMPLETED, -5, new int[][]{{3, 6}, {9, 4}}},
                {6, Priority.URGENT, OrderStatus.COMPLETED, -2, new int[][]{{0, 1}, {13, 2}}},
                {7, Priority.NORMAL, OrderStatus.IN_PROGRESS, 3, new int[][]{{14, 2}, {15, 1}}},
                {8, Priority.HIGH, OrderStatus.SCHEDULED, 5, new int[][]{{17, 4}, {18, 2}}},
                {9, Priority.LOW, OrderStatus.CREATED, 15, new int[][]{{21, 10}, {6, 15}}},
                {10, Priority.URGENT, OrderStatus.IN_PROGRESS, -1, new int[][]{{1, 2}, {8, 3}}}, // Delayed urgent!
                {11, Priority.HIGH, OrderStatus.APPROVED, 6, new int[][]{{2, 1}, {7, 1}}},
                {0, Priority.NORMAL, OrderStatus.VALIDATED, 8, new int[][]{{16, 2}, {4, 2}}},
                {1, Priority.LOW, OrderStatus.COMPLETED, -10, new int[][]{{11, 12}, {10, 6}}},
                {2, Priority.URGENT, OrderStatus.SCHEDULED, 1, new int[][]{{0, 1}, {4, 2}}},
                {3, Priority.HIGH, OrderStatus.IN_PROGRESS, 4, new int[][]{{13, 3}, {14, 3}}},
                {4, Priority.NORMAL, OrderStatus.CREATED, 10, new int[][]{{3, 8}, {5, 4}}},
                {5, Priority.NORMAL, OrderStatus.COMPLETED, -4, new int[][]{{17, 6}, {20, 2}}},
                {6, Priority.HIGH, OrderStatus.CANCELLED, 5, new int[][]{{2, 2}}},
                {7, Priority.URGENT, OrderStatus.APPROVED, 2, new int[][]{{18, 2}, {19, 1}}},
                {8, Priority.LOW, OrderStatus.CREATED, 14, new int[][]{{6, 30}, {10, 5}}},
                {9, Priority.NORMAL, OrderStatus.VALIDATED, 9, new int[][]{{8, 4}, {9, 6}}},
                {10, Priority.HIGH, OrderStatus.IN_PROGRESS, -2, new int[][]{{15, 1}, {16, 3}}}, // Delayed high!
                {11, Priority.URGENT, OrderStatus.COMPLETED, -1, new int[][]{{0, 1}, {1, 1}}}
        };

        for (int i = 0; i < orderConfigs.length; i++) {
            Object[] cfg = orderConfigs[i];
            int custIdx = (Integer) cfg[0];
            Priority priority = (Priority) cfg[1];
            OrderStatus status = (OrderStatus) cfg[2];
            int offsetDays = (Integer) cfg[3];
            int[][] itemsSpec = (int[][]) cfg[4];

            Customer customer = customers.get(custIdx);
            LocalDate orderDate = today.minusDays(Math.max(1, 10 - offsetDays));
            LocalDate reqDelivery = today.plusDays(offsetDays);
            String orderNumber = String.format("ORD-2026-%04d", 1001 + i);

            ManufacturingOrder order = new ManufacturingOrder(
                    orderNumber,
                    customer,
                    orderDate,
                    reqDelivery,
                    priority,
                    "Automated production run for " + customer.getCompany()
            );
            order.setStatus(status);

            for (int[] itemPair : itemsSpec) {
                Product p = products.get(itemPair[0]);
                int qty = itemPair[1];
                OrderItem item = new OrderItem(order, p, qty, p.getUnitPrice());
                order.addItem(item);
            }

            ManufacturingOrder savedOrder = orderRepository.save(order);

            // Create status history chain
            statusHistoryRepository.save(new OrderStatusHistory(
                    savedOrder, null, OrderStatus.CREATED, adminUser, "Order logged into ERP"));

            if (status != OrderStatus.CREATED) {
                statusHistoryRepository.save(new OrderStatusHistory(
                        savedOrder, OrderStatus.CREATED, OrderStatus.VALIDATED, adminUser, "Passed specification checks"));
            }
            if (status == OrderStatus.APPROVED || status == OrderStatus.SCHEDULED ||
                status == OrderStatus.IN_PROGRESS || status == OrderStatus.COMPLETED) {
                statusHistoryRepository.save(new OrderStatusHistory(
                        savedOrder, OrderStatus.VALIDATED, OrderStatus.APPROVED, adminUser, "Order authorized and inventory allocated"));
            }
            if (status == OrderStatus.SCHEDULED || status == OrderStatus.IN_PROGRESS || status == OrderStatus.COMPLETED) {
                statusHistoryRepository.save(new OrderStatusHistory(
                        savedOrder, OrderStatus.APPROVED, OrderStatus.SCHEDULED, adminUser, "Production routing tasks scheduled"));
            }
            if (status == OrderStatus.IN_PROGRESS || status == OrderStatus.COMPLETED) {
                statusHistoryRepository.save(new OrderStatusHistory(
                        savedOrder, OrderStatus.SCHEDULED, OrderStatus.IN_PROGRESS, adminUser, "Manufacturing launched on floor"));
            }
            if (status == OrderStatus.COMPLETED) {
                statusHistoryRepository.save(new OrderStatusHistory(
                        savedOrder, OrderStatus.IN_PROGRESS, OrderStatus.COMPLETED, adminUser, "Quality inspection passed, ready for dispatch"));
            }
            if (status == OrderStatus.CANCELLED) {
                statusHistoryRepository.save(new OrderStatusHistory(
                        savedOrder, OrderStatus.CREATED, OrderStatus.CANCELLED, adminUser, "Cancelled due to client scope change"));
            }

            // Create production tasks for SCHEDULED, IN_PROGRESS, and COMPLETED orders
            if (status == OrderStatus.SCHEDULED || status == OrderStatus.IN_PROGRESS || status == OrderStatus.COMPLETED) {
                ProductionSchedule schedule = new ProductionSchedule(
                        "SCHED-" + savedOrder.getOrderNumber(),
                        today,
                        "Shift Schedule for " + savedOrder.getOrderNumber(),
                        "Standard shift execution"
                );
                scheduleRepository.save(schedule);

                int tIdx = 1;
                for (OrderItem item : savedOrder.getItems()) {
                    Product prod = item.getProduct();
                    int totalHours = prod.getProductionDurationHours() * item.getQuantity();
                    LocalDateTime start = LocalDateTime.now().minusDays(1).plusHours(tIdx * 2L);
                    LocalDateTime end = start.plusHours(totalHours);

                    ProductionTask task = new ProductionTask(
                            "TSK-" + savedOrder.getId() + "-" + tIdx + "-" + prod.getProductCode(),
                            savedOrder,
                            prod,
                            item.getQuantity(),
                            "Machining Bay " + tIdx,
                            savedOrder.getPriority(),
                            start,
                            end
                    );
                    task.setSchedule(schedule);

                    if (status == OrderStatus.COMPLETED) {
                        task.updateProgress(100, ProductionTaskStatus.COMPLETED);
                    } else if (status == OrderStatus.IN_PROGRESS) {
                        int progress = (tIdx % 2 == 0) ? 65 : 30;
                        task.updateProgress(progress, ProductionTaskStatus.IN_PROGRESS);
                    } else {
                        task.setStatus(ProductionTaskStatus.SCHEDULED);
                        task.setProgressPercentage(0);
                    }

                    taskRepository.save(task);
                    savedOrder.addTask(task);
                    tIdx++;
                }
            }
        }

        log.info("Database seed complete: 5 demo users, 12 customers, 22 products, 24 orders, production tasks, and audit logs initialized successfully.");
    }
}
