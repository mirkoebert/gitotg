package com.mirkoebert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) {
                http
                        .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/", "/login**", "/error", "/about", "/putting-index").permitAll()
                                .requestMatchers("/images/**", "/css/**", "/js/**").permitAll()
                                .anyRequest().authenticated()
                        )
                        .oauth2Login(oauth2 -> oauth2
                                .loginPage("/login")
                                .defaultSuccessUrl("/user-page", true)
                                .failureUrl("/login?error=true")
                        )
                        .logout(logout -> logout
                                .logoutSuccessUrl("/login?logout=true")
                                .permitAll()
                        );
                return http.build();
        }
}
