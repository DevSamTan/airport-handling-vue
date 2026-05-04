package com.airops.handling.dto.request;

import com.airops.handling.model.enums.ContractType;
import com.airops.handling.model.enums.Department;
import jakarta.validation.constraints.*;

public record CreateStaffRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String jobTitle,
        @NotNull Department department,
        @NotNull ContractType contractType,
        @Min(1) @Max(48) int contractHoursPerWeek,
        @Min(1) int monthlyTargetHours
) {}
