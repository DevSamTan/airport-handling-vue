<template>
  <div class="p-4 space-y-4" @click="closeCtxMenu" @keydown.esc="closeCtxMenu">
    <!-- Toolbar -->
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-2">
        <button
          @click="prevWeek"
          class="flex items-center gap-1.5 px-3 py-1.5 dark:bg-slate-700 bg-gray-200 dark:hover:bg-slate-600 hover:bg-gray-300 dark:text-white text-black text-sm rounded-lg transition-colors"
        >
          <ChevronLeft :size="16" /> Prec.
        </button>
        <span class="text-sm font-semibold dark:text-white text-black px-2">{{
          weekLabel
        }}</span>
        <button
          @click="nextWeek"
          class="flex items-center gap-1.5 px-3 py-1.5 dark:bg-slate-700 bg-gray-200 dark:hover:bg-slate-600 hover:bg-gray-300 dark:text-white text-black text-sm rounded-lg transition-colors"
        >
          Succ. <ChevronRight :size="16" />
        </button>
        <button
          @click="goToday"
          class="px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white text-sm rounded-lg transition-colors"
        >
          Oggi
        </button>
      </div>

      <div class="flex items-center gap-2">
        <!-- Clipboard indicator -->
        <Transition name="fade">
          <div
            v-if="clipboard"
            class="flex items-center gap-2 px-3 py-1.5 bg-blue-500/15 border border-blue-500/40 rounded-lg text-xs"
          >
            <Clipboard :size="13" class="text-blue-400 shrink-0" />
            <span class="text-blue-300 font-medium">
              Copiato:
              <span class="font-bold"
                >{{ SHIFT_TYPES[clipboard]?.abbr ?? clipboard }} –
                {{ SHIFT_TYPES[clipboard]?.label }}</span
              >
            </span>
            <button
              @click.stop="clipboard = null"
              class="ml-1 text-blue-400 hover:text-white transition-colors"
            >
              <X :size="13" />
            </button>
          </div>
        </Transition>

        <select
          v-model="deptFilter"
          class="dark:bg-slate-700 bg-gray-200 dark:text-slate-200 text-gray-800 text-xs px-3 py-1.5 rounded-lg dark:border-slate-600 border-gray-400 focus:outline-none focus:border-blue-500"
        >
          <option value="">Tutti i reparti</option>
          <option v-for="d in departments" :key="d" :value="d">{{ d }}</option>
        </select>

        <button
          @click="triggerCsvImport"
          class="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs rounded-lg transition-colors font-medium"
        >
          <Upload :size="14" /> Importa CSV
        </button>
        <input
          ref="csvInput"
          type="file"
          accept=".csv,.txt"
          class="hidden"
          @change="handleCsvFile"
        />
      </div>
    </div>

    <!-- Legend + hint -->
    <div class="flex flex-wrap items-center justify-between gap-2">
      <div class="flex flex-wrap gap-2">
        <div
          v-for="(type, key) in SHIFT_TYPES"
          :key="key"
          class="flex items-center gap-1"
        >
          <div
            :class="['w-3 h-3 rounded-sm', SHIFT_COLORS[key].split(' ')[0]]"
          ></div>
          <span class="text-[10px] text-slate-400"
            >{{ type.abbr }} {{ type.label }}</span
          >
        </div>
      </div>
      <p class="text-[10px] text-slate-500 flex items-center gap-1">
        <MousePointerClick :size="11" />
        Click sinistro per modificare · Click destro per copiare/incollare
      </p>
    </div>

    <!-- Grid -->
    <div
      class="dark:bg-slate-800 bg-white rounded-xl dark:border-slate-700 border-gray-300 overflow-hidden"
    >
      <div class="overflow-x-auto">
        <table class="w-full min-w-[900px] border-collapse text-xs">
          <thead>
            <tr
              class="dark:bg-slate-900/60 bg-gray-100 dark:text-slate-400 text-gray-600"
            >
              <th
                class="text-left px-4 py-3 font-medium w-40 dark:border-r border-r dark:border-slate-700 border-gray-300"
              >
                Operatore
              </th>
              <th
                v-for="day in weekDays"
                :key="day.iso"
                :class="[
                  'px-2 py-3 text-center border-r border-slate-200 dark:border-slate-700 font-medium',
                  isToday(day.date) && 'text-blue-400',
                ]"
              >
                <div class="capitalize">{{ day.weekday }}</div>
                <div
                  :class="[
                    'text-base font-bold mt-0.5',
                    isToday(day.date)
                      ? 'text-blue-400'
                      : 'text-slate-900 dark:text-white',
                  ]"
                >
                  {{ day.dayNum }}
                </div>
              </th>
              <th class="px-3 py-3 text-center font-medium text-slate-400">
                Totale
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="member in filteredStaff"
              :key="member.id"
              class="border-t border-slate-100 dark:border-slate-700/50 hover:bg-slate-50 dark:hover:bg-slate-700/20 transition-colors"
            >
              <td
                class="px-4 py-2 border-r border-slate-200 dark:border-slate-700"
              >
                <div class="flex items-center gap-2">
                  <div
                    class="w-7 h-7 rounded-full bg-blue-600 flex items-center justify-center text-[10px] font-bold text-white shrink-0"
                  >
                    {{ member.initials }}
                  </div>
                  <div>
                    <div
                      class="font-medium text-slate-900 dark:text-white text-xs"
                    >
                      {{ member.name }}
                    </div>
                    <div class="text-slate-500 text-[10px]">
                      {{ member.dept }}
                    </div>
                  </div>
                </div>
              </td>

              <td
                v-for="day in weekDays"
                :key="day.iso"
                class="px-1 py-1 border-r border-slate-100 dark:border-slate-700/50 align-top"
              >
                <div class="space-y-0.5">
                  <!-- One pill per shift type -->
                  <div
                    v-for="(shiftType, idx) in shiftStore.getShifts(
                      member.id,
                      day.date,
                    )"
                    :key="shiftType + idx"
                    :class="[
                      'group relative flex items-center justify-between rounded px-1.5 py-1 cursor-pointer transition-opacity',
                      SHIFT_COLORS[shiftType],
                      isPasteTarget(member.id, day.date) &&
                        idx === 0 &&
                        'ring-2 ring-blue-400 ring-offset-1 ring-offset-white dark:ring-offset-slate-800',
                    ]"
                    @click.stop="openEdit(member, day.date, shiftType)"
                    @contextmenu.prevent.stop="
                      openCtxMenu($event, member, day.date, shiftType)
                    "
                    title="Click: modifica · Click destro: opzioni"
                  >
                    <div>
                      <div class="font-bold text-[10px]">
                        {{ SHIFT_TYPES[shiftType]?.abbr ?? shiftType }}
                      </div>
                      <div class="text-[9px] opacity-80 leading-tight">
                        {{ SHIFT_TYPES[shiftType]?.hours ?? '—' }}
                      </div>
                    </div>
                    <button
                      @click.stop="
                        shiftStore.removeShiftItem(
                          member.id,
                          day.date,
                          shiftType,
                        )
                      "
                      class="opacity-0 group-hover:opacity-60 hover:!opacity-100 text-current shrink-0 ml-1 transition-opacity"
                      title="Rimuovi questo turno"
                    >
                      <X :size="10" />
                    </button>
                  </div>

                  <!-- Empty cell -->
                  <button
                    v-if="
                      shiftStore.getShifts(member.id, day.date).length === 0
                    "
                    @click.stop="openEdit(member, day.date)"
                    @contextmenu.prevent.stop="
                      openCtxMenu($event, member, day.date)
                    "
                    :class="[
                      'w-full h-11 border border-dashed rounded transition-all flex items-center justify-center',
                      clipboard
                        ? 'border-blue-500/50 text-blue-500/50 hover:border-blue-400 hover:text-blue-400 hover:bg-blue-500/5'
                        : 'border-slate-300 dark:border-slate-600 text-slate-400 dark:text-slate-600 hover:border-blue-500 hover:text-blue-500',
                    ]"
                  >
                    <ClipboardPaste v-if="clipboard" :size="15" />
                    <Plus v-else :size="15" />
                  </button>

                  <!-- Add another shift -->
                  <button
                    v-if="shiftStore.getShifts(member.id, day.date).length > 0"
                    @click.stop="openEdit(member, day.date, null)"
                    class="w-full flex items-center justify-center gap-0.5 py-0.5 text-[9px] text-slate-400 dark:text-slate-600 hover:text-blue-500 dark:hover:text-blue-400 transition-colors"
                    title="Aggiungi un altro turno"
                  >
                    <Plus :size="9" /> turno
                  </button>

                  <!-- Vacation conflict badge -->
                  <div
                    v-if="hasVacationConflict(member.id, day.date)"
                    class="flex items-center gap-0.5 px-1 py-0.5 text-[8px] font-medium text-orange-600 dark:text-orange-400 bg-orange-500/10 rounded"
                    title="Turno assegnato durante ferie approvate"
                  >
                    <AlertTriangle :size="8" /> Ferie approv.
                  </div>
                </div>
              </td>

              <td class="px-3 py-2 text-center">
                <div
                  :class="[
                    'text-xs font-bold font-mono',
                    weeklyH(member.id) > member.contractHours
                      ? 'text-orange-400'
                      : weeklyH(member.id) < member.contractHours - 4
                        ? 'text-red-400'
                        : 'text-emerald-400',
                  ]"
                >
                  {{ weeklyH(member.id) }}h
                </div>
                <div class="text-[10px] text-slate-500">
                  / {{ member.contractHours }}h
                </div>
              </td>
            </tr>

            <!-- Daily totals -->
            <tr
              class="border-t-2 border-slate-200 dark:border-slate-600 bg-slate-100 dark:bg-slate-900/40"
            >
              <td
                class="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 border-r border-slate-200 dark:border-slate-700"
              >
                Operatori/giorno
              </td>
              <td
                v-for="day in weekDays"
                :key="day.iso"
                class="px-2 py-2 text-center border-r border-slate-100 dark:border-slate-700/50"
              >
                <span
                  :class="[
                    'text-xs font-bold',
                    dayOp(day.date) < 3
                      ? 'text-red-500 dark:text-red-400'
                      : 'text-slate-700 dark:text-slate-300',
                  ]"
                >
                  {{ dayOp(day.date) }}
                </span>
              </td>
              <td></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Context Menu -->
    <Teleport to="body">
      <div
        v-if="ctxMenu.open"
        :style="{ top: ctxMenu.y + 'px', left: ctxMenu.x + 'px' }"
        class="fixed z-[100] bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded-xl shadow-2xl py-1 min-w-[180px]"
        @click.stop
      >
        <!-- Header: shows what shift is in this cell -->
        <div
          v-if="ctxMenu.existingShift"
          class="px-3 py-2 border-b border-slate-100 dark:border-slate-700 mb-1"
        >
          <div class="flex items-center gap-2">
            <div
              :class="[
                'w-2 h-2 rounded-sm',
                SHIFT_COLORS[ctxMenu.existingShift].split(' ')[0],
              ]"
            ></div>
            <span class="text-[10px] text-slate-500 dark:text-slate-400"
              >{{ SHIFT_TYPES[ctxMenu.existingShift]?.label ?? ctxMenu.existingShift }} ·
              {{ SHIFT_TYPES[ctxMenu.existingShift]?.hours ?? '—' }}</span
            >
          </div>
        </div>

        <!-- Copy (only if cell has a shift) -->
        <button
          v-if="ctxMenu.existingShift"
          @click="copyShift"
          class="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
        >
          <Copy :size="14" class="text-slate-400" />
          Copia turno
          <kbd
            class="ml-auto text-[9px] text-slate-400 dark:text-slate-500 bg-slate-100 dark:bg-slate-700 px-1.5 py-0.5 rounded"
            >Ctrl+C</kbd
          >
        </button>

        <!-- Paste (only if clipboard has content) -->
        <button
          v-if="clipboard"
          @click="pasteShift"
          class="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-blue-300 hover:bg-blue-500/15 transition-colors"
        >
          <ClipboardPaste :size="14" class="text-blue-400" />
          <span>
            Incolla
            <span class="font-bold">{{ SHIFT_TYPES[clipboard]?.abbr ?? clipboard }}</span>
            <span class="text-blue-400/70">
              – {{ SHIFT_TYPES[clipboard]?.label }}</span
            >
          </span>
          <kbd
            class="ml-auto text-[9px] text-slate-400 dark:text-slate-500 bg-slate-100 dark:bg-slate-700 px-1.5 py-0.5 rounded"
            >Ctrl+V</kbd
          >
        </button>

        <div
          v-if="ctxMenu.existingShift || clipboard"
          class="my-1 border-t border-slate-100 dark:border-slate-700"
        ></div>

        <!-- Edit -->
        <button
          @click="ctxEdit"
          class="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
        >
          <Pencil :size="14" class="text-slate-400" />
          Modifica turno
        </button>

        <!-- Remove (only if cell has a shift) -->
        <button
          v-if="ctxMenu.existingShift"
          @click="ctxRemove"
          class="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-red-400 hover:bg-red-500/10 transition-colors"
        >
          <Trash2 :size="14" />
          Rimuovi turno
        </button>
      </div>

      <!-- Click-outside overlay for context menu -->
      <div
        v-if="ctxMenu.open"
        class="fixed inset-0 z-[99]"
        @click="closeCtxMenu"
        @contextmenu.prevent="closeCtxMenu"
      ></div>
    </Teleport>

    <!-- CSV Import Preview Modal -->
    <div
      v-if="importPreview.open"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
      @click.self="importPreview.open = false"
    >
      <div
        class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 w-full max-w-lg shadow-2xl"
      >
        <div
          class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between"
        >
          <h3
            class="text-sm font-semibold text-slate-900 dark:text-white flex items-center gap-2"
          >
            <Upload :size="16" class="text-emerald-500" /> Anteprima
            Importazione CSV
          </h3>
          <button
            @click="importPreview.open = false"
            class="text-slate-400 hover:text-slate-700 dark:hover:text-white"
          >
            <X :size="20" />
          </button>
        </div>

        <div class="px-6 py-4 space-y-4">
          <!-- Start date picker -->
          <div>
            <label class="text-xs text-slate-500 dark:text-slate-400 block mb-1"
              >Data di inizio (prima colonna del CSV)</label
            >
            <input
              type="date"
              v-model="importPreview.startDate"
              @change="refreshPreview"
              class="bg-white dark:bg-slate-700 border border-slate-300 dark:border-slate-600 text-slate-900 dark:text-white text-xs px-3 py-1.5 rounded-lg focus:outline-none focus:border-blue-500"
            />
          </div>

          <!-- Stats -->
          <div class="grid grid-cols-3 gap-3">
            <div
              class="bg-slate-50 dark:bg-slate-700/40 rounded-xl p-3 text-center"
            >
              <p class="text-slate-500 dark:text-slate-400 text-[10px] mb-1">
                Operatori
              </p>
              <p class="text-xl font-bold text-slate-900 dark:text-white">
                {{ importPreview.rows.length }}
              </p>
            </div>
            <div
              class="bg-slate-50 dark:bg-slate-700/40 rounded-xl p-3 text-center"
            >
              <p class="text-slate-500 dark:text-slate-400 text-[10px] mb-1">
                Turni totali
              </p>
              <p
                class="text-xl font-bold text-emerald-600 dark:text-emerald-400"
              >
                {{ importPreview.totalShifts }}
              </p>
            </div>
            <div
              class="bg-slate-50 dark:bg-slate-700/40 rounded-xl p-3 text-center"
            >
              <p class="text-slate-500 dark:text-slate-400 text-[10px] mb-1">
                Periodo
              </p>
              <p
                class="text-[10px] font-semibold text-slate-700 dark:text-slate-300 leading-tight mt-1"
              >
                {{ importPreview.dateRange }}
              </p>
            </div>
          </div>

          <!-- Employee list -->
          <div v-if="importPreview.rows.length">
            <p class="text-xs text-slate-500 dark:text-slate-400 mb-2">
              Operatori trovati:
            </p>
            <div class="max-h-48 overflow-y-auto space-y-1 pr-1">
              <div
                v-for="row in importPreview.rows"
                :key="row.name"
                class="flex items-center justify-between px-3 py-1.5 bg-slate-50 dark:bg-slate-700/40 rounded-lg"
              >
                <span
                  class="text-xs text-slate-900 dark:text-white font-medium"
                  >{{ row.name }}</span
                >
                <span
                  class="text-[10px] text-slate-500 dark:text-slate-400 font-mono"
                  >{{ Object.keys(row.shifts).length }} turni</span
                >
              </div>
            </div>
          </div>

          <div
            v-else
            class="text-center py-4 text-xs text-slate-400 dark:text-slate-500"
          >
            Nessun operatore rilevato. Controlla la data di inizio o il formato
            del file.
          </div>
        </div>

        <div
          class="px-6 py-4 border-t border-slate-100 dark:border-slate-700 flex gap-2 justify-end"
        >
          <button
            @click="importPreview.open = false"
            class="px-4 py-2 text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors"
          >
            Annulla
          </button>
          <button
            @click="confirmImport"
            :disabled="!importPreview.rows.length"
            class="px-4 py-2 text-sm bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 disabled:cursor-not-allowed text-white rounded-lg transition-colors font-medium flex items-center gap-1.5"
          >
            <Upload :size="14" /> Importa
            {{ importPreview.rows.length }} operatori
          </button>
        </div>
      </div>
    </div>

    <!-- Modal shift assignment -->
    <div
      v-if="modal.open"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
      @click.self="modal.open = false"
    >
      <div
        class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 w-full max-w-sm shadow-2xl"
      >
        <div
          class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between"
        >
          <h3 class="text-sm font-semibold text-slate-900 dark:text-white">
            {{ modal.originalType ? "Modifica Turno" : "Aggiungi Turno" }}
          </h3>
          <button
            @click="modal.open = false"
            class="text-slate-400 hover:text-slate-700 dark:hover:text-white"
          >
            <X :size="20" />
          </button>
        </div>
        <div class="px-6 py-4 space-y-4">
          <div>
            <p class="text-xs text-slate-500 dark:text-slate-400">Operatore</p>
            <p class="text-sm font-semibold text-slate-900 dark:text-white">
              {{ modal.staff?.name }}
            </p>
          </div>
          <div>
            <p class="text-xs text-slate-500 dark:text-slate-400">Data</p>
            <p class="text-sm font-semibold text-slate-900 dark:text-white">
              {{ modal.dateLabel }}
            </p>
          </div>
          <div>
            <label class="text-xs text-slate-500 dark:text-slate-400 block mb-2"
              >Tipo turno</label
            >
            <div class="grid grid-cols-2 gap-2">
              <button
                v-for="(type, key) in SHIFT_TYPES"
                :key="key"
                @click="modal.selected = key"
                :class="[
                  'p-2.5 rounded-lg border text-left transition-all',
                  modal.selected === key
                    ? 'border-blue-500 bg-blue-500/20'
                    : 'border-slate-200 dark:border-slate-600 hover:border-slate-400 dark:hover:border-slate-500 bg-slate-50 dark:bg-slate-700/50',
                ]"
              >
                <div class="text-xs font-bold text-slate-900 dark:text-white">
                  {{ type.abbr }}
                </div>
                <div
                  class="text-[10px] text-slate-500 dark:text-slate-400 mt-0.5"
                >
                  {{ type.label }}
                </div>
                <div class="text-[10px] text-slate-400 dark:text-slate-500">
                  {{ type.hours }}
                </div>
              </button>
            </div>
          </div>
        </div>
        <div
          class="px-6 py-4 border-t border-slate-100 dark:border-slate-700 flex gap-2 justify-end"
        >
          <button
            v-if="modal.originalType"
            @click="removeShift"
            class="px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
          >
            Rimuovi
          </button>
          <button
            @click="modal.open = false"
            class="px-4 py-2 text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors"
          >
            Annulla
          </button>
          <button
            @click="saveShift"
            class="px-4 py-2 text-sm bg-blue-600 hover:bg-blue-500 text-white rounded-lg transition-colors font-medium"
          >
            Salva
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
    AlertTriangle,
    ChevronLeft,
    ChevronRight,
    Clipboard,
    ClipboardPaste,
    Copy,
    MousePointerClick,
    Pencil,
    Plus,
    Trash2,
    Upload,
    X,
} from "lucide-vue-next";
import { computed, reactive, ref } from "vue";
import { useRequestStore } from "../stores/useRequestStore";
import {
    DEPARTMENTS,
    SHIFT_COLORS,
    SHIFT_TYPES,
    useShiftStore,
} from "../stores/useShiftStore";

