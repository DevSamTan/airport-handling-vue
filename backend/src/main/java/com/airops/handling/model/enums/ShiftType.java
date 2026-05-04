package com.airops.handling.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Maps 1-to-1 with the frontend shift codes (M, P, N, …).
 * duration = working hours counted for contractual purposes.
 */
@Getter
@RequiredArgsConstructor
public enum ShiftType {
    MORNING  ("M",   "Mattina",    "06:00-14:00", 8,  true),
    AFTERNOON("P",   "Pomeriggio", "14:00-22:00", 8,  true),
    NIGHT    ("N",   "Notte",      "22:00-06:00", 8,  true),
    LONG_13H ("L",   "Lungo 13h",  "06:00-19:00", 13, true),
    SHORT_6H ("S6",  "Corto 6h",   "08:00-14:00", 6,  true),
    REST     ("R",   "Riposo",     null,           0,  false),
    HOLIDAY  ("F",   "Ferie",      null,           0,  false),
    SICK     ("MAL", "Malattia",   null,           0,  false),
    INJURY   ("INF", "Infortunio", null,           0,  false),
    PERMIT   ("PER", "Permesso",   null,           0,  false);

    private final String code;
    private final String label;
    private final String hours;
    private final int    duration;
    private final boolean operative; // counts as worked shift for coverage

    public static ShiftType fromCode(String code) {
        for (ShiftType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new IllegalArgumentException("Unknown shift code: " + code);
    }
}
