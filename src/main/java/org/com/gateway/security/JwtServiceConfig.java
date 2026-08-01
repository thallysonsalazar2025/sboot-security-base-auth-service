package org.com.gateway.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceConfig {

    private final SecretKey signingKey;
    private final long expirationInSeconds;
    private final String issuer;
    private final String audience;

    public JwtServiceConfig(@Value("${security.jwt.secret}") String secret,
                            @Value("${security.jwt.expiration-seconds:3600}") long expirationInSeconds,
                            @Value("${security.jwt.issuer}") String issuer,
                            @Value("${security.jwt.audience}") String audience) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must not be blank");
        }
        if (expirationInSeconds <= 0) {
            throw new IllegalStateException("JWT expiration must be greater than zero");
        }
        this.issuer = requiredSetting(issuer, "issuer");
        this.audience = requiredSetting(audience, "audience");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationInSeconds = expirationInSeconds;
    }

    public String generateToken(String username, List<String> roles, String companyId, String employeeId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("roles", roles)
                .claim("companyId", companyId)
                .claim("employeeId", employeeId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationInSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationInSeconds() {
        return expirationInSeconds;
    }

    private static String requiredSetting(String value, String settingName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("JWT " + settingName + " must not be blank");
        }
        return value.trim();
    }
}
