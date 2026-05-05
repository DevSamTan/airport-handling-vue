import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

const DEFAULT_SWAPS = [
  { id: 1, requester: 'Marco Rossi',   requesterId: 1, colleague: 'Sofia Marino',   colleagueId: 6,    date: '2026-05-02', fromShift: 'P', toShift: 'M', reason: 'Impegno familiare', status: 'pending',  createdAt: '2026-04-30 09:14' },
  { id: 2, requester: 'Luca Romano',   requesterId: 7, colleague: null,              colleagueId: null, date: '2026-05-05', fromShift: 'N', toShift: 'P', reason: 'Visita medica',    status: 'approved', createdAt: '2026-04-29 14:30' },
  { id: 3, requester: 'Laura Bianchi', requesterId: 2, colleague: 'Carlo Esposito', colleagueId: 5,    date: '2026-04-30', fromShift: 'M', toShift: 'P', reason: '',                  status: 'rejected', createdAt: '2026-04-28 08:00' },
  { id: 4, requester: 'Anna Ferrari',  requesterId: 4, colleague: null,              colleagueId: null, date: '2026-05-07', fromShift: 'P', toShift: 'M', reason: 'Corso formazione', status: 'pending',  createdAt: '2026-04-30 11:05' },
]

const DEFAULT_SICK = [
  { id: 1, employee: 'Elena Costa', employeeId: 8, startDate: '2026-04-28', endDate: '2026-04-30', type: 'MAL', status: 'cert_pending',  certificate: null,       note: '',                    protocolNumber: null,           hrSentAt: null                },
  { id: 2, employee: 'Luca Romano', employeeId: 7, startDate: '2026-04-22', endDate: '2026-04-23', type: 'MAL', status: 'cert_received', certificate: 'cert.pdf', note: 'Influenza',           protocolNumber: 'MAL-2026-0038', hrSentAt: '22/04/2026 14:00' },
  { id: 3, employee: 'Marco Rossi', employeeId: 1, startDate: '2026-03-10', endDate: '2026-03-11', type: 'INF', status: 'cert_received', certificate: 'cert.pdf', note: 'Infortunio piazzale', protocolNumber: 'INF-2026-0012', hrSentAt: '10/03/2026 09:15' },
]

const DEFAULT_VACATIONS = [
  { id: 1, employee: 'Giuseppe Verdi', employeeId: 3, startDate: '2026-05-10', endDate: '2026-05-17', reason: 'Vacanze estive',    status: 'approved', createdAt: '2026-04-20 11:00' },
  { id: 2, employee: 'Laura Bianchi',  employeeId: 2, startDate: '2026-05-20', endDate: '2026-05-23', reason: 'Impegno personale', status: 'pending',  createdAt: '2026-04-29 16:30' },
]

function loadJson(key) {
  try { const r = localStorage.getItem(key); return r ? JSON.parse(r) : null } catch { return null }
}
function saveJson(key, value) {
  try { localStorage.setItem(key, JSON.stringify(value)) } catch {}
}

export const useRequestStore = defineStore('requests', () => {
  const shiftSwaps       = ref(loadJson('airops_swaps')     ?? DEFAULT_SWAPS.map(s => ({ ...s })))
  const sickLeaves       = ref(loadJson('airops_sick')      ?? DEFAULT_SICK.map(s => ({ ...s })))
  const vacationRequests = ref(loadJson('airops_vacations') ?? DEFAULT_VACATIONS.map(v => ({ ...v })))

  watch(shiftSwaps,       v => saveJson('airops_swaps',     v), { deep: true })
  watch(sickLeaves,       v => saveJson('airops_sick',      v), { deep: true })
  watch(vacationRequests, v => saveJson('airops_vacations', v), { deep: true })

  const pendingSwaps     = computed(() => shiftSwaps.value.filter(s => s.status === 'pending'))
  const pendingSick      = computed(() => sickLeaves.value.filter(s => s.status === 'cert_pending'))
  const pendingVacations = computed(() => vacationRequests.value.filter(v => v.status === 'pending'))

  // ── Shift swaps ──────────────────────────────────────────────────────────
  function approveSwap(id) {
    const s = shiftSwaps.value.find(x => x.id === id); if (s) s.status = 'approved'
  }
  function rejectSwap(id) {
    const s = shiftSwaps.value.find(x => x.id === id); if (s) s.status = 'rejected'
  }
  function addShiftSwap(req) {
    shiftSwaps.value.unshift({ ...req, id: Date.now(), status: 'pending', createdAt: new Date().toLocaleString('it-IT') })
  }

  // ── Sick leaves ──────────────────────────────────────────────────────────
  function addSickLeave(entry) {
    sickLeaves.value.unshift({ ...entry, id: Date.now(), status: 'cert_pending', hrSentAt: null })
  }
  function updateSickStatus(id, status) {
    const s = sickLeaves.value.find(x => x.id === id); if (s) s.status = status
  }
  function setSickProtocol(id, number) {
    const s = sickLeaves.value.find(x => x.id === id); if (s) s.protocolNumber = number
  }
  function sendSickToHR(id) {
    const s = sickLeaves.value.find(x => x.id === id)
    if (s) s.hrSentAt = new Date().toLocaleString('it-IT')
  }

  // ── Vacation requests ────────────────────────────────────────────────────
  function addVacationRequest(req) {
    vacationRequests.value.unshift({ ...req, id: Date.now(), status: 'pending', createdAt: new Date().toLocaleString('it-IT') })
  }
  function approveVacation(id) {
    const v = vacationRequests.value.find(x => x.id === id); if (v) v.status = 'approved'
  }
  function rejectVacation(id) {
    const v = vacationRequests.value.find(x => x.id === id); if (v) v.status = 'rejected'
  }

  // Returns true if the given date falls within an approved vacation for staffId
  function hasApprovedVacation(staffId, dateStr) {
    return vacationRequests.value.some(v =>
      v.employeeId === staffId && v.status === 'approved' &&
      dateStr >= v.startDate && dateStr <= v.endDate
    )
  }

  return {
    shiftSwaps, sickLeaves, vacationRequests,
    pendingSwaps, pendingSick, pendingVacations,
    approveSwap, rejectSwap, addShiftSwap,
    addSickLeave, updateSickStatus, setSickProtocol, sendSickToHR,
    addVacationRequest, approveVacation, rejectVacation, hasApprovedVacation,
  }
})
