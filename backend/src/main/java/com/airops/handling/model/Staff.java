package com.airops.handling.model;

import com.airops.handling.model.enums.ContractType;
import com.airops.handling.model.enums.Department;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staff")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Staff extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    /** Job title, e.g. "Op. Rampa", "Team Leader" */
    @Column(nullable = false, length = 80)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContractType contractType;

    /** Weekly contracted hours (e.g. 40, 30, 24) */
    @Column(nullable = false)
    private int contractHoursPerWeek;

    /** Monthly target hours (derived from contractHoursPerWeek × ~4) */
    @Column(nullable = false)
    private int monthlyTargetHours;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // Computed convenience
    public String getFullName()  { return firstName + " " + lastName; }
    public String getInitials()  {
        return (firstName.isEmpty() ? "" : String.valueOf(firstName.charAt(0)))
             + (lastName.isEmpty()  ? "" : String.valueOf(lastName.charAt(0)));
    }
}
