package br.umc.demo;

import br.umc.demo.dto.AuthResponse;
import br.umc.demo.dto.LoginRequest;
import br.umc.demo.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração de segurança.
 *
 * Cobrem os principais fluxos:
 * 1. Registro de novo usuário
 * 2. Login e geração de token
 * 3. Bloqueio de rota administrativa para role USER (403 Forbidden)
 * 4. Acesso de ADMIN a rota administrativa (201 Created)
 * 5. Acesso sem token → 401 Unauthorized
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {

    @LocalServerPort
    private int port;

    private String base() {
        return "http://localhost:" + port;
    }

    private RestClient client() {
        return RestClient.builder()
                .baseUrl(base())
                .defaultStatusHandler(status -> true, (req, res) -> {}) // não lança em 4xx/5xx
                .build();
    }

    // -------------------------------------------------------
    // 1. Registro
    // -------------------------------------------------------

    @Test
    void registroDeveRetornar201ETokenJWT() {
        RegisterRequest request = buildRegister(
                "João Silva", "joao.silva@teste.com", "11999991234", "Senha@123");

        ResponseEntity<AuthResponse> response = client().post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getEmail()).isEqualTo("joao.silva@teste.com");
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    void registroComEmailDuplicadoDeveRetornar409() {
        RegisterRequest request = buildRegister(
                "Maria Souza", "duplicado@teste.com", "11988887777", "Senha@456");

        // Primeiro registro
        client().post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).body(request).retrieve().toBodilessEntity();

        // Segundo registro com mesmo email
        ResponseEntity<Void> response = client().post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).body(request).retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // -------------------------------------------------------
    // 2. Login
    // -------------------------------------------------------

    @Test
    void loginComCredenciaisValidasDeveRetornar200EToken() {
        // Registra o usuário
        client().post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildRegister("Carlos", "carlos.login@teste.com", "11977776666", "Senha@789"))
                .retrieve().toBodilessEntity();

        // Faz login
        LoginRequest login = buildLogin("carlos.login@teste.com", "Senha@789");
        ResponseEntity<AuthResponse> response = client().post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).body(login).retrieve().toEntity(AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void loginComSenhaErradaDeveRetornar401() {
        ResponseEntity<Void> response = client().post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildLogin("admin@biblioteca.com", "senhaErrada999"))
                .retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // -------------------------------------------------------
    // 3. Autorização: USER bloqueado em rota ADMIN
    // -------------------------------------------------------

    @Test
    void usuarioUSERDeveReceber403AoTentarCriarLivro() {
        ResponseEntity<AuthResponse> registerResponse = client().post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildRegister("Usuario Comum", "usuario.comum@teste.com", "11966665555", "Senha@User"))
                .retrieve().toEntity(AuthResponse.class);

        String tokenUser = registerResponse.getBody().getToken();

        // Tenta criar livro com token USER → esperado: 403 Forbidden
        ResponseEntity<Void> response = client().post().uri("/api/books")
                .header("Authorization", "Bearer " + tokenUser)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"title\":\"Proibido\",\"authorIds\":[],\"publisher\":\"Ed\",\"isbn\":\"X01\",\"summary\":\"\"}")
                .retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------
    // 4. Autorização: ADMIN pode criar livro
    // -------------------------------------------------------

    @Test
    void adminDevePoderCriarLivroEReceber201() {
        // Login como admin (seed criado pelo DataInitializer)
        ResponseEntity<AuthResponse> loginResponse = client().post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildLogin("admin@biblioteca.com", "Admin@123"))
                .retrieve().toEntity(AuthResponse.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String tokenAdmin = loginResponse.getBody().getToken();

        // Admin cria livro → esperado: 201 Created
        ResponseEntity<Void> response = client().post().uri("/api/books")
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"title\":\"Clean Code\",\"authorIds\":[],\"publisher\":\"PH\",\"isbn\":\"978-01\",\"summary\":\"\"}")
                .retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // -------------------------------------------------------
    // 5. Acesso sem token deve retornar 401
    // -------------------------------------------------------

    @Test
    void acessoSemTokenDeveRetornar401() {
        ResponseEntity<Void> response = client().get().uri("/api/books")
                .retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private RegisterRequest buildRegister(String fullName, String email, String phone, String password) {
        RegisterRequest r = new RegisterRequest();
        r.setFullName(fullName);
        r.setEmail(email);
        r.setPhone(phone);
        r.setPassword(password);
        return r;
    }

    private LoginRequest buildLogin(String email, String password) {
        LoginRequest l = new LoginRequest();
        l.setEmail(email);
        l.setPassword(password);
        return l;
    }
}
