<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex align-center justify-space-between">
        <span class="text-h5">Gestión de Empleados</span>
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="abrirDialogoNuevo"
        >
          Nuevo Empleado
        </v-btn>
      </v-card-title>

      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="empleados"
          :loading="loading"
          :search="search"
          class="elevation-1"
          item-value="id"
        >
          <template v-slot:top>
            <v-row class="ma-2">
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="search"
                  prepend-inner-icon="mdi-magnify"
                  label="Buscar empleados..."
                  single-line
                  hide-details
                  clearable
                />
              </v-col>
            </v-row>
          </template>

          <template v-slot:item.activo="{ item }">
            <v-chip
              :color="item.activo ? 'success' : 'error'"
              size="small"
            >
              {{ item.activo ? 'Activo' : 'Inactivo' }}
            </v-chip>
          </template>

          <template v-slot:item.actions="{ item }">
            <v-btn
              icon="mdi-pencil"
              size="small"
              color="primary"
              variant="text"
              @click="editarEmpleado(item)"
            />
            <v-btn
              icon="mdi-delete"
              size="small"
              color="error"
              variant="text"
              @click="confirmarEliminar(item)"
            />
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- Dialog para crear/editar empleado -->
    <v-dialog v-model="dialog" max-width="600px" persistent>
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ editandoEmpleado ? 'Editar' : 'Nuevo' }} Empleado</span>
        </v-card-title>

        <v-card-text>
          <v-form ref="form" v-model="formularioValido">
            <v-container>
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="empleadoForm.nombres"
                    label="Nombres *"
                    :rules="reglasNombres"
                    required
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="empleadoForm.apellidos"
                    label="Apellidos *"
                    :rules="reglasApellidos"
                    required
                  />
                </v-col>
              </v-row>

              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="empleadoForm.dni"
                    label="DNI *"
                    :rules="reglasDni"
                    maxlength="8"
                    required
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="empleadoForm.cargo"
                    label="Cargo *"
                    :rules="reglasCargo"
                    required
                  />
                </v-col>
              </v-row>

              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="empleadoForm.area"
                    label="Área *"
                    :rules="reglasArea"
                    required
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-switch
                    v-model="empleadoForm.activo"
                    label="Activo"
                    color="primary"
                  />
                </v-col>
              </v-row>

              <v-row v-if="editandoEmpleado">
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="empleadoForm.codigoUnico"
                    label="Código Único"
                    readonly
                    disabled
                  />
                </v-col>
              </v-row>
            </v-container>
          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn
            color="grey-darken-1"
            variant="text"
            @click="cerrarDialog"
          >
            Cancelar
          </v-btn>
          <v-btn
            color="primary"
            variant="text"
            :loading="guardando"
            :disabled="!formularioValido"
            @click="guardarEmpleado"
          >
            {{ editandoEmpleado ? 'Actualizar' : 'Crear' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog de confirmación para eliminar -->
    <v-dialog v-model="dialogEliminar" max-width="400px">
      <v-card>
        <v-card-title class="text-h5">
          Confirmar Eliminación
        </v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar al empleado <strong>{{ empleadoAEliminar?.nombres }} {{ empleadoAEliminar?.apellidos }}</strong>?
          <br><br>
          <v-alert type="warning" variant="tonal">
            Esta acción mantendrá los registros históricos de asistencia.
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            color="grey-darken-1"
            variant="text"
            @click="dialogEliminar = false"
          >
            Cancelar
          </v-btn>
          <v-btn
            color="error"
            variant="text"
            :loading="eliminando"
            @click="eliminarEmpleado"
          >
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar para mensajes -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="4000"
    >
      {{ snackbar.message }}
      <template v-slot:actions>
        <v-btn
          color="white"
          variant="text"
          @click="snackbar.show = false"
        >
          Cerrar
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import type { Empleado } from '@/types'
import { empleadoService } from '@/services/empleadoService'

// Reactive data
const empleados = ref<Empleado[]>([])
const loading = ref(false)
const search = ref('')
const dialog = ref(false)
const dialogEliminar = ref(false)
const formularioValido = ref(false)
const guardando = ref(false)
const eliminando = ref(false)
const editandoEmpleado = ref(false)
const empleadoAEliminar = ref<Empleado | null>(null)

// Form refs
const form = ref()

// Snackbar
const snackbar = reactive({
  show: false,
  message: '',
  color: 'success'
})

// Form data
const empleadoForm = reactive<Partial<Empleado>>({
  nombres: '',
  apellidos: '',
  dni: '',
  cargo: '',
  area: '',
  activo: true,
  codigoUnico: ''
})

// Table headers
const headers = [
  { title: 'Código', key: 'codigoUnico', sortable: true },
  { title: 'DNI', key: 'dni', sortable: true },
  { title: 'Nombres', key: 'nombres', sortable: true },
  { title: 'Apellidos', key: 'apellidos', sortable: true },
  { title: 'Cargo', key: 'cargo', sortable: true },
  { title: 'Área', key: 'area', sortable: true },
  { title: 'Estado', key: 'activo', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false, width: '120px' }
]

// Validation rules
const reglasNombres = [
  (v: string) => !!v || 'Los nombres son requeridos',
  (v: string) => (v && v.length >= 2) || 'Los nombres deben tener al menos 2 caracteres',
  (v: string) => (v && v.length <= 100) || 'Los nombres no pueden exceder 100 caracteres'
]

const reglasApellidos = [
  (v: string) => !!v || 'Los apellidos son requeridos',
  (v: string) => (v && v.length >= 2) || 'Los apellidos deben tener al menos 2 caracteres',
  (v: string) => (v && v.length <= 100) || 'Los apellidos no pueden exceder 100 caracteres'
]

const reglasDni = [
  (v: string) => !!v || 'El DNI es requerido',
  (v: string) => /^\d{8}$/.test(v) || 'El DNI debe tener exactamente 8 dígitos',
  (v: string) => {
    if (!editandoEmpleado.value) {
      return !empleados.value.some(emp => emp.dni === v) || 'Este DNI ya está registrado'
    }
    return !empleados.value.some(emp => emp.dni === v && emp.id !== empleadoForm.id) || 'Este DNI ya está registrado'
  }
]

const reglasCargo = [
  (v: string) => !!v || 'El cargo es requerido',
  (v: string) => (v && v.length >= 2) || 'El cargo debe tener al menos 2 caracteres',
  (v: string) => (v && v.length <= 100) || 'El cargo no puede exceder 100 caracteres'
]

const reglasArea = [
  (v: string) => !!v || 'El área es requerida',
  (v: string) => (v && v.length >= 2) || 'El área debe tener al menos 2 caracteres',
  (v: string) => (v && v.length <= 100) || 'El área no puede exceder 100 caracteres'
]

// Methods
const cargarEmpleados = async () => {
  loading.value = true
  try {
    empleados.value = await empleadoService.obtenerTodos()
  } catch (error) {
    mostrarError('Error al cargar empleados')
    console.error('Error cargando empleados:', error)
  } finally {
    loading.value = false
  }
}

const abrirDialogoNuevo = () => {
  editandoEmpleado.value = false
  limpiarFormulario()
  dialog.value = true
}

const editarEmpleado = (empleado: Empleado) => {
  editandoEmpleado.value = true
  Object.assign(empleadoForm, empleado)
  dialog.value = true
}

const limpiarFormulario = () => {
  Object.assign(empleadoForm, {
    nombres: '',
    apellidos: '',
    dni: '',
    cargo: '',
    area: '',
    activo: true,
    codigoUnico: ''
  })
  form.value?.resetValidation()
}

const cerrarDialog = () => {
  dialog.value = false
  limpiarFormulario()
}

const guardarEmpleado = async () => {
  if (!formularioValido.value) return

  guardando.value = true
  try {
    if (editandoEmpleado.value) {
      await empleadoService.actualizar(empleadoForm.id!, empleadoForm as Empleado)
      mostrarExito('Empleado actualizado correctamente')
    } else {
      await empleadoService.crear(empleadoForm as Omit<Empleado, 'id' | 'codigoUnico'>)
      mostrarExito('Empleado creado correctamente')
    }
    
    await cargarEmpleados()
    cerrarDialog()
  } catch (error: any) {
    const mensaje = error.response?.data?.mensaje || 'Error al guardar empleado'
    mostrarError(mensaje)
    console.error('Error guardando empleado:', error)
  } finally {
    guardando.value = false
  }
}

const confirmarEliminar = (empleado: Empleado) => {
  empleadoAEliminar.value = empleado
  dialogEliminar.value = true
}

const eliminarEmpleado = async () => {
  if (!empleadoAEliminar.value) return

  eliminando.value = true
  try {
    await empleadoService.eliminar(empleadoAEliminar.value.id!)
    mostrarExito('Empleado eliminado correctamente')
    await cargarEmpleados()
    dialogEliminar.value = false
    empleadoAEliminar.value = null
  } catch (error: any) {
    const mensaje = error.response?.data?.mensaje || 'Error al eliminar empleado'
    mostrarError(mensaje)
    console.error('Error eliminando empleado:', error)
  } finally {
    eliminando.value = false
  }
}

const mostrarExito = (mensaje: string) => {
  snackbar.message = mensaje
  snackbar.color = 'success'
  snackbar.show = true
}

const mostrarError = (mensaje: string) => {
  snackbar.message = mensaje
  snackbar.color = 'error'
  snackbar.show = true
}

// Lifecycle
onMounted(() => {
  cargarEmpleados()
})
</script>

<style scoped>
.v-data-table {
  background-color: transparent;
}
</style>