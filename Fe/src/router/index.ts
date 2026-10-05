import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import StudentRegistryView from '@/views/student/StudentRegistryView.vue'
import FacilityManagementView from '@/views/facility/FacilityManagementView.vue'
import UserPortalView from '@/views/portal/UserPortalView.vue'
import RegistrationManagementView from '@/views/registration/RegistrationManagementView.vue'
import ContractManagementView from '@/views/contract/ContractManagementView.vue'
import BillingManagementView from '@/views/billing/BillingManagementView.vue'
import MyBillingView from '@/views/billing/MyBillingView.vue'
import VnPayReturnView from '@/views/billing/VnPayReturnView.vue'
import VerifyEmailView from '@/views/auth/VerifyEmailView.vue'
import ResetPasswordView from '@/views/auth/ResetPasswordView.vue'
import { homePathForRole, parseJwt } from '@/utils/jwt'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView, meta: { public: true } },
    { path: '/home', redirect: '/' },
    { path: '/login', component: LoginView, meta: { guestOnly: true } },
    { path: '/register', component: RegisterView, meta: { guestOnly: true } },
    { path: '/forgot-password', component: ForgotPasswordView, meta: { guestOnly: true } },
    { path: '/verify-email', component: VerifyEmailView, meta: { public: true } },
    { path: '/reset-password', component: ResetPasswordView, meta: { public: true } },
    { path: '/payment/vnpay-return', component: VnPayReturnView, meta: { public: true } },
    { path: '/portal', component: UserPortalView, meta: { requiresAuth: true, roles: ['USER'] } },
    { path: '/my-billing', component: MyBillingView, meta: { requiresAuth: true, roles: ['USER'] } },
    { path: '/dashboard', component: DashboardView, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] } },
    { path: '/students', component: StudentRegistryView, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] } },
    { path: '/facilities', component: FacilityManagementView, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] } },
    { path: '/registrations', component: RegistrationManagementView, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] } },
    { path: '/contracts', component: ContractManagementView, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] } },
    { path: '/billing', component: BillingManagementView, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] } },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const accessToken = localStorage.getItem('ktx.accessToken')
  const role = parseJwt(accessToken)?.role
  if (to.meta.requiresAuth && !accessToken) return '/login'
  if (to.meta.guestOnly && accessToken) return homePathForRole(role)
  const roles = to.meta.roles as string[] | undefined
  if (accessToken && roles && (!role || !roles.includes(role))) return homePathForRole(role)
  // public routes (home) are always accessible
})

export default router
