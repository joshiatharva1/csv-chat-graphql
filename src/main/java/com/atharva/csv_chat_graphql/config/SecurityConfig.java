package com.nupur.csv_chat_graphql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for simplicity (OK for your demo)
                .csrf(csrf -> csrf.disable())

                // For now: everything is allowed (no login)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",              // index.html
                                "/index.html",
                                "/api/upload-csv",
                                "/graphql",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}