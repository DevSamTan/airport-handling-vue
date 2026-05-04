package com.airops.handling.service;

import com.airops.handling.dto.response.DashboardResponse;
import com.airops.handling.model.Shift;
import com.airops.handling.model.Staff;
import com.airops.handling.model.enums.Department;
import com.airops.handling.model.enums.SickLeaveStatus;
import com.airops.handling.model.enums.SwapStatus;
import com.airops.handling.repository.ShiftRepository;
import com.airops.handling.repository.ShiftSwapRepository;
import com.airops.handling.repository.SickLeaveRepository;
import com.airops.handling.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final StaffRepository staffRepo;
    private final ShiftRepository shiftRepo;
    private final ShiftSwapRepository swapRepo;
    private final SickLeaveRepository sickLeaveRepo;

    public DashboardResponse getToday() {
        LocalDate today = LocalDate.now();

        List<Staff> activeStaff = staffRepo.findByActiveTrue();
        List<Shift> todayShifts = shiftRepo.findByDateBetweenOrderByDate(today, today);

        long pendingSwaps      = swapRepo.countByStatus(SwapStatus.PENDING);
        long pendingSickLeaves = sickLeaveRepo.countByStatus(SickLeaveStatus.CERT_PENDING);

        Map<Department, DashboardResponse.DeptCoverage> coverage = buildCoverage(activeStaff, todayShifts);
        List<DashboardResponse.HourlySlot> hourly = buildHourlyStaffing(todayShifts);

        return new DashboardResponse(
                activeStaff.size(),
                todayShifts.size(),
                (int) pendingSwaps,
                (int) pendingSickLeaves,
                coverage,
                hourly
        );
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private Map<Department, DashboardResponse.DeptCoverage> buildCoverage(
            List<Staff> active, List<Shift> shifts) {

        Map<Department, Long> totalByDept = active.stream()
                .collect(Collectors.groupingBy(Staff::getDepartment, Collectors.counting()));

        Map<Department, Long> scheduledByDept = shifts.stream()
                .filter(s -> s.getType().isOperative())
                .collect(Collectors.groupingBy(
                        s -> s.getStaff().getDepartment(), Collectors.counting()));

        Map<Department, DashboardResponse.DeptCoverage> result = new EnumMap<>(Department.class);
        for (Department dept : Department.values()) {
            int total     = Math.toIntExact(totalByDept.getOrDefault(dept, 0L));
            int scheduled = Math.toIntExact(scheduledByDept.getOrDefault(dept, 0L));
            double pct    = total == 0 ? 0 : (scheduled * 100.0 / total);
            result.put(dept, new DashboardResponse.DeptCoverage(scheduled, total, pct));
        }
        return result;
    }

    private List<DashboardResponse.HourlySlot> buildHourlyStaffing(List<Shift> shifts) {
        Map<Integer, Integer> counts = new TreeMap<>();
        for (int h = 0; h < 24; h++) counts.put(h, 0);

        for (Shift s : shifts) {
            if (s.getType().getHours() == null) continue;
            String[] parts = s.getType().getHours().split("-");
            if (parts.length != 2) continue;
            int start = LocalTime.parse(parts[0]).getHour();
            int end   = LocalTime.parse(parts[1]).getHour();
            if (end <= start) end += 24;
            for (int h = start; h < end; h++) {
                counts.merge(h % 24, 1, Integer::sum);
            }
        }

        return counts.entrySet().stream()
                .map(e -> new DashboardResponse.HourlySlot(e.getKey(), e.getValue()))
                .toList();
    }
}
