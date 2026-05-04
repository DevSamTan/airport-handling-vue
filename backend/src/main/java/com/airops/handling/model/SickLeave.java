package com.airops.handling.model;

import com.airops.handling.model.enums.SickLeaveStatus;
import com.airops.handling.model.enums.SickLeaveType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sick_leaves")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SickLeave extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SickLeaveType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SickLeaveStatus status = SickLeaveStatus.CERT_PENDING;

    /** URL/path to the uploaded medical certificate */
    @Column(length = 500)
    private String certificateUrl;

    @Column(length = 500)
    private String note;
}
