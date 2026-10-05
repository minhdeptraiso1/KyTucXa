import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '@/services/api'
import { homePathForRole, parseJwt } from '@/utils/jwt'

type LoginResponse = {
  accessToken: string
  refreshToken: string
}

type MessageResponse = { message: string }
type CurrentUser = {
  id: string
  username: string
  email: string
  status: string
  role: 'ADMIN' | 'STAFF' | 'USER'
}
type CurrentProfile = {
  userId: string
  fullName: string
  studentCode?: string
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('ktx.accessToken'))
  const refreshToken = ref(localStorage.getItem('ktx.refreshToken'))
  const loading = ref(false)
  const error = ref('')
  const currentUser = ref<CurrentUser | null>(null)
  
  let initialProfile: CurrentProfile | null = null
  try {
    const raw = localStorage.getItem('ktx.userProfile')
    if (raw) initialProfile = JSON.parse(raw)
  } catch {
    // Ignore storage parse error
  }
  const profile = ref<CurrentProfile | null>(initialProfile)

  const isAuthenticated = computed(() => Boolean(accessToken.value))
  const claims = computed(() => parseJwt(accessToken.value))
  const role = computed(() => claims.value?.role || null)
  const permissions = computed(() => claims.value?.permissions || [])
  const homePath = computed(() => homePathForRole(role.value || undefined))

  const displayName = computed(() => {
    if (profile.value?.fullName?.trim()) return profile.value.fullName.trim()
    if (claims.value?.fullName?.trim()) return claims.value.fullName.trim()
    const storedName = localStorage.getItem('ktx.userFullName')?.trim()
    if (storedName) return storedName
    const uname = currentUser.value?.username || claims.value?.username
    if (uname && !/^\d+$/.test(uname)) return uname
    if (profile.value?.studentCode || uname) {
      return `Sinh viên ${profile.value?.studentCode || uname}`
    }
    return 'Người dùng KTX'
  })

  const studentCode = computed(() => {
    return profile.value?.studentCode || currentUser.value?.username || claims.value?.username || ''
  })

  const roleLabel = computed(() => ({ ADMIN: 'Quản trị viên', STAFF: 'Nhân viên', USER: 'Sinh viên nội trú' } as Record<string, string>)[role.value || ''] || '')

  async function loadSession() {
    if (!accessToken.value) return
    const [userResult, profileResult] = await Promise.allSettled([
      api.get<{ data: CurrentUser }>('/users/me'),
      api.get<{ data: CurrentProfile }>('/users/me/profile'),
    ])
    if (userResult.status === 'fulfilled') {
      currentUser.value = userResult.value.data.data
    }
    if (profileResult.status === 'fulfilled') {
      profile.value = profileResult.value.data.data
      if (profile.value?.fullName) {
        localStorage.setItem('ktx.userFullName', profile.value.fullName)
        localStorage.setItem('ktx.userProfile', JSON.stringify(profile.value))
      }
    }
  }

  async function login(email: string, password: string) {
    loading.value = true
    error.value = ''
    try {
      const { data } = await api.post<{ data: LoginResponse }>('/auth/login', { email, password })
      accessToken.value = data.data.accessToken
      refreshToken.value = data.data.refreshToken
      localStorage.setItem('ktx.accessToken', accessToken.value)
      localStorage.setItem('ktx.refreshToken', refreshToken.value)
      await loadSession().catch(() => undefined)
    } catch (cause: unknown) {
      const responseMessage = (cause as { response?: { data?: { error?: { message?: string } } } })
        .response?.data?.error?.message
      error.value = responseMessage || 'Không thể đăng nhập. Kiểm tra API hoặc thông tin tài khoản.'
      throw cause
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      if (accessToken.value && refreshToken.value) {
        await api.post('/auth/logout', null, { params: { refreshToken: refreshToken.value } })
      }
    } finally {
      accessToken.value = null
      refreshToken.value = null
      currentUser.value = null
      profile.value = null
      localStorage.removeItem('ktx.accessToken')
      localStorage.removeItem('ktx.refreshToken')
      localStorage.removeItem('ktx.userFullName')
      localStorage.removeItem('ktx.userProfile')
    }
  }

  async function register(payload: {
    username?: string
    email: string
    password: string
    confirmPassword?: string
    fullName: string
    studentCode: string
    phone: string
  }) {
    if (payload.fullName) {
      localStorage.setItem('ktx.userFullName', payload.fullName.trim())
    }
    const { data } = await api.post<{ data: MessageResponse }>('/auth/register', payload)
    return data.data
  }

  async function forgotPassword(email: string) {
    const { data } = await api.post<{ data: MessageResponse }>('/auth/forgot-password', { email })
    return data.data
  }

  async function verifyEmail(token: string) {
    const { data } = await api.post<{ data: MessageResponse }>('/auth/verify-email', null, { params: { token } })
    return data.data
  }

  async function resetPassword(token: string, newPassword: string) {
    const { data } = await api.post<{ data: MessageResponse }>('/auth/reset-password', { token, newPassword })
    return data.data
  }

  return {
    accessToken, refreshToken, loading, error, currentUser, profile,
    isAuthenticated, claims, role, permissions, homePath, displayName, studentCode, roleLabel,
    login, logout, loadSession, register, forgotPassword, verifyEmail, resetPassword,
  }
})