const shiftStore = useShiftStore();
const reqStore = useRequestStore();
const departments = DEPARTMENTS;
const deptFilter = ref("");

function hasVacationConflict(staffId, date) {
  const iso = (date instanceof Date ? date : new Date(date))
    .toISOString()
    .slice(0, 10);
  const hasShifts = shiftStore
    .getShifts(staffId, date)
    .some((t) => !["R", "F", "MAL", "INF", "PER"].includes(t));
  return hasShifts && reqStore.hasApprovedVacation(staffId, iso);
}

// ########## Clipboard ######################################################################
const clipboard = ref(null); // stores shift type key, e.g. 'M'

// ########## Context menu ##################################################─
const ctxMenu = reactive({
  open: false,
  x: 0,
  y: 0,
  staff: null,
  date: null,
  existingShift: null,
});

function openCtxMenu(event, member, date, shiftType = null) {
  const shifts = shiftStore.getShifts(member.id, date);
  const existing = shiftType ?? (shifts.length > 0 ? shifts[0] : null);
  const menuW = 200,
    menuH = 160;
  const x = Math.min(event.clientX, window.innerWidth - menuW - 8);
  const y = Math.min(event.clientY, window.innerHeight - menuH - 8);
  ctxMenu.open = true;
  ctxMenu.x = x;
  ctxMenu.y = y;
  ctxMenu.staff = member;
  ctxMenu.date = date;
  ctxMenu.existingShift = existing;
}

