import { defineStore } from "pinia";
import { computed, ref } from "vue";

export const SHIFT_TYPES = {
  M: { label: "Mattina", hours: "06:00–14:00", duration: 8, abbr: "MAT" },
  P: { label: "Pomeriggio", hours: "14:00–22:00", duration: 8, abbr: "POM" },
  N: { label: "Notte", hours: "22:00–06:00", duration: 8, abbr: "NOT" },
  L: { label: "Lungo 13h", hours: "06:00–19:00", duration: 13, abbr: "13H" },
  S6: { label: "Corto 6h", hours: "08:00–14:00", duration: 6, abbr: "6H" },
  R: { label: "Riposo 1", hours: "—", duration: 0, abbr: "RIP" },
  F: { label: "Ferie", hours: "—", duration: 8, abbr: "FER" },
  MAL: { label: "Malattia", hours: "—", duration: 0, abbr: "MAL" },
  INF: { label: "Infortunio", hours: "—", duration: 0, abbr: "INF" },
  PER: { label: "Permesso", hours: "—", duration: 0, abbr: "PER" },
};
//color shift
export const SHIFT_COLORS = {
  M: "bg-blue-500/20 text-blue-300 border-l-2 border-blue-400",
  P: "bg-orange-500/20 text-orange-300 border-l-2 border-orange-400",
  N: "bg-indigo-500/20 text-indigo-300 border-l-2 border-indigo-400",
  L: "bg-purple-500/20 text-purple-300 border-l-2 border-purple-400",
  S6: "bg-cyan-500/20 text-cyan-300 border-l-2 border-cyan-400",
  R: "bg-slate-700/60 text-slate-400",
  F: "bg-emerald-500/20 text-emerald-300 border-l-2 border-emerald-400",
  MAL: "bg-red-500/20 text-red-300 border-l-2 border-red-400",
  INF: "bg-rose-800/40 text-rose-300 border-l-2 border-rose-600",
  PER: "bg-yellow-500/20 text-yellow-300 border-l-2 border-yellow-400",
};

export const DEPARTMENTS = ["Rampa", "Magazzino", "Coordinatori", "GSE"];

export const useShiftStore = defineStore("shifts", () => {
  const staff = ref([
    {
      id: 1,
      name: "Marco Rossi",
      initials: "MR",
      role: "Op. Rampa",
      dept: "Rampa",
      contract: "Full-time",
      contractHours: 40,
      monthlyTarget: 160,
    },
    {
      id: 2,
      name: "Laura Bianchi",
      initials: "LB",
      role: "Op. Magazzino",
      dept: "Magazzino",
      contract: "Part-time",
      contractHours: 30,
      monthlyTarget: 120,
    },
    {
      id: 3,
      name: "Giuseppe Verdi",
      initials: "GV",
      role: "Team Leader",
      dept: "Rampa",
      contract: "Full-time",
      contractHours: 40,
      monthlyTarget: 160,
    },
    {
      id: 4,
      name: "Anna Ferrari",
      initials: "AF",
      role: "Coordinatrice",
      dept: "Coordinatori",
      contract: "Full-time",
      contractHours: 40,
      monthlyTarget: 160,
    },
    {
      id: 5,
      name: "Carlo Esposito",
      initials: "CE",
      role: "GSE Tecnico",
      dept: "GSE",
      contract: "Full-time",
      contractHours: 40,
      monthlyTarget: 160,
    },
    {
      id: 6,
      name: "Sofia Marino",
      initials: "SM",
      role: "Op. Rampa",
      dept: "Rampa",
      contract: "Part-time",
      contractHours: 24,
      monthlyTarget: 96,
    },
    {
      id: 7,
      name: "Luca Romano",
      initials: "LR",
      role: "Op. Magazzino",
      dept: "Magazzino",
      contract: "Full-time",
      contractHours: 40,
      monthlyTarget: 160,
    },
    {
      id: 8,
      name: "Elena Costa",
      initials: "EC",
      role: "Op. Rampa",
      dept: "Rampa",
      contract: "Full-time",
      contractHours: 40,
      monthlyTarget: 160,
    },
  ]);

  // shifts keyed as "staffId_YYYY-MM-DD"
  const shifts = ref({});

  function _key(staffId, date) {
    const d = date instanceof Date ? date : new Date(date);
    return `${staffId}_${d.toISOString().slice(0, 10)}`;
  }

  function _seed() {
    // Seed week of 2026-04-27 (Mon) through 2026-05-03
    const base = new Date("2026-04-27");
    const patterns = [
      [1, ["M", "M", "P", "P", "R", "R", "N"]],
      [2, ["P", "P", "R", "M", "M", "S6", "R"]],
      [3, ["N", "R", "R", "M", "M", "P", "N"]],
      [4, ["M", "P", "P", "R", "M", "M", "P"]],
      [5, ["R", "M", "M", "P", "P", "N", "R"]],
      [6, ["P", "N", "M", "M", "R", "P", "P"]],
      [7, ["M", "R", "N", "P", "M", "M", "F"]],
      [8, ["MAL", "MAL", "MAL", "R", "M", "P", "N"]],
    ];
    // Also previous week 2026-04-20
    const base2 = new Date("2026-04-20");
    const patterns2 = [
      [1, ["P", "M", "M", "N", "R", "M", "P"]],
      [2, ["M", "R", "P", "P", "M", "R", "M"]],
      [3, ["M", "P", "N", "R", "M", "P", "M"]],
      [4, ["P", "P", "R", "M", "M", "P", "R"]],
      [5, ["N", "M", "P", "R", "M", "M", "N"]],
      [6, ["M", "M", "R", "P", "N", "M", "R"]],
      [7, ["P", "M", "M", "R", "P", "P", "M"]],
      [8, ["M", "P", "M", "M", "R", "N", "MAL"]],
    ];
    [
      ...patterns.map((p) => ({ base, p })),
      ...patterns2.map((p) => ({ base: base2, p })),
    ].forEach(({ base: b, p: [sid, types] }) => {
      types.forEach((type, i) => {
        const d = new Date(b);
        d.setDate(d.getDate() + i);
        shifts.value[_key(sid, d)] = type;
      });
    });
  }
  _seed();

  function getShift(staffId, date) {
    return shifts.value[_key(staffId, date)] || null;
  }

  function setShift(staffId, date, type) {
    if (type === null) {
      delete shifts.value[_key(staffId, date)];
    } else {
      shifts.value[_key(staffId, date)] = type;
    }
  }

  function weeklyHours(staffId, weekStart) {
    let total = 0;
    for (let i = 0; i < 7; i++) {
      const d = new Date(weekStart);
      d.setDate(d.getDate() + i);
      const type = getShift(staffId, d);
      if (type && SHIFT_TYPES[type]) total += SHIFT_TYPES[type].duration;
    }
    return total;
  }

  function monthlyHours(staffId, year, month) {
    let total = 0;
    const days = new Date(year, month, 0).getDate();
    for (let d = 1; d <= days; d++) {
      const date = new Date(year, month - 1, d);
      const type = getShift(staffId, date);
      if (type && SHIFT_TYPES[type]) total += SHIFT_TYPES[type].duration;
    }
    return total;
  }

  const currentStaff = computed(() => staff.value.find((s) => s.id === 1));

  return {
    staff,
    shifts,
    getShift,
    setShift,
    weeklyHours,
    monthlyHours,
    currentStaff,
  };
});
