package com.airops.handling.controller;

import com.airops.handling.dto.request.CreateStaffRequest;
import com.airops.handling.dto.response.StaffResponse;
import com.airops.handling.model.enums.Department;
import com.airops.handling.service.StaffService;
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
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff")
public class StaffController {

    private final StaffService staffService;

    @Operation(summary = "List all active staff, optionally filtered by department")
    @GetMapping
    public List<StaffResponse> list(@RequestParam(required = false) Department department) {
        return department != null
                ? staffService.findByDepartment(department)
                : staffService.findAll();
    }

    @Operation(summary = "Get a single staff member")
    @GetMapping("/{id}")
    public StaffResponse get(@PathVariable Long id) {
        return staffService.findById(id);
    }

    @Operation(summary = "Create a new staff member")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody CreateStaffRequest req) {
        StaffResponse created = staffService.create(req);
        return ResponseEntity.created(URI.create("/api/staff/" + created.id())).body(created);
    }

    @Operation(summary = "Update a staff member")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StaffResponse update(@PathVariable Long id, @Valid @RequestBody CreateStaffRequest req) {
        return staffService.update(id, req);
    }

    @Operation(summary = "Deactivate (soft-delete) a staff member")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        staffService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