function closeCtxMenu() {
  ctxMenu.open = false;
}

function copyShift() {
  clipboard.value = ctxMenu.existingShift;
  closeCtxMenu();
}

function pasteShift() {
  if (clipboard.value && ctxMenu.staff && ctxMenu.date) {
    shiftStore.setShift(ctxMenu.staff.id, ctxMenu.date, clipboard.value);
  }
  closeCtxMenu();
}

function ctxEdit() {
  closeCtxMenu();
  openEdit(ctxMenu.staff, ctxMenu.date, ctxMenu.existingShift);
}

function ctxRemove() {
  if (ctxMenu.existingShift) {
    shiftStore.removeShiftItem(
      ctxMenu.staff.id,
      ctxMenu.date,
      ctxMenu.existingShift,
    );
  } else {
    shiftStore.setShift(ctxMenu.staff.id, ctxMenu.date, null);
  }
  closeCtxMenu();
}

function isPasteTarget(staffId, date) {
  if (!clipboard.value) return false;
  const existing = shiftStore.getShifts(staffId, date);
  return existing.length > 0 && !existing.every((t) => t === clipboard.value);
}

// ########## Week navigation ########################################
function getMonday(d) {
  const date = new Date(d);
  const day = date.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  date.setDate(date.getDate() + diff);
  date.setHours(0, 0, 0, 0);
  return date;
}

