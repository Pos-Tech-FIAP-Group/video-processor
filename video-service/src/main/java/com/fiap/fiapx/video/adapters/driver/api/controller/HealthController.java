package com.fiap.fiapx.video.adapters.driver.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/videos/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}