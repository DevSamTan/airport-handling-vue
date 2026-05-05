import { createRouter, createWebHistory } from 'vue-router'
import Dashboard     from '../views/Dashboard.vue'
import Planning      from '../views/Planning.vue'
import Employee      from '../views/Employee.vue'
import Requests      from '../views/RequestsView.vue'
import HR            from '../views/HR.vue'
import ContractHours from '../views/ContractHours.vue'
import LoginView     from '../views/LoginView.vue'
import { useAuthStore } from '../stores/useAuthStore'

const routes = [
  { path: '/login',          component: LoginView,     meta: { public: true } },
  { path: '/',               component: Dashboard     },
  { path: '/planning',       component: Planning      },
  { path: '/employee',       component: Employee      },
  { path: '/requests',       component: Requests      },
  { path: '/hr',             component: HR            },
  { path: '/contract-hours', component: ContractHours },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Guard runs after Pinia is initialized, so useAuthStore() is safe inside the callback
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (!auth.isLoggedIn && !to.meta.public) {
    return '/login'
  }
  if (auth.isLoggedIn && to.path === '/login') {
    return auth.defaultRoute
  }
  if (auth.isLoggedIn && !to.meta.public && !auth.canAccess(to.path)) {
    return auth.defaultRoute
  }
})

export default router
