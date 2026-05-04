package com.airops.handling.dto.response;

import com.airops.handling.model.Staff;
import com.airops.handling.model.enums.ContractType;
import com.airops.handling.model.enums.Department;

import java.time.LocalDateTime;

public record StaffResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String initials,
        String email,
        String phone,
        String jobTitle,
        Department department,
        ContractType contractType,
        int contractHoursPerWeek,
        int monthlyTargetHours,
        boolean active,
        LocalDateTime createdAt
) {
    public static StaffResponse from(Staff s) {
        return new StaffResponse(
                s.getId(), s.getFirstName(), s.getLastName(),
                s.getFullName(), s.getInitials(),
                s.getEmail(), s.getPhone(), s.getJobTitle(),
                s.getDepartment(), s.getContractType(),
                s.getContractHoursPerWeek(), s.getMonthlyTargetHours(),
                s.isActive(), s.getCreatedAt()
        );
    }
}
