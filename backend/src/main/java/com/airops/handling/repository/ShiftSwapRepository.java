package com.airops.handling.repository;

import com.airops.handling.model.ShiftSwap;
import com.airops.handling.model.enums.SwapStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftSwapRepository extends JpaRepository<ShiftSwap, Long> {
    List<ShiftSwap> findByStatus(SwapStatus status);
    List<ShiftSwap> findByRequesterIdOrderByCreatedAtDesc(Long staffId);
    List<ShiftSwap> findAllByOrderByCreatedAtDesc();
    long countByStatus(SwapStatus status);
}
