<template>
  <div class="p-6 space-y-6">
    <!-- KPI cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div
        v-for="kpi in kpis"
        :key="kpi.label"
        class="bg-white dark:bg-slate-800 rounded-xl p-4 border border-slate-200 dark:border-slate-700"
      >
        <div class="flex items-start justify-between">
          <div>
            <p class="text-xs text-slate-600 dark:text-slate-400 mb-1">
              {{ kpi.label }}
            </p>
            <p class="text-2xl font-bold text-slate-900 dark:text-white">
              {{ kpi.value }}
            </p>
            <p
              :class="[
                'text-xs mt-1',
                kpi.deltaPositive ? 'text-emerald-400' : 'text-red-400',
              ]"
            >
              {{ kpi.delta }}
            </p>
          </div>
          <span :class="['p-2 rounded-lg', kpi.iconBg]">
            <component :is="kpi.icon" :size="20" :class="kpi.iconColor" />
          </span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Coverage today -->
      <div
        class="lg:col-span-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5"
      >
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white mb-4">
          Copertura Operativa – Oggi
        </h2>
        <div class="space-y-3">
          <div v-for="dept in deptCoverage" :key="dept.name">
            <div
              class="flex items-center justify-between text-xs text-slate-600 dark:text-slate-400 mb-1"
            >
              <span>{{ dept.name }}</span>
              <span>{{ dept.onShift }}/{{ dept.total }} operatori</span>
            </div>
            <div
              class="h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden"
            >
              <div
                :class="[
                  'h-full rounded-full transition-all',
                  coverageColor(dept.onShift / dept.total),
                ]"
                :style="{ width: (dept.onShift / dept.total) * 100 + '%' }"
              ></div>
            </div>
          </div>
        </div>

        <!-- Timeline fascia oraria -->
        <h3
          class="text-xs font-semibold text-slate-600 dark:text-slate-400 mt-5 mb-3"
        >
          Distribuzione per Fascia
        </h3>
        <div class="flex gap-1">
          <div
            v-for="slot in timeSlots"
            :key="slot.hour"
            class="flex-1 text-center"
          >
            <div
              :class="[
                'rounded text-[10px] py-1 font-mono',
                staffingLevel(slot.count),
              ]"
              :title="`${slot.hour}:00 – ${slot.count} operatori`"
            >
              {{ slot.count }}
            </div>
            <div class="text-[9px] text-slate-500 dark:text-slate-500 mt-0.5">
              {{ slot.hour }}
            </div>
          </div>
        </div>
      </div>

      <!-- Alerts & Pending -->
      <div
        class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5"
      >
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white mb-4">
          Situazione Attuale
        </h2>
        <div class="space-y-3">
          <div
            v-for="alert in alerts"
            :key="alert.id"
            :class="['flex items-start gap-3 p-3 rounded-lg', alert.bg]"
          >
            <span :class="['shrink-0 mt-0.5', alert.iconColor]">
              <component :is="alert.icon" :size="16" />
            </span>
            <div>
              <p class="text-xs font-medium text-slate-900 dark:text-white">
                {{ alert.title }}
              </p>
              <p class="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
                {{ alert.sub }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Weekly stats table -->
    <!-- <div
      class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5"
    >
      <h2 class="text-sm font-semibold text-slate-900 dark:text-white mb-4">
        Riepilogo Settimana – Ore Personale
      </h2>
      <div class="overflow-x-auto">
        <table class="w-full text-xs">
          <thead>
            <tr
              class="text-slate-600 dark:text-slate-400 border-b border-slate-200 dark:border-slate-700"
            >
              <th class="text-left py-2 pr-4 font-medium">Dipendente</th>
              <th class="text-left py-2 pr-4 font-medium">Reparto</th>
              <th class="text-right py-2 pr-4 font-medium">Target/sett</th>
              <th class="text-right py-2 pr-4 font-medium">Ore lavorate</th>
              <th class="text-right py-2 pr-4 font-medium">Saldo</th>
              <th class="text-left py-2 font-medium">Stato</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in weekSummary"
              :key="row.id"
              class="border-b border-slate-200 dark:border-slate-700/50 hover:bg-slate-100 dark:hover:bg-slate-700/30 transition-colors"
            >
              <td class="py-2.5 pr-4"></td>
              <div class="flex items-center gap-2">
                <div
                  class="w-6 h-6 rounded-full bg-blue-600 flex items-center justify-center text-[10px] font-bold text-white"
                >
                  {{ row.initials }}
                </div>
                <span class="text-slate-900 dark:text-white font-medium">{{
                  row.name
                }}</span>
              </div>
              <td class="py-2.5 pr-4 text-slate-600 dark:text-slate-400">
                {{ row.dept }}
              </td>
              <td
                class="py-2.5 pr-4 text-right text-slate-700 dark:text-slate-300"
              >
                {{ row.target }}h
              </td>
              <td
                class="py-2.5 pr-4 text-right font-mono text-slate-900 dark:text-white"
              >
                {{ row.worked }}h
              </td>
              <td
                class="py-2.5 pr-4 text-right font-mono"
                :class="row.balance >= 0 ? 'text-emerald-400' : 'text-red-400'"
              >
                {{ row.balance >= 0 ? "+" : "" }}{{ row.balance }}h
              </td>
              <td class="py-2.5">
                <span
                  :class="[
                    'px-2 py-0.5 rounded-full text-[10px] font-medium',
                    row.statusClass,
                  ]"
                  >{{ row.statusLabel }}</span
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div> -->
  </div>
