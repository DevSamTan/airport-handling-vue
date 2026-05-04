package com.airops.handling.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record WeeklyPlanResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<StaffWeekRow> rows
) {
    public record StaffWeekRow(
            StaffResponse staff,
            Map<LocalDate, ShiftResponse> shifts,
            double totalHours
    ) {}
}
