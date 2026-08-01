package org.com.gateway.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.com.gateway.model.request.LoginRequest;
import org.com.gateway.security.JwtServiceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtServiceConfig jwtServiceConfig;

    private final String validUsername = "admin";
    private final String validPassword = "admin123";

    @Test
    void shouldLoginAndReturnJwt() throws Exception {
        LoginRequest loginRequest = new LoginRequest(validUsername, validPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldIncludeCompanyAndEmployeeClaimsInJwt() throws Exception {
        LoginRequest loginRequest = new LoginRequest(validUsername, validPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        String token = responseJson.get("accessToken").asText();

        Claims claims = jwtServiceConfig.extractAllClaims(token);

        org.junit.jupiter.api.Assertions.assertEquals("10", claims.get("companyId", String.class));
        org.junit.jupiter.api.Assertions.assertEquals("123", claims.get("employeeId", String.class));
    }

    @Test
    void shouldAccessProtectedRouteWithToken() throws Exception {
        // Step 1: Login to get the token
        LoginRequest loginRequest = new LoginRequest(validUsername, validPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        String token = responseJson.get("accessToken").asText();

        // Step 2: Use the token to access a protected route
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(validUsername));
    }
}
