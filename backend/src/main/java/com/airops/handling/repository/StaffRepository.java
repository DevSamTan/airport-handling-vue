package com.airops.handling.repository;

import com.airops.handling.model.Staff;
import com.airops.handling.model.enums.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByActiveTrue();
    List<Staff> findByDepartmentAndActiveTrue(Department department);
    boolean existsByEmail(String email);
}
