package com.example.blog.controller;

import com.example.blog.dto.DashboardResponse;
import com.example.blog.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/me")
    public DashboardResponse getMyDashboard() {
        return dashboardService.getMyDashboard();
    }
}
