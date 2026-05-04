package com.airops.handling.dto.response;

import com.airops.handling.model.Shift;
import com.airops.handling.model.enums.ShiftType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShiftResponse(
        Long id,
        Long staffId,
        String staffName,
        LocalDate date,
        ShiftType type,
        String typeLabel,
        String timeRange,
        int duration,
        String note,
        String assignedBy,
        LocalDateTime createdAt
) {
    public static ShiftResponse from(Shift s) {
        return new ShiftResponse(
                s.getId(),
                s.getStaff().getId(),
                s.getStaff().getFullName(),
                s.getDate(),
                s.getType(),
                s.getType().getLabel(),
                s.getType().getHours(),
                s.getType().getDuration(),
                s.getNote(),
                s.getAssignedBy() != null ? s.getAssignedBy().getUsername() : null,
                s.getCreatedAt()
        );
    }
}
