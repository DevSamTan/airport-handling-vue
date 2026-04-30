<template>
  <div class="flex h-screen overflow-hidden bg-slate-900">
    <!-- Sidebar -->
    <aside
      :class="[
        'flex flex-col bg-slate-800 border-r border-slate-700 transition-all duration-300 shrink-0',
        appStore.sidebarOpen ? 'w-60' : 'w-16'
      ]"
    >
      <!-- Logo -->
      <div class="flex items-center gap-3 px-4 py-5 border-b border-slate-700">
        <div class="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white shrink-0">
          <Plane :size="16" />
        </div>
        <span v-if="appStore.sidebarOpen" class="font-bold text-white text-sm tracking-wide">AirOps</span>
      </div>

      <!-- Nav -->
      <nav class="flex-1 p-2 space-y-1 overflow-y-auto">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-700 transition-colors"
          active-class="bg-blue-600/20 text-blue-300 hover:bg-blue-600/30 hover:text-blue-200"
        >
          <component :is="item.icon" :size="18" class="shrink-0" />
          <span v-if="appStore.sidebarOpen" class="text-sm font-medium truncate">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- Role switcher -->
      <div class="p-3 border-t border-slate-700">
        <div v-if="appStore.sidebarOpen" class="mb-2">
          <label class="text-xs text-slate-500 mb-1 block">Ruolo attivo</label>
          <select
            v-model="appStore.currentRole"
            class="w-full bg-slate-700 text-slate-200 text-xs px-2 py-1.5 rounded border border-slate-600 focus:outline-none focus:border-blue-500"
          >
            <option v-for="r in roles" :key="r" :value="r">{{ r }}</option>
          </select>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-xs font-bold shrink-0">MR</div>
          <div v-if="appStore.sidebarOpen" class="overflow-hidden">
            <p class="text-sm font-medium text-white truncate">Marco Rossi</p>
            <p class="text-xs text-slate-400 truncate">{{ appStore.currentRole }}</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Topbar -->
      <header class="h-14 bg-slate-800 border-b border-slate-700 flex items-center justify-between px-4 shrink-0">
        <div class="flex items-center gap-3">
          <button @click="appStore.toggleSidebar" class="text-slate-400 hover:text-white transition-colors p-1 rounded">
            <Menu :size="20" />
          </button>
          <h1 class="text-sm font-semibold text-white">{{ currentPageTitle }}</h1>
        </div>
        <div class="flex items-center gap-2">
          <!-- Notification bell -->
          <div class="relative">
            <button
              @click="showNotifications = !showNotifications"
              class="relative p-2 text-slate-400 hover:text-white transition-colors rounded-lg hover:bg-slate-700"
            >
              <Bell :size="20" />
              <span
                v-if="appStore.unreadCount > 0"
                class="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center font-bold"
              >{{ appStore.unreadCount }}</span>
            </button>

            <!-- Dropdown -->
            <div
              v-if="showNotifications"
              class="absolute right-0 top-12 w-80 bg-slate-800 border border-slate-700 rounded-xl shadow-2xl z-50"
            >
              <div class="flex items-center justify-between px-4 py-3 border-b border-slate-700">
                <span class="text-sm font-semibold text-white">Notifiche</span>
                <button @click="appStore.markAllRead(); showNotifications=false" class="text-xs text-blue-400 hover:text-blue-300">Segna tutte lette</button>
              </div>
              <div class="max-h-72 overflow-y-auto">
                <div
                  v-for="n in appStore.notifications"
                  :key="n.id"
                  :class="['px-4 py-3 border-b border-slate-700/50 hover:bg-slate-700/50 transition-colors', !n.read && 'bg-blue-500/5']"
                >
                  <div class="flex items-start gap-2">
                    <span class="shrink-0 mt-0.5" :class="notifColor(n.type)">
                      <component :is="notifIcon(n.type)" :size="15" />
                    </span>
                    <div class="flex-1 min-w-0">
                      <p class="text-xs text-slate-200 leading-snug">{{ n.text }}</p>
                      <p class="text-xs text-slate-500 mt-0.5">{{ n.time }}</p>
                    </div>
                    <div v-if="!n.read" class="w-1.5 h-1.5 rounded-full bg-blue-500 mt-1.5 shrink-0"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="text-xs text-slate-400 hidden sm:block">
            {{ today }}
          </div>
        </div>
      </header>

      <!-- Page content -->
      <main class="flex-1 overflow-y-auto">
        <router-view />
      </main>
    </div>

    <!-- Notification overlay close -->
    <div v-if="showNotifications" @click="showNotifications=false" class="fixed inset-0 z-40"></div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Plane, Menu, Bell,
  LayoutDashboard, Calendar, User, ArrowLeftRight, ClipboardList, Clock,
  RefreshCw, Thermometer, AlertTriangle, Info,
} from 'lucide-vue-next'
import { useAppStore, ROLES } from './stores/useAppStore'

const appStore = useAppStore()
const route = useRoute()
const showNotifications = ref(false)
const roles = Object.values(ROLES)

const navItems = [
  { to: '/',               icon: LayoutDashboard, label: 'Dashboard'        },
  { to: '/planning',       icon: Calendar,        label: 'Pianificazione'   },
  { to: '/employee',       icon: User,            label: 'Area Dipendente'  },
  { to: '/requests',       icon: ArrowLeftRight,  label: 'Richieste'        },
  { to: '/hr',             icon: ClipboardList,   label: 'Report HR'        },
  { to: '/contract-hours', icon: Clock,           label: 'Ore Contrattuali' },
]

const currentPageTitle = computed(() => {
  const item = navItems.find(n => n.to === route.path)
  return item ? item.label : 'AirOps'
})

const today = computed(() =>
  new Date().toLocaleDateString('it-IT', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
)

const NOTIF_ICONS = { swap: RefreshCw, sick: Thermometer, warning: AlertTriangle, info: Info }
const NOTIF_COLORS = { swap: 'text-blue-400', sick: 'text-red-400', warning: 'text-yellow-400', info: 'text-slate-400' }

function notifIcon(type)  { return NOTIF_ICONS[type]  ?? Bell }
function notifColor(type) { return NOTIF_COLORS[type] ?? 'text-slate-400' }
</script>
