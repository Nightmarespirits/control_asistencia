-- Test data for JWT authentication tests

-- Insert test admin user
INSERT INTO usuarios (username, password, email, activo) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjBdtND9TgZF2Nm', 'admin@test.com', true);

-- Insert test horarios
INSERT INTO horarios (nombre, hora_inicio, hora_fin, tipo, activo) VALUES 
('Test Entrada', '08:00:00', '08:30:00', 'ENTRADA', true),
('Test Salida Almuerzo', '12:00:00', '12:30:00', 'SALIDA_ALMUERZO', true),
('Test Retorno Almuerzo', '13:30:00', '14:00:00', 'RETORNO_ALMUERZO', true),
('Test Salida', '17:00:00', '17:30:00', 'SALIDA', true);

-- Insert test empleado
INSERT INTO empleados (codigo_unico, dni, nombres, apellidos, cargo, area, activo) VALUES 
('TEST001', '12345678', 'Test', 'User', 'Tester', 'QA', true);