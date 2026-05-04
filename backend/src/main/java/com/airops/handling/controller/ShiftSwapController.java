package com.airops.handling.controller;

import com.airops.handling.dto.request.CreateSwapRequest;
import com.airops.handling.dto.response.SwapRequestResponse;
import com.airops.handling.model.enums.SwapStatus;
import com.airops.handling.service.ShiftSwapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/shift-swaps")
@RequiredArgsConstructor
@Tag(name = "Shift Swaps")
public class ShiftSwapController {

    private final ShiftSwapService swapService;

    @Operation(summary = "List all swap requests")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PLANNER','HR_VIEWER')")
    public List<SwapRequestResponse> list() {
        return swapService.findAll();
    }

    @Operation(summary = "List pending swap requests")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','PLANNER')")
    public List<SwapRequestResponse> pending() {
        return swapService.findPending();
    }

    @Operation(summary = "List swap requests submitted by a staff member")
    @GetMapping("/staff/{staffId}")
    public List<SwapRequestResponse> byStaff(@PathVariable Long staffId) {
        return swapService.findByStaff(staffId);
    }

    @Operation(summary = "Submit a shift swap request")
    @PostMapping
    public ResponseEntity<SwapRequestResponse> create(@Valid @RequestBody CreateSwapRequest req) {
        SwapRequestResponse created = swapService.create(req);
        return ResponseEntity.created(URI.create("/api/shift-swaps/" + created.id())).body(created);
    }

    @Operation(summary = "Approve or reject a swap request")
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','PLANNER')")
    public SwapRequestResponse review(
            @PathVariable Long id,
            @RequestParam SwapStatus decision,
            @AuthenticationPrincipal UserDetails user) {
        return swapService.review(id, decision, user.getUsername());
    }
}
