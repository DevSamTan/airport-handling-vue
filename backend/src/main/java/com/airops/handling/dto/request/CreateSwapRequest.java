package com.airops.handling.dto.request;

import com.airops.handling.model.enums.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSwapRequest(
        @NotNull Long requesterId,
        @NotNull Long colleagueId,
        @NotNull LocalDate shiftDate,
        @NotNull ShiftType fromType,
        @NotNull ShiftType toType,
        @NotBlank String reason
) {}
