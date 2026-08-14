import axios from 'axios'

/**
 * Base URL is relative — Vite dev server proxies /api to http://localhost:8080
 * (see vite.config.ts). This avoids CORS entirely since the backend has no
 * CORS configuration.
 */
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor — attach JWT if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor — catch 401s and redirect to /login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

/**
 * HTTP status names that carry no useful detail on their own — prefer the
 * backend's `message` field over these.
 */
const GENERIC_ERROR_NAMES = new Set([
  'Bad Request',
  'Unauthorized',
  'Forbidden',
  'Not Found',
  'Method Not Allowed',
  'Conflict',
  'Internal Server Error',
  'Service Unavailable',
  'Gateway Timeout',
])

/**
 * Best-effort extraction of a human-readable message from the backend's
 * GlobalExceptionHandler shape: { status, error, details, message }.
 */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as Record<string, unknown> | undefined
    if (data && typeof data === 'object') {
      if (
        typeof data.error === 'string' &&
        data.error !== 'Validation Failed' &&
        !GENERIC_ERROR_NAMES.has(data.error)
      ) {
        // A specific business error (e.g. "Booking not found with ID: 5")
        return data.error
      }
      const details = data.details
      if (details && typeof details === 'object') {
        const first = Object.values(details as Record<string, unknown>)[0]
        if (typeof first === 'string') return first
      }
      if (typeof data.message === 'string') return data.message
    }
    if (error.code === 'ECONNABORTED') return 'Request timed out. Is the backend running?'
    return error.message || 'Something went wrong'
  }
  if (error instanceof Error) return error.message
  return 'Something went wrong'
}

export default api
