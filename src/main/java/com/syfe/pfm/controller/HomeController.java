package com.syfe.pfm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to provide a public landing/status response at the root and API paths.
 */
@RestController
public class HomeController {

    @GetMapping({"/", "/api", "/api/"})
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Personal Finance Manager API");
        response.put("status", "UP");
        response.put("version", "1.0.0");
        response.put("message", "Welcome! The backend service is running successfully.");
        response.put("description", "A secure RESTful API for personal finance tracking, category analysis, savings goals, and reports.");
        response.put("authentication", "Session-based. Register at /api/auth/register and Login at /api/auth/login to receive your JSESSIONID cookie.");
        response.put("repository", "https://github.com/wadhwaumeshzira/Personal-Finance-Manager");
        return response;
    }
}
