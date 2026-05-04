package com.airops.handling.dto.response;

import com.airops.handling.model.ShiftSwap;
import com.airops.handling.model.enums.ShiftType;
import com.airops.handling.model.enums.SwapStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SwapRequestResponse(
        Long id,
        Long requesterId,
        String requesterName,
        Long colleagueId,
        String colleagueName,
        LocalDate shiftDate,
        ShiftType fromType,
        ShiftType toType,
        String reason,
        SwapStatus status,
        String reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
    public static SwapRequestResponse from(ShiftSwap s) {
        return new SwapRequestResponse(
                s.getId(),
                s.getRequester().getId(), s.getRequester().getFullName(),
                s.getColleague().getId(), s.getColleague().getFullName(),
                s.getShiftDate(),
                s.getFromType(), s.getToType(),
                s.getReason(), s.getStatus(),
                s.getReviewedBy() != null ? s.getReviewedBy().getUsername() : null,
                s.getReviewedAt(),
                s.getCreatedAt()
        );
    }
}
