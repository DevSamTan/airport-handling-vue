import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Planning from '../views/Planning.vue'
import Employee from '../views/Employee.vue'
import Requests from '../views/RequestsView.vue'
import HR from '../views/HR.vue'
import ContractHours from '../views/ContractHours.vue'

const routes = [
  { path: '/',               component: Dashboard     },
  { path: '/planning',       component: Planning      },
  { path: '/employee',       component: Employee      },
  { path: '/requests',       component: Requests      },
  { path: '/hr',             component: HR            },
  { path: '/contract-hours', component: ContractHours },
]

export default createRouter({
  history: createWebHistory(),
  routes
})
