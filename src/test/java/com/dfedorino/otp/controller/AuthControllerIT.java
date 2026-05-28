package com.dfedorino.otp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dfedorino.otp.controller.dto.LoginResponse;
import com.dfedorino.otp.controller.dto.UserRequest;
import com.dfedorino.otp.domain.enums.Role;
import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.controller.config.WebConfig;
import com.dfedorino.otp.service.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

class AuthControllerIT extends AbstractIntegrationTest {

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebConfig.class, ServiceConfig.class, RepositoryConfig.class);
        context.refresh();

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void should_create_user_successfully() throws Exception {
        UserRequest request = new UserRequest("testuser", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("testuser"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UserDto userDto = objectMapper.readValue(responseContent, UserDto.class);

        assertThat(userDto.id()).isNotNull();
        assertThat(userDto.login()).isEqualTo("testuser");
        assertThat(userDto.role()).isEqualTo(Role.USER);
    }

    @Test
    void should_login_successfully() throws Exception {
        UserRequest registerRequest = new UserRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

        UserRequest loginRequest = new UserRequest("testuser", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseContent, LoginResponse.class);

        assertThat(response.token()).isNotBlank();
    }

    @Test
    void should_reject_duplicate_login() throws Exception {
        UserRequest request = new UserRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        MvcResult result = mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserRequest("testuser", "newpass"))))
            .andExpect(status().isConflict())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("Login already exists");
    }

    @Test
    void should_reject_login_for_invalid_password() throws Exception {
        UserRequest registerRequest = new UserRequest("testuser", "correctpass");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserRequest("testuser", "wrongpass"))))
            .andExpect(status().isUnauthorized())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("Invalid credentials");
    }

    @Test
    void should_reject_login_for_unknown_user() throws Exception {
        UserRequest request = new UserRequest("unknown", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("User not found by login: unknown");
    }
}
