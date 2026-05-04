import { defineStore } from "pinia";
import { ref, watch } from "vue";

export const useThemeStore = defineStore("theme", () => {
  // Inizializza il tema dal localStorage o dalle preferenze di sistema
  const getInitialTheme = () => {
    try {
      const saved = localStorage.getItem("theme");
      if (saved) return saved === "dark";
      return window.matchMedia("(prefers-color-scheme: dark)").matches;
    } catch {
      return false;
    }
  };

  const isDark = ref(getInitialTheme());

  // Osserva i cambiamenti e aggiorna il DOM e localStorage
  watch(
    isDark,
    (newVal) => {
      try {
        localStorage.setItem("theme", newVal ? "dark" : "light");
      } catch {
        // Ignored
      }
      if (newVal) {
        document.documentElement.classList.add("dark");
      } else {
        document.documentElement.classList.remove("dark");
      }
    },
    { immediate: true },
  );

  const toggleTheme = () => {
    isDark.value = !isDark.value;
  };

  return {
    isDark,
    toggleTheme,
  };
});
