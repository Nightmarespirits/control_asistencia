<template>
  <v-container fluid class="marcacion-container">
    <v-row justify="center" align="center" class="fill-height">
      <v-col cols="12" md="8" lg="6">
        <v-card class="scanner-card" elevation="8">
          <v-card-title class="text-center pa-6">
            <div class="scanner-title">
              <v-icon size="48" color="primary" class="mb-2">mdi-qrcode-scan</v-icon>
              <h2 class="text-h4 font-weight-bold">Control de Asistencia</h2>
              <p class="text-subtitle-1 text-medium-emphasis mt-2">
                Escanea tu DNI para registrar tu asistencia
              </p>
            </div>
          </v-card-title>

          <v-card-text class="pa-6">
            <div class="scanner-area">
              <v-text-field
                ref="dniInputRef"
                v-model="dniEscaneado"
                @input="handleInput"
                placeholder="Coloca el cursor aquí y escanea tu DNI"
                variant="outlined"
                class="dni-input"
                autofocus
                hide-details
                :loading="isProcessing"
                :disabled="isProcessing"
                prepend-inner-icon="mdi-card-account-details"
                clearable
              >
                <template #append-inner>
                  <v-fade-transition>
                    <v-icon v-if="isProcessing" color="primary">
                      mdi-loading mdi-spin
                    </v-icon>
                  </v-fade-transition>
                </template>
              </v-text-field>

              <div class="text-center mt-4">
                <v-chip
                  v-if="!mostrarMensaje && !isProcessing"
                  color="primary"
                  variant="tonal"
                  size="large"
                >
                  <v-icon start>mdi-information</v-icon>
                  Esperando escaneo...
                </v-chip>
              </div>
            </div>

            <!-- Mensaje de resultado -->
            <v-fade-transition>
              <v-alert
                v-if="mostrarMensaje"
                :type="tipoMensaje"
                :icon="iconoMensaje"
                class="mensaje-alert mt-4"
                prominent
                border="start"
                elevation="2"
              >
                <div class="d-flex align-center">
                  <div class="flex-grow-1">
                    <div class="text-h6 font-weight-bold">{{ mensaje }}</div>
                  </div>
                  <v-icon
                    v-if="tipoMensaje === 'success'"
                    size="48"
                    color="success"
                    class="ml-4"
                  >
                    mdi-check-circle
                  </v-icon>
                </div>
              </v-alert>
            </v-fade-transition>

            <!-- Indicador de procesamiento -->
            <v-fade-transition>
              <div v-if="isProcessing" class="text-center mt-4">
                <v-progress-circular
                  indeterminate
                  color="primary"
                  size="64"
                  width="6"
                />
                <p class="text-subtitle-1 mt-2">Procesando marcación...</p>
              </div>
            </v-fade-transition>
          </v-card-text>
        </v-card>

        <!-- Instrucciones -->
        <v-card class="mt-4" variant="tonal">
          <v-card-text>
            <div class="text-center">
              <h3 class="text-h6 mb-3">Instrucciones</h3>
              <v-row>
                <v-col cols="12" sm="4">
                  <div class="instruction-item">
                    <v-icon size="32" color="primary" class="mb-2">mdi-numeric-1-circle</v-icon>
                    <p class="text-body-2">Haz clic en el campo de texto</p>
                  </div>
                </v-col>
                <v-col cols="12" sm="4">
                  <div class="instruction-item">
                    <v-icon size="32" color="primary" class="mb-2">mdi-numeric-2-circle</v-icon>
                    <p class="text-body-2">Escanea tu DNI con el lector</p>
                  </div>
                </v-col>
                <v-col cols="12" sm="4">
                  <div class="instruction-item">
                    <v-icon size="32" color="primary" class="mb-2">mdi-numeric-3-circle</v-icon>
                    <p class="text-body-2">Espera la confirmación</p>
                  </div>
                </v-col>
              </v-row>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>


  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useZKTecoScanner } from '@/composables/useZKTecoScanner'

// Template refs
const dniInputRef = ref<HTMLInputElement | null>(null)

// Composable
const {
  dniEscaneado,
  isProcessing,
  mensaje,
  tipoMensaje,
  iconoMensaje,
  mostrarMensaje,
  procesarEntrada,
  initAudio
} = useZKTecoScanner()

// Handle input changes
const handleInput = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value
  
  // Procesar automáticamente cuando se detecten 8 dígitos
  if (value.length === 8) {
    await procesarEntrada(value)
    // Refocus después del procesamiento
    await nextTick()
    if (dniInputRef.value) {
      dniInputRef.value.focus()
    }
  }
}

// Setup on mount
onMounted(async () => {
  // Initialize audio context
  initAudio()
  
  // Ensure input is focused
  await nextTick()
  if (dniInputRef.value) {
    dniInputRef.value.focus()
  }
  
  // Re-focus input when clicking anywhere on the card
  document.addEventListener('click', () => {
    if (dniInputRef.value && !isProcessing.value) {
      dniInputRef.value.focus()
    }
  })
})
</script>

<style scoped>
.marcacion-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.scanner-card {
  border-radius: 16px !important;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.scanner-title {
  text-align: center;
}

.dni-input {
  font-size: 1.2rem;
}

.dni-input :deep(.v-field__input) {
  text-align: center;
  font-size: 1.2rem;
  font-weight: 500;
}

.mensaje-alert {
  border-radius: 12px !important;
  animation: pulse 0.5s ease-in-out;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.instruction-item {
  text-align: center;
  padding: 16px;
}

.instruction-item p {
  margin-top: 8px;
  font-weight: 500;
}

/* Ensure input stays focused */
.dni-input :deep(.v-field__input) {
  caret-color: transparent;
}

.dni-input:focus-within :deep(.v-field__input) {
  caret-color: auto;
}

/* Loading animation */
.mdi-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>