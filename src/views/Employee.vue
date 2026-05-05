<template>
  <div class="max-w-2xl mx-auto p-4 space-y-5">
    <!-- Today card -->
    <div
      class="bg-gradient-to-br from-blue-600 to-indigo-700 rounded-2xl p-6 text-white shadow-xl"
    >
      <p class="text-blue-100 text-sm">Turno di Oggi – {{ todayLabel }}</p>
      <template v-if="todayShifts.length">
        <h2 class="text-3xl font-bold mt-1">
          {{ SHIFT_TYPES[todayShifts[0]].hours }}
        </h2>
        <div class="mt-3 flex flex-wrap items-center gap-2">
          <span
            v-for="t in todayShifts"
            :key="t"
            class="bg-blue-500/50 px-3 py-1 rounded-full text-sm font-medium"
          >
            {{ SHIFT_TYPES[t].label }}
          </span>
          <span class="bg-white/20 px-3 py-1 rounded-full text-sm">
            {{ todayShifts.reduce((s, t) => s + SHIFT_TYPES[t].duration, 0) }}h
            – Full-time 40h/sett
          </span>
        </div>
      </template>
      <template v-else>
        <h2 class="text-2xl font-bold mt-1 text-blue-200">Riposo</h2>
        <p class="text-blue-300 text-sm mt-1">Nessun turno assegnato oggi</p>
      </template>
    </div>

    <!-- Alert: shift assigned during approved vacation -->
    <div
      v-if="vacationConflictToday"
      class="flex items-start gap-3 p-4 bg-orange-500/10 border border-orange-500/40 rounded-xl"
    >
      <AlertTriangle :size="18" class="text-orange-500 shrink-0 mt-0.5" />
      <div>
        <p class="text-sm font-semibold text-orange-700 dark:text-orange-300">
          Attenzione: turno assegnato durante ferie approvate
        </p>
        <p class="text-xs text-orange-600 dark:text-orange-400 mt-0.5">
          Hai un turno assegnato oggi ma le tue ferie sono già state approvate.
          Contatta il responsabile.
        </p>
      </div>
    </div>

    <!-- Action buttons -->
    <div class="grid grid-cols-3 gap-3">
      <button
        @click="swapModal = true"
        class="flex flex-col items-center gap-2 p-4 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 hover:border-blue-400 dark:hover:border-blue-500 hover:bg-blue-50 dark:hover:bg-slate-700 transition-all"
      >
        <ArrowLeftRight :size="24" class="text-blue-500 dark:text-blue-400" />
        <span class="text-xs font-semibold text-slate-900 dark:text-white"
          >Cambio Turno</span
        >
        <span
          class="text-[10px] text-slate-500 dark:text-slate-400 text-center leading-tight"
          >Richiedi scambio</span
        >
      </button>
      <button
        @click="vacModal = true"
        class="flex flex-col items-center gap-2 p-4 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 hover:border-emerald-400 dark:hover:border-emerald-500 hover:bg-emerald-50 dark:hover:bg-slate-700 transition-all"
      >
        <Umbrella :size="24" class="text-emerald-500 dark:text-emerald-400" />
        <span
          class="text-xs font-semibold text-emerald-700 dark:text-emerald-400"
          >Richiedi Ferie</span
        >
        <span
          class="text-[10px] text-slate-500 dark:text-slate-400 text-center leading-tight"
          >Inserisci periodo</span
        >
      </button>
      <button
        @click="sickModal = true"
        class="flex flex-col items-center gap-2 p-4 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 hover:border-red-400 dark:hover:border-red-500 hover:bg-red-50 dark:hover:bg-slate-700 transition-all"
      >
        <Thermometer :size="24" class="text-red-500 dark:text-red-400" />
        <span class="text-xs font-semibold text-red-600 dark:text-red-400"
          >Segnala Malattia</span
        >
        <span
          class="text-[10px] text-slate-500 dark:text-slate-400 text-center leading-tight"
          >Comunica assenza</span
        >
      </button>
    </div>

    <!-- Week calendar -->
    <div
      class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4"
    >
      <div class="flex items-center justify-between mb-3">
        <h3 class="text-sm font-semibold text-slate-900 dark:text-white">
          Settimana corrente
        </h3>
        <div class="flex gap-1">
          <button
            @click="prevWeek"
            class="px-2 py-1 text-xs bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600 rounded text-slate-700 dark:text-white transition-colors flex items-center"
          >
            <ChevronLeft :size="14" />
          </button>
          <button
            @click="nextWeek"
            class="px-2 py-1 text-xs bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600 rounded text-slate-700 dark:text-white transition-colors flex items-center"
          >
            <ChevronRight :size="14" />
          </button>
        </div>
      </div>
      <div class="grid grid-cols-7 gap-1">
        <div v-for="day in weekDays" :key="day.iso" class="text-center">
          <div
            class="text-[10px] text-slate-400 dark:text-slate-500 capitalize mb-1"
          >
            {{ day.weekday }}
          </div>
          <div
            :class="[
              'rounded-lg py-2 px-1 relative',
              isToday(day.date) ? 'ring-2 ring-blue-500' : '',
              day.shifts.length
                ? SHIFT_COLORS[day.shifts[0]]
                : day.hasVacation
                  ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30'
                  : 'bg-slate-100 dark:bg-slate-700/40 text-slate-400 dark:text-slate-500',
            ]"
          >
            <div class="text-[10px] font-bold">{{ day.dayNum }}</div>
            <div
              v-if="day.shifts.length"
              class="text-[9px] mt-0.5 leading-tight"
            >
              {{ day.shifts.map((t) => SHIFT_TYPES[t].abbr).join("+") }}
            </div>
            <div
              v-else-if="day.hasVacation"
              class="text-[9px] mt-0.5 leading-tight"
            >
              FER
            </div>
            <div v-else class="text-[9px] mt-0.5">—</div>
            <!-- Conflict badge -->
            <div
              v-if="day.hasVacation && day.shifts.length"
              class="absolute -top-1 -right-1 w-3 h-3 bg-orange-500 rounded-full flex items-center justify-center"
              title="Turno assegnato durante ferie approvate"
            >
              <AlertTriangle :size="8" class="text-white" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Hours balance -->
    <div
      class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4"
    >
      <h3 class="text-sm font-semibold text-slate-900 dark:text-white mb-3">
        Riepilogo Ore – Aprile 2026
      </h3>
      <div class="space-y-2">
        <div
          class="flex justify-between items-center p-3 bg-slate-50 dark:bg-slate-700/50 rounded-lg"
        >
          <span class="text-sm text-slate-600 dark:text-slate-300"
            >Ore lavorate</span
          >
          <span class="font-mono font-bold text-slate-900 dark:text-white"
            >{{ monthlyWorked }} / {{ target }}h</span
          >
        </div>
        <div
          class="flex justify-between items-center p-3 bg-slate-50 dark:bg-slate-700/50 rounded-lg"
        >
          <span class="text-sm text-slate-600 dark:text-slate-300"
            >Rimanenti al target</span
          >
          <span
            class="font-mono font-bold"
            :class="
              target - monthlyWorked > 0
                ? 'text-yellow-600 dark:text-yellow-400'
                : 'text-emerald-600 dark:text-emerald-400'
            "
          >
            {{ Math.max(0, target - monthlyWorked) }}h
          </span>
        </div>
        <div
          class="flex justify-between items-center p-3 bg-slate-50 dark:bg-slate-700/50 rounded-lg"
        >
          <span class="text-sm text-slate-600 dark:text-slate-300"
            >Straordinari</span
          >
          <span class="font-mono font-bold text-orange-600 dark:text-orange-400"
            >+{{ Math.max(0, monthlyWorked - target) }}h</span
          >
        </div>
      </div>
      <div class="mt-3">
        <div
          class="flex justify-between text-[10px] text-slate-500 dark:text-slate-400 mb-1"
        >
          <span>Avanzamento mensile</span>
          <span
            >{{
              Math.min(100, Math.round((monthlyWorked / target) * 100))
            }}%</span
          >
        </div>
        <div
          class="h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden"
        >
          <div
            class="h-full bg-blue-500 rounded-full transition-all"
            :style="{
              width: Math.min(100, (monthlyWorked / target) * 100) + '%',
            }"
          ></div>
        </div>
      </div>
    </div>

    <!-- My requests -->
    <div
      class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4"
    >
      <h3 class="text-sm font-semibold text-slate-900 dark:text-white mb-3">
        Le mie richieste
      </h3>
      <div class="space-y-2">
        <div
          v-for="req in myRequests"
          :key="req.id"
          class="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-700/40 rounded-lg"
        >
          <div class="flex items-center gap-2">
            <component :is="req.icon" :size="14" :class="req.iconColor" />
            <div>
              <p class="text-xs font-medium text-slate-900 dark:text-white">
                {{ req.label }}
              </p>
              <p class="text-[10px] text-slate-500 dark:text-slate-400">
                {{ req.sub }}
              </p>
            </div>
          </div>
          <span
            :class="[
              'text-[10px] font-medium px-2 py-0.5 rounded-full',
              statusClass(req.status),
            ]"
            >{{ statusLabel(req.status) }}</span
          >
        </div>
        <p
          v-if="myRequests.length === 0"
          class="text-xs text-slate-400 dark:text-slate-500 text-center py-2"
        >
          Nessuna richiesta recente
        </p>
      </div>
    </div>

    <!-- ########## VACATION MODAL ########## -->
    <div
      v-if="vacModal"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
      @click.self="vacModal = false"
    >
      <div
        class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 w-full max-w-md shadow-2xl"
      >
        <div
          class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between"
        >
          <div class="flex items-center gap-2 text-slate-900 dark:text-white">
            <Umbrella :size="18" class="text-emerald-500" />
            <h3 class="text-sm font-semibold">Richiesta Ferie</h3>
          </div>
          <button
            @click="vacModal = false"
            class="text-slate-400 hover:text-slate-700 dark:hover:text-white"
          >
            <X :size="20" />
          </button>
        </div>
        <div class="px-6 py-4 space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label
                class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
                >Data inizio</label
              >
              <input
                type="date"
                v-model="vacForm.startDate"
                class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-emerald-500"
              />
            </div>
            <div>
              <label
                class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
                >Data fine</label
              >
              <input
                type="date"
                v-model="vacForm.endDate"
                :min="vacForm.startDate"
                class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>
          <div
            v-if="vacForm.startDate && vacForm.endDate"
            class="flex items-center gap-2 p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-lg"
          >
            <Umbrella :size="15" class="text-emerald-500 shrink-0" />
            <p
              class="text-xs text-emerald-700 dark:text-emerald-300 font-medium"
            >
              {{ vacDays }} giorn{{ vacDays === 1 ? "o" : "i" }} di ferie
              richiesti
            </p>
          </div>
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
              >Motivazione (opzionale)</label
            >
            <textarea
              v-model="vacForm.reason"
              rows="2"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-emerald-500 resize-none"
              placeholder="Es. Vacanze estive, impegno personale..."
            ></textarea>
          </div>
        </div>
        <div
          class="px-6 py-4 border-t border-slate-100 dark:border-slate-700 flex gap-2 justify-end"
        >
          <button
            @click="vacModal = false"
            class="px-4 py-2 text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors"
          >
            Annulla
          </button>
          <button
            @click="submitVacation"
            :disabled="!vacForm.startDate || !vacForm.endDate"
            class="px-4 py-2 text-sm bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 text-white rounded-lg font-medium transition-colors"
          >
            Invia richiesta
          </button>
        </div>
      </div>
    </div>

    <!-- ########## SICK MODAL ########## -->
    <div
      v-if="sickModal"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
      @click.self="sickModal = false"
    >
      <div
        class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 w-full max-w-md shadow-2xl"
      >
        <div
          class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between"
        >
          <div class="flex items-center gap-2 text-slate-900 dark:text-white">
            <Thermometer :size="18" class="text-red-500 dark:text-red-400" />
            <h3 class="text-sm font-semibold">
              Comunicazione Malattia / Infortunio
            </h3>
          </div>
          <button
            @click="sickModal = false"
            class="text-slate-400 hover:text-slate-700 dark:hover:text-white"
          >
            <X :size="20" />
          </button>
        </div>
        <div class="px-6 py-4 space-y-4">
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
              >Tipo assenza</label
            >
            <select
              v-model="sickForm.type"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-red-500"
            >
              <option value="MAL">Malattia</option>
              <option value="INF">Infortunio sul lavoro</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label
                class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
                >Data inizio</label
              >
              <input
                type="date"
                v-model="sickForm.startDate"
                class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-red-500"
              />
            </div>
            <div>
              <label
                class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
                >Data fine presunta</label
              >
              <input
                type="date"
                v-model="sickForm.endDate"
                class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-red-500"
              />
            </div>
          </div>
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
              >N° Protocollo malattia</label
            >
            <input
              type="text"
              v-model="sickForm.protocolNumber"
              placeholder="es. MAL-2026-0042  (inserisci se già disponibile)"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-red-500 font-mono"
            />
          </div>
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
              >Note (opzionale)</label
            >
            <textarea
              v-model="sickForm.note"
              rows="2"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-red-500 resize-none"
              placeholder="Descrizione..."
            ></textarea>
          </div>
          <div
            class="flex items-center gap-2 p-3 bg-orange-500/10 border border-orange-500/30 rounded-lg"
          >
            <AlertTriangle
              :size="16"
              class="text-orange-500 dark:text-orange-400 shrink-0"
            />
            <p class="text-xs text-orange-700 dark:text-orange-300">
              Il certificato medico e il numero di protocollo dovranno essere
              inviati entro 48h all'HR.
            </p>
          </div>
        </div>
        <div
          class="px-6 py-4 border-t border-slate-100 dark:border-slate-700 flex gap-2 justify-end"
        >
          <button
            @click="sickModal = false"
            class="px-4 py-2 text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors"
          >
            Annulla
          </button>
          <button
            @click="submitSick"
            class="px-4 py-2 text-sm bg-red-600 hover:bg-red-500 text-white rounded-lg font-medium transition-colors"
          >
            Invia comunicazione
          </button>
        </div>
      </div>
    </div>

    <!-- ########## SWAP MODAL ########## -->
    <div
      v-if="swapModal"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
      @click.self="swapModal = false"
    >
      <div
        class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 w-full max-w-md shadow-2xl"
      >
        <div
          class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between"
        >
          <div class="flex items-center gap-2 text-slate-900 dark:text-white">
            <ArrowLeftRight
              :size="18"
              class="text-blue-500 dark:text-blue-400"
            />
            <h3 class="text-sm font-semibold">Richiesta Cambio Turno</h3>
          </div>
          <button
            @click="swapModal = false"
            class="text-slate-400 hover:text-slate-700 dark:hover:text-white"
          >
            <X :size="20" />
          </button>
        </div>
        <div class="px-6 py-4 space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label
                class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
                >Data turno da cambiare</label
              >
              <input
                type="date"
                v-model="swapForm.date"
                class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-blue-500"
              />
            </div>
            <div>
              <label
                class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
                >Turno attuale</label
              >
              <select
                v-model="swapForm.fromShift"
                class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-blue-500"
              >
                <option v-for="(t, k) in SHIFT_TYPES" :key="k" :value="k">
                  {{ t.abbr }} – {{ t.label }}
                </option>
              </select>
            </div>
          </div>
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
              >Turno proposto</label
            >
            <select
              v-model="swapForm.toShift"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-blue-500"
            >
              <option v-for="(t, k) in SHIFT_TYPES" :key="k" :value="k">
                {{ t.abbr }} – {{ t.label }}
              </option>
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
              >Collega coinvolto (se scambio)</label
            >
            <select
              v-model="swapForm.colleague"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-blue-500"
            >
              <option value="">Nessuno (spostamento singolo)</option>
              <option v-for="s in colleagues" :key="s.id" :value="s.id">
                {{ s.name }}
              </option>
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1"
              >Motivazione (opzionale)</label
            >
            <textarea
              v-model="swapForm.reason"
              rows="2"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-blue-500 resize-none"
              placeholder="Motivo della richiesta..."
            ></textarea>
          </div>
        </div>
        <div
          class="px-6 py-4 border-t border-slate-100 dark:border-slate-700 flex gap-2 justify-end"
        >
          <button
            @click="swapModal = false"
            class="px-4 py-2 text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors"
          >
            Annulla
          </button>
          <button
            @click="submitSwap"
            class="px-4 py-2 text-sm bg-blue-600 hover:bg-blue-500 text-white rounded-lg font-medium transition-colors"
          >
            Invia richiesta
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  AlertTriangle,
  ArrowLeftRight,
  ChevronLeft,
  ChevronRight,
  Thermometer,
  Umbrella,
  X,
} from "lucide-vue-next";
import { computed, reactive, ref } from "vue";
import { useRequestStore } from "../stores/useRequestStore";
import {
  SHIFT_COLORS,
  SHIFT_TYPES,
  useShiftStore,
} from "../stores/useShiftStore";

