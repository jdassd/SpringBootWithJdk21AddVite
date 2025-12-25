package com.example.demo.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class ContentSanitizer {
    private static final int MAX_CSS_LENGTH = 5000;
    private static final Safelist POST_SAFE_LIST = Safelist.relaxed()
        .addTags("h1", "h2", "h3", "h4", "h5", "h6");

    public String sanitizeRichText(String input) {
        if (input == null) {
            return null;
        }
        return Jsoup.clean(input, POST_SAFE_LIST);
    }

    public String sanitizePlainText(String input) {
        if (input == null) {
            return null;
        }
        return Jsoup.clean(input, Safelist.none());
    }

    public String sanitizeCss(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        String cleaned = trimmed.replaceAll("(?is)/\\*.*?\\*/", "");
        cleaned = cleaned.replaceAll("(?i)@import[^;]+;?", "");
        cleaned = cleaned.replaceAll("(?i)url\\s*\\([^)]*\\)", "");
        cleaned = cleaned.replaceAll("[<>]", "");
        cleaned = cleaned.trim();
        if (cleaned.length() > MAX_CSS_LENGTH) {
            return cleaned.substring(0, MAX_CSS_LENGTH);
        }
        return cleaned;
    }
}
