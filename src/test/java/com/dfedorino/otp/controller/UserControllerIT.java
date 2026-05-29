package com.dfedorino.otp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dfedorino.otp.controller.auth.filter.JwtFilter;
import com.dfedorino.otp.controller.dto.LoginResponse;
import com.dfedorino.otp.controller.dto.OtpRequest;
import com.dfedorino.otp.controller.dto.UserRequest;
import com.dfedorino.otp.controller.dto.ValidateOtpRequest;
import com.dfedorino.otp.common.AbstractIntegrationTest;
import com.dfedorino.otp.delivery.impl.EmailDeliveryChannel;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.service.JwtService;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.controller.config.WebConfig;
import com.dfedorino.otp.service.dto.OtpCodeDto;
import com.dfedorino.otp.service.dto.UserDto;
import com.dfedorino.otp.util.ApplicationPropertiesUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Slf4j
class UserControllerIT extends AbstractIntegrationTest {
    private final Properties props = ApplicationPropertiesUtil.loadApplicationProperties();

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetup.SMTP);

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebConfig.class, ServiceConfig.class, RepositoryConfig.class);
        context.refresh();

        objectMapper = context.getBean(ObjectMapper.class);

        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter(new JwtFilter(context.getBean(JwtService.class), objectMapper))
            .build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void should_generate_otp_successfully() throws Exception {
        // First create a user
        var userRequest = new UserRequest("dfedorino@gmail.com", "password123");
        MvcResult registrationMvcResult = mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andReturn();

        String userResponseContent = registrationMvcResult.getResponse().getContentAsString();
        UserDto user = objectMapper.readValue(userResponseContent, UserDto.class);

        // Then login
        UserRequest loginRequest = new UserRequest(user.login(), "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String loginResponseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(loginResponseContent, LoginResponse.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(loginResponse.token());

        // Then generate OTP
        OtpRequest otpRequest = new OtpRequest(user.id(), "test_operation");
        MvcResult result = mockMvc.perform(post("/api/users/otp/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(user.id()))
            .andExpect(jsonPath("$.operationId").value("test_operation"))
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        log.debug("response: {}", responseContent);

        var otpCodeDto =
            objectMapper.readValue(responseContent, OtpCodeDto.class);

        assertThat(otpCodeDto.code()).isNotBlank();
        assertThat(otpCodeDto.status()).isNotNull();

        assertThat(List.of(greenMail.getReceivedMessages()))
            .hasSize(1)
            .first()
            .satisfies(message -> {
                assertThat(message.getFrom()).containsOnly(new InternetAddress(props.getProperty("email.from")));
                assertThat(message.getAllRecipients()).containsOnly(new InternetAddress(user.login()));
                assertThat(message.getSubject()).isEqualTo(EmailDeliveryChannel.SUBJECT);
                assertThat(message.getContent()).isEqualTo(EmailDeliveryChannel.PREFIX + otpCodeDto.code());
            });
    }

    @Test
    void should_validate_valid_otp() throws Exception {
        // First create a user
        var userRequest = new UserRequest("testuser", "password123");
        MvcResult mvcResult = mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andReturn();

        var userDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);

        // Then login
        UserRequest loginRequest = new UserRequest("testuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String loginResponseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(loginResponseContent, LoginResponse.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(loginResponse.token());

        // Generate OTP first
        OtpRequest otpRequest = new OtpRequest(userDto.id(), "test_operation");
        var otpCodeResponse = mockMvc.perform(post("/api/users/otp/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andReturn().getResponse().getContentAsString();

        var otp = objectMapper.readValue(otpCodeResponse, OtpCodeDto.class);

        // Then validate OTP
        ValidateOtpRequest validateRequest = new ValidateOtpRequest(otp.userId(), otp.operationId(), otp.code());
        MvcResult result = mockMvc.perform(post("/api/users/otp/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(validateRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        boolean isValid = Boolean.parseBoolean(responseContent);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void should_reject_otp_generation_for_nonexistent_user() throws Exception {
        // First create a user
        var userRequest = new UserRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)));

        // Login
        UserRequest loginRequest = new UserRequest("testuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(loginResponse.token());

        OtpRequest otpRequest = new OtpRequest(999L, "test_operation");
        mockMvc.perform(post("/api/users/otp/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(otpRequest)))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("User not found")));
    }

    @Test
    void should_reject_otp_validation_for_nonexistent_user() throws Exception {
        ValidateOtpRequest validateRequest = new ValidateOtpRequest(999L, "test_operation", "123456");
        MvcResult result = mockMvc.perform(post("/api/users/otp/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
            .andExpect(status().isUnauthorized())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        boolean isValid = Boolean.parseBoolean(responseContent);

        assertThat(isValid).isFalse();
    }
}