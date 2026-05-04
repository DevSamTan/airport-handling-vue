package com.airops.handling.config;

import com.airops.handling.model.*;
import com.airops.handling.model.enums.*;
import com.airops.handling.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository     userRepo;
    private final StaffRepository    staffRepo;
    private final ShiftRepository    shiftRepo;
    private final PasswordEncoder    passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("DataInitializer: data already present, skipping.");
            return;
        }

        // ── Users ────────────────────────────────────────────────────────────
        userRepo.save(User.builder()
                .username("admin")
                .email("admin@airops.it")
                .password(passwordEncoder.encode("Admin123!"))
                .role(UserRole.ADMIN)
                .build());

        User planner = userRepo.save(User.builder()
                .username("planner")
                .email("planner@airops.it")
                .password(passwordEncoder.encode("Plan123!"))
                .role(UserRole.PLANNER)
                .build());

        userRepo.save(User.builder()
                .username("hrviewer")
                .email("hr@airops.it")
                .password(passwordEncoder.encode("Hr123!"))
                .role(UserRole.HR_VIEWER)
                .build());

        // ── Staff ─────────────────────────────────────────────────────────────
        List<Staff> crew = staffRepo.saveAll(List.of(
            staff("Marco",  "Rossi",    "m.rossi@airops.it",    "Op. Rampa",     Department.RAMPA,        ContractType.FULL_TIME,          40, 160),
            staff("Sara",   "Bianchi",  "s.bianchi@airops.it",  "Team Leader",   Department.RAMPA,        ContractType.SPECIAL_TL,         40, 160),
            staff("Luca",   "Verdi",    "l.verdi@airops.it",    "Op. Magazzino", Department.MAGAZZINO,    ContractType.FULL_TIME,          40, 160),
            staff("Anna",   "Ferrari",  "a.ferrari@airops.it",  "Op. Magazzino", Department.MAGAZZINO,    ContractType.PART_TIME_HORIZONTAL,30, 120),
            staff("Paolo",  "Ricci",    "p.ricci@airops.it",    "Coordinatore",  Department.COORDINATORI, ContractType.FULL_TIME,          40, 160),
            staff("Giulia", "Esposito", "g.esposito@airops.it", "Coord. Senior", Department.COORDINATORI, ContractType.SPECIAL_SV,         40, 160),
            staff("Matteo", "Romano",   "m.romano@airops.it",   "Tec. GSE",      Department.GSE,          ContractType.SPECIAL_GSE,        40, 160),
            staff("Elena",  "Colombo",  "e.colombo@airops.it",  "Op. GSE",       Department.GSE,          ContractType.PART_TIME_VERTICAL, 24,  96)
        ));

        // ── Sample shifts (current week Mon-Fri) ──────────────────────────────
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        ShiftType[] pattern = {ShiftType.MORNING, ShiftType.AFTERNOON, ShiftType.MORNING,
                               ShiftType.NIGHT, ShiftType.REST, ShiftType.HOLIDAY, ShiftType.MORNING};

        for (int i = 0; i < crew.size(); i++) {
            for (int d = 0; d < 7; d++) {
                ShiftType type = pattern[(i + d) % pattern.length];
                shiftRepo.save(Shift.builder()
                        .staff(crew.get(i))
                        .date(monday.plusDays(d))
                        .type(type)
                        .assignedBy(planner)
                        .build());
            }
        }

        log.info("DataInitializer: seeded {} users, {} staff, {} shifts.",
                userRepo.count(), staffRepo.count(), shiftRepo.count());
    }

    private Staff staff(String first, String last, String email,
                        String jobTitle, Department dept,
                        ContractType contract, int weeklyH, int monthlyH) {
        return Staff.builder()
                .firstName(first).lastName(last).email(email)
                .jobTitle(jobTitle).department(dept)
                .contractType(contract)
                .contractHoursPerWeek(weeklyH)
                .monthlyTargetHours(monthlyH)
                .active(true)
                .build();
    }
}
