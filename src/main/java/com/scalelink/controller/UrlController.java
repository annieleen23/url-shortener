package com.scalelink.controller;

import com.scalelink.model.UrlMapping;
import com.scalelink.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("url");
        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL cannot be empty"));
        }
        UrlMapping mapping = urlService.createShortUrl(originalUrl);
        return ResponseEntity.ok(Map.of(
            "shortCode", mapping.getShortCode(),
            "originalUrl", mapping.getOriginalUrl(),
            "shortUrl", "http://localhost:8080/" + mapping.getShortCode(),
            "expiresAt", mapping.getExpiresAt().toString()
        ));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        try {
            String originalUrl = urlService.getOriginalUrl(shortCode);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", originalUrl)
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "healthy", "service", "ScaleLink URL Shortener"));
    }
}
