package com.airops.handling.controller;

import com.airops.handling.dto.request.CreateSickLeaveRequest;
import com.airops.handling.dto.response.SickLeaveResponse;
import com.airops.handling.model.enums.SickLeaveStatus;
import com.airops.handling.service.SickLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sick-leaves")
@RequiredArgsConstructor
@Tag(name = "Sick Leaves")
public class SickLeaveController {

    private final SickLeaveService sickLeaveService;

    @Operation(summary = "List all sick leave records")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_VIEWER')")
    public List<SickLeaveResponse> list() {
        return sickLeaveService.findAll();
    }

    @Operation(summary = "List sick leaves pending certificate")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','HR_VIEWER')")
    public List<SickLeaveResponse> pending() {
        return sickLeaveService.findPending();
    }

    @Operation(summary = "List sick leaves for a staff member")
    @GetMapping("/staff/{staffId}")
    public List<SickLeaveResponse> byStaff(@PathVariable Long staffId) {
        return sickLeaveService.findByStaff(staffId);
    }

    @Operation(summary = "Report a sick leave")
    @PostMapping
    public ResponseEntity<SickLeaveResponse> create(@Valid @RequestBody CreateSickLeaveRequest req) {
        SickLeaveResponse created = sickLeaveService.create(req);
        return ResponseEntity.created(URI.create("/api/sick-leaves/" + created.id())).body(created);
    }

    @Operation(summary = "Update sick leave status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','HR_VIEWER')")
    public SickLeaveResponse updateStatus(
            @PathVariable Long id,
            @RequestParam SickLeaveStatus status) {
        return sickLeaveService.updateStatus(id, status);
    }

    @Operation(summary = "Mark certificate as received")
    @PatchMapping("/{id}/certificate")
    @PreAuthorize("hasAnyRole('ADMIN','HR_VIEWER')")
    public SickLeaveResponse uploadCertificate(
            @PathVariable Long id,
            @RequestParam String certificateUrl) {
        return sickLeaveService.uploadCertificate(id, certificateUrl);
    }
}
