-- Datos iniciales para el Sistema de Control de Asistencia
-- Este archivo se ejecuta automáticamente por Spring Boot

-- Insertar horarios por defecto si no existen
INSERT INTO horarios (nombre, hora_inicio, hora_fin, tipo, activo) 
SELECT 'Entrada Matutina', '07:50:00', '08:20:00', 'ENTRADA', true
WHERE NOT EXISTS (SELECT 1 FROM horarios WHERE tipo = 'ENTRADA' AND activo = true);

INSERT INTO horarios (nombre, hora_inicio, hora_fin, tipo, activo) 
SELECT 'Salida a Almuerzo', '12:30:00', '13:00:00', 'SALIDA_ALMUERZO', true
WHERE NOT EXISTS (SELECT 1 FROM horarios WHERE tipo = 'SALIDA_ALMUERZO' AND activo = true);

INSERT INTO horarios (nombre, hora_inicio, hora_fin, tipo, activo) 
SELECT 'Retorno de Almuerzo', '14:00:00', '14:30:00', 'RETORNO_ALMUERZO', true
WHERE NOT EXISTS (SELECT 1 FROM horarios WHERE tipo = 'RETORNO_ALMUERZO' AND activo = true);

INSERT INTO horarios (nombre, hora_inicio, hora_fin, tipo, activo) 
SELECT 'Salida Final', '17:30:00', '18:00:00', 'SALIDA', true
WHERE NOT EXISTS (SELECT 1 FROM horarios WHERE tipo = 'SALIDA' AND activo = true);

-- Insertar usuario administrador por defecto si no existe
-- Contraseña: admin123 (hasheada con BCrypt)
INSERT INTO usuarios (username, password, email, activo) 
SELECT 'admin', '$2a$12$CL8r.i9rdmIJQ3JWMR0oEOTEwmhbuzsANYVgwoEIFoLp61yq9rJZO', 'admin@asistencia.com', true
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

-- Insertar empleados de prueba si no existen
INSERT INTO empleados (codigo_unico, dni, nombres, apellidos, cargo, area, activo) 
SELECT 'EMP001', '12345678', 'Juan Carlos', 'Pérez López', 'Desarrollador', 'Tecnología', true
WHERE NOT EXISTS (SELECT 1 FROM empleados WHERE dni = '12345678');

INSERT INTO empleados (codigo_unico, dni, nombres, apellidos, cargo, area, activo) 
SELECT 'EMP002', '87654321', 'María Elena', 'García Rodríguez', 'Analista', 'Sistemas', true
WHERE NOT EXISTS (SELECT 1 FROM empleados WHERE dni = '87654321');

INSERT INTO empleados (codigo_unico, dni, nombres, apellidos, cargo, area, activo) 
SELECT 'EMP003', '11223344', 'Pedro Antonio', 'Martínez Silva', 'Gerente', 'Administración', true
WHERE NOT EXISTS (SELECT 1 FROM empleados WHERE dni = '11223344');