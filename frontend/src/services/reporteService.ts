import api from './api'
import type { ReporteRequest, ReporteResponse } from '@/types'

export const reporteService = {
  // Obtener reportes de asistencia con filtros y paginación
  async obtenerReportes(filtros: ReporteRequest): Promise<ReporteResponse> {
    const params = new URLSearchParams()
    
    if (filtros.fechaInicio) params.append('fechaInicio', filtros.fechaInicio)
    if (filtros.fechaFin) params.append('fechaFin', filtros.fechaFin)
    if (filtros.empleadoId) params.append('empleadoId', filtros.empleadoId.toString())
    if (filtros.tipo) params.append('tipo', filtros.tipo)
    if (filtros.page !== undefined) params.append('page', filtros.page.toString())
    if (filtros.size !== undefined) params.append('size', filtros.size.toString())

    const response = await api.get(`/admin/reportes/asistencias?${params.toString()}`)
    return response.data
  },

  // Exportar reporte a Excel
  async exportarExcel(filtros: ReporteRequest): Promise<Blob> {
    const params = new URLSearchParams()
    
    if (filtros.fechaInicio) params.append('fechaInicio', filtros.fechaInicio)
    if (filtros.fechaFin) params.append('fechaFin', filtros.fechaFin)
    if (filtros.empleadoId) params.append('empleadoId', filtros.empleadoId.toString())
    if (filtros.tipo) params.append('tipo', filtros.tipo)

    const response = await api.get(`/admin/reportes/export/excel?${params.toString()}`, {
      responseType: 'blob'
    })
    return response.data
  },

  // Exportar reporte a PDF
  async exportarPdf(filtros: ReporteRequest): Promise<Blob> {
    const params = new URLSearchParams()
    
    if (filtros.fechaInicio) params.append('fechaInicio', filtros.fechaInicio)
    if (filtros.fechaFin) params.append('fechaFin', filtros.fechaFin)
    if (filtros.empleadoId) params.append('empleadoId', filtros.empleadoId.toString())
    if (filtros.tipo) params.append('tipo', filtros.tipo)

    const response = await api.get(`/admin/reportes/export/pdf?${params.toString()}`, {
      responseType: 'blob'
    })
    return response.data
  }
}