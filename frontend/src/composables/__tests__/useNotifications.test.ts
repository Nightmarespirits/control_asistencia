import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useNotifications, globalNotifications } from '../useNotifications'

describe('useNotifications', () => {
  beforeEach(() => {
    // Clear all notifications before each test
    globalNotifications.clearAll()
    vi.clearAllTimers()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('should add a notification', () => {
    const { notifications, addNotification } = useNotifications()

    const id = addNotification({
      message: 'Test message',
      type: 'success'
    })

    expect(notifications.value).toHaveLength(1)
    expect(notifications.value[0]).toMatchObject({
      id,
      message: 'Test message',
      type: 'success',
      timeout: 4000,
      persistent: false
    })
  })

  it('should remove a notification', () => {
    const { notifications, addNotification, removeNotification } = useNotifications()

    const id = addNotification({
      message: 'Test message',
      type: 'success'
    })

    expect(notifications.value).toHaveLength(1)

    removeNotification(id)

    expect(notifications.value).toHaveLength(0)
  })

  it('should auto-remove non-persistent notifications after timeout', () => {
    const { notifications, addNotification } = useNotifications()

    addNotification({
      message: 'Test message',
      type: 'success',
      timeout: 1000
    })

    expect(notifications.value).toHaveLength(1)

    // Fast-forward time
    vi.advanceTimersByTime(1000)

    expect(notifications.value).toHaveLength(0)
  })

  it('should not auto-remove persistent notifications', () => {
    const { notifications, addNotification } = useNotifications()

    addNotification({
      message: 'Persistent message',
      type: 'info',
      persistent: true,
      timeout: 1000
    })

    expect(notifications.value).toHaveLength(1)

    // Fast-forward time
    vi.advanceTimersByTime(2000)

    expect(notifications.value).toHaveLength(1)
  })

  it('should clear all notifications', () => {
    const { notifications, addNotification, clearAll } = useNotifications()

    addNotification({ message: 'Message 1', type: 'success' })
    addNotification({ message: 'Message 2', type: 'error' })
    addNotification({ message: 'Message 3', type: 'warning' })

    expect(notifications.value).toHaveLength(3)

    clearAll()

    expect(notifications.value).toHaveLength(0)
  })

  it('should provide convenience methods', () => {
    const { notifications, showSuccess, showError, showWarning, showInfo } = useNotifications()

    showSuccess('Success message')
    showError('Error message')
    showWarning('Warning message')
    showInfo('Info message')

    expect(notifications.value).toHaveLength(4)
    expect(notifications.value[0].type).toBe('success')
    expect(notifications.value[1].type).toBe('error')
    expect(notifications.value[2].type).toBe('warning')
    expect(notifications.value[3].type).toBe('info')
  })

  it('should allow custom options in convenience methods', () => {
    const { notifications, showError } = useNotifications()

    showError('Critical error', { persistent: true, timeout: 10000 })

    expect(notifications.value).toHaveLength(1)
    expect(notifications.value[0]).toMatchObject({
      message: 'Critical error',
      type: 'error',
      persistent: true,
      timeout: 10000
    })
  })

  it('should generate unique IDs for notifications', () => {
    const { notifications, addNotification } = useNotifications()

    const id1 = addNotification({ message: 'Message 1', type: 'success' })
    const id2 = addNotification({ message: 'Message 2', type: 'error' })

    expect(id1).not.toBe(id2)
    expect(notifications.value[0].id).toBe(id1)
    expect(notifications.value[1].id).toBe(id2)
  })

  it('should work with global notifications instance', () => {
    globalNotifications.showSuccess('Global success message')
    globalNotifications.showError('Global error message')

    expect(globalNotifications.notifications.value).toHaveLength(2)
    expect(globalNotifications.notifications.value[0].message).toBe('Global success message')
    expect(globalNotifications.notifications.value[1].message).toBe('Global error message')
  })
})