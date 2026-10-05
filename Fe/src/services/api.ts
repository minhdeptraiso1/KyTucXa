import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})
const authApi = axios.create({ baseURL: API_BASE_URL, headers: { 'Content-Type': 'application/json' } })
type RetriableRequest = InternalAxiosRequestConfig & { _retry?: boolean }
type RefreshResponse = { data: { accessToken: string; refreshToken: string } }
let refreshPromise: Promise<string> | null = null

function clearStoredSession() {
  localStorage.removeItem('ktx.accessToken')
  localStorage.removeItem('ktx.refreshToken')
  localStorage.removeItem('ktx.userFullName')
  localStorage.removeItem('ktx.userProfile')
}

function redirectToLogin() {
  clearStoredSession()
  if (!window.location.pathname.startsWith('/login')) {
    window.location.assign('/login?sessionExpired=1')
  }
}

async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('ktx.refreshToken')
  if (!refreshToken) throw new Error('Missing refresh token')
  const { data } = await authApi.post<RefreshResponse>('/auth/refresh', null, { params: { refreshToken } })
  localStorage.setItem('ktx.accessToken', data.data.accessToken)
  localStorage.setItem('ktx.refreshToken', data.data.refreshToken)
  return data.data.accessToken
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('ktx.accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const status = error.response?.status
    const request = error.config as RetriableRequest | undefined
    const isAuthRequest = request?.url?.includes('/auth/login') || request?.url?.includes('/auth/refresh')

    if (status === 401 && request && !request._retry && !isAuthRequest) {
      request._retry = true
      try {
        refreshPromise ??= refreshAccessToken().finally(() => { refreshPromise = null })
        const accessToken = await refreshPromise
        request.headers.Authorization = `Bearer ${accessToken}`
        return api(request)
      } catch {
        redirectToLogin()
      }
    } else if (status === 401 || (status === 403 && !localStorage.getItem('ktx.accessToken'))) {
      redirectToLogin()
    }
    return Promise.reject(error)
  },
)
