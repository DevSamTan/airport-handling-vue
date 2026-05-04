package com.airops.handling.service;

import com.airops.handling.dto.request.ShiftAssignRequest;
import com.airops.handling.dto.response.ShiftResponse;
import com.airops.handling.dto.response.WeeklyPlanResponse;
import com.airops.handling.exception.ContractViolationException;
import com.airops.handling.exception.ResourceNotFoundException;
import com.airops.handling.exception.ShiftConflictException;
import com.airops.handling.model.Shift;
import com.airops.handling.model.Staff;
import com.airops.handling.model.User;
import com.airops.handling.model.enums.ShiftType;
import com.airops.handling.repository.ShiftRepository;
import com.airops.handling.repository.StaffRepository;
import com.airops.handling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShiftService {

    private final ShiftRepository shiftRepo;
    private final StaffRepository staffRepo;
    private final UserRepository userRepo;

    public List<ShiftResponse> getShiftsForWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return shiftRepo.findByDateBetweenOrderByDate(weekStart, weekEnd)
                .stream().map(ShiftResponse::from).toList();
    }

    public WeeklyPlanResponse getWeeklyPlan(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<Shift> shifts = shiftRepo.findByDateBetweenOrderByDate(weekStart, weekEnd);
        List<Staff> allActive = staffRepo.findByActiveTrue();

        Map<Long, Map<LocalDate, ShiftResponse>> byStaff = new LinkedHashMap<>();
        for (Shift sh : shifts) {
            byStaff.computeIfAbsent(sh.getStaff().getId(), k -> new LinkedHashMap<>())
                    .put(sh.getDate(), ShiftResponse.from(sh));
        }

        List<WeeklyPlanResponse.StaffWeekRow> rows = allActive.stream().map(staff -> {
            Map<LocalDate, ShiftResponse> week = byStaff.getOrDefault(staff.getId(), Map.of());
            double total = week.values().stream().mapToInt(ShiftResponse::duration).sum();
            return new WeeklyPlanResponse.StaffWeekRow(
                    com.airops.handling.dto.response.StaffResponse.from(staff), week, total);
        }).toList();

        return new WeeklyPlanResponse(weekStart, weekEnd, rows);
    }

    public List<ShiftResponse> getShiftsForStaff(Long staffId, int year, int month) {
        return shiftRepo.findByStaffIdAndYearAndMonth(staffId, year, month)
                .stream().map(ShiftResponse::from).toList();
    }

    @Transactional
    public ShiftResponse assign(ShiftAssignRequest req, String assignedByUsername) {
        Staff staff = staffRepo.findById(req.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", req.staffId()));

        shiftRepo.findByStaffAndDate(staff, req.date()).ifPresent(existing -> {
            throw new ShiftConflictException(staff.getFullName(), req.date().toString());
        });

        validateWeeklyHours(staff, req.date(), req.type());

        User assigner = userRepo.findByUsername(assignedByUsername).orElse(null);

        Shift shift = Shift.builder()
                .staff(staff)
                .date(req.date())
                .type(req.type())
                .note(req.note())
                .assignedBy(assigner)
                .build();

        return ShiftResponse.from(shiftRepo.save(shift));
    }

    @Transactional
    public ShiftResponse update(Long shiftId, ShiftAssignRequest req, String username) {
        Shift shift = shiftRepo.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", shiftId));

        if (!shift.getType().equals(req.type())) {
            validateWeeklyHours(shift.getStaff(), shift.getDate(), req.type());
        }

        shift.setType(req.type());
        shift.setNote(req.note());
        return ShiftResponse.from(shiftRepo.save(shift));
    }

    @Transactional
    public void delete(Long shiftId) {
        if (!shiftRepo.existsById(shiftId)) {
            throw new ResourceNotFoundException("Shift", shiftId);
        }
        shiftRepo.deleteById(shiftId);
    }

    // ── Contract validation ────────────────────────────────────────────────

    private void validateWeeklyHours(Staff staff, LocalDate date, ShiftType newType) {
        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = weekStart.plusDays(6);

        int existingHours = shiftRepo.findByStaffAndDateBetweenOrderByDate(staff, weekStart, weekEnd)
                .stream()
                .filter(s -> !s.getDate().equals(date))
                .mapToInt(s -> s.getType().getDuration())
                .sum();

        int projected = existingHours + newType.getDuration();
        int max = staff.getContractHoursPerWeek() + 8; // allow up to 8h overtime

        if (projected > max) {
            throw new ContractViolationException(
                    "Assigning %s to %s on %s would exceed weekly contract limit (%dh projected, %dh max)"
                            .formatted(newType.getLabel(), staff.getFullName(), date, projected, max));
        }
    }
}
