package com.airops.handling.repository;

import com.airops.handling.model.Shift;
import com.airops.handling.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    Optional<Shift> findByStaffAndDate(Staff staff, LocalDate date);

    List<Shift> findByStaffAndDateBetweenOrderByDate(Staff staff, LocalDate from, LocalDate to);

    List<Shift> findByDateBetweenOrderByDate(LocalDate from, LocalDate to);

    @Query("SELECT s FROM Shift s WHERE s.staff.id = :staffId AND YEAR(s.date) = :year AND MONTH(s.date) = :month")
    List<Shift> findByStaffIdAndYearAndMonth(
            @Param("staffId") Long staffId,
            @Param("year")    int year,
            @Param("month")   int month);

    @Query("SELECT s FROM Shift s WHERE s.date BETWEEN :from AND :to AND s.staff.department = :dept")
    List<Shift> findByDateRangeAndDepartment(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to,
            @Param("dept") com.airops.handling.model.enums.Department dept);
}
