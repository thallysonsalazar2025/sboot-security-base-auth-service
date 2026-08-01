package org.com.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.com.gateway.model.request.LoginRequest;
import org.com.gateway.model.response.LoginResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Test
    public void testLoginSuccess() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        // Verify database interaction (example)
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(isValidUser(connection, loginRequest.username()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidUser(Connection connection, String username) throws SQLException {
        // Implement your database query to check if the user is valid
        // This is just a placeholder, replace with your actual query
        return true;
    }

    @Test
    public void testLoginFailure() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("invalidUser", "wrongPassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(401))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Unauthorized"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid credentials"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.path").value("/api/auth/login"))
                .andExpect(MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("invalidUser"))))
                .andExpect(MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("wrongPassword"))));
    }
}
