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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.captcha.debug=true")
class CustomPageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sanitizeCustomPageContent() throws Exception {
        String token = obtainToken();
        String payload = """
            {
              "title": "安全页面",
              "slug": "safe-page",
              "content": "<h1>欢迎</h1><script>alert('xss')</script>",
              "customCss": "@import url('http://evil'); body { color: red; }"
            }
            """;

        mockMvc.perform(post("/api/pages")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("<h1>欢迎</h1>"))
            .andExpect(jsonPath("$.customCss").value("body { color: red; }"));
    }

    private String obtainToken() throws Exception {
        JsonNode captcha = fetchCaptcha();
        String username = "user" + UUID.randomUUID().toString().substring(0, 8);
        String registerPayload = """
            {
              "username": "%s",
              "email": "%s@example.com",
              "password": "StrongPass123!",
              "captchaToken": "%s",
              "captchaCode": "%s"
            }
            """.formatted(username, username, captcha.get("token").asText(), captcha.get("debugCode").asText());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerPayload))
            .andExpect(status().isOk());

        JsonNode loginCaptcha = fetchCaptcha();
        String loginPayload = """
            {
              "username": "%s",
              "password": "StrongPass123!",
              "captchaToken": "%s",
              "captchaCode": "%s"
            }
            """.formatted(username, loginCaptcha.get("token").asText(), loginCaptcha.get("debugCode").asText());

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
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
