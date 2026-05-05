<template>
  <div class="max-w-2xl mx-auto p-4 space-y-5">
    <!-- Today card -->
    <div
      class="bg-gradient-to-br from-blue-600 to-indigo-700 rounded-2xl p-6 text-white shadow-xl"
    >
      <p class="text-blue-100 text-sm">Turno di Oggi – {{ todayLabel }}</p>
      <template v-if="todayShifts.length">
        <h2 class="text-3xl font-bold mt-1">
          {{ SHIFT_TYPES[todayShifts[0]]?.hours ?? '—' }}
        </h2>
        <div class="mt-3 flex flex-wrap items-center gap-2">
          <span
            v-for="t in todayShifts"
            :key="t"
            class="bg-blue-500/50 px-3 py-1 rounded-full text-sm font-medium"
          >
            {{ SHIFT_TYPES[t]?.label ?? t }}
          </span>
          <span class="bg-white/20 px-3 py-1 rounded-full text-sm">
            {{ todayShifts.reduce((s, t) => s + (SHIFT_TYPES[t]?.duration ?? 0), 0) }}h
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
              {{ day.shifts.map((t) => SHIFT_TYPES[t]?.abbr ?? t).join("+") }}
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
      @click.self="closeSwapModal"
    >
      <div
        class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 w-full max-w-md shadow-2xl max-h-[90vh] flex flex-col"
      >
        <!-- Header -->
        <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between shrink-0">
          <div class="flex items-center gap-2 text-slate-900 dark:text-white">
            <ArrowLeftRight :size="18" class="text-blue-500 dark:text-blue-400" />
            <h3 class="text-sm font-semibold">Richiesta Cambio Turno</h3>
          </div>
          <button @click="closeSwapModal" class="text-slate-400 hover:text-slate-700 dark:hover:text-white">
            <X :size="20" />
          </button>
        </div>

        <!-- Body (scrollable) -->
        <div class="px-6 py-4 space-y-4 overflow-y-auto">

          <!-- 1. Date -->
          <div>
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1">Data turno da cambiare <span class="text-red-500">*</span></label>
            <input
              type="date"
              v-model="swapForm.date"
              :min="todayIso"
              :class="[
                'w-full text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border focus:outline-none transition-colors',
                swapForm.date && swapDateUserWorkingShifts.length === 0
                  ? 'bg-red-50 dark:bg-red-900/20 border-red-400 dark:border-red-500'
                  : 'bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 focus:border-blue-500'
              ]"
            />
            <!-- Past date warning -->
            <p v-if="swapDateIsPast" class="text-[11px] text-orange-600 dark:text-orange-400 mt-1 flex items-center gap-1">
              <AlertTriangle :size="11" /> La data selezionata è già passata
            </p>
          </div>

          <!-- 2. Your current shift on that date (auto-detected) -->
          <div v-if="swapForm.date">
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1">Il tuo turno in quella data</label>

            <!-- Has working shift(s) -->
            <div v-if="swapDateUserWorkingShifts.length" class="space-y-1.5">
              <button
                v-for="t in swapDateUserWorkingShifts"
                :key="t"
                @click="swapForm.fromShift = t"
                :class="[
                  'w-full flex items-center gap-3 px-3 py-2 rounded-lg border-2 text-left transition-all text-sm',
                  swapForm.fromShift === t
                    ? 'border-blue-500 bg-blue-500/10 dark:bg-blue-500/20'
                    : 'border-slate-200 dark:border-slate-600 hover:border-slate-300 dark:hover:border-slate-500 bg-slate-50 dark:bg-slate-700/40',
                ]"
              >
                <div :class="['w-2.5 h-2.5 rounded-full shrink-0', (SHIFT_COLORS[t] ?? '').split(' ')[0].replace('/20','')]"></div>
                <div class="flex-1">
                  <span class="font-semibold text-slate-900 dark:text-white">{{ SHIFT_TYPES[t]?.abbr ?? t }}</span>
                  <span class="text-slate-500 dark:text-slate-400 ml-2 text-xs">{{ SHIFT_TYPES[t]?.label }} · {{ SHIFT_TYPES[t]?.hours }}</span>
                </div>
                <div v-if="swapForm.fromShift === t" class="w-4 h-4 rounded-full bg-blue-500 flex items-center justify-center shrink-0">
                  <svg viewBox="0 0 12 12" class="w-2.5 h-2.5 text-white fill-current"><path d="M2 6l3 3 5-5"/></svg>
                </div>
              </button>
            </div>

            <!-- No working shift on that day -->
            <div v-else class="flex items-center gap-2 p-3 bg-red-500/10 border border-red-500/30 rounded-lg">
              <AlertTriangle :size="15" class="text-red-500 shrink-0" />
              <p class="text-xs text-red-700 dark:text-red-400">
                <span v-if="swapDateUserShifts.length">Hai {{ swapDateUserShifts.map(t => SHIFT_TYPES[t]?.abbr).join(', ') }} in questa data — non è cambiabile (solo riposo, malattia o infortunio).</span>
                <span v-else>Non hai turni assegnati in questa data.</span>
              </p>
            </div>
          </div>

          <!-- 3. Proposed shift -->
          <div v-if="swapForm.fromShift">
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1">Turno proposto <span class="text-red-500">*</span></label>
            <div class="grid grid-cols-3 gap-1.5">
              <button
                v-for="(t, k) in WORKING_SHIFT_TYPES"
                :key="k"
                @click="swapForm.toShift = k"
                :disabled="k === swapForm.fromShift"
                :class="[
                  'flex flex-col items-center py-2 px-1 rounded-lg border-2 text-center transition-all',
                  swapForm.toShift === k
                    ? 'border-blue-500 bg-blue-500/10'
                    : k === swapForm.fromShift
                      ? 'border-slate-100 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 opacity-30 cursor-not-allowed'
                      : 'border-slate-200 dark:border-slate-600 hover:border-slate-300 dark:hover:border-slate-500 bg-slate-50 dark:bg-slate-700/40 cursor-pointer',
                ]"
              >
                <span class="text-xs font-bold text-slate-900 dark:text-white">{{ t.abbr }}</span>
                <span class="text-[10px] text-slate-500 dark:text-slate-400 leading-tight">{{ t.hours }}</span>
              </button>
            </div>
          </div>

          <!-- 4. Colleague (optional) -->
          <div v-if="swapForm.fromShift">
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1">Collega coinvolto (opzionale)</label>
            <select
              v-model="swapForm.colleague"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-blue-500"
            >
              <option value="">— Nessuno (spostamento singolo)</option>
              <option v-for="s in colleagues" :key="s.id" :value="s.id">
                {{ s.name }} ({{ s.dept }})
              </option>
            </select>

            <!-- Colleague shift info -->
            <div v-if="swapColleague && swapForm.date" class="mt-2 p-2.5 bg-slate-50 dark:bg-slate-700/40 rounded-lg">
              <p class="text-[10px] text-slate-500 dark:text-slate-400 mb-1">Turno di {{ swapColleague.name }} in quella data:</p>
              <div v-if="swapDateColleagueWorkingShifts.length" class="flex flex-wrap gap-1">
                <span
                  v-for="t in swapDateColleagueWorkingShifts"
                  :key="t"
                  :class="['text-[10px] font-bold px-2 py-0.5 rounded', SHIFT_COLORS[t]]"
                >{{ SHIFT_TYPES[t]?.abbr ?? t }} · {{ SHIFT_TYPES[t]?.hours }}</span>
              </div>
              <p v-else class="text-[10px] text-slate-400 dark:text-slate-500 italic">Nessun turno lavorativo assegnato</p>
            </div>

            <!-- Colleague conflict warnings -->
            <div v-if="swapColleague && swapForm.date" class="mt-1.5 space-y-1">
              <div
                v-if="swapDateColleagueWorkingShifts.length === 0"
                class="flex items-center gap-2 p-2 bg-orange-500/10 border border-orange-500/20 rounded-lg"
              >
                <AlertTriangle :size="12" class="text-orange-500 shrink-0" />
                <p class="text-[11px] text-orange-700 dark:text-orange-400">{{ swapColleague.name }} non ha un turno cambiabile in questa data.</p>
              </div>
              <div
                v-if="colleagueHasVacation"
                class="flex items-center gap-2 p-2 bg-orange-500/10 border border-orange-500/20 rounded-lg"
              >
                <AlertTriangle :size="12" class="text-orange-500 shrink-0" />
                <p class="text-[11px] text-orange-700 dark:text-orange-400">{{ swapColleague.name }} ha ferie approvate in questa data.</p>
              </div>
            </div>
          </div>

          <!-- 5. Reason -->
          <div v-if="swapForm.fromShift">
            <label class="text-xs text-slate-600 dark:text-slate-400 block mb-1">Motivazione (opzionale)</label>
            <textarea
              v-model="swapForm.reason"
              rows="2"
              class="w-full bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 focus:outline-none focus:border-blue-500 resize-none"
              placeholder="Motivo della richiesta..."
            ></textarea>
          </div>

          <!-- 6. Preview card -->
          <div
            v-if="swapForm.fromShift && swapForm.toShift && swapForm.date && swapErrors.length === 0"
            class="p-3 bg-blue-500/10 border border-blue-500/30 rounded-xl"
          >
            <p class="text-[10px] text-blue-500 dark:text-blue-400 font-semibold mb-2 uppercase tracking-wide">Riepilogo richiesta</p>
            <div class="flex items-center gap-3 text-sm">
              <span :class="['px-2 py-1 rounded font-bold text-xs', SHIFT_COLORS[swapForm.fromShift] ?? 'bg-slate-200 text-slate-600']">
                {{ SHIFT_TYPES[swapForm.fromShift]?.abbr ?? swapForm.fromShift }}
              </span>
              <ArrowLeftRight :size="16" class="text-blue-400 shrink-0" />
              <span :class="['px-2 py-1 rounded font-bold text-xs', SHIFT_COLORS[swapForm.toShift] ?? 'bg-slate-200 text-slate-600']">
                {{ SHIFT_TYPES[swapForm.toShift]?.abbr ?? swapForm.toShift }}
              </span>
              <span class="text-xs text-slate-600 dark:text-slate-400">
                {{ new Date(swapForm.date).toLocaleDateString('it-IT', { weekday:'short', day:'numeric', month:'short' }) }}
                <span v-if="swapColleague"> · con {{ swapColleague.name }}</span>
              </span>
            </div>
          </div>

          <!-- 7. Errors (blocking) -->
          <div v-if="swapErrors.length" class="space-y-1">
            <div
              v-for="err in swapErrors"
              :key="err"
              class="flex items-center gap-2 p-2.5 bg-red-500/10 border border-red-500/30 rounded-lg"
            >
              <AlertTriangle :size="13" class="text-red-500 shrink-0" />
              <p class="text-xs text-red-700 dark:text-red-400">{{ err }}</p>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 border-t border-slate-100 dark:border-slate-700 flex gap-2 justify-end shrink-0">
          <button
            @click="closeSwapModal"
            class="px-4 py-2 text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors"
          >
            Annulla
          </button>
          <button
            @click="submitSwap"
            :disabled="!swapCanSubmit"
            class="px-4 py-2 text-sm bg-blue-600 hover:bg-blue-500 disabled:opacity-40 disabled:cursor-not-allowed text-white rounded-lg font-medium transition-colors flex items-center gap-2"
          >
            <ArrowLeftRight :size="15" />
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
import { computed, reactive, ref, watch } from "vue";
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

