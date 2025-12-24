package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.captcha.debug=true")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndLogin() throws Exception {
        JsonNode captcha = fetchCaptcha();
        String registerPayload = """
            {
              "username": "demoUser",
              "email": "demo@example.com",
              "password": "StrongPass123!",
              "captchaToken": "%s",
              "captchaCode": "%s"
            }
            """.formatted(captcha.get("token").asText(), captcha.get("debugCode").asText());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerPayload))
            .andExpect(status().isOk());

        JsonNode loginCaptcha = fetchCaptcha();
        String loginPayload = """
            {
              "username": "demoUser",
              "password": "StrongPass123!",
              "captchaToken": "%s",
              "captchaCode": "%s"
            }
            """.formatted(loginCaptcha.get("token").asText(), loginCaptcha.get("debugCode").asText());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk());
    }

    private JsonNode fetchCaptcha() throws Exception {
        String response = mockMvc.perform(get("/api/auth/captcha"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response);
    }
}
