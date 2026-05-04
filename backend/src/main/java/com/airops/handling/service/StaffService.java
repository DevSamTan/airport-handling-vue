package com.airops.handling.service;

import com.airops.handling.dto.request.CreateStaffRequest;
import com.airops.handling.dto.response.StaffResponse;
import com.airops.handling.exception.ResourceNotFoundException;
import com.airops.handling.model.Staff;
import com.airops.handling.model.enums.Department;
import com.airops.handling.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepo;

    public List<StaffResponse> findAll() {
        return staffRepo.findByActiveTrue().stream().map(StaffResponse::from).toList();
    }

    public List<StaffResponse> findByDepartment(Department dept) {
        return staffRepo.findByDepartmentAndActiveTrue(dept).stream().map(StaffResponse::from).toList();
    }

    public StaffResponse findById(Long id) {
        return StaffResponse.from(getOrThrow(id));
    }

    @Transactional
    public StaffResponse create(CreateStaffRequest req) {
        Staff s = Staff.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .phone(req.phone())
                .jobTitle(req.jobTitle())
                .department(req.department())
                .contractType(req.contractType())
                .contractHoursPerWeek(req.contractHoursPerWeek())
                .monthlyTargetHours(req.monthlyTargetHours())
                .active(true)
                .build();
        return StaffResponse.from(staffRepo.save(s));
    }

    @Transactional
    public StaffResponse update(Long id, CreateStaffRequest req) {
        Staff s = getOrThrow(id);
        s.setFirstName(req.firstName());
        s.setLastName(req.lastName());
        s.setEmail(req.email());
        s.setPhone(req.phone());
        s.setJobTitle(req.jobTitle());
        s.setDepartment(req.department());
        s.setContractType(req.contractType());
        s.setContractHoursPerWeek(req.contractHoursPerWeek());
        s.setMonthlyTargetHours(req.monthlyTargetHours());
        return StaffResponse.from(staffRepo.save(s));
    }

    @Transactional
    public void deactivate(Long id) {
        Staff s = getOrThrow(id);
        s.setActive(false);
        staffRepo.save(s);
    }

    Staff getOrThrow(Long id) {
        return staffRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
    }
}
