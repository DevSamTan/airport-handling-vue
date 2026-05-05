import { defineStore } from "pinia";
import { computed, ref, watch } from "vue";

export const SHIFT_TYPES = {
  M:   { label: "Mattina",    hours: "06:00–14:00", duration: 8,  abbr: "MAT" },
  P:   { label: "Pomeriggio", hours: "14:00–22:00", duration: 8,  abbr: "POM" },
  N:   { label: "Notte",      hours: "22:00–06:00", duration: 8,  abbr: "NOT" },
  L:   { label: "Lungo 13h",  hours: "06:00–19:00", duration: 13, abbr: "13H" },
  S6:  { label: "Corto 6h",   hours: "08:00–14:00", duration: 6,  abbr: "6H"  },
  R:   { label: "Riposo 1",   hours: "—",           duration: 0,  abbr: "RIP" },
  F:   { label: "Ferie",      hours: "—",           duration: 8,  abbr: "FER" },
  MAL: { label: "Malattia",   hours: "—",           duration: 0,  abbr: "MAL" },
  INF: { label: "Infortunio", hours: "—",           duration: 0,  abbr: "INF" },
  PER: { label: "Permesso",   hours: "—",           duration: 0,  abbr: "PER" },
};

export const SHIFT_COLORS = {
  M:   "bg-blue-500/20 text-blue-300 border-l-2 border-blue-400",
  P:   "bg-orange-500/20 text-orange-300 border-l-2 border-orange-400",
  N:   "bg-indigo-500/20 text-indigo-300 border-l-2 border-indigo-400",
  L:   "bg-purple-500/20 text-purple-300 border-l-2 border-purple-400",
  S6:  "bg-cyan-500/20 text-cyan-300 border-l-2 border-cyan-400",
  R:   "bg-slate-700/60 text-slate-400",
  F:   "bg-emerald-500/20 text-emerald-300 border-l-2 border-emerald-400",
  MAL: "bg-red-500/20 text-red-300 border-l-2 border-red-400",
  INF: "bg-rose-800/40 text-rose-300 border-l-2 border-rose-600",
  PER: "bg-yellow-500/20 text-yellow-300 border-l-2 border-yellow-400",
};

export const DEPARTMENTS = ["Rampa", "Magazzino", "Coordinatori", "GSE"];

const DEFAULT_STAFF = [
  { id: 1, name: "Marco Rossi",    initials: "MR", role: "Op. Rampa",     dept: "Rampa",        contract: "Full-time", contractHours: 40, monthlyTarget: 160 },
  { id: 2, name: "Laura Bianchi",  initials: "LB", role: "Op. Magazzino", dept: "Magazzino",    contract: "Part-time", contractHours: 30, monthlyTarget: 120 },
  { id: 3, name: "Giuseppe Verdi", initials: "GV", role: "Team Leader",   dept: "Rampa",        contract: "Full-time", contractHours: 40, monthlyTarget: 160 },
  { id: 4, name: "Anna Ferrari",   initials: "AF", role: "Coordinatrice", dept: "Coordinatori", contract: "Full-time", contractHours: 40, monthlyTarget: 160 },
  { id: 5, name: "Carlo Esposito", initials: "CE", role: "GSE Tecnico",   dept: "GSE",          contract: "Full-time", contractHours: 40, monthlyTarget: 160 },
  { id: 6, name: "Sofia Marino",   initials: "SM", role: "Op. Rampa",     dept: "Rampa",        contract: "Part-time", contractHours: 24, monthlyTarget: 96  },
  { id: 7, name: "Luca Romano",    initials: "LR", role: "Op. Magazzino", dept: "Magazzino",    contract: "Full-time", contractHours: 40, monthlyTarget: 160 },
  { id: 8, name: "Elena Costa",    initials: "EC", role: "Op. Rampa",     dept: "Rampa",        contract: "Full-time", contractHours: 40, monthlyTarget: 160 },
];

function loadJson(key) {
  try { const r = localStorage.getItem(key); return r ? JSON.parse(r) : null } catch { return null }
}
function saveJson(key, value) {
  try { localStorage.setItem(key, JSON.stringify(value)) } catch {}
}

