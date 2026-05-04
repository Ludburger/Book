package br.umc.demo.entity;

import jakarta.persistence.*;

/**
 * Representa um usuário da aplicação.
 *
 * Conceito de aula: "autenticação" é o processo de verificar QUEM é o usuário —
 * neste caso, validando email e senha para emitir um token JWT.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    /**
     * Senha armazenada como hash Argon2 — NUNCA em texto puro.
     *
     * Por que Argon2 e não MD5, SHA-1 ou SHA-256?
     * - Hashes rápidos (MD5, SHA-256) foram projetados para velocidade,
     *   o que os torna vulneráveis a ataques de força bruta com GPU.
     * - Argon2 é "memory-hard": exige muita memória para ser computado,
     *   tornando ataques em larga escala (ASIC, GPU) proibitivamente caros.
     * - É o vencedor do Password Hashing Competition (2015) e recomendado
     *   pelo OWASP para armazenamento de senhas.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
