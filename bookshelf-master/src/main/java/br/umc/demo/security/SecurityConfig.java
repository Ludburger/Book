package br.umc.demo.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração central de segurança da aplicação.
 *
 * Conceitos demonstrados nesta classe:
 * - AUTENTICAÇÃO: quem pode entrar (via JWT)
 * - AUTORIZAÇÃO: o que cada role pode fazer
 * - STATELESS: sem sessões HTTP — o token carrega a identidade do usuário
 */
@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF desabilitado: a API é stateless (JWT, sem cookies de sessão).
            // CSRF protege sessões baseadas em cookie — não se aplica a JWT.
            .csrf(csrf -> csrf.disable())

            // Sem sessões HTTP: cada requisição é autenticada pelo token JWT.
            // Isso evita que o servidor precise "lembrar" quem está logado.
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CORS: delega ao Spring MVC, que respeita os @CrossOrigin dos controllers
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of("*"));
                config.setAllowedMethods(java.util.List.of("*"));
                config.setAllowedHeaders(java.util.List.of("*"));
                return config;
            }))

            // -------------------------------------------------------
            // Tratamento de erros de autenticação e autorização
            // -------------------------------------------------------
            .exceptionHandling(ex -> ex
                // 401: usuário não autenticado tentando acessar recurso protegido
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado"))
                // 403: usuário autenticado sem permissão suficiente
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado"))
            )

            // -------------------------------------------------------
            // AUTORIZAÇÃO POR ROTA
            // As regras por role foram movidas para @PreAuthorize nos controllers.
            // Aqui ficam apenas as regras globais: rotas públicas e autenticação base.
            // -------------------------------------------------------
            .authorizeHttpRequests(auth -> auth

                // PÚBLICO: registro e login não exigem autenticação
                .requestMatchers("/api/auth/**").permitAll()

                // Endpoint de erros do Spring Boot (necessário para que exceções retornem corretamente)
                .requestMatchers("/error").permitAll()

                // Console H2: aberto apenas para fins didáticos em dev
                .requestMatchers("/h2-console/**").permitAll()

                // Qualquer outra rota exige ao menos autenticação;
                // as restrições por role são aplicadas pelo @PreAuthorize de cada método
                .anyRequest().authenticated()
            )

            // Permite que o H2 Console seja exibido em iframes no browser (somente dev)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // Insere o filtro JWT antes do filtro padrão de autenticação
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Encoder de senhas com Argon2.
     *
     * Por que Argon2 e não bcrypt ou scrypt?
     * - Argon2 venceu o PHC (Password Hashing Competition) em 2015
     * - Possui 3 variantes: Argon2d (GPU), Argon2i (side-channel), Argon2id (híbrido — recomendado)
     * - Parâmetros configuráveis: memória, iterações, paralelismo
     * - defaultsForSpringSecurity_v5_8() usa Argon2id com parâmetros balanceados e seguros
     *
     * Por que NÃO usar MD5, SHA-1 ou SHA-256?
     * - São hashes de propósito geral, projetados para VELOCIDADE
     * - Uma GPU moderna calcula bilhões de SHA-256 por segundo (ataque de força bruta viável)
     * - Argon2 é deliberadamente lento e consome memória — cada tentativa de ataque custa muito
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Spring Security 7: UserDetailsService é obrigatório no construtor
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
