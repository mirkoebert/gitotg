package com.mirkoebert;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                        .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/", "/login**", "/error", "/about").permitAll()  // Allow public access to home and login
                                .anyRequest().authenticated()  // Protect other routes
                        )
                        .oauth2Login(oauth2 -> oauth2
                                .loginPage("/login")  // Optional custom login page
                                .defaultSuccessUrl("/user-page", true)  // Redirect after successful login
                                .failureUrl("/login?error=true")  // Handle failures
                        )
                        .logout(logout -> logout
                                .logoutSuccessUrl("/login?logout=true")
                                .permitAll()
                        );
                return http.build();
        }
}
