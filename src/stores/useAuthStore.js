import { defineStore } from "pinia";
import { computed, ref } from "vue";

const DEMO_USERS = [
  { id: 1, username: "admin",       password: "admin",    name: "Amministratore", initials: "AD", role: "Admin"       },
  { id: 2, username: "marco.rossi", password: "password", name: "Marco Rossi",    initials: "MR", role: "Dipendente", staffId: 1 },
  { id: 3, username: "hr",          password: "hr123",    name: "Ufficio HR",     initials: "HR", role: "HR / Viewer" },
  { id: 4, username: "planner",     password: "plan123",  name: "Pianificatore",  initials: "PL", role: "Planner"    },
];

// Which routes each role can visit
export const ROLE_ROUTES = {
  "Admin":       ["/", "/planning", "/employee", "/requests", "/hr", "/contract-hours"],
  "Planner":     ["/", "/planning", "/requests"],
  "Dipendente":  ["/employee"],
  "HR / Viewer": ["/hr", "/contract-hours"],
};

// Default landing page after login per role
export const ROLE_DEFAULT = {
  "Admin":       "/",
  "Planner":     "/",
  "Dipendente":  "/employee",
  "HR / Viewer": "/hr",
};

// Which nav items each role can see (used in App.vue)
export const NAV_ROLES = {
  "/":               ["Admin", "Planner"],
  "/planning":       ["Admin", "Planner"],
  "/employee":       ["Admin", "Dipendente"],
  "/requests":       ["Admin", "Planner"],
  "/hr":             ["Admin", "HR / Viewer"],
  "/contract-hours": ["Admin", "HR / Viewer"],
};

function loadSession() {
  try {
    const s = sessionStorage.getItem("airops_user");
    return s ? JSON.parse(s) : null;
  } catch { return null; }
}

export const useAuthStore = defineStore("auth", () => {
  const currentUser = ref(loadSession());

  const isLoggedIn    = computed(() => currentUser.value !== null);
  const userRole      = computed(() => currentUser.value?.role ?? null);
  const defaultRoute  = computed(() => ROLE_DEFAULT[userRole.value] ?? "/");
  const allowedRoutes = computed(() => ROLE_ROUTES[userRole.value] ?? []);

  function login(username, password) {
    const found = DEMO_USERS.find(
      (u) => u.username === username && u.password === password,
    );
    if (!found) return false;
    // eslint-disable-next-line no-unused-vars
    const { password: _, ...safe } = found;
    currentUser.value = safe;
    sessionStorage.setItem("airops_user", JSON.stringify(safe));
    return true;
  }

  function logout() {
    currentUser.value = null;
    sessionStorage.removeItem("airops_user");
  }

  function canAccess(path) {
    return allowedRoutes.value.includes(path);
  }

  return { currentUser, isLoggedIn, userRole, defaultRoute, allowedRoutes, login, logout, canAccess };
});