const currentWeekStart = ref(getMonday(new Date()));

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
function goToday() {
  currentWeekStart.value = getMonday(new Date());
}

const weekDays = computed(() =>
  Array.from({ length: 7 }, (_, i) => {
    const d = new Date(currentWeekStart.value);
    d.setDate(d.getDate() + i);
    return {
      date: new Date(d),
      iso: d.toISOString().slice(0, 10),
      weekday: d.toLocaleDateString("it-IT", { weekday: "short" }),
      dayNum: d.getDate(),
    };
  }),
);

const weekLabel = computed(() => {
  const start = weekDays.value[0].date;
  const end = weekDays.value[6].date;
  const fmtS = start.toLocaleDateString("it-IT", {
    day: "numeric",
    month: "short",
  });
  const fmtE = end.toLocaleDateString("it-IT", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
  return `${fmtS} – ${fmtE}`;
});

function isToday(d) {
  return d.toDateString() === new Date().toDateString();
}

const filteredStaff = computed(() =>
  deptFilter.value
    ? shiftStore.staff.filter((s) => s.dept === deptFilter.value)
    : shiftStore.staff,
);

function weeklyH(staffId) {
  return shiftStore.weeklyHours(staffId, currentWeekStart.value);
}

function dayOp(date) {
  return shiftStore.staff.filter((s) =>
    shiftStore
      .getShifts(s.id, date)
      .some((t) => !["R", "F", "MAL", "INF", "PER"].includes(t)),
  ).length;
}

// ########## Modal (edit / assign) ##########
const modal = reactive({
  open: false,
  staff: null,
  date: null,
  dateLabel: "",
  selected: "M",
  originalType: null, // null = add mode, string = edit/replace that specific type
});

// shiftType: the specific pill that was clicked
//   undefined → opened from empty cell (legacy behaviour: edit first shift or add)
//   null      → "+ turno" button (add another shift)
//   string    → clicked on existing pill (edit/replace that type)
function openEdit(member, date, shiftType = undefined) {
  modal.staff = member;
  modal.date = date;
  modal.dateLabel = date.toLocaleDateString("it-IT", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });
  if (shiftType === undefined) {
    const existing = shiftStore.getShifts(member.id, date);
    modal.originalType = existing.length > 0 ? existing[0] : null;
    modal.selected = existing.length > 0 ? existing[0] : "M";
  } else {
    modal.originalType = shiftType;
    modal.selected = shiftType ?? "M";
  }
  modal.open = true;
}

