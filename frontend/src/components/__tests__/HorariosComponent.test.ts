import { describe, it, expect, vi } from 'vitest'
import { TipoMarcacion } from '@/types/index'

// Mock the horario service
vi.mock('@/services/horarioService', () => ({
  horarioService: {
    getAll: vi.fn().mockResolvedValue([
      {
        id: 1,
        nombre: 'Horario de Entrada',
        horaInicio: '08:00',
        horaFin: '08:30',
        tipo: TipoMarcacion.ENTRADA,
        activo: true,
        fechaCreacion: '2024-01-01T00:00:00'
      }
    ]),
    getActivos: vi.fn().mockResolvedValue([]),
    create: vi.fn().mockResolvedValue({}),
    update: vi.fn().mockResolvedValue({}),
    delete: vi.fn().mockResolvedValue(undefined),
    checkOverlap: vi.fn().mockResolvedValue(false)
  }
}))

describe('HorariosComponent Utils', () => {

  it('validates time format correctly', () => {
    const timeFormatRule = (value: string) => {
      if (!value) return true
      const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/
      return timeRegex.test(value) || 'Formato de hora inválido (HH:mm)'
    }
    
    expect(timeFormatRule('08:30')).toBe(true)
    expect(timeFormatRule('25:00')).toBe('Formato de hora inválido (HH:mm)')
    expect(timeFormatRule('8:30')).toBe(true)
    expect(timeFormatRule('invalid')).toBe('Formato de hora inválido (HH:mm)')
  })

  it('formats time correctly', () => {
    const formatTime = (time: string): string => {
      if (!time) return ''
      const [hours, minutes] = time.split(':')
      return `${hours}:${minutes}`
    }
    
    expect(formatTime('08:30')).toBe('08:30')
    expect(formatTime('12:00')).toBe('12:00')
    expect(formatTime('')).toBe('')
  })

  it('calculates duration correctly', () => {
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
    expect(calcularDuracion('08:00', '09:00')).toBe('1h')
    expect(calcularDuracion('08:00', '09:30')).toBe('1h 30min')
    expect(calcularDuracion('08:30', '08:00')).toBe('Rango inválido')
  })

  it('gets tipo information correctly', () => {
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

    const getTipoText = (tipo: TipoMarcacion): string => {
      return tiposMarcacion.find(t => t.value === tipo)?.text || tipo
    }

    const getTipoIcon = (tipo: TipoMarcacion): string => {
      return tiposMarcacion.find(t => t.value === tipo)?.icon || 'mdi-clock'
    }

    const getTipoColor = (tipo: TipoMarcacion): string => {
      return tiposMarcacion.find(t => t.value === tipo)?.color || 'primary'
    }
    
    expect(getTipoText(TipoMarcacion.ENTRADA)).toBe('Entrada')
    expect(getTipoIcon(TipoMarcacion.ENTRADA)).toBe('mdi-login')
    expect(getTipoColor(TipoMarcacion.ENTRADA)).toBe('green')
  })
})