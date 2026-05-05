<template>
  <div class="p-6 space-y-6">
    <!-- Tabs -->
    <div class="flex gap-1 bg-slate-100 dark:bg-slate-800 p-1 rounded-xl border border-slate-200 dark:border-slate-700 w-fit flex-wrap">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        @click="activeTab = tab.key"
        :class="['px-4 py-2 rounded-lg text-sm font-medium transition-all', activeTab === tab.key ? 'bg-blue-600 text-white' : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white']"
      >
        {{ tab.label }}
        <span v-if="tab.badge" class="ml-1.5 text-[10px] bg-red-500 text-white px-1.5 py-0.5 rounded-full">{{ tab.badge }}</span>
      </button>
    </div>

    <!-- ── CAMBI TURNO ─────────────────────────────────────────────── -->
    <div v-if="activeTab === 'swaps'" class="space-y-3">
      <div class="flex items-center justify-between">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white">Richieste Cambio Turno</h2>
        <select v-model="swapFilter" class="bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none">
          <option value="">Tutti gli stati</option>
          <option value="pending">In attesa</option>
          <option value="approved">Approvate</option>
          <option value="rejected">Rifiutate</option>
        </select>
      </div>

      <div class="space-y-3">
        <div
          v-for="req in filteredSwaps"
          :key="req.id"
          :class="[
            'bg-white dark:bg-slate-800 rounded-xl border p-4 transition-colors',
            req.status === 'pending' && swapChecks(req).blockingConflicts.length
              ? 'border-red-400/60 dark:border-red-500/50'
              : req.status === 'pending' && swapChecks(req).warnings.length
                ? 'border-orange-400/60 dark:border-orange-500/50'
                : 'border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600',
          ]"
        >
          <!-- Header row -->
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-full bg-blue-600 flex items-center justify-center text-sm font-bold text-white shrink-0">
                {{ initials(req.requester) }}
              </div>
              <div>
                <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ req.requester }}</p>
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ req.createdAt }}</p>
              </div>
            </div>
            <span :class="['text-xs font-medium px-2 py-0.5 rounded-full shrink-0', statusClass(req.status)]">{{ statusLabel(req.status) }}</span>
          </div>

          <!-- Data + shift pills + collega -->
          <div class="mt-3 grid grid-cols-3 gap-3 text-xs">
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-1">Data</p>
              <p class="font-semibold text-slate-900 dark:text-white font-mono">{{ req.date }}</p>
              <p class="text-slate-400 dark:text-slate-500 text-[10px] mt-0.5 capitalize">
                {{ new Date(req.date).toLocaleDateString('it-IT', { weekday: 'long' }) }}
              </p>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5 flex flex-col justify-center">
              <p class="text-slate-500 dark:text-slate-400 mb-1.5">Cambio</p>
              <div class="flex items-center gap-1.5">
                <span :class="['px-1.5 py-0.5 rounded text-[10px] font-bold', SHIFT_COLORS[req.fromShift] || 'bg-slate-200 text-slate-600']">
                  {{ SHIFT_TYPES[req.fromShift]?.abbr ?? req.fromShift }}
                </span>
                <ArrowRight :size="10" class="text-slate-400 shrink-0" />
                <span :class="['px-1.5 py-0.5 rounded text-[10px] font-bold', SHIFT_COLORS[req.toShift] || 'bg-slate-200 text-slate-600']">
                  {{ SHIFT_TYPES[req.toShift]?.abbr ?? req.toShift }}
                </span>
              </div>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Collega</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ req.colleague || '—' }}</p>
            </div>
          </div>

          <!-- Reason -->
          <div v-if="req.reason" class="mt-2 text-xs text-slate-500 dark:text-slate-400 italic">"{{ req.reason }}"</div>

          <!-- ── Verifica (solo per richieste in attesa) ── -->
          <div v-if="req.status === 'pending'" class="mt-3 space-y-2">
            <p class="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Verifica planning</p>

            <!-- Turno richiedente nel planning -->
            <div :class="['flex items-start gap-2 px-3 py-2 rounded-lg text-xs', swapChecks(req).requesterHasShift ? 'bg-emerald-500/10 border border-emerald-500/20' : 'bg-red-500/10 border border-red-500/20']">
              <component :is="swapChecks(req).requesterHasShift ? CheckCircle : XCircle" :size="13" :class="swapChecks(req).requesterHasShift ? 'text-emerald-500 shrink-0 mt-0.5' : 'text-red-500 shrink-0 mt-0.5'" />
              <div class="flex-1">
                <span :class="swapChecks(req).requesterHasShift ? 'text-emerald-700 dark:text-emerald-300' : 'text-red-700 dark:text-red-400'">
                  <strong>{{ req.requester }}</strong> –
                  <span v-if="swapChecks(req).requesterActualShifts.length">
                    turno attuale:
                    <span v-for="t in swapChecks(req).requesterActualShifts" :key="t" :class="['ml-1 px-1 py-0.5 rounded text-[10px] font-bold', SHIFT_COLORS[t]]">{{ SHIFT_TYPES[t]?.abbr }}</span>
                    <span v-if="!swapChecks(req).requesterHasShift" class="ml-1 font-normal">(dichiarato: {{ req.fromShift }}, non corrisponde)</span>
                  </span>
                  <span v-else class="text-red-700 dark:text-red-400"> nessun turno assegnato in quella data</span>
                </span>
              </div>
            </div>

            <!-- Turno collega nel planning -->
            <div v-if="req.colleagueId" :class="['flex items-start gap-2 px-3 py-2 rounded-lg text-xs', swapChecks(req).colleagueHasWorkingShift ? 'bg-emerald-500/10 border border-emerald-500/20' : 'bg-orange-500/10 border border-orange-500/20']">
              <component :is="swapChecks(req).colleagueHasWorkingShift ? CheckCircle : AlertTriangle" :size="13" :class="swapChecks(req).colleagueHasWorkingShift ? 'text-emerald-500 shrink-0 mt-0.5' : 'text-orange-500 shrink-0 mt-0.5'" />
              <div class="flex-1">
                <span :class="swapChecks(req).colleagueHasWorkingShift ? 'text-emerald-700 dark:text-emerald-300' : 'text-orange-700 dark:text-orange-400'">
                  <strong>{{ req.colleague }}</strong> –
                  <span v-if="swapChecks(req).colleagueActualShifts.length">
                    turno attuale:
                    <span v-for="t in swapChecks(req).colleagueActualShifts" :key="t" :class="['ml-1 px-1 py-0.5 rounded text-[10px] font-bold', SHIFT_COLORS[t]]">{{ SHIFT_TYPES[t]?.abbr }}</span>
                  </span>
                  <span v-else>nessun turno lavorativo in quella data</span>
                </span>
              </div>
            </div>

            <!-- Blocking conflicts (red) -->
            <div v-for="c in swapChecks(req).blockingConflicts" :key="c" class="flex items-center gap-2 px-3 py-2 bg-red-500/10 border border-red-500/20 rounded-lg text-xs text-red-700 dark:text-red-400">
              <XCircle :size="13" class="text-red-500 shrink-0" />
              {{ c }}
            </div>

            <!-- Warnings (orange) -->
            <div v-for="w in swapChecks(req).warnings" :key="w" class="flex items-center gap-2 px-3 py-2 bg-orange-500/10 border border-orange-500/20 rounded-lg text-xs text-orange-700 dark:text-orange-400">
              <AlertTriangle :size="13" class="text-orange-500 shrink-0" />
              {{ w }}
            </div>
          </div>

          <!-- Actions -->
          <div v-if="req.status === 'pending'" class="mt-3 flex flex-wrap gap-2">
            <button
              @click="reqStore.rejectSwap(req.id)"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-red-600 dark:text-red-400 border border-red-500/40 hover:bg-red-500/10 rounded-lg transition-colors"
            >
              <XCircle :size="13" /> Rifiuta
            </button>
            <button
              @click="reqStore.approveSwap(req.id)"
              :disabled="swapChecks(req).blockingConflicts.length > 0"
              :title="swapChecks(req).blockingConflicts.length ? 'Risolvi i conflitti prima di approvare' : 'Approva la richiesta senza modificare il planning'"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-slate-600 dark:text-slate-300 border border-slate-400/40 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <CheckCircle :size="13" /> Approva
            </button>
            <button
              @click="approveAndApply(req)"
              :disabled="swapChecks(req).blockingConflicts.length > 0"
              :title="swapChecks(req).blockingConflicts.length ? 'Risolvi i conflitti prima di approvare' : 'Approva e aggiorna il planning automaticamente'"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-emerald-600 dark:text-emerald-400 border border-emerald-500/40 hover:bg-emerald-500/10 rounded-lg transition-colors disabled:opacity-40 disabled:cursor-not-allowed font-medium"
            >
              <CheckCircle :size="13" /> Approva e applica al planning
            </button>
          </div>
        </div>

        <p v-if="filteredSwaps.length === 0" class="text-slate-400 dark:text-slate-500 text-sm text-center py-8">Nessuna richiesta trovata</p>
      </div>
    </div>

    <!-- ── MALATTIE ─────────────────────────────────────────────────── -->
    <div v-if="activeTab === 'sick'" class="space-y-3">
      <div class="flex items-center justify-between">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white">Malattie / Infortuni</h2>
        <select v-model="sickFilter" class="bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none">
          <option value="">Tutti</option>
          <option value="cert_pending">Certificato atteso</option>
          <option value="cert_received">Certificato ricevuto</option>
        </select>
      </div>

      <div class="space-y-3">
        <div v-for="sick in filteredSick" :key="sick.id" class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4">
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-center gap-3">
              <div :class="['w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold text-white shrink-0', sick.type === 'INF' ? 'bg-rose-700' : 'bg-red-600']">
                {{ initials(sick.employee) }}
              </div>
              <div>
                <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ sick.employee }}</p>
                <div class="flex items-center gap-1 mt-0.5">
                  <component :is="sick.type === 'INF' ? Bandage : Thermometer" :size="12" :class="sick.type === 'INF' ? 'text-rose-400' : 'text-red-400'" />
                  <p class="text-xs text-slate-500 dark:text-slate-400">{{ sick.type === 'INF' ? 'Infortunio' : 'Malattia' }}</p>
                </div>
              </div>
            </div>
            <div class="flex flex-col items-end gap-1 shrink-0">
              <span :class="['text-xs font-medium px-2 py-0.5 rounded-full', sick.status === 'cert_pending' ? 'bg-orange-500/20 text-orange-600 dark:text-orange-300' : 'bg-blue-500/20 text-blue-600 dark:text-blue-300']">
                {{ sick.status === 'cert_pending' ? 'Cert. atteso' : 'Cert. ricevuto' }}
              </span>
              <span v-if="sick.hrSentAt" class="text-[10px] text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                <Send :size="10" /> Inviato a HR {{ sick.hrSentAt }}
              </span>
            </div>
          </div>

          <div class="mt-3 grid grid-cols-2 gap-3 text-xs">
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Inizio</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ sick.startDate }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Fine presunta</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ sick.endDate || '—' }}</p>
            </div>
          </div>

          <div v-if="sick.note" class="mt-2 text-xs text-slate-500 dark:text-slate-400 italic">"{{ sick.note }}"</div>

          <div class="mt-3 flex items-center gap-2">
            <label class="text-xs text-slate-500 dark:text-slate-400 shrink-0">N° protocollo:</label>
            <input
              :value="sick.protocolNumber || ''"
              @change="reqStore.setSickProtocol(sick.id, $event.target.value || null)"
              placeholder="es. MAL-2026-0042"
              class="flex-1 bg-slate-50 dark:bg-slate-700/60 border border-slate-200 dark:border-slate-600 text-slate-900 dark:text-white text-xs px-2 py-1 rounded-lg focus:outline-none focus:border-blue-500"
            />
          </div>

          <div class="mt-3 flex flex-wrap gap-2">
            <button
              v-if="sick.status === 'cert_pending'"
              @click="reqStore.updateSickStatus(sick.id, 'cert_received')"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-blue-600 dark:text-blue-400 border border-blue-500/40 hover:bg-blue-500/10 rounded-lg transition-colors"
            >
              <CheckCircle :size="13" /> Segna certificato ricevuto
            </button>
            <button
              v-if="!sick.hrSentAt"
              @click="reqStore.sendSickToHR(sick.id)"
              :disabled="!sick.protocolNumber"
              :title="!sick.protocolNumber ? 'Inserisci il numero di protocollo prima' : 'Invia comunicazione a HR'"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-emerald-600 dark:text-emerald-400 border border-emerald-500/40 hover:bg-emerald-500/10 rounded-lg transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <Send :size="13" /> Invia a HR
            </button>
          </div>
        </div>

        <p v-if="filteredSick.length === 0" class="text-slate-400 dark:text-slate-500 text-sm text-center py-8">Nessuna assenza trovata</p>
      </div>
    </div>

    <!-- ── FERIE ─────────────────────────────────────────────────────── -->
    <div v-if="activeTab === 'vacations'" class="space-y-3">
      <div class="flex items-center justify-between">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white">Richieste Ferie</h2>
        <select v-model="vacFilter" class="bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none">
          <option value="">Tutti gli stati</option>
          <option value="pending">In attesa</option>
          <option value="approved">Approvate</option>
          <option value="rejected">Rifiutate</option>
        </select>
      </div>

      <div class="space-y-3">
        <div v-for="vac in filteredVacations" :key="vac.id" class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4">
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-full bg-emerald-600 flex items-center justify-center text-sm font-bold text-white shrink-0">
                {{ initials(vac.employee) }}
              </div>
              <div>
                <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ vac.employee }}</p>
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ vac.createdAt }}</p>
              </div>
            </div>
            <span :class="['text-xs font-medium px-2 py-0.5 rounded-full shrink-0', statusClass(vac.status)]">{{ statusLabel(vac.status) }}</span>
          </div>

          <div class="mt-3 grid grid-cols-3 gap-3 text-xs">
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Dal</p>
              <p class="font-medium text-slate-900 dark:text-white font-mono">{{ vac.startDate }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Al</p>
              <p class="font-medium text-slate-900 dark:text-white font-mono">{{ vac.endDate }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Giorni</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ daysBetween(vac.startDate, vac.endDate) }}gg</p>
            </div>
          </div>

          <!-- Verifica copertura per richieste in attesa -->
          <div v-if="vac.status === 'pending'" class="mt-3 space-y-1.5">
            <p class="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Verifica copertura</p>
            <div v-for="check in vacChecks(vac)" :key="check.msg" :class="['flex items-center gap-2 px-3 py-2 rounded-lg text-xs', check.ok ? 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-700 dark:text-emerald-400' : 'bg-orange-500/10 border border-orange-500/20 text-orange-700 dark:text-orange-400']">
              <component :is="check.ok ? CheckCircle : AlertTriangle" :size="12" class="shrink-0" />
              {{ check.msg }}
            </div>
          </div>

          <div v-if="vac.reason" class="mt-2 text-xs text-slate-500 dark:text-slate-400 italic">"{{ vac.reason }}"</div>

          <div v-if="vac.status === 'pending'" class="mt-3 flex gap-2">
            <button @click="reqStore.rejectVacation(vac.id)" class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-red-600 dark:text-red-400 border border-red-500/40 hover:bg-red-500/10 rounded-lg transition-colors">
              <XCircle :size="13" /> Rifiuta
            </button>
            <button @click="reqStore.approveVacation(vac.id)" class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-emerald-600 dark:text-emerald-400 border border-emerald-500/40 hover:bg-emerald-500/10 rounded-lg transition-colors font-medium">
              <CheckCircle :size="13" /> Approva
            </button>
          </div>
        </div>

        <p v-if="filteredVacations.length === 0" class="text-slate-400 dark:text-slate-500 text-sm text-center py-8">Nessuna richiesta ferie trovata</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { AlertTriangle, ArrowRight, Bandage, CheckCircle, Send, Thermometer, XCircle } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRequestStore } from '../stores/useRequestStore'
