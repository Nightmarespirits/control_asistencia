import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/AdminLayout.vue'
import PublicLayout from '@/layouts/PublicLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Public routes with PublicLayout
    {
      path: '/',
      component: PublicLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
          meta: { title: 'Inicio' }
        },
        {
          path: 'login',
          name: 'login',
          component: () => import('@/views/LoginView.vue'),
          meta: { 
            requiresGuest: true,
            title: 'Iniciar Sesión'
          }
        },
        {
          path: 'marcacion',
          name: 'marcacion',
          component: () => import('@/views/MarcacionView.vue'),
          meta: { title: 'Marcación de Asistencia' }
        }
      ]
    },
    
    // Administrative routes with AdminLayout
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/HomeView.vue'),
          meta: { 
            requiresAuth: true,
            title: 'Dashboard',
            breadcrumb: 'Dashboard'
          }
        },
        {
          path: 'empleados',
          name: 'admin-empleados',
          component: () => import('@/views/EmpleadosView.vue'),
          meta: { 
            requiresAuth: true,
            title: 'Gestión de Empleados',
            breadcrumb: 'Empleados'
          }
        },
        {
          path: 'horarios',
          name: 'admin-horarios',
          component: () => import('@/views/HorariosView.vue'),
          meta: { 
            requiresAuth: true,
            title: 'Configuración de Horarios',
            breadcrumb: 'Horarios'
          }
        },
        {
          path: 'reportes',
          name: 'admin-reportes',
          component: () => import('@/views/ReportesView.vue'),
          meta: { 
            requiresAuth: true,
            title: 'Reportes de Asistencia',
            breadcrumb: 'Reportes'
          }
        }
      ]
    },

    // Legacy routes - redirect to new structure
    {
      path: '/empleados',
      redirect: '/admin/empleados'
    },
    {
      path: '/horarios',
      redirect: '/admin/horarios'
    },
    {
      path: '/reportes',
      redirect: '/admin/reportes'
    },

    // 404 Not Found
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: 'Página no encontrada' }
    }
  ]
})

// Navigation guards
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  
  // Initialize auth store if not already done
  if (!authStore.user && authStore.token) {
    try {
      await authStore.initializeAuth()
    } catch (error) {
      console.error('Failed to initialize auth:', error)
      authStore.logout()
    }
  }

  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresGuest = to.matched.some(record => record.meta.requiresGuest)

  // Handle authentication requirements
  if (requiresAuth && !authStore.isAuthenticated) {
    // Redirect to login with return path
    next({
      name: 'login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // Handle guest-only pages (like login)
  if (requiresGuest && authStore.isAuthenticated) {
    // Check if there's a redirect query parameter
    const redirectPath = to.query.redirect as string
    if (redirectPath && redirectPath !== '/login') {
      next(redirectPath)
    } else {
      // Redirect authenticated users to admin dashboard
      next({ name: 'admin-dashboard' })
    }
    return
  }

  // Auto-redirect authenticated users from home to admin dashboard
  if (to.name === 'home' && authStore.isAuthenticated) {
    next({ name: 'admin-dashboard' })
    return
  }

  next()
})

// After each navigation, update document title
router.afterEach((to) => {
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - Sistema de Control de Asistencia`
  } else {
    document.title = 'Sistema de Control de Asistencia'
  }
})

export default router