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
class TaskAndMailControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTaskAndFetchSummary() throws Exception {
        String token = obtainToken();
        String payload = """
            {
              "title": "准备月度报告",
              "dueDate": "2025-01-01",
              "reminderTime": "2025-01-01T09:00:00Z"
            }
            """;

        mockMvc.perform(post("/api/tasks")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("准备月度报告"));

        mockMvc.perform(get("/api/tasks/summary?period=month")
                .header("X-Auth-Token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.period").value("month"));
    }

    @Test
    void receiveMailStoresMessage() throws Exception {
        String token = obtainToken();
        String payload = """
            {
              "fromAddress": "sender@example.com",
              "toAddress": "receiver@example.com",
              "subject": "测试收信",
              "body": "这是一封测试邮件。"
            }
            """;

        mockMvc.perform(post("/api/mail/receive")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.direction").value("INBOUND"));

        mockMvc.perform(get("/api/mail")
                .header("X-Auth-Token", token))
            .andExpect(status().isOk());
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
