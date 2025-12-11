package com.umari.queueManager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class securityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desativa CSRF para facilitar chamadas REST
                .authorizeHttpRequests(auth -> auth
                        // 1. PÚBLICO: Qualquer um pode criar uma senha (Cliente)
                        .requestMatchers(HttpMethod.POST, "/api/tickets").permitAll()

                        // 1.1 PÚBLICO: WebSocket (para o atendente receber notificações sem bloqueio de protocolo)
                        .requestMatchers("/ws-queue/**").permitAll()

                        // 2. PRIVADO: Só o atendente logado pode listar, chamar ou mudar status
                        .requestMatchers(HttpMethod.GET, "/api/tickets").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/**").authenticated()

                        // Bloqueia qualquer outra coisa por padrão
                        .anyRequest().authenticated()
                )
                // Habilita o login básico (janela de popup ou header Auth) para o atendente
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}