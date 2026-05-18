package org.com.gateway.config;

import lombok.RequiredArgsConstructor;
import org.com.gateway.model.User;
import org.com.gateway.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class BootstrapConfig {

    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            String adminUser = "admin";
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = userRepository.findByUsername(adminUser).orElseGet(User::new);

            boolean isNewUser = user.getId() == null;
            user.setUsername(adminUser);
            if (isNewUser) {
                user.setPassword(encoder.encode("admin123"));
            }
            user.setRole("ROLE_ADMIN");
            user.setCompanyId("10");
            user.setEmployeeId("123");
            userRepository.save(user);

            if (isNewUser) {
                System.out.println("Bootstrap admin created: username=admin password=admin123 role=ROLE_ADMIN companyId=10 employeeId=123");
            } else {
                System.out.println("Bootstrap admin updated with companyId=10 and employeeId=123");
            }
        };
    }
}
