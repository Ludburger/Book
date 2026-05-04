package br.umc.demo.dto;

/**
 * Resposta de autenticação.
 *
 * Retorna apenas o necessário — sem expor dados sensíveis como a senha.
 * Boas práticas OWASP: evite Sensitive Data Exposure nas respostas da API.
 *
 * O cliente deve enviar o token no header de todas as requisições protegidas:
 *   Authorization: Bearer <token>
 */
public class AuthResponse {

    private String token;
    private String email;
    private String role;

    public AuthResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
