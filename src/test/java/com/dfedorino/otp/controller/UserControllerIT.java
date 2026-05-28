package com.dfedorino.otp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dfedorino.otp.controller.dto.OtpRequest;
import com.dfedorino.otp.controller.dto.UserRequest;
import com.dfedorino.otp.controller.dto.ValidateOtpRequest;
import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.controller.config.WebConfig;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Slf4j
class UserControllerIT extends AbstractIntegrationTest {

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
        objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void should_generate_otp_successfully() throws Exception {
        // First create a user
        var userRequest = new UserRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)));

        // Then generate OTP
        OtpRequest otpRequest = new OtpRequest(1L, "test_operation");
        MvcResult result = mockMvc.perform(post("/api/users/otp/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.operationId").value("test_operation"))
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        log.debug("response: {}", responseContent);

        var otpCodeDto =
            objectMapper.readValue(responseContent, OtpCodeDto.class);

        assertThat(otpCodeDto.code()).isNotBlank();
        assertThat(otpCodeDto.status()).isNotNull();
    }

    @Test
    void should_validate_valid_otp() throws Exception {
        // First create a user
        var userRequest = new UserRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)));

        // Generate OTP first
        OtpRequest otpRequest = new OtpRequest(1L, "test_operation");
        var otpCodeResponse = mockMvc.perform(post("/api/users/otp/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(otpRequest)))
            .andReturn().getResponse().getContentAsString();

        var otp = objectMapper.readValue(otpCodeResponse, OtpCodeDto.class);

        // Then validate OTP
        ValidateOtpRequest validateRequest = new ValidateOtpRequest(otp.userId(), otp.operationId(), otp.code());
        MvcResult result = mockMvc.perform(post("/api/users/otp/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        boolean isValid = Boolean.parseBoolean(responseContent);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void should_reject_otp_generation_for_nonexistent_user() throws Exception {
        OtpRequest otpRequest = new OtpRequest(999L, "test_operation");
        MvcResult result = mockMvc.perform(post("/api/users/otp/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andExpect(status().isNotFound())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("User not found");
    }

    @Test
    void should_reject_otp_validation_for_nonexistent_user() throws Exception {
        ValidateOtpRequest validateRequest = new ValidateOtpRequest(999L, "test_operation", "123456");
        MvcResult result = mockMvc.perform(post("/api/users/otp/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        boolean isValid = Boolean.parseBoolean(responseContent);

        assertThat(isValid).isFalse();
    }
}