import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const MainLayout = () => import('../views/MainLayout.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const UsersView = () => import('../views/UsersView.vue')
const ImportView = () => import('../views/ImportView.vue')
const ExportView = () => import('../views/ExportView.vue')
const CustomerQueryView = () => import('../views/CustomerQueryView.vue')
const OrderRevenueView = () => import('../views/OrderRevenueView.vue')
const PartSupplierView = () => import('../views/PartSupplierView.vue')
const TpchView = () => import('../views/TpchView.vue')
const NewOrderView = () => import('../views/NewOrderView.vue')
const PaymentView = () => import('../views/PaymentView.vue')
const PerformanceView = () => import('../views/PerformanceView.vue')

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