const shiftStore = useShiftStore();
const reqStore = useRequestStore();

const CURRENT_STAFF_ID = 1;
const currentStaff = computed(() =>
  shiftStore.staff.find((s) => s.id === CURRENT_STAFF_ID),
);
const colleagues = computed(() =>
  shiftStore.staff.filter((s) => s.id !== CURRENT_STAFF_ID),
);

const today = new Date();
today.setHours(0, 0, 0, 0);
const todayLabel = today.toLocaleDateString("it-IT", {
  weekday: "long",
  day: "numeric",
  month: "long",
});
const todayIso = today.toISOString().slice(0, 10);
const todayShifts = computed(() =>
  shiftStore.getShifts(CURRENT_STAFF_ID, today),
);

const vacationConflictToday = computed(
  () =>
    todayShifts.value.length > 0 &&
    reqStore.hasApprovedVacation(CURRENT_STAFF_ID, todayIso),
);

const currentWeekStart = ref(new Date("2026-04-27"));
function prevWeek() {
  currentWeekStart.value = new Date(
    currentWeekStart.value.getTime() - 7 * 86400000,
  );
}
function nextWeek() {
  currentWeekStart.value = new Date(
    currentWeekStart.value.getTime() + 7 * 86400000,
  );
}

const weekDays = computed(() =>
  Array.from({ length: 7 }, (_, i) => {
    const d = new Date(currentWeekStart.value);
    d.setDate(d.getDate() + i);
    const iso = d.toISOString().slice(0, 10);
    return {
      date: new Date(d),
      iso,
      weekday: d.toLocaleDateString("it-IT", { weekday: "short" }),
      dayNum: d.getDate(),
      shifts: shiftStore.getShifts(CURRENT_STAFF_ID, d),
      hasVacation: reqStore.hasApprovedVacation(CURRENT_STAFF_ID, iso),
    };
  }),
);

