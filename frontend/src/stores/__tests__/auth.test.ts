import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import api from '@/services/api'

// Mock the API
vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn()
  }
}))

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorageMock.getItem.mockReturnValue(null)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('initializes with empty state', () => {
    const authStore = useAuthStore()
    
    expect(authStore.isAuthenticated).toBe(false)
    expect(authStore.currentUser).toBe(null)
    expect(authStore.token).toBe(null)
    expect(authStore.isLoading).toBe(false)
    expect(authStore.error).toBe(null)
  })

  it('loads token from localStorage on initialization', () => {
    const mockToken = 'mock-token'
    const mockUser = { id: 1, username: 'testuser' }
    
    localStorageMock.getItem.mockImplementation((key) => {
      if (key === 'auth_token') return mockToken
      if (key === 'user') return JSON.stringify(mockUser)
      return null
    })

    const authStore = useAuthStore()
    authStore.initializeAuth()
    
    expect(authStore.token).toBe(mockToken)
    expect(authStore.user).toEqual(mockUser)
    expect(authStore.isAuthenticated).toBe(true)
  })

  it('handles successful login', async () => {
    const mockResponse = {
      data: {
        accessToken: 'new-token',
        refreshToken: 'refresh-token',
        tokenType: 'Bearer',
        expiresIn: 28800,
        username: 'testuser'
      }
    }

    vi.mocked(api.post).mockResolvedValue(mockResponse)

    const authStore = useAuthStore()
    await authStore.login({ username: 'testuser', password: 'password' })

    expect(authStore.token).toBe('new-token')
    expect(authStore.refreshToken).toBe('refresh-token')
    expect(authStore.user).toEqual({ id: 0, username: 'testuser' })
    expect(authStore.isAuthenticated).toBe(true)
    expect(authStore.error).toBe(null)

    // Verify localStorage calls
    expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_token', 'new-token')
    expect(localStorageMock.setItem).toHaveBeenCalledWith('refresh_token', 'refresh-token')
    expect(localStorageMock.setItem).toHaveBeenCalledWith('user', JSON.stringify({ id: 0, username: 'testuser' }))
    expect(localStorageMock.setItem).toHaveBeenCalledWith('token_type', 'Bearer')
    expect(localStorageMock.setItem).toHaveBeenCalledWith('expires_in', '28800')
  })

  it('handles login failure', async () => {
    const mockError = {
      response: {
        data: {
          message: 'Credenciales inválidas'
        }
      }
    }

    vi.mocked(api.post).mockRejectedValue(mockError)

    const authStore = useAuthStore()
    
    await expect(authStore.login({ username: 'wrong', password: 'wrong' }))
      .rejects.toThrow()

    expect(authStore.error).toBe('Credenciales inválidas')
    expect(authStore.isAuthenticated).toBe(false)
  })

  it('handles logout correctly', async () => {
    const authStore = useAuthStore()
    
    // Set some initial state
    authStore.token = 'some-token'
    authStore.user = { id: 1, username: 'testuser' }

    await authStore.logout()

    expect(authStore.token).toBe(null)
    expect(authStore.user).toBe(null)
    expect(authStore.isAuthenticated).toBe(false)

    // Verify localStorage cleanup
    expect(localStorageMock.removeItem).toHaveBeenCalledWith('auth_token')
    expect(localStorageMock.removeItem).toHaveBeenCalledWith('refresh_token')
    expect(localStorageMock.removeItem).toHaveBeenCalledWith('user')
    expect(localStorageMock.removeItem).toHaveBeenCalledWith('token_type')
    expect(localStorageMock.removeItem).toHaveBeenCalledWith('expires_in')
  })

  it('handles token refresh', async () => {
    const mockResponse = {
      data: {
        accessToken: 'new-refreshed-token',
        refreshToken: 'refresh-token',
        tokenType: 'Bearer',
        expiresIn: 28800,
        username: 'testuser'
      }
    }

    vi.mocked(api.post).mockResolvedValue(mockResponse)

    const authStore = useAuthStore()
    authStore.refreshToken = 'valid-refresh-token'

    await authStore.refreshAuthToken()

    expect(authStore.token).toBe('new-refreshed-token')
    expect(localStorageMock.setItem).toHaveBeenCalledWith('auth_token', 'new-refreshed-token')
    expect(localStorageMock.setItem).toHaveBeenCalledWith('token_type', 'Bearer')
    expect(localStorageMock.setItem).toHaveBeenCalledWith('expires_in', '28800')
  })

  it('logs out user when refresh token fails', async () => {
    vi.mocked(api.post).mockRejectedValue(new Error('Refresh failed'))

    const authStore = useAuthStore()
    authStore.refreshToken = 'invalid-refresh-token'

    await expect(authStore.refreshAuthToken()).rejects.toThrow()

    expect(authStore.token).toBe(null)
    expect(authStore.user).toBe(null)
    expect(authStore.isAuthenticated).toBe(false)
  })

  it('clears error state', () => {
    const authStore = useAuthStore()
    authStore.error = 'Some error'

    authStore.clearError()

    expect(authStore.error).toBe(null)
  })
})