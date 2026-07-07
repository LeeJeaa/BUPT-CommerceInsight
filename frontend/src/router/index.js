import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import MainLayout from '../views/MainLayout.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import DashboardView from '../views/DashboardView.vue'
import UsersView from '../views/UsersView.vue'
import ImportView from '../views/ImportView.vue'
import ExportView from '../views/ExportView.vue'
import CustomerQueryView from '../views/CustomerQueryView.vue'
import OrderRevenueView from '../views/OrderRevenueView.vue'
import PartSupplierView from '../views/PartSupplierView.vue'
import TpchView from '../views/TpchView.vue'
import NewOrderView from '../views/NewOrderView.vue'
import PaymentView from '../views/PaymentView.vue'
import PerformanceView from '../views/PerformanceView.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/register', component: RegisterView, meta: { public: true } },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: DashboardView },
      { path: 'users', component: UsersView, meta: { admin: true } },
      { path: 'import', component: ImportView, meta: { admin: true } },
      { path: 'export', component: ExportView, meta: { admin: true } },
      { path: 'query/customers', component: CustomerQueryView },
      { path: 'query/order-revenue', component: OrderRevenueView },
      { path: 'query/part-supplier', component: PartSupplierView },
      { path: 'tpch', component: TpchView },
      { path: 'tpcc/new-order', component: NewOrderView },
      { path: 'tpcc/payment', component: PaymentView },
      { path: 'performance', component: PerformanceView }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    return true
  }
  if (!auth.token) {
    return '/login'
  }
  if (to.meta.admin && auth.role !== 'admin') {
    return '/dashboard'
  }
  return true
})

export default router
