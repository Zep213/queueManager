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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // Desativa proteção CSRF para facilitar
                .authorizeHttpRequests(auth -> auth
                        // 1. RECURSOS PÚBLICOS (Não pede senha)
                        .requestMatchers("/", "/index.html").permitAll() // A página do Totem
                        .requestMatchers("/css/**", "/js/**").permitAll() // Estilos e Scripts
                        .requestMatchers(HttpMethod.POST, "/api/tickets").permitAll() // Criar senha
                        .requestMatchers("/ws-queue/**").permitAll() // WebSocket

                        // 2. RECURSOS PRIVADOS (Pede senha)
                        .requestMatchers("/admin.html").authenticated() // <--- A página do Admin
                        .requestMatchers(HttpMethod.GET, "/api/tickets").authenticated() // Listar fila
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/**").authenticated() // Chamar/Atender
                        .requestMatchers(HttpMethod.POST, "/api/tickets/proximo").authenticated() // Botão Próximo

                        // Qualquer outra coisa exige login
                        .anyRequest().authenticated()
                )
                // Usa o formulário de login padrão do browser ou do Spring
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}