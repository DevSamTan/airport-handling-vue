<template>
  <div class="p-4 space-y-4" @click="closeCtxMenu" @keydown.esc="closeCtxMenu">
    <!-- Toolbar -->
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-2">
        <button
          @click="prevWeek"
          class="flex items-center gap-1.5 px-3 py-1.5 bg-slate-700 hover:bg-slate-600 text-white text-sm rounded-lg transition-colors"
        >
          <ChevronLeft :size="16" /> Prec.
        </button>
        <span class="text-sm font-semibold text-white px-2">{{
          weekLabel
        }}</span>
        <button
          @click="nextWeek"
          class="flex items-center gap-1.5 px-3 py-1.5 bg-slate-700 hover:bg-slate-600 text-white text-sm rounded-lg transition-colors"
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
                >{{ SHIFT_TYPES[clipboard].abbr }} –
                {{ SHIFT_TYPES[clipboard].label }}</span
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
          class="bg-slate-700 text-slate-200 text-xs px-3 py-1.5 rounded-lg border border-slate-600 focus:outline-none focus:border-blue-500"
        >
          <option value="">Tutti i reparti</option>
          <option v-for="d in departments" :key="d" :value="d">{{ d }}</option>
        </select>
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
      class="bg-slate-800 rounded-xl border border-slate-700 overflow-hidden"
    >
      <div class="overflow-x-auto">
        <table class="w-full min-w-[900px] border-collapse text-xs">
          <thead>
            <tr class="bg-slate-900/60 text-slate-400">
              <th
                class="text-left px-4 py-3 font-medium w-40 border-r border-slate-700"
              >
                Operatore
              </th>
              <th
                v-for="day in weekDays"
                :key="day.iso"
                :class="[
                  'px-2 py-3 text-center border-r border-slate-700 font-medium',
                  isToday(day.date) && 'text-blue-400',
                ]"
              >
                <div class="capitalize">{{ day.weekday }}</div>
                <div
                  :class="[
                    'text-base font-bold mt-0.5',
                    isToday(day.date) ? 'text-blue-400' : 'text-white',
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
              class="border-t border-slate-700/50 hover:bg-slate-700/20 transition-colors"
            >
              <td class="px-4 py-2 border-r border-slate-700">
                <div class="flex items-center gap-2">
                  <div
                    class="w-7 h-7 rounded-full bg-blue-600 flex items-center justify-center text-[10px] font-bold text-white shrink-0"
                  >
                    {{ member.initials }}
                  </div>
                  <div>
                    <div class="font-medium text-white text-xs">
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
                class="px-1 py-1.5 border-r border-slate-700/50 align-top"
              >
                <!-- Filled cell -->
                <div
                  v-if="shiftStore.getShift(member.id, day.date)"
                  :class="[
                    'group relative rounded p-1.5 cursor-pointer transition-opacity',
                    SHIFT_COLORS[shiftStore.getShift(member.id, day.date)],
                    isPasteTarget(member.id, day.date) &&
                      'ring-2 ring-blue-400 ring-offset-1 ring-offset-slate-800',
                  ]"
                  @click.stop="openEdit(member, day.date)"
                  @contextmenu.prevent.stop="
                    openCtxMenu($event, member, day.date)
                  "
                  :title="`Click sinistro: modifica · Click destro: opzioni`"
                >
                  <div class="font-bold text-[10px]">
                    {{
                      SHIFT_TYPES[shiftStore.getShift(member.id, day.date)].abbr
                    }}
                  </div>
                  <div class="text-[9px] opacity-80 leading-tight">
                    {{
                      SHIFT_TYPES[shiftStore.getShift(member.id, day.date)]
                        .hours
                    }}
                  </div>

                  <!-- Copy hint icon on hover -->
                  <div
                    class="absolute top-0.5 right-0.5 opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <Copy :size="10" class="text-current opacity-60" />
                  </div>
                </div>

                <!-- Empty cell -->
                <button
                  v-else
                  @click.stop="openEdit(member, day.date)"
                  @contextmenu.prevent.stop="
                    openCtxMenu($event, member, day.date)
                  "
                  :class="[
                    'w-full h-12 border border-dashed rounded transition-all flex items-center justify-center',
                    clipboard
                      ? 'border-blue-500/50 text-blue-500/50 hover:border-blue-400 hover:text-blue-400 hover:bg-blue-500/5'
                      : 'border-slate-600 text-slate-600 hover:border-blue-500 hover:text-blue-500',
                  ]"
                  :title="
                    clipboard
                      ? `Incolla: ${SHIFT_TYPES[clipboard].label} (click destro)`
                      : 'Assegna turno'
                  "
                >
                  <ClipboardPaste v-if="clipboard" :size="15" />
                  <Plus v-else :size="16" />
                </button>
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
            <tr class="border-t-2 border-slate-600 bg-slate-900/40">
              <td
                class="px-4 py-2 text-xs font-semibold text-slate-400 border-r border-slate-700"
              >
                Operatori/giorno
              </td>
              <td
                v-for="day in weekDays"
                :key="day.iso"
                class="px-2 py-2 text-center border-r border-slate-700/50"
              >
                <span
                  :class="[
                    'text-xs font-bold',
                    dayOp(day.date) < 3 ? 'text-red-400' : 'text-slate-300',
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
        class="fixed z-[100] bg-slate-800 border border-slate-600 rounded-xl shadow-2xl py-1 min-w-[180px]"
        @click.stop
      >
        <!-- Header: shows what shift is in this cell -->
        <div
          v-if="ctxMenu.existingShift"
          class="px-3 py-2 border-b border-slate-700 mb-1"
        >
          <div class="flex items-center gap-2">
            <div
              :class="[
                'w-2 h-2 rounded-sm',
                SHIFT_COLORS[ctxMenu.existingShift].split(' ')[0],
              ]"
            ></div>
            <span class="text-[10px] text-slate-400"
              >{{ SHIFT_TYPES[ctxMenu.existingShift].label }} ·
              {{ SHIFT_TYPES[ctxMenu.existingShift].hours }}</span
            >
          </div>
        </div>

        <!-- Copy (only if cell has a shift) -->
        <button
          v-if="ctxMenu.existingShift"
          @click="copyShift"
          class="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-slate-200 hover:bg-slate-700 transition-colors"
        >
          <Copy :size="14" class="text-slate-400" />
          Copia turno
          <kbd
            class="ml-auto text-[9px] text-slate-500 bg-slate-700 px-1.5 py-0.5 rounded"
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
            <span class="font-bold">{{ SHIFT_TYPES[clipboard].abbr }}</span>
            <span class="text-blue-400/70">
              – {{ SHIFT_TYPES[clipboard].label }}</span
            >
          </span>
          <kbd
            class="ml-auto text-[9px] text-slate-500 bg-slate-700 px-1.5 py-0.5 rounded"
            >Ctrl+V</kbd
          >
        </button>

        <div
          v-if="ctxMenu.existingShift || clipboard"
          class="my-1 border-t border-slate-700"
        ></div>

        <!-- Edit -->
        <button
          @click="ctxEdit"
          class="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-slate-200 hover:bg-slate-700 transition-colors"
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

    <!-- Modal shift assignment -->
    <div
      v-if="modal.open"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
      @click.self="modal.open = false"
    >
      <div
        class="bg-slate-800 rounded-2xl border border-slate-700 w-full max-w-sm shadow-2xl"
      >
        <div
          class="px-6 py-4 border-b border-slate-700 flex items-center justify-between"
        >
          <h3 class="text-sm font-semibold text-white">Assegna Turno</h3>
          <button
            @click="modal.open = false"
            class="text-slate-400 hover:text-white"
          >
            <X :size="20" />
          </button>
        </div>
        <div class="px-6 py-4 space-y-4">
          <div>
            <p class="text-xs text-slate-400">Operatore</p>
            <p class="text-sm font-semibold text-white">
              {{ modal.staff?.name }}
            </p>
          </div>
          <div>
            <p class="text-xs text-slate-400">Data</p>
            <p class="text-sm font-semibold text-white">
              {{ modal.dateLabel }}
            </p>
          </div>
          <div>
            <label class="text-xs text-slate-400 block mb-2">Tipo turno</label>
            <div class="grid grid-cols-2 gap-2">
              <button
                v-for="(type, key) in SHIFT_TYPES"
                :key="key"
                @click="modal.selected = key"
                :class="[
                  'p-2.5 rounded-lg border text-left transition-all',
                  modal.selected === key
                    ? 'border-blue-500 bg-blue-500/20'
                    : 'border-slate-600 hover:border-slate-500 bg-slate-700/50',
                ]"
              >
                <div class="text-xs font-bold text-white">{{ type.abbr }}</div>
                <div class="text-[10px] text-slate-400 mt-0.5">
                  {{ type.label }}
                </div>
                <div class="text-[10px] text-slate-500">{{ type.hours }}</div>
              </button>
            </div>
          </div>
        </div>
        <div class="px-6 py-4 border-t border-slate-700 flex gap-2 justify-end">
          <button
            v-if="shiftStore.getShift(modal.staff?.id, modal.date)"
            @click="removeShift"
            class="px-4 py-2 text-sm text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
          >
            Rimuovi
          </button>
          <button
            @click="modal.open = false"
            class="px-4 py-2 text-sm text-slate-400 hover:bg-slate-700 rounded-lg transition-colors"
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
  ChevronLeft,
  ChevronRight,
  Clipboard,
  ClipboardPaste,
  Copy,
  MousePointerClick,
  Pencil,
  Plus,
  Trash2,
  X,
} from "lucide-vue-next";
import { computed, reactive, ref } from "vue";
import {
  DEPARTMENTS,
  SHIFT_COLORS,
  SHIFT_TYPES,
  useShiftStore,
} from "../stores/useShiftStore";

