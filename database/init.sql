-- Inicialización de la base de datos para el Sistema de Control de Asistencia

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Crear esquema si no existe
CREATE SCHEMA IF NOT EXISTS public;

-- Configurar timezone
SET timezone = 'America/Lima';

-- Crear función para actualizar timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ language 'plpgsql';

-- Crear tablas si no existen (para compatibilidad con JPA)
CREATE TABLE IF NOT EXISTS empleados (
    id BIGSERIAL PRIMARY KEY,
    codigo_unico VARCHAR(20) UNIQUE NOT NULL,
    dni VARCHAR(8) UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS horarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA_ALMUERZO', 'RETORNO_ALMUERZO', 'SALIDA')),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS asistencias (
    id BIGSERIAL PRIMARY KEY,
    empleado_id BIGINT NOT NULL REFERENCES empleados(id),
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA_ALMUERZO', 'RETORNO_ALMUERZO', 'SALIDA', 'FUERA_HORARIO')),
    estado VARCHAR(20) DEFAULT 'PUNTUAL' CHECK (estado IN ('PUNTUAL', 'TARDANZA', 'FUERA_HORARIO')),
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear triggers para actualizar timestamp
DROP TRIGGER IF EXISTS update_empleados_updated_at ON empleados;
CREATE TRIGGER update_empleados_updated_at 
    BEFORE UPDATE ON empleados 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insertar horarios por defecto si no existen
INSERT INTO horarios (nombre, hora_inicio, hora_fin, tipo, activo) 
VALUES 
    ('Entrada Matutina', '07:50:00', '08:20:00', 'ENTRADA', true),
    ('Salida a Almuerzo', '12:30:00', '13:00:00', 'SALIDA_ALMUERZO', true),
    ('Retorno de Almuerzo', '14:00:00', '14:30:00', 'RETORNO_ALMUERZO', true),
    ('Salida Final', '17:30:00', '18:00:00', 'SALIDA', true)
ON CONFLICT DO NOTHING;

-- Insertar usuario administrador por defecto si no existe
-- Contraseña: admin123 (debe ser hasheada en la aplicación)
INSERT INTO usuarios (username, password, email, activo) 
VALUES 
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjBdtND9TgZF2Nm', 'admin@asistencia.com', true)
ON CONFLICT (username) DO NOTHING;

-- Insertar empleados de prueba si no existen
INSERT INTO empleados (codigo_unico, dni, nombres, apellidos, cargo, area, activo) 
VALUES 
    ('EMP001', '12345678', 'Juan Carlos', 'Pérez López', 'Desarrollador', 'Tecnología', true),
    ('EMP002', '87654321', 'María Elena', 'García Rodríguez', 'Analista', 'Sistemas', true),
    ('EMP003', '11223344', 'Pedro Antonio', 'Martínez Silva', 'Gerente', 'Administración', true)
ON CONFLICT (dni) DO NOTHING;

-- Crear índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_empleados_dni ON empleados(dni);
CREATE INDEX IF NOT EXISTS idx_empleados_activo ON empleados(activo);
CREATE INDEX IF NOT EXISTS idx_asistencias_empleado_fecha ON asistencias(empleado_id, fecha_hora);
CREATE INDEX IF NOT EXISTS idx_asistencias_fecha_hora ON asistencias(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_asistencias_tipo ON asistencias(tipo);
CREATE INDEX IF NOT EXISTS idx_horarios_tipo_activo ON horarios(tipo, activo);

-- Comentarios para documentación
COMMENT ON DATABASE asistencia_db IS 'Base de datos para el Sistema MVP de Control de Asistencia';
COMMENT ON SCHEMA public IS 'Esquema principal del sistema de asistencia';
COMMENT ON TABLE empleados IS 'Tabla de empleados del sistema';
COMMENT ON TABLE horarios IS 'Tabla de configuración de horarios de trabajo';
COMMENT ON TABLE asistencias IS 'Tabla de registros de asistencia de empleados';
COMMENT ON TABLE usuarios IS 'Tabla de usuarios administrativos del sistema';