function isToday(d) {
  return d.toDateString() === today.toDateString();
}

const target = computed(() => currentStaff.value?.monthlyTarget || 160);
const monthlyWorked = computed(() =>
  shiftStore.monthlyHours(CURRENT_STAFF_ID, 2026, 4),
);

// ########## Modals ##########
const sickModal = ref(false);
const swapModal = ref(false);
const vacModal = ref(false);

const sickForm = reactive({
  type: "MAL",
  startDate: "",
  endDate: "",
  note: "",
  protocolNumber: "",
});
const swapForm = reactive({
  date: "",
  fromShift: "M",
  toShift: "P",
  colleague: "",
  reason: "",
});
const vacForm = reactive({ startDate: "", endDate: "", reason: "" });

const vacDays = computed(() => {
  if (!vacForm.startDate || !vacForm.endDate) return 0;
  return Math.max(
    1,
    Math.round(
      (new Date(vacForm.endDate) - new Date(vacForm.startDate)) / 86400000,
    ) + 1,
  );
});

function submitSick() {
  reqStore.addSickLeave({
    employee: "Marco Rossi",
    employeeId: CURRENT_STAFF_ID,
    ...sickForm,
    protocolNumber: sickForm.protocolNumber || null,
  });
  sickModal.value = false;
  Object.assign(sickForm, {
    type: "MAL",
    startDate: "",
    endDate: "",
    note: "",
    protocolNumber: "",
  });
}

