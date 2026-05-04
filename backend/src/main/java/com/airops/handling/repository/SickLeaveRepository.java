package com.airops.handling.repository;

import com.airops.handling.model.SickLeave;
import com.airops.handling.model.enums.SickLeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {
    List<SickLeave> findByStaffIdOrderByStartDateDesc(Long staffId);
    List<SickLeave> findByStatusOrderByStartDateDesc(SickLeaveStatus status);
    List<SickLeave> findAllByOrderByStartDateDesc();
    long countByStatus(SickLeaveStatus status);
}
