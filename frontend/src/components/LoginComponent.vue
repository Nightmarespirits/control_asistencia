<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4">
        <v-card class="elevation-12">
          <v-toolbar color="primary" dark flat>
            <v-toolbar-title>Iniciar Sesión</v-toolbar-title>
          </v-toolbar>
          
          <v-card-text>
            <v-form ref="form" v-model="valid" @submit.prevent="handleLogin">
              <v-text-field
                v-model="credentials.username"
                :rules="usernameRules"
                label="Usuario"
                prepend-icon="mdi-account"
                type="text"
                required
                :disabled="authStore.isLoading"
              />
              
              <v-text-field
                v-model="credentials.password"
                :rules="passwordRules"
                label="Contraseña"
                prepend-icon="mdi-lock"
                :type="showPassword ? 'text' : 'password'"
                :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                @click:append="showPassword = !showPassword"
                required
                :disabled="authStore.isLoading"
              />
              
              <v-alert
                v-if="authStore.error"
                type="error"
                class="mb-4"
                dismissible
                @click:close="authStore.clearError"
              >
                {{ authStore.error }}
              </v-alert>
            </v-form>
          </v-card-text>
          
          <v-card-actions>
            <v-spacer />
            <v-btn
              color="primary"
              :loading="authStore.isLoading"
              :disabled="!valid || authStore.isLoading"
              @click="handleLogin"
              block
            >
              Iniciar Sesión
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// Form state
const form = ref()
const valid = ref(false)
const showPassword = ref(false)

const credentials = reactive({
  username: '',
  password: ''
})

// Validation rules
const usernameRules = [
  (v: string) => !!v || 'El usuario es requerido',
  (v: string) => v.length >= 3 || 'El usuario debe tener al menos 3 caracteres'
]

const passwordRules = [
  (v: string) => !!v || 'La contraseña es requerida',
  (v: string) => v.length >= 4 || 'La contraseña debe tener al menos 4 caracteres'
]

const handleLogin = async () => {
  if (!valid.value) return

  try {
    await authStore.login(credentials)
    
    // Redirect to intended route or default to admin dashboard
    const redirectTo = router.currentRoute.value.query.redirect as string || '/admin/dashboard'
    await router.push(redirectTo)
    
  } catch (error) {
    // Error is handled by the store
    console.error('Login failed:', error)
  }
}
</script>

<style scoped>
.fill-height {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.v-card {
  border-radius: 12px;
}
</style>