<template>
  <div class="reportes-container">
    <v-card>
      <v-card-title>
        <v-icon left>mdi-chart-line</v-icon>
        Reportes de Asistencia
      </v-card-title>
      
      <!-- Filtros -->
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3">
            <v-text-field
              v-model="filtros.fechaInicio"
              label="Fecha Inicio"
              type="date"
              variant="outlined"
              density="compact"
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field
              v-model="filtros.fechaFin"
              label="Fecha Fin"
              type="date"
              variant="outlined"
              density="compact"
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="filtros.empleadoId"
              :items="empleados"
              item-title="nombreCompleto"
              item-value="id"
              label="Empleado"
              variant="outlined"
              density="compact"
              clearable
              :loading="loadingEmpleados"
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="filtros.tipo"
              :items="tiposMarcacion"
              item-title="text"
              item-value="value"
              label="Tipo de Marcación"
              variant="outlined"
              density="compact"
              clearable
            />
          </v-col>
        </v-row>
        
        <v-row>
          <v-col cols="12" class="d-flex gap-2">
            <v-btn
              color="primary"
              @click="buscarReportes"
              :loading="loading"
              prepend-icon="mdi-magnify"
            >
              Buscar
            </v-btn>
            <v-btn
              color="success"
              @click="exportarExcel"
              :loading="exportandoExcel"
              :disabled="!hayDatos"
              prepend-icon="mdi-file-excel"
            >
              Exportar Excel
            </v-btn>
            <v-btn
              color="error"
              @click="exportarPdf"
              :loading="exportandoPdf"
              :disabled="!hayDatos"
              prepend-icon="mdi-file-pdf-box"
            >
              Exportar PDF
            </v-btn>
            <v-btn
              variant="outlined"
              @click="limpiarFiltros"
              prepend-icon="mdi-filter-remove"
            >
              Limpiar
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Tabla de Resultados -->
    <v-card class="mt-4">
      <v-card-text>
        <v-data-table-server
          v-model:items-per-page="itemsPerPage"
          v-model:page="page"
          :headers="headers"
          :items="reportes"
          :items-length="totalItems"
          :loading="loading"
          :no-data-text="mensajeNoData"
          class="elevation-1"
          @update:options="actualizarOpciones"
        >
          <template v-slot:item.empleado="{ item }">
            {{ item.empleado.nombres }} {{ item.empleado.apellidos }}
          </template>
          
          <template v-slot:item.fechaHora="{ item }">
            {{ formatearFechaHora(item.fechaHora) }}
          </template>
          
          <template v-slot:item.tipo="{ item }">
            <v-chip
              :color="getColorTipo(item.tipo)"
              size="small"
              variant="flat"
            >
              {{ formatearTipo(item.tipo) }}
            </v-chip>
          </template>
          
          <template v-slot:item.estado="{ item }">
            <v-chip
              :color="getColorEstado(item.estado)"
              size="small"
              variant="flat"
            >
              {{ formatearEstado(item.estado) }}
            </v-chip>
          </template>
          
          <template v-slot:item.observaciones="{ item }">
            <span v-if="item.observaciones" class="text-caption">
              {{ item.observaciones }}
            </span>
            <span v-else class="text-grey">-</span>
          </template>
        </v-data-table-server>
      </v-card-text>
    </v-card>

    <!-- Snackbar para mensajes -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="4000"
    >
      {{ snackbar.message }}
      <template v-slot:actions>
        <v-btn
          variant="text"
          @click="snackbar.show = false"
        >
          Cerrar
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { reporteService } from '@/services/reporteService'
import { empleadoService } from '@/services/empleadoService'
import type { 
  ReporteRequest, 
  Asistencia, 
  Empleado, 
  TipoMarcacion, 
  EstadoMarcacion 
} from '@/types'

// Estado reactivo
const loading = ref(false)
const loadingEmpleados = ref(false)
const exportandoExcel = ref(false)
const exportandoPdf = ref(false)
const reportes = ref<Asistencia[]>([])
const empleados = ref<(Empleado & { nombreCompleto: string })[]>([])
const totalItems = ref(0)
const page = ref(1)
const itemsPerPage = ref(10)

// Filtros
const filtros = ref<ReporteRequest>({
  fechaInicio: '',
  fechaFin: '',
  empleadoId: undefined,
  tipo: undefined
})

// Snackbar para mensajes
const snackbar = ref({
  show: false,
  message: '',
  color: 'success'
})

// Headers de la tabla
const headers = [
  { title: 'Empleado', key: 'empleado', sortable: false },
  { title: 'Fecha y Hora', key: 'fechaHora', sortable: true },
  { title: 'Tipo', key: 'tipo', sortable: true },
  { title: 'Estado', key: 'estado', sortable: true },
  { title: 'Observaciones', key: 'observaciones', sortable: false }
]

