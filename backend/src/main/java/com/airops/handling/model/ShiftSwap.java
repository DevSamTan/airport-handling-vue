package com.airops.handling.model;

import com.airops.handling.model.enums.ShiftType;
import com.airops.handling.model.enums.SwapStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_swaps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftSwap extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Staff requester;

    /** Null for single-person shift-move requests */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colleague_id")
    private Staff colleague;

    @Column(nullable = false)
    private LocalDate shiftDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ShiftType fromType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ShiftType toType;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private SwapStatus status = SwapStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private LocalDateTime reviewedAt;
}
