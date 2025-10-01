import api from './api';
import type { Horario } from '@/types';

export const horarioService = {
  // Obtener todos los horarios
  async getAll(): Promise<Horario[]> {
    const response = await api.get('/admin/horarios');
    return response.data.data;
  },

  // Obtener horarios activos
  async getActivos(): Promise<Horario[]> {
    const response = await api.get('/admin/horarios/activos');
    return response.data.data;
  },

  // Obtener horario por ID
  async getById(id: number): Promise<Horario> {
    const response = await api.get(`/admin/horarios/${id}`);
    return response.data.data;
  },

  // Crear nuevo horario
  async create(horario: Omit<Horario, 'id'>): Promise<Horario> {
    const response = await api.post('/admin/horarios', horario);
    return response.data.data;
  },

  // Actualizar horario
  async update(id: number, horario: Partial<Horario>): Promise<Horario> {
    const response = await api.put(`/admin/horarios/${id}`, horario);
    return response.data.data;
  },

  // Eliminar horario (soft delete)
  async delete(id: number): Promise<void> {
    await api.delete(`/admin/horarios/${id}`);
  },

  // Verificar solapamientos
  async checkOverlap(horario: Omit<Horario, 'id'>, excludeId?: number): Promise<boolean> {
    const params = new URLSearchParams();
    params.append('tipo', horario.tipo);
    params.append('horaInicio', horario.horaInicio);
    params.append('horaFin', horario.horaFin);
    if (excludeId) {
      params.append('excludeId', excludeId.toString());
    }
    
    const response = await api.get(`/admin/horarios/check-overlap?${params}`);
    return response.data.hasOverlap;
  }
};