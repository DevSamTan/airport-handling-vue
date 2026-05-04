package com.airops.handling.dto.response;

import com.airops.handling.model.enums.Department;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        int totalActiveStaff,
        int shiftsToday,
        int pendingSwaps,
        int pendingSickLeaves,
        Map<Department, DeptCoverage> coverageByDepartment,
        List<HourlySlot> hourlyStaffing
) {
    public record DeptCoverage(int scheduled, int total, double percentage) {}
    public record HourlySlot(int hour, int count) {}
}
