package com.airops.handling.dto.request;

import com.airops.handling.model.enums.ShiftType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ShiftAssignRequest(
        @NotNull Long staffId,
        @NotNull LocalDate date,
        @NotNull ShiftType type,
        String note
) {}
