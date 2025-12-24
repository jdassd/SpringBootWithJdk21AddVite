package com.example.demo.auth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CaptchaService {
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> captchas = new ConcurrentHashMap<>();
    private final Duration ttl = Duration.ofMinutes(5);
    private final boolean debug;

    public CaptchaService(boolean debug) {
        this.debug = debug;
    }

    public CaptchaResponse generateCaptcha() {
        String code = String.valueOf(1000 + random.nextInt(9000));
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(ttl);
        captchas.put(token, new CaptchaEntry(code, expiresAt));
        String imageBase64 = renderCaptchaImage(code);
        return new CaptchaResponse(token, imageBase64, expiresAt, debug ? code : null);
    }

    public boolean verify(String token, String code) {
        if (token == null || code == null) {
            return false;
        }
        CaptchaEntry entry = captchas.remove(token);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return false;
        }
        return entry.code().equalsIgnoreCase(code.trim());
    }

    private String renderCaptchaImage(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(245, 247, 250));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(52, 82, 122));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
        graphics.drawString(code, 18, 28);
        graphics.setColor(new Color(200, 210, 230));
        for (int i = 0; i < 5; i++) {
            graphics.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        graphics.dispose();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to render captcha", ex);
        }
    }

    private record CaptchaEntry(String code, Instant expiresAt) {
    }

    public record CaptchaResponse(String token, String imageBase64, Instant expiresAt, String debugCode) {
    }
}
