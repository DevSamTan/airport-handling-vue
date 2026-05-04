<template>
  <div class="p-6 space-y-6">
    <!-- Filters -->
    <div class="flex flex-wrap gap-3 items-center">
      <select
        v-model="deptFilter"
        class="bg-slate-700 text-slate-200 text-xs px-3 py-2 rounded-lg border border-slate-600 focus:outline-none focus:border-blue-500"
      >
        <option value="">Tutti i reparti</option>
        <option v-for="d in departments" :key="d" :value="d">{{ d }}</option>
      </select>
      <select
        v-model="monthFilter"
        class="bg-slate-700 text-slate-200 text-xs px-3 py-2 rounded-lg border border-slate-600 focus:outline-none focus:border-blue-500"
      >
        <option value="4">Aprile 2026</option>
        <option value="3">Marzo 2026</option>
        <option value="2">Febbraio 2026</option>
      </select>
      <button
        class="flex items-center gap-2 px-4 py-2 text-xs bg-slate-700 hover:bg-slate-600 text-white rounded-lg border border-slate-600 transition-colors"
      >
        <Download :size="13" /> Esporta CSV
      </button>
      <button
        class="flex items-center gap-2 px-4 py-2 text-xs bg-slate-700 hover:bg-slate-600 text-white rounded-lg border border-slate-600 transition-colors"
      >
        <Printer :size="13" /> Stampa Report
      </button>
    </div>

    <!-- KPI row -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div
        v-for="kpi in kpis"
        :key="kpi.label"
        class="bg-slate-800 border border-slate-700 rounded-xl p-4"
      >
        <p class="text-xs text-slate-400">{{ kpi.label }}</p>
        <p class="text-xl font-bold text-white mt-1">{{ kpi.value }}</p>
      </div>
    </div>

    <!-- Hours per employee -->
    <div class="bg-slate-800 rounded-xl border border-slate-700 p-5">
      <h2 class="text-sm font-semibold text-white mb-4">
        Ore Mensili per Dipendente – Aprile 2026
      </h2>
      <div class="overflow-x-auto">
        <table class="w-full text-xs">
          <thead>
            <tr class="text-slate-400 border-b border-slate-700">
              <th class="text-left py-2.5 pr-4 font-medium">Dipendente</th>
              <th class="text-left py-2.5 pr-4 font-medium">Reparto</th>
              <th class="text-left py-2.5 pr-4 font-medium">Contratto</th>
              <th class="text-right py-2.5 pr-4 font-medium">Target</th>
              <th class="text-right py-2.5 pr-4 font-medium">Lavorate</th>
              <th class="text-right py-2.5 pr-4 font-medium">Straord.</th>
              <th class="text-right py-2.5 pr-4 font-medium">Assenze</th>
              <th class="text-left py-2.5 font-medium">Stato</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in staffSummary"
              :key="row.id"
              class="border-b border-slate-700/40 hover:bg-slate-700/20 transition-colors"
            >
              <td class="py-3 pr-4">
                <div class="flex items-center gap-2">
                  <div
                    class="w-7 h-7 rounded-full bg-blue-600 flex items-center justify-center text-[10px] font-bold text-white"
                  >
                    {{ row.initials }}
                  </div>
                  <span class="text-white font-medium">{{ row.name }}</span>
                </div>
              </td>
              <td class="py-3 pr-4 text-slate-400">{{ row.dept }}</td>
              <td class="py-3 pr-4 text-slate-400">{{ row.contract }}</td>
              <td class="py-3 pr-4 text-right text-slate-300 font-mono">
                {{ row.target }}h
              </td>
              <td class="py-3 pr-4 text-right font-mono text-white">
                {{ row.worked }}h
              </td>
              <td
                class="py-3 pr-4 text-right font-mono"
                :class="row.overtime > 0 ? 'text-orange-400' : 'text-slate-500'"
              >
                {{ row.overtime > 0 ? "+" + row.overtime : "—" }}
              </td>
              <td
                class="py-3 pr-4 text-right font-mono"
                :class="row.absences > 0 ? 'text-red-400' : 'text-slate-500'"
              >
                {{ row.absences > 0 ? row.absences + "gg" : "—" }}
              </td>
              <td class="py-3">
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
    </div>

    <!-- Absence register -->
    <div class="bg-slate-800 rounded-xl border border-slate-700 p-5">
      <h2 class="text-sm font-semibold text-white mb-4">Registro Assenze</h2>
      <div class="overflow-x-auto">
        <table class="w-full text-xs">
          <thead>
            <tr class="text-slate-400 border-b border-slate-700">
              <th class="text-left py-2 pr-4 font-medium">Dipendente</th>
              <th class="text-left py-2 pr-4 font-medium">Tipo</th>
              <th class="text-left py-2 pr-4 font-medium">Dal</th>
              <th class="text-left py-2 pr-4 font-medium">Al</th>
              <th class="text-left py-2 pr-4 font-medium">Giorni</th>
              <th class="text-left py-2 font-medium">Certificato</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="abs in absenceRegister"
              :key="abs.id"
              class="border-b border-slate-700/40 hover:bg-slate-700/20"
            >
              <td class="py-2.5 pr-4 text-white font-medium">
                {{ abs.employee }}
              </td>
              <td class="py-2.5 pr-4">
                <span
                  :class="[
                    'px-2 py-0.5 rounded text-[10px] font-medium',
                    abs.type === 'INF'
                      ? 'bg-rose-500/20 text-rose-300'
                      : 'bg-red-500/20 text-red-300',
                  ]"
                >
                  {{ abs.type === "INF" ? "Infortunio" : "Malattia" }}
                </span>
              </td>
              <td class="py-2.5 pr-4 text-slate-300 font-mono">
                {{ abs.startDate }}
              </td>
              <td class="py-2.5 pr-4 text-slate-300 font-mono">
                {{ abs.endDate }}
              </td>
              <td class="py-2.5 pr-4 text-slate-300">
                {{ daysBetween(abs.startDate, abs.endDate) }}
              </td>
              <td class="py-2.5">
                <div class="flex items-center gap-1.5">
                  <component
                    :is="
                      abs.status === 'cert_received' ? CheckCircle : Hourglass
                    "
                    :size="13"
                    :class="
                      abs.status === 'cert_received'
                        ? 'text-emerald-400'
                        : 'text-orange-400'
                    "
                  />
                  <span
                    :class="[
                      'text-[10px] font-medium',
                      abs.status === 'cert_received'
                        ? 'text-emerald-400'
                        : 'text-orange-400',
                    ]"
                  >
                    {{ abs.status === "cert_received" ? "Ricevuto" : "Atteso" }}
                  </span>
                </div>
              </td>
            </tr>
            <tr v-if="absenceRegister.length === 0">
              <td colspan="6" class="py-6 text-center text-slate-500">
                Nessuna assenza registrata
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<!-- ########## SCRIPT ########## -->