export const useShiftStore = defineStore("shifts", () => {
  const savedStaff  = loadJson("airops_staff");
  const savedShifts = loadJson("airops_shifts");

  const staff  = ref(savedStaff  ?? DEFAULT_STAFF.map(s => ({ ...s })));
  const shifts = ref(savedShifts ?? {});

  watch(staff,  v => saveJson("airops_staff",  v), { deep: true });
  watch(shifts, v => saveJson("airops_shifts", v), { deep: true });

  function _key(staffId, date) {
    const d = date instanceof Date ? date : new Date(date);
    return `${staffId}_${d.toISOString().slice(0, 10)}`;
  }

  // Returns string[] — handles both legacy string values and new array values
  function getShifts(staffId, date) {
    const val = shifts.value[_key(staffId, date)];
    if (!val) return [];
    return Array.isArray(val) ? val : [val];
  }

  // Backward-compat: returns first shift or null
  function getShift(staffId, date) {
    const arr = getShifts(staffId, date);
    return arr.length > 0 ? arr[0] : null;
  }

  // Replaces all shifts for a day with a single type (or deletes if null)
  function setShift(staffId, date, type) {
    const key = _key(staffId, date);
    if (type === null) {
      delete shifts.value[key];
    } else {
      shifts.value[key] = [type];
    }
  }

  // Adds a shift type to a day (avoids duplicates)
  function addShift(staffId, date, type) {
    const key = _key(staffId, date);
    const current = shifts.value[key];
    if (!current) {
      shifts.value[key] = [type];
    } else {
      const arr = Array.isArray(current) ? [...current] : [current];
      if (!arr.includes(type)) arr.push(type);
      shifts.value[key] = arr;
    }
  }

  // Removes one specific shift type from a day
  function removeShiftItem(staffId, date, type) {
    const key = _key(staffId, date);
    const current = shifts.value[key];
    if (!current) return;
    const arr = (Array.isArray(current) ? current : [current]).filter(t => t !== type);
    if (arr.length === 0) delete shifts.value[key];
    else shifts.value[key] = arr;
  }

  function _seed() {
    const base  = new Date("2026-04-27");
    const base2 = new Date("2026-04-20");
    const patterns = [
      [1, ["M","M","P","P","R","R","N"]],
      [2, ["P","P","R","M","M","S6","R"]],
      [3, ["N","R","R","M","M","P","N"]],
      [4, ["M","P","P","R","M","M","P"]],
      [5, ["R","M","M","P","P","N","R"]],
      [6, ["P","N","M","M","R","P","P"]],
      [7, ["M","R","N","P","M","M","F"]],
      [8, ["MAL","MAL","MAL","R","M","P","N"]],
    ];
    const patterns2 = [
      [1, ["P","M","M","N","R","M","P"]],
      [2, ["M","R","P","P","M","R","M"]],
      [3, ["M","P","N","R","M","P","M"]],
      [4, ["P","P","R","M","M","P","R"]],
      [5, ["N","M","P","R","M","M","N"]],
      [6, ["M","M","R","P","N","M","R"]],
      [7, ["P","M","M","R","P","P","M"]],
      [8, ["M","P","M","M","R","N","MAL"]],
    ];
    [...patterns.map(p => ({ base, p })), ...patterns2.map(p => ({ base: base2, p }))].forEach(({ base: b, p: [sid, types] }) => {
      types.forEach((type, i) => {
        const d = new Date(b);
        d.setDate(d.getDate() + i);
        shifts.value[_key(sid, d)] = [type];
      });
    });
  }

  if (!savedShifts) _seed();

  function weeklyHours(staffId, weekStart) {
    let total = 0;
    for (let i = 0; i < 7; i++) {
      const d = new Date(weekStart);
      d.setDate(d.getDate() + i);
      for (const type of getShifts(staffId, d)) {
        if (SHIFT_TYPES[type]) total += SHIFT_TYPES[type].duration;
      }
    }
    return total;
  }

  function monthlyHours(staffId, year, month) {
    let total = 0;
    const days = new Date(year, month, 0).getDate();
    for (let d = 1; d <= days; d++) {
      const date = new Date(year, month - 1, d);
      for (const type of getShifts(staffId, date)) {
        if (SHIFT_TYPES[type]) total += SHIFT_TYPES[type].duration;
      }
    }
    return total;
  }

  const currentStaff = computed(() => staff.value.find(s => s.id === 1));

  function importFromCsv(parsedRows) {
    let nextId = Math.max(...staff.value.map(s => s.id)) + 1;
    for (const row of parsedRows) {
      let member = staff.value.find(s => s.name.toLowerCase() === row.name.toLowerCase());
      if (!member) {
        const parts = row.name.split(" ");
        const initials = parts.map(p => p[0] || "").join("").slice(0, 2).toUpperCase();
        member = { id: nextId++, name: row.name, initials, role: "Op. Rampa", dept: "Rampa", contract: "Full-time", contractHours: 40, monthlyTarget: 160 };
        staff.value.push(member);
      }
      for (const [dateStr, shiftType] of Object.entries(row.shifts)) {
        setShift(member.id, dateStr, shiftType);
      }
    }
  }

  return {
    staff, shifts,
    getShift, getShifts, setShift, addShift, removeShiftItem,
    weeklyHours, monthlyHours, currentStaff, importFromCsv,
  };
});
