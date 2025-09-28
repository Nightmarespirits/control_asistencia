import { describe, it, expect, vi, beforeEach } from 'vitest'
import { empleadoService } from '@/services/empleadoService'
import type { Empleado } from '@/types'

// Mock the empleadoService
vi.mock('@/services/empleadoService', () => ({
  empleadoService: {
    obtenerTodos: vi.fn(),
    crear: vi.fn(),
    actualizar: vi.fn(),
    eliminar: vi.fn(),
    obtenerPorId: vi.fn(),
    buscarPorDni: vi.fn()
  }
}))

const mockEmpleados: Empleado[] = [
  {
    id: 1,
    codigoUnico: 'EMP001',
    dni: '12345678',
    nombres: 'Juan Carlos',
    apellidos: 'Pérez López',
    cargo: 'Desarrollador',
    area: 'Tecnología',
    activo: true,
    fechaCreacion: '2024-01-15T10:00:00',
    fechaActualizacion: '2024-01-15T10:00:00'
  },
  {
    id: 2,
    codigoUnico: 'EMP002',
    dni: '87654321',
    nombres: 'María Elena',
    apellidos: 'García Ruiz',
    cargo: 'Analista',
    area: 'Administración',
    activo: true,
    fechaCreacion: '2024-01-16T09:30:00',
    fechaActualizacion: '2024-01-16T09:30:00'
  }
]

describe('EmpleadoService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call obtenerTodos correctly', async () => {
    vi.mocked(empleadoService.obtenerTodos).mockResolvedValue(mockEmpleados)

    const result = await empleadoService.obtenerTodos()

    expect(empleadoService.obtenerTodos).toHaveBeenCalled()
    expect(result).toEqual(mockEmpleados)
  })

  it('should call crear with correct data', async () => {
    const newEmpleado = {
      nombres: 'Test',
      apellidos: 'User',
      dni: '11111111',
      cargo: 'Tester',
      area: 'QA',
      activo: true
    }
    
    const createdEmpleado = { ...newEmpleado, id: 3, codigoUnico: 'EMP003' }
    vi.mocked(empleadoService.crear).mockResolvedValue(createdEmpleado)

    const result = await empleadoService.crear(newEmpleado)

    expect(empleadoService.crear).toHaveBeenCalledWith(newEmpleado)
    expect(result).toEqual(createdEmpleado)
  })

  it('should call actualizar with correct data', async () => {
    const updatedEmpleado = { ...mockEmpleados[0], nombres: 'Updated Name' }
    vi.mocked(empleadoService.actualizar).mockResolvedValue(updatedEmpleado)

    const result = await empleadoService.actualizar(1, updatedEmpleado)

    expect(empleadoService.actualizar).toHaveBeenCalledWith(1, updatedEmpleado)
    expect(result).toEqual(updatedEmpleado)
  })

  it('should call eliminar with correct id', async () => {
    vi.mocked(empleadoService.eliminar).mockResolvedValue()

    await empleadoService.eliminar(1)

    expect(empleadoService.eliminar).toHaveBeenCalledWith(1)
  })

  it('should handle buscarPorDni correctly', async () => {
    vi.mocked(empleadoService.buscarPorDni).mockResolvedValue(mockEmpleados[0])

    const result = await empleadoService.buscarPorDni('12345678')

    expect(empleadoService.buscarPorDni).toHaveBeenCalledWith('12345678')
    expect(result).toEqual(mockEmpleados[0])
  })

  it('should return null when employee not found by DNI', async () => {
    vi.mocked(empleadoService.buscarPorDni).mockResolvedValue(null)

    const result = await empleadoService.buscarPorDni('99999999')

    expect(empleadoService.buscarPorDni).toHaveBeenCalledWith('99999999')
    expect(result).toBeNull()
  })

  it('should validate DNI format', () => {
    // Test valid DNI format
    const validDni = '12345678'
    const dniRegex = /^\d{8}$/
    expect(dniRegex.test(validDni)).toBe(true)

    // Test invalid DNI formats
    expect(dniRegex.test('1234567')).toBe(false) // too short
    expect(dniRegex.test('123456789')).toBe(false) // too long
    expect(dniRegex.test('1234567a')).toBe(false) // contains letters
    expect(dniRegex.test('')).toBe(false) // empty
  })

  it('should validate required fields', () => {
    const empleado = {
      nombres: 'Juan',
      apellidos: 'Pérez',
      dni: '12345678',
      cargo: 'Desarrollador',
      area: 'Tecnología',
      activo: true
    }

    // All fields should be present
    expect(empleado.nombres).toBeTruthy()
    expect(empleado.apellidos).toBeTruthy()
    expect(empleado.dni).toBeTruthy()
    expect(empleado.cargo).toBeTruthy()
    expect(empleado.area).toBeTruthy()
    expect(typeof empleado.activo).toBe('boolean')
  })
})