<template>
  <router-view />
  <GlobalNotifications />
</template>

<script setup lang="ts">
import GlobalNotifications from '@/components/GlobalNotifications.vue'

// App.vue now just renders the router-view and global notifications
// Layouts handle the navigation and structure
</script>

<style>
/* Global application styles */
html {
  overflow-y: auto !important;
}

.v-application {
  font-family: 'Roboto', sans-serif;
}

/* Custom scrollbar styles */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Transition animations */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.3s ease;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
}
</style>