const shiftStore = useShiftStore();
const departments = DEPARTMENTS;
const deptFilter = ref("");

// ── Clipboard ──────────────────────────────────────────────────────────────
const clipboard = ref(null); // stores shift type key, e.g. 'M'

// ── Context menu ───────────────────────────────────────────────────────────
const ctxMenu = reactive({
  open: false,
  x: 0,
  y: 0,
  staff: null,
  date: null,
  existingShift: null,
});

function openCtxMenu(event, member, date) {
  const existing = shiftStore.getShift(member.id, date);
  // Smart positioning: keep menu inside viewport
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
  openEdit(ctxMenu.staff, ctxMenu.date);
}

function ctxRemove() {
  shiftStore.setShift(ctxMenu.staff.id, ctxMenu.date, null);
  closeCtxMenu();
}

// Highlight cells that would receive a paste (same shift type already there = no highlight)
function isPasteTarget(staffId, date) {
  if (!clipboard.value) return false;
  const existing = shiftStore.getShift(staffId, date);
  return existing && existing !== clipboard.value;
}

// ── Week navigation ────────────────────────────────────────────────────────
const currentWeekStart = ref(new Date("2026-04-27"));

function getMonday(d) {
  const date = new Date(d);
  const day = date.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  date.setDate(date.getDate() + diff);
  date.setHours(0, 0, 0, 0);
  return date;
}

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
  return shiftStore.staff.filter((s) => {
    const type = shiftStore.getShift(s.id, date);
    return type && !["R", "F", "MAL", "INF", "PER"].includes(type);
  }).length;
}

// ── Modal (edit / assign) ──────────────────────────────────────────────────
const modal = reactive({
  open: false,
  staff: null,
  date: null,
  dateLabel: "",
  selected: "M",
});

function openEdit(member, date) {
  modal.staff = member;
  modal.date = date;
  modal.dateLabel = date.toLocaleDateString("it-IT", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });
  modal.selected = shiftStore.getShift(member.id, date) || "M";
  modal.open = true;
}

function saveShift() {
  shiftStore.setShift(modal.staff.id, modal.date, modal.selected);
  modal.open = false;
}

function removeShift() {
  shiftStore.setShift(modal.staff.id, modal.date, null);
  modal.open = false;
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
