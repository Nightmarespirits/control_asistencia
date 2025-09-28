import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

interface User {
  id: number
  username: string
  email?: string
}

interface LoginCredentials {
  username: string
  password: string
}

interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  username: string
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refresh_token'))
  const user = ref<User | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const currentUser = computed(() => user.value)

  // Actions
  const login = async (credentials: LoginCredentials): Promise<void> => {
    try {
      isLoading.value = true
      error.value = null

      const response = await api.post<LoginResponse>('/auth/login', credentials)
      const { accessToken, refreshToken: refToken, tokenType, expiresIn, username } = response.data

      // Map username to user object structure
      const userData: User = {
        id: 0, // We don't have user ID from backend, using 0 as placeholder
        username: username
      }

      // Store tokens and user data
      token.value = accessToken
      refreshToken.value = refToken
      user.value = userData

      // Persist to localStorage
      localStorage.setItem('auth_token', accessToken)
      localStorage.setItem('refresh_token', refToken)
      localStorage.setItem('user', JSON.stringify(userData))

      // Store additional token info for potential future use
      localStorage.setItem('token_type', tokenType)
      localStorage.setItem('expires_in', expiresIn.toString())

    } catch (err: any) {
      error.value = err.response?.data?.message || 'Error de autenticación'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const logout = async (): Promise<void> => {
    try {
      // Clear state
      token.value = null
      refreshToken.value = null
      user.value = null
      error.value = null

      // Clear localStorage
      localStorage.removeItem('auth_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('user')
      localStorage.removeItem('token_type')
      localStorage.removeItem('expires_in')

    } catch (err) {
      console.error('Error during logout:', err)
    }
  }

  const refreshAuthToken = async (): Promise<void> => {
    try {
      if (!refreshToken.value) {
        throw new Error('No refresh token available')
      }

      const response = await api.post<LoginResponse>('/auth/refresh', {
        refreshToken: refreshToken.value
      })

      const { accessToken, tokenType, expiresIn } = response.data
      token.value = accessToken
      localStorage.setItem('auth_token', accessToken)

      // Update additional token info
      localStorage.setItem('token_type', tokenType)
      localStorage.setItem('expires_in', expiresIn.toString())

    } catch (err) {
      // If refresh fails, logout user
      await logout()
      throw err
    }
  }

  const initializeAuth = (): void => {
    // Load user from localStorage if token exists
    const storedUser = localStorage.getItem('user')
    if (token.value && storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (err) {
        console.error('Error parsing stored user:', err)
        logout()
      }
    }
  }

  const clearError = (): void => {
    error.value = null
  }

  return {
    // State
    token,
    refreshToken,
    user,
    isLoading,
    error,
    // Getters
    isAuthenticated,
    currentUser,
    // Actions
    login,
    logout,
    refreshAuthToken,
    initializeAuth,
    clearError
  }
})