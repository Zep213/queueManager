package com.umari.queueManager.config;

// ... imports (mantenha os imports que já tinha) ...
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;

@Configuration
@EnableWebSecurity
public class securityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Estáticos e Públicos
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/", "/index.html", "/login.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tickets").permitAll()
                        .requestMatchers("/api/tickets/info-totem", "/api/tickets/fila/tamanho").permitAll()
                        .requestMatchers("/ws-queue/**").permitAll()

                        // ROTA DO GERENTE (Só Admin pode ver o dashboard)
                        .requestMatchers("/admin.html", "/api/tickets/dashboard", "/api/tickets/estatisticas").hasRole("ADMIN")

                        // ROTA DA MESA (Admin e Atendente podem trabalhar)
                        .requestMatchers("/atendente.html").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler(redirecionamentoInteligente()) // <--- AQUI ESTÁ O SEGREDO
                        .failureUrl("/login.html?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Lógica para decidir para onde mandar depois do login
    @Bean
    public AuthenticationSuccessHandler redirecionamentoInteligente() {
        return (request, response, authentication) -> {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            for (GrantedAuthority authority : authorities) {
                // Se for chefe, vai pro painel
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin.html");
                    return;
                }
            }

            // Se for peão (atendente), vai pra mesa
            response.sendRedirect("/atendente.html");
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("#Administradir20261").password("#Administrador20261#").roles("ADMIN").build();

        UserDetails g1 = User.withDefaultPasswordEncoder()
                .username("#Atendimeto2026").password("#Atendimento20261#").roles("USER").build();

        UserDetails g2 = User.withDefaultPasswordEncoder()
                .username("guiche02").password("user123").roles("USER").build();

        return new InMemoryUserDetailsManager(admin, g1, g2);
    }
}