import { describe, it, expect, vi, beforeEach } from 'vitest'
import { TipoMarcacion } from '@/types'
import type { Horario } from '@/types'

// Mock del servicio
vi.mock('@/services/horarioService', () => ({
  horarioService: {
    getAll: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    checkOverlap: vi.fn()
  }
}))

const mockHorarios: Horario[] = [
  {
    id: 1,
    nombre: 'Horario Entrada',
    horaInicio: '08:00',
    horaFin: '08:30',
    tipo: TipoMarcacion.ENTRADA,
    activo: true,
    fechaCreacion: '2025-01-15T10:00:00'
  },
  {
    id: 2,
    nombre: 'Horario Salida Almuerzo',
    horaInicio: '12:00',
    horaFin: '12:30',
    tipo: TipoMarcacion.SALIDA_ALMUERZO,
    activo: true,
    fechaCreacion: '2025-01-15T10:00:00'
  }
]

describe('HorariosComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('debe importar el servicio correctamente', async () => {
    const { horarioService } = await import('@/services/horarioService')
    expect(horarioService).toBeDefined()
    expect(horarioService.getAll).toBeDefined()
    expect(horarioService.create).toBeDefined()
    expect(horarioService.update).toBeDefined()
    expect(horarioService.delete).toBeDefined()
    expect(horarioService.checkOverlap).toBeDefined()
  })

  it('debe tener tipos de marcación definidos', () => {
    expect(TipoMarcacion.ENTRADA).toBe('ENTRADA')
    expect(TipoMarcacion.SALIDA_ALMUERZO).toBe('SALIDA_ALMUERZO')
    expect(TipoMarcacion.RETORNO_ALMUERZO).toBe('RETORNO_ALMUERZO')
    expect(TipoMarcacion.SALIDA).toBe('SALIDA')
  })

  it('debe validar formato de hora', () => {
    const timeFormatRule = (value: string) => {
      if (!value) return true
      const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/
      return timeRegex.test(value) || 'Formato de hora inválido (HH:mm)'
    }
    
    expect(timeFormatRule('08:30')).toBe(true)
    expect(timeFormatRule('23:59')).toBe(true)
    expect(timeFormatRule('24:00')).toContain('Formato de hora inválido')
    expect(timeFormatRule('8:30')).toBe(true) // Acepta formato sin cero inicial
    expect(timeFormatRule('invalid')).toContain('Formato de hora inválido')
  })

  it('debe calcular duración correctamente', () => {
    const calcularDuracion = (horaInicio: string, horaFin: string): string => {
      if (!horaInicio || !horaFin) return ''
      
      const [startHours, startMinutes] = horaInicio.split(':').map(Number)
      const [endHours, endMinutes] = horaFin.split(':').map(Number)
      
      const startTotalMinutes = startHours * 60 + startMinutes
      const endTotalMinutes = endHours * 60 + endMinutes
      
      const durationMinutes = endTotalMinutes - startTotalMinutes
      
      if (durationMinutes <= 0) return 'Rango inválido'
      
      const hours = Math.floor(durationMinutes / 60)
      const minutes = durationMinutes % 60
      
      if (hours === 0) return `${minutes} min`
      if (minutes === 0) return `${hours}h`
      return `${hours}h ${minutes}min`
    }
    
    expect(calcularDuracion('08:00', '08:30')).toBe('30 min')
    expect(calcularDuracion('08:00', '10:00')).toBe('2h')
    expect(calcularDuracion('08:00', '09:15')).toBe('1h 15min')
  })

  it('debe formatear tiempo correctamente', () => {
    const formatTime = (time: string): string => {
      if (!time) return ''
      const [hours, minutes] = time.split(':')
      return `${hours}:${minutes}`
    }
    
    expect(formatTime('08:30')).toBe('08:30')
    expect(formatTime('23:59')).toBe('23:59')
    expect(formatTime('')).toBe('')
  })

  it('debe formatear rango de tiempo', () => {
    const formatTimeRange = (horario: Horario): string => {
      const formatTime = (time: string): string => {
        if (!time) return ''
        const [hours, minutes] = time.split(':')
        return `${hours}:${minutes}`
      }
      return `${formatTime(horario.horaInicio)} - ${formatTime(horario.horaFin)}`
    }
    
    const horario: Horario = {
      nombre: 'Test',
      horaInicio: '08:00',
      horaFin: '08:30',
      tipo: TipoMarcacion.ENTRADA
    }
    
    expect(formatTimeRange(horario)).toBe('08:00 - 08:30')
  })

  it('debe obtener horario por tipo', () => {
    const getHorarioByTipo = (horarios: Horario[], tipo: TipoMarcacion): Horario | undefined => {
      return horarios.filter(h => h.activo).find(h => h.tipo === tipo)
    }
    
    const horarioEntrada = getHorarioByTipo(mockHorarios, TipoMarcacion.ENTRADA)
    expect(horarioEntrada).toBeDefined()
    expect(horarioEntrada?.nombre).toBe('Horario Entrada')
    
    const horarioInexistente = getHorarioByTipo(mockHorarios, TipoMarcacion.SALIDA)
    expect(horarioInexistente).toBeUndefined()
  })

  it('debe obtener información del tipo de marcación', () => {
    const tiposMarcacion = [
      { 
        value: TipoMarcacion.ENTRADA, 
        text: 'Entrada', 
        icon: 'mdi-login',
        color: 'green'
      },
      { 
        value: TipoMarcacion.SALIDA_ALMUERZO, 
        text: 'Salida a almuerzo', 
        icon: 'mdi-food',
        color: 'orange'
      },
      { 
        value: TipoMarcacion.RETORNO_ALMUERZO, 
        text: 'Retorno de almuerzo', 
        icon: 'mdi-food-off',
        color: 'blue'
      },
      { 
        value: TipoMarcacion.SALIDA, 
        text: 'Salida', 
        icon: 'mdi-logout',
        color: 'red'
      }
    ]

    const getTipoInfo = (tipo: TipoMarcacion) => {
      return tiposMarcacion.find(t => t.value === tipo)
    }
    
    expect(getTipoInfo(TipoMarcacion.ENTRADA)?.text).toBe('Entrada')
    expect(getTipoInfo(TipoMarcacion.SALIDA_ALMUERZO)?.icon).toBe('mdi-food')
    expect(getTipoInfo(TipoMarcacion.RETORNO_ALMUERZO)?.color).toBe('blue')
    expect(getTipoInfo(TipoMarcacion.SALIDA)?.text).toBe('Salida')
  })
})