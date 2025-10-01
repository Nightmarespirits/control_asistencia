import api from './api'
import type { ReporteRequest, ReporteResponse } from '@/types'

export const reporteService = {
  // Obtener reportes de asistencia con filtros y paginación
  async obtenerReportes(filtros: ReporteRequest): Promise<ReporteResponse> {
    // Extraer page y size para los query parameters
    const { page, size, ...requestBody } = filtros
    
    // Construir query parameters para paginación
    const params = new URLSearchParams()
    if (page !== undefined) params.append('page', page.toString())
    if (size !== undefined) params.append('size', size.toString())
    
    // Enviar el cuerpo de la petición como JSON
    const response = await api.post(`/admin/reportes/asistencias?${params.toString()}`, requestBody)
    return response.data
  },

  // Exportar reporte a Excel
  async exportarExcel(filtros: ReporteRequest): Promise<Blob> {
    // Remover page y size para exportación
    const { page, size, ...requestBody } = filtros

    const response = await api.post(`/admin/reportes/export/excel`, requestBody, {
      responseType: 'blob'
    })
    return response.data
  },

  // Exportar reporte a PDF
  async exportarPdf(filtros: ReporteRequest): Promise<Blob> {
    // Remover page y size para exportación
    const { page, size, ...requestBody } = filtros

    const response = await api.post(`/admin/reportes/export/pdf`, requestBody, {
      responseType: 'blob'
    })
    return response.data
  }
}