function submitSwap() {
  const col = shiftStore.staff.find((s) => s.id === Number(swapForm.colleague));
  reqStore.addShiftSwap({
    requester: "Marco Rossi",
    requesterId: CURRENT_STAFF_ID,
    colleague: col?.name || null,
    colleagueId: col?.id || null,
    ...swapForm,
  });
  swapModal.value = false;
  Object.assign(swapForm, {
    date: "",
    fromShift: "M",
    toShift: "P",
    colleague: "",
    reason: "",
  });
}

function submitVacation() {
  reqStore.addVacationRequest({
    employee: "Marco Rossi",
    employeeId: CURRENT_STAFF_ID,
    ...vacForm,
  });
  vacModal.value = false;
  Object.assign(vacForm, { startDate: "", endDate: "", reason: "" });
}

// ########## My requests ##################################################
const myRequests = computed(() => [
  ...reqStore.shiftSwaps
    .filter((r) => r.requesterId === CURRENT_STAFF_ID)
    .map((r) => ({
      id: "swap_" + r.id,
      icon: ArrowLeftRight,
      iconColor: "text-blue-400",
      label: `Cambio turno – ${r.date}`,
      sub: `${r.fromShift} → ${r.toShift}`,
      status: r.status,
    })),
  ...reqStore.sickLeaves
    .filter((r) => r.employeeId === CURRENT_STAFF_ID)
    .map((r) => ({
      id: "sick_" + r.id,
      icon: Thermometer,
      iconColor: "text-red-400",
      label: `Malattia – ${r.startDate}`,
      sub: r.note || "Assenza per malattia",
      status: r.status,
    })),
  ...reqStore.vacationRequests
    .filter((r) => r.employeeId === CURRENT_STAFF_ID)
    .map((r) => ({
      id: "vac_" + r.id,
      icon: Umbrella,
      iconColor: "text-emerald-400",
      label: `Ferie – ${r.startDate} / ${r.endDate}`,
      sub: r.reason || `${vacDays.value}gg richiesti`,
      status: r.status,
    })),
]);

function statusLabel(s) {
  return (
    {
      pending: "In attesa",
      approved: "Approvato",
      rejected: "Rifiutato",
      cert_pending: "Cert. atteso",
      cert_received: "Cert. ricevuto",
    }[s] || s
  );
}
function statusClass(s) {
  return (
    {
      pending: "bg-yellow-500/20 text-yellow-700 dark:text-yellow-300",
      approved: "bg-emerald-500/20 text-emerald-700 dark:text-emerald-300",
      rejected: "bg-red-500/20 text-red-700 dark:text-red-300",
      cert_pending: "bg-orange-500/20 text-orange-700 dark:text-orange-300",
      cert_received: "bg-blue-500/20 text-blue-700 dark:text-blue-300",
    }[s] || "bg-slate-200 dark:bg-slate-600 text-slate-700 dark:text-slate-300"
  );
}
</script>