import { SHIFT_COLORS, SHIFT_TYPES, useShiftStore } from '../stores/useShiftStore'

const reqStore   = useRequestStore()
const shiftStore = useShiftStore()

const activeTab  = ref('swaps')
const swapFilter = ref('')
const sickFilter = ref('')
const vacFilter  = ref('')

const tabs = computed(() => [
  { key: 'swaps',     label: 'Cambi Turno',          badge: reqStore.pendingSwaps.length     || null },
  { key: 'sick',      label: 'Malattie / Infortuni', badge: reqStore.pendingSick.length      || null },
  { key: 'vacations', label: 'Ferie',                badge: reqStore.pendingVacations.length || null },
])

const filteredSwaps     = computed(() => swapFilter.value ? reqStore.shiftSwaps.filter(r => r.status === swapFilter.value) : reqStore.shiftSwaps)
const filteredSick      = computed(() => sickFilter.value ? reqStore.sickLeaves.filter(r => r.status === sickFilter.value) : reqStore.sickLeaves)
const filteredVacations = computed(() => vacFilter.value  ? reqStore.vacationRequests.filter(r => r.status === vacFilter.value) : reqStore.vacationRequests)

const WORKING = ['M', 'P', 'N', 'L', 'S6', 'F']

// ── Swap conflict checks ──────────────────────────────────────────────────
function swapChecks(req) {
  const dateObj = new Date(req.date)

  // Richiedente
  const requesterActualShifts       = shiftStore.getShifts(req.requesterId, dateObj)
  const requesterWorkingShifts      = requesterActualShifts.filter(t => WORKING.includes(t))
  const requesterHasShift           = requesterWorkingShifts.includes(req.fromShift)

  // Collega
  const colleagueActualShifts       = req.colleagueId ? shiftStore.getShifts(req.colleagueId, dateObj) : []
  const colleagueWorkingShifts      = colleagueActualShifts.filter(t => WORKING.includes(t))
  const colleagueHasWorkingShift    = colleagueWorkingShifts.length > 0

  // Ferie approvate
  const requesterOnVacation  = reqStore.hasApprovedVacation(req.requesterId, req.date)
  const colleagueOnVacation  = req.colleagueId ? reqStore.hasApprovedVacation(req.colleagueId, req.date) : false

  // Altra richiesta pendente stessa data (stesso richiedente, diverso id)
  const duplicatePending = reqStore.shiftSwaps.some(s =>
    s.id !== req.id &&
    s.requesterId === req.requesterId &&
    s.date === req.date &&
    s.status === 'pending'
  )

  // Blocking conflicts (impediscono approvazione)
  const blockingConflicts = []
  if (!requesterHasShift)
    blockingConflicts.push(`Il turno dichiarato (${req.fromShift}) non corrisponde a quello presente nel planning`)
  if (requesterOnVacation)
    blockingConflicts.push(`${req.requester} ha ferie approvate in questa data`)
  if (duplicatePending)
    blockingConflicts.push('Esiste già un\'altra richiesta di cambio in attesa per lo stesso richiedente e data')

  // Warnings (non bloccanti)
  const warnings = []
  if (req.colleagueId && !colleagueHasWorkingShift)
    warnings.push(`${req.colleague} non ha un turno lavorativo cambiabile in questa data`)
  if (colleagueOnVacation)
    warnings.push(`${req.colleague} ha ferie approvate in questa data`)

  return {
    requesterActualShifts,
    requesterHasShift,
    colleagueActualShifts,
    colleagueHasWorkingShift,
    blockingConflicts,
    warnings,
  }
}

