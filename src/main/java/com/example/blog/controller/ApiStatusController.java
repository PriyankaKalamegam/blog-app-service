package com.example.blog.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiStatusController {

    @GetMapping("/")
    public Map<String, String> status() {
        return Map.of(
                "service", "DevLog Platform API",
                "status", "running",
                "api", "/api"
        );
    }
}
