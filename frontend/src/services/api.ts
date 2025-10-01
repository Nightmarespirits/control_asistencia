import axios, { type AxiosError, type AxiosResponse } from 'axios'
import { globalNotifications } from '@/composables/useNotifications'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor for auth token and logging
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // Log request in development
    if (import.meta.env?.DEV) {
      console.log(`Interceptor /services/api.ts ${config.method?.toUpperCase()} ${config.url}`, config.data)
    }

    return config
  },
  (error) => {
    console.error('❌ Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor for error handling, token refresh, and notifications
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log successful responses in development
    if (import.meta.env?.DEV) {
      console.log(`::::DEBUGGING RESPONSE ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data)
    }
    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as any

    // Handle different error scenarios
    if (error.response) {
      const { status, data } = error.response
      const errorMessage = (data as any)?.mensaje || (data as any)?.message || 'Error desconocido'

      console.error(`::: DEBUGGING ERROR ${status} ${originalRequest?.method?.toUpperCase()} ${originalRequest?.url}:`, errorMessage)

      // Handle 401 - Unauthorized (token refresh)
      if (status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        try {
          // Try to refresh token
          const refreshToken = localStorage.getItem('refresh_token')
          if (refreshToken) {
            const response = await axios.post('/api/auth/refresh', {
              refreshToken
            })

            const newToken = response.data.token
            localStorage.setItem('auth_token', newToken)

            // Retry original request with new token
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return api(originalRequest)
          }
        } catch (refreshError) {
          // Refresh failed, clear tokens and redirect to login
          localStorage.removeItem('auth_token')
          localStorage.removeItem('refresh_token')
          localStorage.removeItem('user')

          globalNotifications.showError('Sesión expirada. Por favor, inicia sesión nuevamente.')

          // Import router dynamically to avoid circular dependency
          const { default: router } = await import('@/router')
          router.push('/login')
          return Promise.reject(error)
        }
      }

      // Handle specific error codes with notifications
      switch (status) {
        case 400:
          // Don't show notification for validation errors in forms (handled by components)
          if (!originalRequest.url?.includes('/marcar')) {
            globalNotifications.showError(`Error de validación: ${errorMessage}`)
          }
          break
        case 403:
          globalNotifications.showError('No tienes permisos para realizar esta acción')
          break
        case 404:
          // Don't show notification for empleado not found in marcacion (handled by component)
          if (!originalRequest.url?.includes('/marcar')) {
            globalNotifications.showError('Recurso no encontrado')
          }
          break
        case 409:
          // Don't show notification for duplicate marcacion (handled by component)
          if (!originalRequest.url?.includes('/marcar')) {
            globalNotifications.showError(`Conflicto: ${errorMessage}`)
          }
          break
        case 422:
          globalNotifications.showError(`Error de procesamiento: ${errorMessage}`)
          break
        case 500:
          globalNotifications.showError('Error interno del servidor. Por favor, intenta más tarde.')
          break
        default:
          // Only show generic error for unexpected status codes
          if (status >= 500) {
            globalNotifications.showError('Error del servidor. Por favor, intenta más tarde.')
          }
      }
    } else if (error.request) {
      // Network error
      console.error('❌ Network error:', error.message)
      globalNotifications.showError('Error de conexión. Verifica tu conexión a internet.')
    } else {
      // Other error
      console.error('❌ Error:', error.message)
      globalNotifications.showError('Error inesperado. Por favor, intenta nuevamente.')
    }

    return Promise.reject(error)
  }
)

export default api