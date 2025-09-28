<template>
  <div class="global-notifications">
    <v-snackbar
      v-for="notification in notifications"
      :key="notification.id"
      v-model="notification.show"
      :color="getColor(notification.type)"
      :timeout="notification.persistent ? -1 : notification.timeout"
      location="top right"
      multi-line
      class="notification-snackbar"
    >
      <div class="d-flex align-center">
        <v-icon class="me-2">{{ getIcon(notification.type) }}</v-icon>
        <span>{{ notification.message }}</span>
      </div>
      
      <template #actions>
        <v-btn
          variant="text"
          size="small"
          @click="removeNotification(notification.id)"
        >
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { globalNotifications, type Notification } from '@/composables/useNotifications'

const { notifications: notificationList, removeNotification } = globalNotifications

// Add show property for v-model
const notifications = computed(() => 
  notificationList.value.map(notification => ({
    ...notification,
    show: true
  }))
)

const getColor = (type: Notification['type']) => {
  switch (type) {
    case 'success': return 'success'
    case 'error': return 'error'
    case 'warning': return 'warning'
    case 'info': return 'info'
    default: return 'info'
  }
}

const getIcon = (type: Notification['type']) => {
  switch (type) {
    case 'success': return 'mdi-check-circle'
    case 'error': return 'mdi-alert-circle'
    case 'warning': return 'mdi-alert'
    case 'info': return 'mdi-information'
    default: return 'mdi-information'
  }
}
</script>

<style scoped>
.global-notifications {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  pointer-events: none;
}

.notification-snackbar {
  pointer-events: auto;
  margin-bottom: 8px;
}
</style>