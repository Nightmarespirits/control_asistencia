import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth.ts'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
    currentRoute: {
      value: {
        query: {}
      }
    }
  })
}))

// Mock API
vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn()
  }
}))

describe('LoginComponent Logic', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('auth store is available and functional', () => {
    const authStore = useAuthStore()
    
    expect(authStore).toBeDefined()
    expect(authStore.isAuthenticated).toBe(false)
    expect(authStore.isLoading).toBe(false)
    expect(authStore.error).toBe(null)
  })

  it('validation rules work correctly', () => {
    // Username validation rules
    const usernameRules = [
      (v: string) => !!v || 'El usuario es requerido',
      (v: string) => v.length >= 3 || 'El usuario debe tener al menos 3 caracteres'
    ]

    // Test empty username
    expect(usernameRules[0]('')).toBe('El usuario es requerido')
    
    // Test short username
    expect(usernameRules[1]('ab')).toBe('El usuario debe tener al menos 3 caracteres')
    
    // Test valid username
    expect(usernameRules[0]('validuser')).toBe(true)
    expect(usernameRules[1]('validuser')).toBe(true)

    // Password validation rules
    const passwordRules = [
      (v: string) => !!v || 'La contraseña es requerida',
      (v: string) => v.length >= 4 || 'La contraseña debe tener al menos 4 caracteres'
    ]

    // Test empty password
    expect(passwordRules[0]('')).toBe('La contraseña es requerida')
    
    // Test short password
    expect(passwordRules[1]('123')).toBe('La contraseña debe tener al menos 4 caracteres')
    
    // Test valid password
    expect(passwordRules[0]('validpass')).toBe(true)
    expect(passwordRules[1]('validpass')).toBe(true)
  })

  it('login flow integration with auth store', async () => {
    const authStore = useAuthStore()
    
    // Mock successful login response
    const mockResponse = {
      data: {
        accessToken: 'test-token',
        refreshToken: 'test-refresh-token',
        tokenType: 'Bearer',
        expiresIn: 28800,
        username: 'testuser'
      }
    }

    // Mock the API call
    const mockApi = await import('@/services/api')
    vi.mocked(mockApi.default.post).mockResolvedValue(mockResponse)

    // Perform login
    await authStore.login({ username: 'testuser', password: 'password' })

    // Verify state changes
    expect(authStore.isAuthenticated).toBe(true)
    expect(authStore.token).toBe('test-token')
    expect(authStore.user?.username).toBe('testuser')
  })

  it('handles login errors correctly', async () => {
    const authStore = useAuthStore()
    
    // Reset auth store state
    await authStore.logout()
    
    // Mock failed login response
    const mockError = {
      response: {
        data: {
          message: 'Credenciales inválidas'
        }
      }
    }

    // Mock the API call to reject
    const mockApi = await import('@/services/api')
    vi.mocked(mockApi.default.post).mockRejectedValue(mockError)

    // Attempt login and expect it to throw
    await expect(authStore.login({ username: 'wrong', password: 'wrong' }))
      .rejects.toThrow()

    // Verify error state
    expect(authStore.error).toBe('Credenciales inválidas')
    expect(authStore.isAuthenticated).toBe(false)
  })
})