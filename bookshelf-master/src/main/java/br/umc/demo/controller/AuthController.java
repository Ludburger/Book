package br.umc.demo.controller;

import br.umc.demo.dto.AuthResponse;
import br.umc.demo.dto.LoginRequest;
import br.umc.demo.dto.RegisterRequest;
import br.umc.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos de autenticação.
 *
 * POST /api/auth/register → cria conta e retorna token JWT
 * POST /api/auth/login    → valida credenciais e retorna token JWT
 *
 * Após o login/registro, o cliente usa o token assim:
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