function saveShift() {
  if (modal.originalType) {
    shiftStore.removeShiftItem(modal.staff.id, modal.date, modal.originalType);
  }
  shiftStore.addShift(modal.staff.id, modal.date, modal.selected);
  modal.open = false;
}

function removeShift() {
  if (modal.originalType) {
    shiftStore.removeShiftItem(modal.staff.id, modal.date, modal.originalType);
  } else {
    shiftStore.setShift(modal.staff.id, modal.date, null);
  }
  modal.open = false;
}

// ########## CSV Import ############################################################─
const csvInput = ref(null);

const importPreview = reactive({
  open: false,
  startDate: "2026-05-01",
  rows: [],
  totalShifts: 0,
  dateRange: "",
  rawText: "",
});

function decodeShiftCode(code) {
  if (!code) return null;
  const c = code.trim().toUpperCase().replace(/\s+/g, "");
  if (!c || c === "-") return null;
  if (c === "R1" || c === "R2" || c === "R") return "R";
  if (c === "FER") return "F";
  if (c === "FS" || c === "CSO") return "PER";
  if (c === "MAL") return "MAL";
  if (c === "INF") return "INF";
  // Numeric codes: H32, F12, E38 → parseInt(digits) / 2 = start hour
  const match = c.match(/^[HFE](\d+)$/);
  if (match) {
    const hour = parseInt(match[1]) / 2;
    if (hour >= 22 || hour < 5) return "N";
    if (hour >= 14) return "P";
    return "M";
  }
  // Already a known shift key
  if (["M", "P", "N", "L", "S6", "F", "MAL", "INF", "PER"].includes(c))
    return c;
  return null;
}

