<template>
  <div class="p-6 space-y-6">
    <!-- Tabs -->
    <div class="flex gap-1 bg-slate-100 dark:bg-slate-800 p-1 rounded-xl border border-slate-200 dark:border-slate-700 w-fit">
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

    <!-- Shift swaps -->
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
          class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4 hover:border-slate-300 dark:hover:border-slate-600 transition-colors"
        >
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

          <div class="mt-3 grid grid-cols-3 gap-3 text-xs">
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Data</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ req.date }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Da → A</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ req.fromShift }} → {{ req.toShift }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700/40 rounded-lg p-2.5">
              <p class="text-slate-500 dark:text-slate-400 mb-0.5">Collega</p>
              <p class="font-medium text-slate-900 dark:text-white">{{ req.colleague || '—' }}</p>
            </div>
          </div>

          <div v-if="req.reason" class="mt-2 text-xs text-slate-500 dark:text-slate-400 italic">
            "{{ req.reason }}"
          </div>

          <div v-if="req.status === 'pending'" class="mt-3 flex gap-2">
            <button
              @click="reqStore.rejectSwap(req.id)"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-red-600 dark:text-red-400 border border-red-500/40 hover:bg-red-500/10 rounded-lg transition-colors"
            >
              <XCircle :size="13" /> Rifiuta
            </button>
            <button
              @click="reqStore.approveSwap(req.id)"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-emerald-600 dark:text-emerald-400 border border-emerald-500/40 hover:bg-emerald-500/10 rounded-lg transition-colors"
            >
              <CheckCircle :size="13" /> Approva
            </button>
          </div>
        </div>

        <p v-if="filteredSwaps.length === 0" class="text-slate-400 dark:text-slate-500 text-sm text-center py-8">Nessuna richiesta trovata</p>
      </div>
    </div>

    <!-- Sick leaves -->
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
        <div
          v-for="sick in filteredSick"
          :key="sick.id"
          class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4"
        >
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
            <span :class="['text-xs font-medium px-2 py-0.5 rounded-full shrink-0', sick.status === 'cert_pending' ? 'bg-orange-500/20 text-orange-600 dark:text-orange-300' : 'bg-blue-500/20 text-blue-600 dark:text-blue-300']">
              {{ sick.status === 'cert_pending' ? 'Cert. atteso' : 'Cert. ricevuto' }}
            </span>
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

          <div v-if="sick.status === 'cert_pending'" class="mt-3">
            <button
              @click="reqStore.updateSickStatus(sick.id, 'cert_received')"
              class="flex items-center gap-1.5 px-3 py-1.5 text-xs text-blue-600 dark:text-blue-400 border border-blue-500/40 hover:bg-blue-500/10 rounded-lg transition-colors"
            >
              <CheckCircle :size="13" /> Segna certificato ricevuto
            </button>
          </div>
        </div>

        <p v-if="filteredSick.length === 0" class="text-slate-400 dark:text-slate-500 text-sm text-center py-8">Nessuna assenza trovata</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { XCircle, CheckCircle, Thermometer, Bandage } from 'lucide-vue-next'
import { useRequestStore } from '../stores/useRequestStore'

const reqStore = useRequestStore()
const activeTab = ref('swaps')
const swapFilter = ref('')
const sickFilter = ref('')

const tabs = computed(() => [
  { key: 'swaps', label: 'Cambi Turno',          badge: reqStore.pendingSwaps.length || null },
  { key: 'sick',  label: 'Malattie / Infortuni', badge: reqStore.pendingSick.length  || null },
])

const filteredSwaps = computed(() =>
  swapFilter.value
    ? reqStore.shiftSwaps.filter(r => r.status === swapFilter.value)
    : reqStore.shiftSwaps
)

const filteredSick = computed(() =>
  sickFilter.value
    ? reqStore.sickLeaves.filter(r => r.status === sickFilter.value)
    : reqStore.sickLeaves
)

function initials(name) {
  return name?.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || '?'
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