</template>

<!-- ########## SCRIPT ########## -->

<script setup>
import {
  AlertTriangle,
  ArrowLeftRight,
  Clock,
  Thermometer,
  Users,
} from "lucide-vue-next";
import { computed } from "vue";
import { useRequestStore } from "../stores/useRequestStore";
import { useShiftStore } from "../stores/useShiftStore";

const shiftStore = useShiftStore();
const reqStore = useRequestStore();

const kpis = computed(() => [
  {
    label: "Operatori in turno oggi",
    value: "6",
    delta: "+2 rispetto a ieri",
    deltaPositive: true,
    icon: Users,
    iconBg: "bg-blue-500/20",
    iconColor: "text-blue-400",
  },
  {
    label: "Richieste in attesa",
    value: reqStore.pendingSwaps.length,
    delta: `${reqStore.pendingSwaps.length} cambi turno`,
    deltaPositive: false,
    icon: ArrowLeftRight,
    iconBg: "bg-yellow-500/20",
    iconColor: "text-yellow-400",
  },
  {
    label: "Assenze attive",
    value: reqStore.pendingSick.length,
    delta: `${reqStore.pendingSick.length} certificato mancante`,
    deltaPositive: false,
    icon: Thermometer,
    iconBg: "bg-red-500/20",
    iconColor: "text-red-400",
  },
  {
    label: "Scoperture questa sett.",
    value: "2",
    delta: "-1 rispetto a settimana scorsa",
    deltaPositive: true,
    icon: AlertTriangle,
    iconBg: "bg-orange-500/20",
    iconColor: "text-orange-400",
  },
]);

const deptCoverage = [
  { name: "Rampa / Piazzale", onShift: 4, total: 5 },
  { name: "Magazzino", onShift: 2, total: 2 },
  { name: "Coordinatori", onShift: 1, total: 1 },
  { name: "GSE / Mezzi", onShift: 1, total: 1 },
];

function coverageColor(ratio) {
  if (ratio >= 0.8) return "bg-emerald-500";
  if (ratio >= 0.5) return "bg-yellow-500";
  return "bg-red-500";
}

const timeSlots = [
  { hour: "00", count: 3 },
  { hour: "02", count: 3 },
  { hour: "04", count: 2 },
  { hour: "06", count: 5 },
  { hour: "08", count: 6 },
  { hour: "10", count: 6 },
  { hour: "12", count: 6 },
  { hour: "14", count: 6 },
  { hour: "16", count: 5 },
  { hour: "18", count: 4 },
  { hour: "20", count: 4 },
  { hour: "22", count: 3 },
];

function staffingLevel(count) {
  if (count >= 5)
    return "bg-emerald-500/20 text-emerald-700 dark:text-emerald-300";
  if (count >= 3)
    return "bg-yellow-500/20 text-yellow-700 dark:text-yellow-300";
  return "bg-red-500/20 text-red-700 dark:text-red-300";
}

const alerts = [
  {
    id: 1,
    icon: AlertTriangle,
    iconColor: "text-red-400",
    title: "Scopertura Venerdì Notte",
    sub: "Rampa – nessun operatore assegnato",
    bg: "bg-red-500/10 border border-red-500/30",
  },
  {
    id: 2,
    icon: Thermometer,
    iconColor: "text-orange-400",
    title: "Elena Costa – Malattia",
    sub: "Certificato non ancora ricevuto",
    bg: "bg-orange-500/10 border border-orange-500/30",
  },
  {
    id: 3,
    icon: ArrowLeftRight,
    iconColor: "text-blue-400",
    title: "2 richieste in attesa",
    sub: "Cambi turno da approvare",
    bg: "bg-blue-500/10 border border-blue-500/30",
  },
  {
    id: 4,
    icon: Clock,
    iconColor: "text-yellow-400",
    title: "G. Verdi – 50h settimana",
    sub: "Vicino al limite straordinari",
    bg: "bg-yellow-500/10 border border-yellow-500/30",
  },
];

const weekStart = new Date("2026-04-27");

const weekSummary = computed(() =>
  shiftStore.staff.map((s) => {
    const worked = shiftStore.weeklyHours(s.id, weekStart);
    const target = s.contractHours;
    const balance = worked - target;
    let statusLabel, statusClass;
    if (s.id === 8) {
      statusLabel = "Malattia";
      statusClass = "bg-red-500/20 text-red-700 dark:text-red-300";
    } else if (worked > target + 4) {
      statusLabel = "Straordinario";
      statusClass = "bg-orange-500/20 text-orange-700 dark:text-orange-300";
    } else if (worked < target - 4) {
      statusLabel = "Ore mancanti";
      statusClass = "bg-yellow-500/20 text-yellow-700 dark:text-yellow-300";
    } else {
      statusLabel = "Regolare";
      statusClass = "bg-emerald-500/20 text-emerald-700 dark:text-emerald-300";
    }
    return { ...s, worked, target, balance, statusLabel, statusClass };
  }),
);
</script>
