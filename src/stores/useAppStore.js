import { defineStore } from "pinia";
import { computed, ref } from "vue";

export const ROLES = {
  ADMIN: "Admin",
  PLANNER: "Planner",
  EMPLOYEE: "Dipendente",
  HR: "HR / Viewer",
};

export const useAppStore = defineStore("app", () => {
  const currentRole = ref(ROLES.PLANNER);
  const sidebarOpen = ref(true);

  const notifications = ref([
    {
      id: 1,
      type: "swap",
      text: "Marco Rossi ha richiesto un cambio turno",
      time: "10 min fa",
      read: false,
    },
    {
      id: 2,
      type: "sick",
      text: "Elena Costa ha comunicato malattia",
      time: "1 ora fa",
      read: false,
    },
    {
      id: 3,
      type: "warning",
      text: "Scopertura turno notte venerdì – Rampa",
      time: "2 ore fa",
      read: false,
    },
    {
      id: 4,
      type: "info",
      text: "Luca Romano: certificato malattia ricevuto",
      time: "Ieri",
      read: true,
    },
    {
      id: 5,
      type: "warning",
      text: "Giuseppe Verdi supera 50h settimanali",
      time: "Ieri",
      read: true,
    },
  ]);

  const unreadCount = computed(
    () => notifications.value.filter((n) => !n.read).length,
  );

  function markAllRead() {
    notifications.value.forEach((n) => (n.read = true));
  }

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value;
  }

  return {
    currentRole,
    sidebarOpen,
    notifications,
    unreadCount,
    markAllRead,
    toggleSidebar,
  };
});