// Approva + applica lo scambio direttamente nel planning
function approveAndApply(req) {
  const checks = swapChecks(req)
  if (checks.blockingConflicts.length) return

  // Sostituisce il fromShift del richiedente con toShift
  shiftStore.removeShiftItem(req.requesterId, new Date(req.date), req.fromShift)
  shiftStore.addShift(req.requesterId, new Date(req.date), req.toShift)

  // Se c'è un collega, gli assegna il fromShift al posto del suo turno attuale
  if (req.colleagueId) {
    const colleagueShifts = shiftStore.getShifts(req.colleagueId, new Date(req.date))
    const colleagueWorking = colleagueShifts.filter(t => WORKING.includes(t))
    if (colleagueWorking.length) {
      shiftStore.removeShiftItem(req.colleagueId, new Date(req.date), colleagueWorking[0])
    }
    shiftStore.addShift(req.colleagueId, new Date(req.date), req.fromShift)
  }

  reqStore.approveSwap(req.id)
}

// ── Vacation coverage checks ──────────────────────────────────────────────
function vacChecks(vac) {
  const results = []
  const start = new Date(vac.startDate)
  const end   = new Date(vac.endDate)

  // Conta quanti altri dipendenti hanno già ferie approvate che si sovrappongono
  const overlapping = reqStore.vacationRequests.filter(v =>
    v.id !== vac.id &&
    v.employeeId !== vac.employeeId &&
    v.status === 'approved' &&
    v.startDate <= vac.endDate &&
    v.endDate >= vac.startDate
  )
  if (overlapping.length) {
    results.push({ ok: false, msg: `${overlapping.length} altro/i dipendente/i in ferie nello stesso periodo (${overlapping.map(v => v.employee.split(' ')[0]).join(', ')})` })
  } else {
    results.push({ ok: true, msg: 'Nessun altro dipendente in ferie nello stesso periodo' })
  }

  // Verifica se l'utente ha turni lavorativi assegnati nel periodo
  let assignedShiftCount = 0
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const shifts = shiftStore.getShifts(vac.employeeId, new Date(d))
    if (shifts.some(t => WORKING.includes(t))) assignedShiftCount++
  }
  if (assignedShiftCount > 0) {
    results.push({ ok: false, msg: `${assignedShiftCount} giorn${assignedShiftCount === 1 ? 'o' : 'i'} con turni lavorativi già assegnati nel periodo — da aggiornare nel planning` })
  } else {
    results.push({ ok: true, msg: 'Nessun turno lavorativo già assegnato nel periodo richiesto' })
  }

  return results
}

function initials(name) {
  return name?.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || '?'
}
function daysBetween(d1, d2) {
  if (!d1 || !d2) return 1
  return Math.max(1, Math.round((new Date(d2) - new Date(d1)) / 86400000) + 1)
}
function statusLabel(s) {
  return { pending: 'In attesa', approved: 'Approvato', rejected: 'Rifiutato' }[s] || s
}
function statusClass(s) {
  return {
    pending:  'bg-yellow-500/20 text-yellow-700 dark:text-yellow-300',
    approved: 'bg-emerald-500/20 text-emerald-700 dark:text-emerald-300',
    rejected: 'bg-red-500/20 text-red-700 dark:text-red-300',
  }[s] || 'bg-slate-200 dark:bg-slate-600 text-slate-700 dark:text-slate-300'
}
</script>
