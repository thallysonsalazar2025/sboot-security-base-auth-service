package org.com.gateway.controller;


import jakarta.validation.Valid;
import org.com.gateway.model.request.LoginRequest;
import org.com.gateway.model.response.LoginResponse;
import org.com.gateway.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentativa de login recebida para o usuário: {}", request.username());
        try {
            LoginResponse response = authService.authenticate(request);
            log.info("Login bem-sucedido para o usuário: {}", request.username());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException exception) {
            log.warn("Falha de autenticação por credenciais inválidas");
            throw exception;
        } catch (Exception e) {
            log.error("Falha na tentativa de login para o usuário: {}", request.username(), e);
            throw e;
        }
    }
}
