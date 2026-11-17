package FinTwin.EGR.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    /** HS512 precisa de uma chave de pelo menos 64 bytes. */
    private static final int TAMANHO_MINIMO_SEGREDO = 64;
    private static final long EXPIRACAO_MS = 15 * 60 * 1000;

    private final SecretKey chaveAssinatura;

    /** O segredo vem da variavel de ambiente JWT_SECRET (fintwin.jwt.secret no application.properties). */
    public JwtService(@Value("${fintwin.jwt.secret}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos " + TAMANHO_MINIMO_SEGREDO
                    + " caracteres; o valor atual tem " + bytes.length + ".");
        }
        this.chaveAssinatura = Keys.hmacShaKeyFor(bytes);
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
