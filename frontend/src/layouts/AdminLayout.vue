<template>
  <v-app>
    <!-- App Bar -->
    <v-app-bar app color="primary" dark elevation="1">
      <v-app-bar-nav-icon @click="drawer = !drawer" />
      <v-app-bar-title>Sistema de Control de Asistencia</v-app-bar-title>
      
      <v-spacer />
      
      <!-- Breadcrumbs -->
      <v-breadcrumbs 
        :items="breadcrumbs" 
        class="pa-0"
        color="white"
      >
        <template v-slot:item="{ item }">
          <v-breadcrumbs-item
            :to="item.to"
            :disabled="item.disabled"
            class="text-white"
          >
            {{ item.title }}
          </v-breadcrumbs-item>
        </template>
        <template v-slot:divider>
          <v-icon color="white">mdi-chevron-right</v-icon>
        </template>
      </v-breadcrumbs>
      
      <v-spacer />
      
      <!-- User menu -->
      <v-menu offset-y>
        <template v-slot:activator="{ props }">
          <v-btn text v-bind="props">
            <v-icon left>mdi-account</v-icon>
            {{ authStore.currentUser?.username }}
            <v-icon right>mdi-chevron-down</v-icon>
          </v-btn>
        </template>
        
        <v-list>
          <v-list-item @click="handleLogout">
            <template v-slot:prepend>
              <v-icon>mdi-logout</v-icon>
            </template>
            <v-list-item-title>Cerrar Sesión</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-app-bar>

    <!-- Navigation Drawer -->
    <v-navigation-drawer
      v-model="drawer"
      app
      color="grey-lighten-5"
      width="280"
    >
      <!-- Logo/Header -->
      <v-list-item class="px-2">
        <v-list-item-title class="text-h6 font-weight-bold">
          Administración
        </v-list-item-title>
      </v-list-item>

      <v-divider />

      <!-- Navigation Menu -->
      <v-list nav>
        <v-list-item
          v-for="item in navigationItems"
          :key="item.title"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.title"
          :subtitle="item.subtitle"
          color="primary"
        />
      </v-list>

      <v-divider class="my-4" />

      <!-- Public Access Section -->
      <v-list nav>
        <v-list-subheader>Acceso Público</v-list-subheader>
        <v-list-item
          v-for="item in publicItems"
          :key="item.title"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.title"
          color="primary"
        />
      </v-list>
    </v-navigation-drawer>

    <!-- Main Content -->
    <v-main>
      <v-container fluid class="pa-4">
        <router-view />
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const drawer = ref(true)

// Navigation items for admin panel
const navigationItems = [
  {
    title: 'Dashboard',
    subtitle: 'Vista general',
    icon: 'mdi-view-dashboard',
    to: '/admin/dashboard'
  },
  {
    title: 'Empleados',
    subtitle: 'Gestión de personal',
    icon: 'mdi-account-group',
    to: '/admin/empleados'
  },
  {
    title: 'Horarios',
    subtitle: 'Configuración de turnos',
    icon: 'mdi-clock-outline',
    to: '/admin/horarios'
  },
  {
    title: 'Reportes',
    subtitle: 'Análisis y exportación',
    icon: 'mdi-chart-line',
    to: '/admin/reportes'
  }
]

// Public access items
const publicItems = [
  {
    title: 'Marcación',
    icon: 'mdi-clock-check',
    to: '/marcacion'
  },
  {
    title: 'Inicio Público',
    icon: 'mdi-home',
    to: '/'
  }
]

// Generate breadcrumbs based on current route
const breadcrumbs = computed(() => {
  const crumbs = []
  
  // Always start with Dashboard for admin routes
  if (route.path.startsWith('/admin')) {
    crumbs.push({
      title: 'Dashboard',
      to: '/admin/dashboard',
      disabled: route.name === 'admin-dashboard'
    })
    
    // Add current page if not dashboard
    if (route.name !== 'admin-dashboard' && route.meta.breadcrumb) {
      crumbs.push({
        title: route.meta.breadcrumb as string,
        to: route.path,
        disabled: true
      })
    }
  }
  
  return crumbs
})

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.v-breadcrumbs {
  padding: 0;
}

.v-breadcrumbs-item {
  font-size: 0.875rem;
}
</style>