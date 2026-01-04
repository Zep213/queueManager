package com.umari.queueManager.config;

// 1. IMPORTANTE: Importar o seu serviço de autenticação
import com.umari.queueManager.service.AutenticacaoService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;

@Configuration
@EnableWebSecurity
public class securityConfig {

    // 2. IMPORTANTE: Declarar a variável do serviço aqui no topo
    private final AutenticacaoService autenticacaoService;

    // 3. IMPORTANTE: Criar o construtor para receber (injetar) o serviço
    public securityConfig(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Rotas Públicas
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/", "/index.html", "/login.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tickets").permitAll()
                        .requestMatchers("/api/tickets/info-totem", "/api/tickets/fila/tamanho").permitAll()
                        .requestMatchers("/ws-queue/**").permitAll()

                        // Rotas Restritas (Gerente)
                        .requestMatchers("/admin.html", "/api/tickets/dashboard", "/api/tickets/estatisticas").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // Rotas de Atendimento (Qualquer funcionário)
                        .requestMatchers("/atendente.html").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler(redirecionamentoInteligente())
                        .failureUrl("/login.html?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll()
                )
                // Removemos a configuração antiga de userDetailsService daqui
                .authenticationManager(authenticationManager());

        return http.build();
    }

    // 4. Configuração do AuthenticationManager (AQUI OCORRIA O ERRO)
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Agora ele encontra a variável 'autenticacaoService' declarada lá no topo
        provider.setUserDetailsService(autenticacaoService);

        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Lógica de Redirecionamento após Login
    @Bean
    public AuthenticationSuccessHandler redirecionamentoInteligente() {
        return (request, response, authentication) -> {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin.html");
                    return;
                }
            }
            response.sendRedirect("/atendente.html");
        };
    }
}