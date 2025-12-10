package com.umari.queueManager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class securityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desativa a proteção CSRF (necessário para o Postman fazer POST/PUT)
                .csrf(csrf -> csrf.disable())

                // Configura as permissões de acesso
                .authorizeHttpRequests(auth -> auth
                        // Permite acesso total a qualquer rota
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}