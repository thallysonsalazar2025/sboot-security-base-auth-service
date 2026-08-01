package org.com.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceConfigTest {

    private static final String SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void shouldEmitIssuerAudienceAndExistingIdentityClaims() {
        JwtServiceConfig service = service("payroll-auth", "payroll-publisher");

        Claims claims = service.extractAllClaims(service.generateToken(
                "actor-a", List.of("ROLE_EMPLOYEE"), "tenant-a", "employee-a"));

        assertEquals("payroll-auth", claims.getIssuer());
        assertEquals(List.of("payroll-publisher"), claims.getAudience().stream().toList());
        assertEquals("actor-a", claims.getSubject());
        assertEquals(List.of("ROLE_EMPLOYEE"), claims.get("roles", List.class));
        assertEquals("tenant-a", claims.get("companyId", String.class));
        assertEquals("employee-a", claims.get("employeeId", String.class));
    }

    @Test
    void shouldRejectTokenWhenConfiguredIssuerOrAudienceDoesNotMatch() {
        JwtServiceConfig emitter = service("payroll-auth", "payroll-publisher");
        String token = emitter.generateToken(
                "actor-a", List.of("ROLE_EMPLOYEE"), "tenant-a", "employee-a");

        assertThrows(JwtException.class,
                () -> service("other-issuer", "payroll-publisher").extractAllClaims(token));
        assertThrows(JwtException.class,
                () -> service("payroll-auth", "other-audience").extractAllClaims(token));
    }

    @Test
    void shouldFailFastForInvalidConfiguration() {
        assertThrows(IllegalStateException.class,
                () -> new JwtServiceConfig(SECRET, 3600, " ", "payroll-publisher"));
        assertThrows(IllegalStateException.class,
                () -> new JwtServiceConfig(SECRET, 3600, "payroll-auth", null));
        assertThrows(IllegalStateException.class,
                () -> new JwtServiceConfig(" ", 3600, "payroll-auth", "payroll-publisher"));
        assertThrows(IllegalStateException.class,
                () -> new JwtServiceConfig(SECRET, 0, "payroll-auth", "payroll-publisher"));
    }

    private static JwtServiceConfig service(String issuer, String audience) {
        return new JwtServiceConfig(SECRET, 3600, issuer, audience);
    }
}
