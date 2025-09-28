import { ref, nextTick } from 'vue'
import api from '@/services/api'
import type { MarcacionRequest, MarcacionResponse } from '@/types'
import { useSoundEffects } from './useSoundEffects'

export function useZKTecoScanner() {
  const dniEscaneado = ref('')
  const isProcessing = ref(false)
  const mensaje = ref('')
  const tipoMensaje = ref<'success' | 'error' | 'warning'>('success')
  const iconoMensaje = ref('')
  const mostrarMensaje = ref(false)

  // Sound effects
  const { playSuccessSound, playErrorSound, initAudioContext } = useSoundEffects()

  const limpiarMensaje = () => {
    setTimeout(() => {
      mostrarMensaje.value = false
      mensaje.value = ''
    }, 5000)
  }

  // Initialize audio context on first user interaction
  const initAudio = () => {
    initAudioContext()
  }

  const mostrarMensajeExito = (texto: string) => {
    mensaje.value = texto
    tipoMensaje.value = 'success'
    iconoMensaje.value = 'mdi-check-circle'
    mostrarMensaje.value = true
    playSuccessSound()
    limpiarMensaje()
  }

  const mostrarMensajeError = (texto: string) => {
    mensaje.value = texto
    tipoMensaje.value = 'error'
    iconoMensaje.value = 'mdi-alert-circle'
    mostrarMensaje.value = true
    playErrorSound()
    limpiarMensaje()
  }

  const mostrarMensajeAdvertencia = (texto: string) => {
    mensaje.value = texto
    tipoMensaje.value = 'warning'
    iconoMensaje.value = 'mdi-alert'
    mostrarMensaje.value = true
    limpiarMensaje()
  }

  const procesarMarcacion = async (dni: string) => {
    // Validar que sea un DNI válido (8 dígitos)
    if (!/^\d{8}$/.test(dni)) {
      return
    }

    if (isProcessing.value) {
      return // Evitar procesamiento múltiple
    }

    isProcessing.value = true

    try {
      const request: MarcacionRequest = { dni }
      const response = await api.post<MarcacionResponse>('/public/asistencia/marcar', request)
      
      const data = response.data
      mostrarMensajeExito(data.mensaje)
      
    } catch (error: any) {
      if (error.response?.status === 404) {
        mostrarMensajeError('Empleado no encontrado')
      } else if (error.response?.status === 409) {
        mostrarMensajeAdvertencia('Ya existe una marcación reciente')
      } else {
        mostrarMensajeError('Error al procesar la marcación')
      }
    } finally {
      isProcessing.value = false
      dniEscaneado.value = '' // Limpiar campo para próxima lectura
    }
  }

  const procesarEntrada = async (input: string) => {
    // Procesar automáticamente cuando se detecten 8 dígitos
    if (input.length === 8 && /^\d{8}$/.test(input)) {
      await procesarMarcacion(input)
    }
  }

  return {
    dniEscaneado,
    isProcessing,
    mensaje,
    tipoMensaje,
    iconoMensaje,
    mostrarMensaje,
    procesarEntrada,
    procesarMarcacion,
    initAudio
  }
}