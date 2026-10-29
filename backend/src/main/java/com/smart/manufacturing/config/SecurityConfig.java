package com.smart.manufacturing.config;

import com.smart.manufacturing.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http
            .authenticationProvider(authProvider)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            .headers(headers -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/login", "/h2-console/**").permitAll()
                .requestMatchers("/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/inventory/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_INVENTORY_COORDINATOR", "ROLE_SUPERVISOR")
                .requestMatchers("/production/**", "/scheduling/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_PRODUCTION_MANAGER", "ROLE_SUPERVISOR")
                .requestMatchers("/reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERVISOR", "ROLE_PRODUCTION_MANAGER")
                .requestMatchers("/customers/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ORDER_STAFF", "ROLE_SUPERVISOR")
                .requestMatchers("/orders/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ORDER_STAFF", "ROLE_PRODUCTION_MANAGER", "ROLE_SUPERVISOR")
                .requestMatchers("/products/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_ORDER_STAFF", "ROLE_INVENTORY_COORDINATOR", "ROLE_PRODUCTION_MANAGER", "ROLE_SUPERVISOR")
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/dashboard", "/").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .httpBasic(org.springframework.security.config.Customizer.withDefaults())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
