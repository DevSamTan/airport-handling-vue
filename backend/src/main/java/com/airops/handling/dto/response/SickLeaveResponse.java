package com.airops.handling.dto.response;

import com.airops.handling.model.SickLeave;
import com.airops.handling.model.enums.SickLeaveStatus;
import com.airops.handling.model.enums.SickLeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record SickLeaveResponse(
        Long id,
        Long staffId,
        String staffName,
        LocalDate startDate,
        LocalDate endDate,
        long days,
        SickLeaveType type,
        SickLeaveStatus status,
        String certificateUrl,
        String note,
        LocalDateTime createdAt
) {
    public static SickLeaveResponse from(SickLeave sl) {
        return new SickLeaveResponse(
                sl.getId(),
                sl.getStaff().getId(), sl.getStaff().getFullName(),
                sl.getStartDate(), sl.getEndDate(),
                ChronoUnit.DAYS.between(sl.getStartDate(), sl.getEndDate()) + 1,
                sl.getType(), sl.getStatus(),
                sl.getCertificateUrl(), sl.getNote(),
                sl.getCreatedAt()
        );
    }
}
