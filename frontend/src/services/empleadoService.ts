import api from './api'
import type { Empleado } from '@/types'

export const empleadoService = {
  async obtenerTodos(): Promise<Empleado[]> {
    const response = await api.get('/admin/empleados')
    return response.data.data || response.data
  },

  async obtenerPorId(id: number): Promise<Empleado> {
    const response = await api.get(`/admin/empleados/${id}`)
    return response.data.data || response.data
  },

  async crear(empleado: Omit<Empleado, 'id' | 'codigoUnico'>): Promise<Empleado> {
    const response = await api.post('/admin/empleados', empleado)
    return response.data.data || response.data
  },

  async actualizar(id: number, empleado: Empleado): Promise<Empleado> {
    const response = await api.put(`/admin/empleados/${id}`, empleado)
    return response.data.data || response.data
  },

  async eliminar(id: number): Promise<void> {
    await api.delete(`/admin/empleados/${id}`)
  },

  async buscarPorDni(dni: string): Promise<Empleado | null> {
    try {
      const response = await api.get(`/admin/empleados/buscar/dni/${dni}`)
      return response.data.data || response.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  }
}