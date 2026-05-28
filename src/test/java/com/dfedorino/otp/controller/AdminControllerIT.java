package com.dfedorino.otp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dfedorino.otp.controller.dto.LoginResponse;
import com.dfedorino.otp.controller.dto.UserRequest;
import com.dfedorino.otp.controller.auth.filter.JwtFilter;
import com.dfedorino.otp.domain.model.OtpConfig;
import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.service.JwtService;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.controller.config.WebConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Slf4j
class AdminControllerIT extends AbstractIntegrationTest {

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebConfig.class, ServiceConfig.class, RepositoryConfig.class);
        context.refresh();

        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter(new JwtFilter(
                context.getBean(JwtService.class),
                context.getBean(ObjectMapper.class)
            ))
            .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void should_get_users_successfully() throws Exception {
        // Login as admin
        UserRequest loginRequest = new UserRequest("admin", "admin");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(loginResponse.token());

        // Create regular user
        UserRequest userRequest = new UserRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)));

        // Test getting users
        mockMvc.perform(get("/api/admin/users")
                .headers(httpHeaders))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].login").exists())
            .andExpect(jsonPath("$[0].role").exists());
    }

    @Test
    void should_delete_user_successfully() throws Exception {
        // Create a regular user to delete
        UserRequest userRequest = new UserRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)));

        // Login as admin
        UserRequest loginRequest = new UserRequest("admin", "admin");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(loginResponse.token());

        // Delete the user
        mockMvc.perform(delete("/api/admin/users?userId=2")
                .headers(httpHeaders))
            .andExpect(status().isNoContent());
    }

    @Test
    void should_update_otp_config_successfully() throws Exception {
        // Login as admin
        UserRequest loginRequest = new UserRequest("admin", "admin");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(loginResponse.token());

        // Update OTP config
        OtpConfig config = new OtpConfig(null, 6, 300);
        mockMvc.perform(put("/api/admin/otp/config")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(config)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codeLength").value(6))
            .andExpect(jsonPath("$.ttlSeconds").value(300));
    }

    @Test
    void should_reject_access_for_non_admin_user() throws Exception {
        // Create a regular user
        UserRequest userRequest = new UserRequest("regularuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)));

        // Login as regular user
        UserRequest loginRequest = new UserRequest("regularuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(loginResponse.token());

        // Try to access admin endpoint - should be rejected
        mockMvc.perform(get("/api/admin/users")
                .headers(httpHeaders))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_reject_unauthorized_access() throws Exception {
        // Try to access admin endpoint without authentication
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized());
    }
}