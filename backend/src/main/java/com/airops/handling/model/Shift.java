package com.airops.handling.model;

import com.airops.handling.model.enums.ShiftType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "shifts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_shift_staff_date", columnNames = {"staff_id", "shift_date"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shift extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "shift_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ShiftType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(length = 255)
    private String note;
}
