package br.umc.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Serviço responsável por gerar e validar tokens JWT.
 *
 * O que é JWT?
 * Um token composto por 3 partes (header.payload.signature) codificadas em Base64.
 * O servidor assina o token com uma chave secreta — qualquer alteração invalida a assinatura.
 * Por ser stateless, o servidor NÃO precisa armazenar sessões: basta validar a assinatura.
 */
@Service
public class JwtService {

    // A chave secreta vem do application.properties — NUNCA hardcoded no código-fonte.
    // Boas práticas OWASP: Security Misconfiguration — segredos não pertencem ao código.
    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    /**
     * Gera um token JWT assinado para o usuário autenticado.
     * O "subject" do token é o email do usuário.
     */
    public String generateToken(UserDetails userDetails) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /** Extrai o email (subject) do token. */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Valida o token: verifica se pertence ao usuário e se não expirou. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
