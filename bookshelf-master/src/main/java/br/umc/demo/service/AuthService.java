package br.umc.demo.service;

import br.umc.demo.dto.AuthResponse;
import br.umc.demo.dto.LoginRequest;
import br.umc.demo.dto.RegisterRequest;
import br.umc.demo.entity.Role;
import br.umc.demo.entity.User;
import br.umc.demo.repository.UserRepository;
import br.umc.demo.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Registra um novo usuário na aplicação.
     *
     * Fluxo:
     * 1. Verifica se o email já existe (email único)
     * 2. Faz o hash da senha com Argon2 — a senha em texto puro nunca é persistida
     * 3. Persiste o usuário com role USER (novos cadastros sempre são USER)
     * 4. Gera e retorna um token JWT para login imediato após registro
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        // A senha é hashada ANTES de ser salva — nunca armazene senha em texto puro
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Todo novo registro é USER — ADMIN só pode ser configurado manualmente
        user.setRole(Role.USER);

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    /**
     * Autentica um usuário e retorna um token JWT.
     *
     * O AuthenticationManager:
     * 1. Busca o usuário pelo email (via UserDetailsService)
     * 2. Compara a senha fornecida com o hash Argon2 armazenado
     * 3. Lança BadCredentialsException se inválido (→ 401 Unauthorized)
     */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Credenciais inválidas → 401 Unauthorized
            // OWASP: nunca revele se foi o email ou a senha que falhou
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
