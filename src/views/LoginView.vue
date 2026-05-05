<template>
  <div
    class="min-h-screen flex items-center justify-center bg-slate-100 dark:bg-slate-900 p-4"
  >
    <div class="w-full max-w-sm">
      <!-- Card -->
      <div
        class="bg-white dark:bg-slate-800 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-700 overflow-hidden"
      >
        <!-- Header -->
        <div class="bg-blue-600 px-8 py-8 text-center">
          <div
            class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center mx-auto mb-4"
          >
            <Plane :size="28" class="text-white" />
          </div>
          <h1 class="text-xl font-bold text-white tracking-wide">
            Nome Azienda
          </h1>
          <p class="text-blue-200 text-sm mt-1">Gestione Turni & Personale</p>
        </div>

        <!-- Form -->
        <div class="px-8 py-7">
          <h2
            class="text-base font-semibold text-slate-900 dark:text-white mb-6 text-center"
          >
            Accedi al tuo account
          </h2>

          <form @submit.prevent="handleLogin" class="space-y-4">
            <!-- Username -->
            <div>
              <label
                class="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1.5"
                >Nome utente</label
              >
              <div class="relative">
                <User
                  :size="15"
                  class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  v-model="form.username"
                  type="text"
                  placeholder="es. admin"
                  autocomplete="username"
                  class="w-full pl-9 pr-3 py-2.5 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                />
              </div>
            </div>

            <!-- Password -->
            <div>
              <label
                class="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1.5"
                >Password</label
              >
              <div class="relative">
                <Lock
                  :size="15"
                  class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  v-model="form.password"
                  :type="showPwd ? 'text' : 'password'"
                  placeholder="••••••••"
                  autocomplete="current-password"
                  class="w-full pl-9 pr-10 py-2.5 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 text-slate-900 dark:text-white text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                />
                <button
                  type="button"
                  @click="showPwd = !showPwd"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition"
                >
                  <EyeOff v-if="showPwd" :size="15" />
                  <Eye v-else :size="15" />
                </button>
              </div>
            </div>

            <!-- Error -->
            <div
              v-if="error"
              class="flex items-center gap-2 bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/30 rounded-lg px-3 py-2.5"
            >
              <AlertCircle :size="14" class="text-red-500 shrink-0" />
              <span class="text-xs text-red-600 dark:text-red-400">{{
                error
              }}</span>
            </div>

            <!-- Submit -->
            <button
              type="submit"
              :disabled="loading"
              class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-semibold text-sm py-2.5 rounded-lg transition-colors mt-2"
            >
              <span v-if="!loading">Accedi</span>
              <span v-else class="flex items-center justify-center gap-2">
                <span
                  class="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin"
                ></span>
                Accesso in corso…
              </span>
            </button>
          </form>
        </div>
      </div>

      <!-- Demo credentials hint -->
      <div
        class="mt-4 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4"
      >
        <p
          class="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-2 uppercase tracking-wide"
        >
          Credenziali demo
        </p>
        <div class="space-y-1.5">
          <button
            v-for="hint in hints"
            :key="hint.user"
            type="button"
            @click="fillDemo(hint)"
            class="w-full flex items-center justify-between px-3 py-2 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors text-left group"
          >
            <div class="flex items-center gap-2.5">
              <div
                :class="[
                  'w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold text-white shrink-0',
                  hint.color,
                ]"
              >
                {{ hint.initials }}
              </div>
              <div>
                <span
                  class="text-xs font-medium text-slate-800 dark:text-slate-200"
                  >{{ hint.label }}</span
                >
                <span class="text-xs text-slate-400 ml-1.5">{{
                  hint.user
                }}</span>
              </div>
            </div>
            <span
              class="text-[10px] text-blue-500 group-hover:underline opacity-0 group-hover:opacity-100 transition"
              >Usa →</span
            >
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { AlertCircle, Eye, EyeOff, Lock, Plane, User } from "lucide-vue-next";
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/useAuthStore";

const authStore = useAuthStore();
const router = useRouter();

const form = reactive({ username: "", password: "" });
const error = ref("");
const loading = ref(false);
const showPwd = ref(false);

const hints = [
  {
    label: "Admin",
    user: "admin",
    pwd: "admin",
    initials: "AD",
    color: "bg-blue-600",
  },
  {
    label: "Dipendente",
    user: "marco.rossi",
    pwd: "password",
    initials: "MR",
    color: "bg-emerald-600",
  },
  {
    label: "HR / Viewer",
    user: "hr",
    pwd: "hr123",
    initials: "HR",
    color: "bg-purple-600",
  },
  {
    label: "Planner",
    user: "planner",
    pwd: "plan123",
    initials: "PL",
    color: "bg-orange-500",
  },
];

function fillDemo(hint) {
  form.username = hint.user;
  form.password = hint.pwd;
  error.value = "";
}

async function handleLogin() {
  error.value = "";
  if (!form.username || !form.password) {
    error.value = "Inserisci nome utente e password.";
    return;
  }
  loading.value = true;
  await new Promise((r) => setTimeout(r, 400));
  const ok = authStore.login(form.username, form.password);
  loading.value = false;
  if (!ok) {
    error.value = "Credenziali non valide. Riprova.";
    return;
  }
  router.push(authStore.defaultRoute);
}
</script>
