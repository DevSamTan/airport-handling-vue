package com.airops.handling.controller;

import com.airops.handling.dto.request.ShiftAssignRequest;
import com.airops.handling.dto.response.ShiftResponse;
import com.airops.handling.dto.response.WeeklyPlanResponse;
import com.airops.handling.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts")
public class ShiftController {

    private final ShiftService shiftService;

    @Operation(summary = "Get weekly plan grid")
    @GetMapping("/weekly")
    public WeeklyPlanResponse weeklyPlan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return shiftService.getWeeklyPlan(weekStart);
    }

    @Operation(summary = "Get raw shifts for a week")
    @GetMapping
    public List<ShiftResponse> forWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return shiftService.getShiftsForWeek(weekStart);
    }

    @Operation(summary = "Get shifts for a staff member in a given month")
    @GetMapping("/staff/{staffId}")
    public List<ShiftResponse> forStaff(
            @PathVariable Long staffId,
            @RequestParam int year,
            @RequestParam int month) {
        return shiftService.getShiftsForStaff(staffId, year, month);
    }

    @Operation(summary = "Assign a shift")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PLANNER')")
    public ResponseEntity<ShiftResponse> assign(
            @Valid @RequestBody ShiftAssignRequest req,
            @AuthenticationPrincipal UserDetails user) {
        ShiftResponse created = shiftService.assign(req, user.getUsername());
        return ResponseEntity.created(URI.create("/api/shifts/" + created.id())).body(created);
    }

    @Operation(summary = "Update a shift")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PLANNER')")
    public ShiftResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ShiftAssignRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return shiftService.update(id, req, user.getUsername());
    }

    @Operation(summary = "Delete a shift")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PLANNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shiftService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
