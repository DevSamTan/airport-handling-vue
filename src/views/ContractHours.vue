<template>
  <div class="p-6 space-y-6">
    <div class="flex flex-wrap gap-3 items-center justify-between">
      <h1 class="text-base font-semibold text-slate-900 dark:text-white">
        Monitoraggio Ore Contrattuali – Aprile 2026
      </h1>
      <select
        v-model="deptFilter"
        class="bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none"
      >
        <option value="">Tutti i reparti</option>
        <option v-for="d in departments" :key="d" :value="d">{{ d }}</option>
      </select>
    </div>

    <!-- Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div
        v-for="emp in filteredStaff"
        :key="emp.id"
        class="bg-white dark:bg-slate-800 rounded-xl border dark:border-slate-700 border-slate-200 p-4 space-y-3 transition-colors"
        :class="emp.statusBorder"
      >
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <div
              class="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-sm font-bold text-white"
            >
              {{ emp.initials }}
            </div>
            <div>
              <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ emp.name }}</p>
              <p class="text-xs text-slate-500 dark:text-slate-400">
                {{ emp.dept }} · {{ emp.contract }} ({{ emp.contractHours }}h/sett)
              </p>
            </div>
          </div>
          <span
            :class="[
              'text-[10px] px-2 py-0.5 rounded-full font-medium',
              emp.statusClass,
            ]"
            >{{ emp.statusLabel }}</span
          >
        </div>

        <!-- Progress bar -->
        <div>
          <div class="flex justify-between text-[10px] text-slate-500 dark:text-slate-400 mb-1">
            <span>{{ emp.worked }}h / {{ emp.target }}h</span>
            <span>{{ Math.min(100, Math.round((emp.worked / emp.target) * 100)) }}%</span>
          </div>
          <div class="h-2.5 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
            <div
              :class="['h-full rounded-full transition-all', emp.barColor]"
              :style="{ width: Math.min(100, (emp.worked / emp.target) * 100) + '%' }"
            ></div>
          </div>
        </div>

        <!-- Stats row -->
        <div class="grid grid-cols-3 gap-2 text-xs">
          <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2 text-center">
            <p class="text-slate-500 dark:text-slate-400 text-[10px]">Lavorate</p>
            <p class="font-bold text-slate-900 dark:text-white font-mono">{{ emp.worked }}h</p>
          </div>
          <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2 text-center">
            <p class="text-slate-500 dark:text-slate-400 text-[10px]">Residue</p>
            <p
              class="font-bold font-mono"
              :class="emp.remaining > 0 ? 'text-yellow-600 dark:text-yellow-400' : 'text-slate-400 dark:text-slate-500'"
            >
              {{ Math.max(0, emp.remaining) }}h
            </p>
          </div>
          <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2 text-center">
            <p class="text-slate-500 dark:text-slate-400 text-[10px]">Straord.</p>
            <p
              class="font-bold font-mono"
              :class="emp.overtime > 0 ? 'text-orange-600 dark:text-orange-400' : 'text-slate-400 dark:text-slate-500'"
            >
              {{ emp.overtime > 0 ? "+" + emp.overtime : "—" }}
            </p>
          </div>
        </div>

        <!-- Warnings -->
        <div v-if="emp.warnings.length" class="space-y-1">
          <div
            v-for="w in emp.warnings"
            :key="w"
            class="flex items-center gap-2 text-[10px] text-orange-700 dark:text-orange-300 bg-orange-500/10 border border-orange-500/20 rounded-lg px-2 py-1"
          >
            <AlertTriangle :size="11" class="shrink-0" />
            {{ w }}
          </div>
        </div>
      </div>
    </div>

    <!-- Compliance table -->
    <div class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
      <h2 class="text-sm font-semibold text-slate-900 dark:text-white mb-4">
        Riepilogo Conformità Contrattuale
      </h2>
      <div class="overflow-x-auto">
        <table class="w-full text-xs">
          <thead>
            <tr class="text-slate-600 dark:text-slate-400 border-b border-slate-200 dark:border-slate-700">
              <th class="text-left py-2.5 pr-4 font-medium">Dipendente</th>
              <th class="text-right py-2.5 pr-4 font-medium">Max ore/giorno</th>
              <th class="text-right py-2.5 pr-4 font-medium">Riposi minimi</th>
              <th class="text-right py-2.5 pr-4 font-medium">Ore notturne</th>
              <th class="text-left py-2.5 font-medium">Conformità</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="emp in filteredStaff"
              :key="emp.id"
              class="border-b border-slate-100 dark:border-slate-700/40 hover:bg-slate-50 dark:hover:bg-slate-700/20"
            >
              <td class="py-2.5 pr-4 text-slate-900 dark:text-white font-medium">{{ emp.name }}</td>
              <td class="py-2.5 pr-4 text-right font-mono text-emerald-600 dark:text-emerald-400">
                <div class="flex items-center justify-end gap-1">
                  <CheckCircle :size="12" /> &lt;13h
                </div>
              </td>
              <td
                class="py-2.5 pr-4 text-right font-mono"
                :class="emp.id === 3 ? 'text-red-600 dark:text-red-400' : 'text-emerald-600 dark:text-emerald-400'"
              >
                <div class="flex items-center justify-end gap-1">
                  <component :is="emp.id === 3 ? XCircle : CheckCircle" :size="12" />
                  {{ emp.id === 3 ? "10h (min 11h)" : "≥11h" }}
                </div>
              </td>
              <td class="py-2.5 pr-4 text-right font-mono text-slate-600 dark:text-slate-300">
                {{ [3, 1, 5].includes(emp.id) ? "8h" : "0h" }}
              </td>
              <td class="py-2.5">
                <span
                  :class="[
                    'px-2 py-0.5 rounded-full text-[10px] font-medium',
                    emp.id === 3
                      ? 'bg-red-500/20 text-red-700 dark:text-red-300'
                      : 'bg-emerald-500/20 text-emerald-700 dark:text-emerald-300',
                  ]"
                >
                  {{ emp.id === 3 ? "Violazione" : "Conforme" }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { AlertTriangle, CheckCircle, XCircle } from "lucide-vue-next";
import { computed, ref } from "vue";
import { DEPARTMENTS, useShiftStore } from "../stores/useShiftStore";

const shiftStore = useShiftStore();
const departments = DEPARTMENTS;
const deptFilter = ref("");

const filteredStaff = computed(() =>
  shiftStore.staff
    .filter((s) => !deptFilter.value || s.dept === deptFilter.value)
    .map((s) => {
      const worked = shiftStore.monthlyHours(s.id, 2026, 4);
      const target = s.monthlyTarget;
      const remaining = target - worked;
      const overtime = Math.max(0, worked - target);

      const warnings = [];
      if (overtime > 16)
        warnings.push("Superamento soglia straordinari (>16h/mese)");
      if (s.id === 3)
        warnings.push("Riposo insufficiente rilevato (Gio 30/04)");
      if (worked < target * 0.7)
        warnings.push("Ore lavorate sotto il 70% del target mensile");

      let statusLabel, statusClass, statusBorder, barColor;
      if (s.id === 8) {
        statusLabel = "In malattia";
        statusClass = "bg-red-500/20 text-red-700 dark:text-red-300";
        statusBorder = "border-red-500/40";
        barColor = "bg-red-500";
      } else if (overtime > 8) {
        statusLabel = "Straordinari";
        statusClass = "bg-orange-500/20 text-orange-700 dark:text-orange-300";
        statusBorder = "border-orange-500/40";
        barColor = "bg-orange-500";
      } else if (worked >= target * 0.9) {
        statusLabel = "Regolare";
        statusClass = "bg-emerald-500/20 text-emerald-700 dark:text-emerald-300";
        statusBorder = "border-slate-200 dark:border-slate-700";
        barColor = "bg-emerald-500";
      } else {
        statusLabel = "Ore mancanti";
        statusClass = "bg-yellow-500/20 text-yellow-700 dark:text-yellow-300";
        statusBorder = "border-yellow-500/40";
        barColor = "bg-yellow-500";
      }

      return {
        ...s,
        worked,
        target,
        remaining,
        overtime,
        warnings,
        statusLabel,
        statusClass,
        statusBorder,
        barColor,
      };
    }),
);
</script>
