package com.airops.handling.dto.request;

import com.airops.handling.model.enums.SickLeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSickLeaveRequest(
        @NotNull Long staffId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull SickLeaveType type,
        String note
) {}
