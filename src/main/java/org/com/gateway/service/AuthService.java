package org.com.gateway.service;

import org.com.gateway.model.request.LoginRequest;
import org.com.gateway.model.response.LoginResponse;
import org.com.gateway.repository.UserRepository;
import org.com.gateway.security.JwtServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtServiceConfig jwtServiceConfig;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtServiceConfig jwtServiceConfig) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtServiceConfig = jwtServiceConfig;
    }

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Autenticando usuário: {}", request.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        log.info("Usuário autenticado com sucesso: {}", authentication.getName());

        List<String> profiles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        log.debug("Perfis do usuário {}: {}", authentication.getName(), profiles);

        var user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in repository: " + authentication.getName()));

        if (user.getCompanyId() == null || user.getCompanyId().isBlank()
                || user.getEmployeeId() == null || user.getEmployeeId().isBlank()) {
            throw new IllegalStateException("User is missing companyId/employeeId required for JWT generation");
        }

        String token = jwtServiceConfig.generateToken(
                authentication.getName(),
                profiles,
                user.getCompanyId(),
                user.getEmployeeId()
        );
        log.info("Token JWT gerado para o usuário: {}", authentication.getName());

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtServiceConfig.getExpirationInSeconds(),
                profiles.isEmpty() ? "ROLE_USER" : profiles.getFirst()
        );

        log.info("Resposta de login criada para o usuário: {}", authentication.getName());
        return response;
    }
}