<script setup>
import { CheckCircle, Download, Hourglass, Printer } from "lucide-vue-next";
import { computed, ref } from "vue";
import { useRequestStore } from "../stores/useRequestStore";
import { DEPARTMENTS, useShiftStore } from "../stores/useShiftStore";

const shiftStore = useShiftStore();
const reqStore = useRequestStore();
const departments = DEPARTMENTS;
const deptFilter = ref("");
const monthFilter = ref("4");

const kpis = computed(() => [
  {
    label: "Totale ore lavorate",
    value: staffSummary.value.reduce((a, r) => a + r.worked, 0) + "h",
  },
  {
    label: "Totale straordinari",
    value: "+" + staffSummary.value.reduce((a, r) => a + r.overtime, 0) + "h",
  },
  {
    label: "Giorni di malattia",
    value:
      absenceRegister.value.reduce(
        (a, r) => a + daysBetween(r.startDate, r.endDate),
        0,
      ) + "gg",
  },
  { label: "Tasso di assenza", value: "4.2%" },
]);

const staffSummary = computed(() => {
  const month = Number(monthFilter.value);
  return shiftStore.staff
    .filter((s) => !deptFilter.value || s.dept === deptFilter.value)
    .map((s) => {
      const worked = shiftStore.monthlyHours(s.id, 2026, month);
      const target = s.monthlyTarget;
      const overtime = Math.max(0, worked - target);
      const absences = reqStore.sickLeaves.filter(
        (r) => r.employeeId === s.id,
      ).length;
      let statusLabel, statusClass;
      if (absences > 0) {
        statusLabel = "Assente";
        statusClass = "bg-red-500/20 text-red-300";
      } else if (overtime > 8) {
        statusLabel = "Straordinari";
        statusClass = "bg-orange-500/20 text-orange-300";
      } else if (worked < target - 8) {
        statusLabel = "Ore mancanti";
        statusClass = "bg-yellow-500/20 text-yellow-300";
      } else {
        statusLabel = "Regolare";
        statusClass = "bg-emerald-500/20 text-emerald-300";
      }
      return {
        ...s,
        worked,
        target,
        overtime,
        absences,
        statusLabel,
        statusClass,
      };
    });
});

const absenceRegister = computed(() =>
  reqStore.sickLeaves.filter(
    (r) =>
      !deptFilter.value ||
      shiftStore.staff.find((s) => s.id === r.employeeId)?.dept ===
        deptFilter.value,
  ),
);

function daysBetween(d1, d2) {
  if (!d1 || !d2) return 1;
  return Math.max(1, Math.round((new Date(d2) - new Date(d1)) / 86400000) + 1);
}
</script>