// Opciones para select de tipos de marcación
const tiposMarcacion = [
  { text: 'Entrada', value: 'ENTRADA' },
  { text: 'Salida a Almuerzo', value: 'SALIDA_ALMUERZO' },
  { text: 'Retorno de Almuerzo', value: 'RETORNO_ALMUERZO' },
  { text: 'Salida', value: 'SALIDA' },
  { text: 'Fuera de Horario', value: 'FUERA_HORARIO' }
]

// Computed properties
const hayDatos = computed(() => reportes.value.length > 0)

const mensajeNoData = computed(() => {
  if (loading.value) return 'Cargando...'
  if (totalItems.value === 0 && (filtros.value.fechaInicio || filtros.value.fechaFin || filtros.value.empleadoId || filtros.value.tipo)) {
    return 'No se encontraron registros para los filtros aplicados'
  }
  return 'No hay registros de asistencia'
})

// Métodos
const cargarEmpleados = async () => {
  try {
    loadingEmpleados.value = true
    const response = await empleadoService.obtenerTodos()
    empleados.value = response.map((emp: Empleado) => ({
      ...emp,
      nombreCompleto: `${emp.nombres} ${emp.apellidos} (${emp.dni})`
    }))
  } catch (error) {
    mostrarError('Error al cargar empleados')
  } finally {
    loadingEmpleados.value = false
  }
}

const buscarReportes = async () => {
  try {
    loading.value = true
    const request: ReporteRequest = {
      ...filtros.value,
      page: page.value - 1, // Backend usa índice base 0
      size: itemsPerPage.value
    }
    
    const response = await reporteService.obtenerReportes(request)
    reportes.value = response.content
    totalItems.value = response.totalElements
  } catch (error) {
    mostrarError('Error al cargar reportes')
    reportes.value = []
    totalItems.value = 0
  } finally {
    loading.value = false
  }
}

const actualizarOpciones = () => {
  buscarReportes()
}

const exportarExcel = async () => {
  try {
    exportandoExcel.value = true
    const blob = await reporteService.exportarExcel(filtros.value)
    descargarArchivo(blob, 'reporte-asistencia.xlsx')
    mostrarExito('Reporte Excel descargado exitosamente')
  } catch (error) {
    mostrarError('Error al exportar a Excel')
  } finally {
    exportandoExcel.value = false
  }
}

const exportarPdf = async () => {
  try {
    exportandoPdf.value = true
    const blob = await reporteService.exportarPdf(filtros.value)
    descargarArchivo(blob, 'reporte-asistencia.pdf')
    mostrarExito('Reporte PDF descargado exitosamente')
  } catch (error) {
    mostrarError('Error al exportar a PDF')
  } finally {
    exportandoPdf.value = false
  }
}

const descargarArchivo = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const limpiarFiltros = () => {
  filtros.value = {
    fechaInicio: '',
    fechaFin: '',
    empleadoId: undefined,
    tipo: undefined
  }
  page.value = 1
  buscarReportes()
}

// Funciones de formato
const formatearFechaHora = (fechaHora: string): string => {
  const fecha = new Date(fechaHora)
  return fecha.toLocaleString('es-PE', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatearTipo = (tipo: TipoMarcacion): string => {
  const tipos = {
    'ENTRADA': 'Entrada',
    'SALIDA_ALMUERZO': 'Salida Almuerzo',
    'RETORNO_ALMUERZO': 'Retorno Almuerzo',
    'SALIDA': 'Salida',
    'FUERA_HORARIO': 'Fuera de Horario'
  }
  return tipos[tipo] || tipo
}

const formatearEstado = (estado: EstadoMarcacion): string => {
  const estados = {
    'PUNTUAL': 'Puntual',
    'TARDANZA': 'Tardanza',
    'FUERA_HORARIO': 'Fuera de Horario'
  }
  return estados[estado] || estado
}

const getColorTipo = (tipo: TipoMarcacion): string => {
  const colores = {
    'ENTRADA': 'green',
    'SALIDA_ALMUERZO': 'orange',
    'RETORNO_ALMUERZO': 'blue',
    'SALIDA': 'purple',
    'FUERA_HORARIO': 'grey'
  }
  return colores[tipo] || 'grey'
}

const getColorEstado = (estado: EstadoMarcacion): string => {
  const colores = {
    'PUNTUAL': 'success',
    'TARDANZA': 'warning',
    'FUERA_HORARIO': 'error'
  }
  return colores[estado] || 'grey'
}

// Funciones de mensajes
const mostrarExito = (mensaje: string) => {
  snackbar.value = {
    show: true,
    message: mensaje,
    color: 'success'
  }
}

const mostrarError = (mensaje: string) => {
  snackbar.value = {
    show: true,
    message: mensaje,
    color: 'error'
  }
}

// Lifecycle
onMounted(() => {
  cargarEmpleados()
  buscarReportes()
})
</script>

<style scoped>
.reportes-container {
  padding: 16px;
}

.gap-2 {
  gap: 8px;
}
</style>