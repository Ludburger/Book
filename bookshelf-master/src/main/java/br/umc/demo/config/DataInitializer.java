package br.umc.demo.config;

import br.umc.demo.entity.Role;
import br.umc.demo.entity.User;
import br.umc.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed de dados para ambiente de desenvolvimento e aula.
 *
 * ============================================================
 * ATENÇÃO: USE APENAS EM DESENVOLVIMENTO / AULA
 * NÃO inclua seeds com credenciais fixas em ambiente de produção.
 * ============================================================
 *
 * Credenciais do administrador de aula:
 *   Email: admin@biblioteca.com
 *   Senha: Admin@123
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@biblioteca.com")) {
                User admin = new User();
                admin.setFullName("Administrador da Biblioteca");
                admin.setEmail("admin@biblioteca.com");
                admin.setPhone("11999990000");
                // Mesmo aqui, a senha é hashada com Argon2 — nunca em texto puro
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                System.out.println("==============================================");
                System.out.println("  [DEV] Admin de aula criado com sucesso!");
                System.out.println("  Email: admin@biblioteca.com");
                System.out.println("  Senha: Admin@123");
                System.out.println("==============================================");
            }
        };
    }
}