function getMonday(d) {
  const date = new Date(d);
  const day = date.getDay();
  const diff = (day === 0 ? -6 : 1 - day);
  date.setDate(date.getDate() + diff);
  date.setHours(0, 0, 0, 0);
  return date;
}
const currentWeekStart = ref(getMonday(today));
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
// Shift types that can be swapped (working shifts + ferie)
const SWAPPABLE = ['M', 'P', 'N', 'L', 'S6', 'F'];
const WORKING_SHIFT_TYPES = Object.fromEntries(
  Object.entries(SHIFT_TYPES).filter(([k]) => SWAPPABLE.includes(k))
);

const swapForm = reactive({
  date: "",
  fromShift: "",
  toShift: "",
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

// ── Swap validation ──────────────────────────────────────────────────────
// All shifts the current user has on the chosen swap date
const swapDateUserShifts = computed(() => {
  if (!swapForm.date) return [];
  return shiftStore.getShifts(CURRENT_STAFF_ID, new Date(swapForm.date));
});
// Only the swappable ones (working shifts + ferie, excludes R, MAL, INF, PER)
const swapDateUserWorkingShifts = computed(() =>
  swapDateUserShifts.value.filter(t => SWAPPABLE.includes(t))
);

const swapColleague = computed(() =>
  swapForm.colleague ? shiftStore.staff.find(s => s.id === Number(swapForm.colleague)) : null
);
const swapDateColleagueShifts = computed(() => {
  if (!swapForm.date || !swapColleague.value) return [];
  return shiftStore.getShifts(swapColleague.value.id, new Date(swapForm.date));
});
const swapDateColleagueWorkingShifts = computed(() =>
  swapDateColleagueShifts.value.filter(t => SWAPPABLE.includes(t))
);

const swapDateIsPast = computed(() =>
  !!swapForm.date && new Date(swapForm.date) < today
);
const colleagueHasVacation = computed(() =>
  !!swapColleague.value && !!swapForm.date &&
  reqStore.hasApprovedVacation(swapColleague.value.id, swapForm.date)
);
const swapAlreadyExists = computed(() =>
  reqStore.shiftSwaps.some(s =>
    s.requesterId === CURRENT_STAFF_ID &&
    s.date === swapForm.date &&
    s.status === 'pending'
  )
);

// Blocking errors
const swapErrors = computed(() => {
  const e = [];
  if (!swapForm.date) { e.push('Seleziona una data'); return e; }
  if (swapDateUserWorkingShifts.value.length === 0)
    e.push('Non hai turni cambiabili in questa data (riposo, malattia o infortunio non sono scambiabili)');
  if (!swapForm.fromShift) e.push('Seleziona quale turno vuoi cambiare');
  if (!swapForm.toShift)   e.push('Seleziona il turno proposto');
  if (swapForm.fromShift && swapForm.toShift && swapForm.fromShift === swapForm.toShift)
    e.push('Il turno proposto deve essere diverso da quello attuale');
  if (swapAlreadyExists.value)
    e.push('Hai già una richiesta di cambio in attesa per questa data');
  return e;
});
const swapCanSubmit = computed(() => swapErrors.value.length === 0);

// Auto-detect fromShift when date changes
watch(() => swapForm.date, (newDate) => {
  if (!newDate) { swapForm.fromShift = ''; swapForm.toShift = ''; return; }
  const working = shiftStore.getShifts(CURRENT_STAFF_ID, new Date(newDate))
    .filter(t => SWAPPABLE.includes(t));
  swapForm.fromShift = working[0] ?? '';
  swapForm.toShift = '';
});

// Auto-select first available toShift when fromShift changes
watch(() => swapForm.fromShift, (from) => {
  if (!from) { swapForm.toShift = ''; return; }
  const options = SWAPPABLE.filter(t => t !== from);
  if (!swapForm.toShift || swapForm.toShift === from) {
    swapForm.toShift = options[0] ?? '';
  }
});

function closeSwapModal() {
  swapModal.value = false;
  Object.assign(swapForm, { date: '', fromShift: '', toShift: '', colleague: '', reason: '' });
}

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
  if (!swapCanSubmit.value) return;
  const col = swapColleague.value;
  reqStore.addShiftSwap({
    requester: "Marco Rossi",
    requesterId: CURRENT_STAFF_ID,
    colleague: col?.name || null,
    colleagueId: col?.id || null,
    date: swapForm.date,
    fromShift: swapForm.fromShift,
    toShift: swapForm.toShift,
    reason: swapForm.reason,
  });
  closeSwapModal();
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
      sub: r.reason || `${Math.max(1, Math.round((new Date(r.endDate) - new Date(r.startDate)) / 86400000) + 1)}gg richiesti`,
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
