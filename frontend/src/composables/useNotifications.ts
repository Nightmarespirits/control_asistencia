import { ref, reactive } from 'vue'

export interface Notification {
  id: string
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
  timeout?: number
  persistent?: boolean
}

const notifications = ref<Notification[]>([])
let notificationId = 0

export function useNotifications() {
  const addNotification = (notification: Omit<Notification, 'id'>) => {
    const id = `notification-${++notificationId}`
    const newNotification: Notification = {
      id,
      timeout: 4000,
      persistent: false,
      ...notification
    }
    
    notifications.value.push(newNotification)
    
    // Auto remove notification after timeout (unless persistent)
    if (!newNotification.persistent && newNotification.timeout) {
      setTimeout(() => {
        removeNotification(id)
      }, newNotification.timeout)
    }
    
    return id
  }
  
  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }
  
  const clearAll = () => {
    notifications.value = []
  }
  
  // Convenience methods
  const showSuccess = (message: string, options?: Partial<Notification>) => {
    return addNotification({ message, type: 'success', ...options })
  }
  
  const showError = (message: string, options?: Partial<Notification>) => {
    return addNotification({ message, type: 'error', ...options })
  }
  
  const showWarning = (message: string, options?: Partial<Notification>) => {
    return addNotification({ message, type: 'warning', ...options })
  }
  
  const showInfo = (message: string, options?: Partial<Notification>) => {
    return addNotification({ message, type: 'info', ...options })
  }
  
  return {
    notifications,
    addNotification,
    removeNotification,
    clearAll,
    showSuccess,
    showError,
    showWarning,
    showInfo
  }
}

// Global instance for use across the app
export const globalNotifications = useNotifications()