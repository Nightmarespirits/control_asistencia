export interface Empleado {
  id?: number;
  codigoUnico: string;
  dni: string;
  nombres: string;
  apellidos: string;
  cargo: string;
  area: string;
  activo: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

export interface MarcacionRequest {
  dni: string;
}

export interface MarcacionResponse {
  success: boolean;
  mensaje: string;
  empleado: {
    nombres: string;
    apellidos: string;
  };
  tipo: TipoMarcacion;
  fechaHora: string;
  estado: EstadoMarcacion;
}

export enum TipoMarcacion {
  ENTRADA = 'ENTRADA',
  SALIDA_ALMUERZO = 'SALIDA_ALMUERZO',
  RETORNO_ALMUERZO = 'RETORNO_ALMUERZO',
  SALIDA = 'SALIDA',
  FUERA_HORARIO = 'FUERA_HORARIO'
}

export enum EstadoMarcacion {
  PUNTUAL = 'PUNTUAL',
  TARDANZA = 'TARDANZA',
  FUERA_HORARIO = 'FUERA_HORARIO'
}

export interface Horario {
  id?: number;
  nombre: string;
  horaInicio: string; // Format: "HH:mm"
  horaFin: string; // Format: "HH:mm"
  tipo: TipoMarcacion;
  activo?: boolean;
  fechaCreacion?: string;
}

export interface Asistencia {
  id?: number;
  empleado: Empleado;
  fechaHora: string;
  tipo: TipoMarcacion;
  estado: EstadoMarcacion;
  observaciones?: string;
}

export interface ReporteRequest {
  fechaInicio?: string; // Format: "YYYY-MM-DD"
  fechaFin?: string; // Format: "YYYY-MM-DD"
  empleadoId?: number;
  tipoMarcacion?: string;
  page?: number;
  size?: number;
}

export interface ReporteResponse {
  content: Asistencia[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  codigo: string;
  mensaje: string;
}