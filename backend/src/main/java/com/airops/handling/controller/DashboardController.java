package com.airops.handling.controller;

import com.airops.handling.dto.response.DashboardResponse;
import com.airops.handling.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get today's operational summary (KPIs, coverage, hourly staffing)")
    @GetMapping
    public DashboardResponse today() {
        return dashboardService.getToday();
    }
}
