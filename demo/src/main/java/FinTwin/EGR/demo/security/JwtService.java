package FinTwin.EGR.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_STRING = "FinTwinSecretKeyForJwtAuthenticationTokensMustBeAtLeast256BitsLong2026FinTwinSecure";
    private static final long EXPIRACAO_MS = 15 * 60 * 1000;

    private final SecretKey chaveAssinatura;

    public JwtService() {
        this.chaveAssinatura = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(String email) {
        Date agora = new Date();
        Date dataExpiracao = new Date(agora.getTime() + EXPIRACAO_MS);

        return Jwts.builder()
                .subject(email)
                .issuedAt(agora)
                .expiration(dataExpiracao)
                .signWith(chaveAssinatura)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean validarToken(String token, String email) {
        String emailToken = extrairEmail(token);
        return emailToken.equals(email) && !isTokenExpirado(token);
    }

    public boolean isTokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveAssinatura)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