function parseCsvShifts(text, startDateStr) {
  // Try semicolon first, fallback to comma
  const sep = text.includes(";") ? ";" : ",";
  const lines = text
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l);

  // Locate header row: the row with the most numeric day values (1-31)
  let headerIdx = -1;
  let dayColIndices = [];

  for (let i = 0; i < lines.length; i++) {
    const cols = lines[i].split(sep).map((c) => c.trim().replace(/"/g, ""));
    const indices = [];
    for (let j = 0; j < cols.length; j++) {
      const n = parseInt(cols[j]);
      if (/^\d+$/.test(cols[j]) && n >= 1 && n <= 31) indices.push(j);
    }
    if (indices.length > dayColIndices.length) {
      dayColIndices = indices;
      headerIdx = i;
    }
  }

  if (headerIdx === -1 || dayColIndices.length === 0) return [];

  const startDate = new Date(startDateStr);
  const rows = [];

  for (let i = headerIdx + 1; i < lines.length; i++) {
    const cols = lines[i].split(sep).map((c) => c.trim().replace(/"/g, ""));
    const rawName = cols[0] || "";
    // Skip empty, purely numeric, or summary rows
    if (
      !rawName ||
      /^\d+$/.test(rawName) ||
      /totale|reparto|tot\b/i.test(rawName)
    )
      continue;

    const shifts = {};
    dayColIndices.forEach((colIdx, dayOffset) => {
      const shiftType = decodeShiftCode(cols[colIdx]);
      if (shiftType) {
        const d = new Date(startDate);
        d.setDate(d.getDate() + dayOffset);
        shifts[d.toISOString().slice(0, 10)] = shiftType;
      }
    });

    if (Object.keys(shifts).length > 0) {
      rows.push({ name: rawName, shifts });
    }
  }

  return rows;
}

function refreshPreview() {
  importPreview.rows = parseCsvShifts(
    importPreview.rawText,
    importPreview.startDate,
  );
  importPreview.totalShifts = importPreview.rows.reduce(
    (sum, r) => sum + Object.keys(r.shifts).length,
    0,
  );
  const allDates = importPreview.rows.flatMap((r) => Object.keys(r.shifts));
  if (allDates.length) {
    const sorted = allDates.sort();
    const fmt = (s) =>
      new Date(s).toLocaleDateString("it-IT", {
        day: "numeric",
        month: "short",
        year: "numeric",
      });
    importPreview.dateRange = `${fmt(sorted[0])} – ${fmt(sorted[sorted.length - 1])}`;
  } else {
    importPreview.dateRange = "—";
  }
}

function triggerCsvImport() {
  csvInput.value?.click();
}

function handleCsvFile(event) {
  const file = event.target.files[0];
  if (!file) return;
  // Infer start date from filename like "05_RAMP_MAGGIO_26.csv"
  const fnMonth = file.name.match(/^(\d{2})_/);
  if (fnMonth) {
    importPreview.startDate = `2026-${fnMonth[1]}-01`;
  }
  const reader = new FileReader();
  reader.onload = (e) => {
    importPreview.rawText = e.target.result;
    refreshPreview();
    importPreview.open = true;
  };
  // Italian CSVs are often ISO-8859-1; try latin1 first
  reader.readAsText(file, "ISO-8859-1");
  event.target.value = "";
}

function confirmImport() {
  shiftStore.importFromCsv(importPreview.rows);
  // Jump to the first imported week
  const allDates = importPreview.rows.flatMap((r) => Object.keys(r.shifts));
  if (allDates.length) {
    currentWeekStart.value = getMonday(new Date(allDates.sort()[0]));
  }
  importPreview.open = false;
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.15s,
    transform 0.15s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
