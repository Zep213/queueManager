package com.umari.queueManager.config;

import com.umari.queueManager.Model.Usuario;
import com.umari.queueManager.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class DataInitializer {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.atendente.username}")
    private String atendenteUsername;

    @Value("${app.atendente.password}")
    private String atendentePassword;

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.count() == 0) {
                log.warn("Criando usuários padrão. Configure ADMIN_USERNAME/ADMIN_PASSWORD via variáveis de ambiente em produção.");

                Usuario admin = new Usuario();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");
                repository.save(admin);

                Usuario guiche = new Usuario();
                guiche.setUsername(atendenteUsername);
                guiche.setPassword(passwordEncoder.encode(atendentePassword));
                guiche.setRole("USER");
                repository.save(guiche);

                log.info("Usuários iniciais criados no banco.");
            }
        };
    }
}