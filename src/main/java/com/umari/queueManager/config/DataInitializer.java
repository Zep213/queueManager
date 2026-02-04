package com.umari.queueManager.config;

import com.umari.queueManager.Model.Usuario;
import com.umari.queueManager.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("#Admin20261#%");
                admin.setPassword(passwordEncoder.encode("#Admin202601#$"));
                admin.setRole("ADMIN");
                repository.save(admin);

                Usuario guiche = new Usuario();
                guiche.setUsername("#Atendimento202601#$");
                guiche.setPassword(passwordEncoder.encode("Atendimento202601#%"));
                guiche.setRole("USER");
                repository.save(guiche);

                System.out.println("✅ Usuários iniciais criados no banco!");
            }
        };
    }
}