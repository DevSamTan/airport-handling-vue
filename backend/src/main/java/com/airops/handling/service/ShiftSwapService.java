package com.airops.handling.service;

import com.airops.handling.dto.request.CreateSwapRequest;
import com.airops.handling.dto.response.SwapRequestResponse;
import com.airops.handling.exception.ResourceNotFoundException;
import com.airops.handling.model.ShiftSwap;
import com.airops.handling.model.Staff;
import com.airops.handling.model.User;
import com.airops.handling.model.enums.SwapStatus;
import com.airops.handling.repository.ShiftSwapRepository;
import com.airops.handling.repository.StaffRepository;
import com.airops.handling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShiftSwapService {

    private final ShiftSwapRepository swapRepo;
    private final StaffRepository staffRepo;
    private final UserRepository userRepo;

    public List<SwapRequestResponse> findAll() {
        return swapRepo.findAllByOrderByCreatedAtDesc().stream().map(SwapRequestResponse::from).toList();
    }

    public List<SwapRequestResponse> findPending() {
        return swapRepo.findByStatus(SwapStatus.PENDING).stream().map(SwapRequestResponse::from).toList();
    }

    public List<SwapRequestResponse> findByStaff(Long staffId) {
        return swapRepo.findByRequesterIdOrderByCreatedAtDesc(staffId)
                .stream().map(SwapRequestResponse::from).toList();
    }

    @Transactional
    public SwapRequestResponse create(CreateSwapRequest req) {
        Staff requester = staffRepo.findById(req.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", req.requesterId()));
        Staff colleague = staffRepo.findById(req.colleagueId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", req.colleagueId()));

        ShiftSwap swap = ShiftSwap.builder()
                .requester(requester)
                .colleague(colleague)
                .shiftDate(req.shiftDate())
                .fromType(req.fromType())
                .toType(req.toType())
                .reason(req.reason())
                .status(SwapStatus.PENDING)
                .build();

        return SwapRequestResponse.from(swapRepo.save(swap));
    }

    @Transactional
    public SwapRequestResponse review(Long swapId, SwapStatus decision, String reviewerUsername) {
        ShiftSwap swap = swapRepo.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftSwap", swapId));

        User reviewer = userRepo.findByUsername(reviewerUsername).orElse(null);
        swap.setStatus(decision);
        swap.setReviewedBy(reviewer);
        swap.setReviewedAt(LocalDateTime.now());

        return SwapRequestResponse.from(swapRepo.save(swap));
    }
}
