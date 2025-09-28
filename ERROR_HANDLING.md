# Sistema de Manejo Global de Errores

Este documento describe el sistema completo de manejo de errores implementado en el sistema de control de asistencia.

## Arquitectura del Sistema de Errores

### Backend (Spring Boot)

#### 1. GlobalExceptionHandler
- **Ubicación**: `backend/src/main/java/com/asistencia/exception/GlobalExceptionHandler.java`
- **Propósito**: Maneja todas las excepciones de forma centralizada y proporciona respuestas consistentes

**Tipos de errores manejados:**
- `EmpleadoNotFoundException` - Empleado no encontrado (404)
- `MarcacionDuplicadaException` - Marcación duplicada (409)
- `EmpleadoValidationException` - Errores de validación de empleados (400)
- `AuthenticationException` - Errores de autenticación (401)
- `BadCredentialsException` - Credenciales incorrectas (401)
- `AccessDeniedException` - Acceso denegado (403)
- `DataIntegrityViolationException` - Violaciones de integridad de datos (409)
- `MethodArgumentNotValidException` - Errores de validación de campos (400)
- `HttpRequestMethodNotSupportedException` - Método HTTP no soportado (405)
- `HttpMessageNotReadableException` - JSON malformado (400)
- `Exception` - Errores genéricos (500)

#### 2. ErrorLoggingService
- **Ubicación**: `backend/src/main/java/com/asistencia/service/ErrorLoggingService.java`
- **Propósito**: Proporciona logging estructurado con contexto adicional usando MDC (Mapped Diagnostic Context)

**Tipos de logging:**
- `logAuthenticationError()` - Errores de autenticación
- `logAccessDeniedError()` - Errores de acceso denegado
- `logValidationError()` - Errores de validación
- `logDataIntegrityError()` - Errores de integridad de datos
- `logInternalServerError()` - Errores internos del servidor
- `logMarcacionError()` - Errores específicos de marcación
- `logSuspiciousActivity()` - Actividad sospechosa

**Características del logging:**
- Extracción automática de IP real del cliente (X-Forwarded-For, X-Real-IP)
- Sanitización de parámetros sensibles (passwords, tokens, secrets)
- Contexto estructurado con MDC para facilitar análisis
- Diferentes niveles de log según la severidad

#### 3. Configuración de Logging
- **Ubicación**: `backend/src/main/resources/application.yml`
- **Características**:
  - Logs rotativos con tamaño máximo de 10MB
  - Retención de 30 días de historial
  - Formato consistente con timestamp, thread, nivel y mensaje
  - Archivo de log: `logs/asistencia.log`

### Frontend (Vue.js)

#### 1. Sistema de Notificaciones Globales
- **Composable**: `frontend/src/composables/useNotifications.ts`
- **Componente**: `frontend/src/components/GlobalNotifications.vue`

**Características:**
- Notificaciones tipo toast con diferentes tipos (success, error, warning, info)
- Auto-eliminación configurable con timeout
- Notificaciones persistentes opcionales
- Gestión centralizada de estado
- Métodos de conveniencia para cada tipo

#### 2. Interceptores de API
- **Ubicación**: `frontend/src/services/api.ts`
- **Funcionalidades**:
  - Manejo automático de errores HTTP
  - Refresh automático de tokens JWT
  - Logging de requests/responses en desarrollo
  - Notificaciones automáticas para errores específicos
  - Redirección automática en caso de sesión expirada

**Manejo de errores por código:**
- `400` - Errores de validación (solo para endpoints no-marcación)
- `401` - Token expirado (intenta refresh automático)
- `403` - Sin permisos
- `404` - Recurso no encontrado (solo para endpoints no-marcación)
- `409` - Conflictos (solo para endpoints no-marcación)
- `422` - Error de procesamiento
- `500+` - Errores del servidor

## Flujo de Manejo de Errores

### 1. Error en el Backend
```
1. Excepción ocurre en cualquier parte del código
2. GlobalExceptionHandler captura la excepción
3. ErrorLoggingService registra el error con contexto
4. Se retorna respuesta HTTP estandarizada
```

### 2. Error en el Frontend
```
1. Request HTTP falla
2. Interceptor de Axios captura el error
3. Se determina el tipo de error y acción apropiada
4. Se muestra notificación global (si aplica)
5. Se ejecuta lógica específica (redirect, retry, etc.)
```

## Ejemplos de Uso

### Backend - Lanzar Excepción Personalizada
```java
@Service
public class EmpleadoService {
    public Empleado buscarPorDni(String dni) {
        return empleadoRepository.findByDni(dni)
            .orElseThrow(() -> new EmpleadoNotFoundException("Empleado con DNI " + dni + " no encontrado"));
    }
}
```

### Frontend - Mostrar Notificación
```typescript
import { globalNotifications } from '@/composables/useNotifications'

// Mostrar notificación de éxito
globalNotifications.showSuccess('Empleado creado correctamente')

// Mostrar notificación de error persistente
globalNotifications.showError('Error crítico del sistema', { 
  persistent: true 
})
```

### Frontend - Manejo de Error Específico
```typescript
try {
  await api.post('/admin/empleados', empleadoData)
  globalNotifications.showSuccess('Empleado creado correctamente')
} catch (error) {
  // El interceptor ya manejó la notificación global
  // Aquí solo manejamos lógica específica del componente
  console.error('Error creating employee:', error)
}
```

## Estructura de Respuestas de Error

### Respuesta Estándar de Error
```json
{
  "success": false,
  "mensaje": "Descripción del error",
  "timestamp": "2025-01-15T10:30:00",
  "status": 400
}
```

### Respuesta de Error de Validación
```json
{
  "success": false,
  "mensaje": "Error de validación",
  "errores": {
    "dni": "DNI es requerido",
    "nombres": "Nombres no puede estar vacío"
  },
  "timestamp": "2025-01-15T10:30:00",
  "status": 400
}
```

### Respuesta de Error de Marcación
```json
{
  "success": false,
  "mensaje": "Empleado no encontrado",
  "timestamp": "2025-01-15T10:30:00"
}
```

## Logging Estructurado

### Ejemplo de Log de Autenticación
```
2025-01-15 10:30:00 [http-nio-8080-exec-1] WARN  c.a.s.ErrorLoggingService - Authentication failed for user: admin from IP: 192.168.1.100 - Bad credentials
MDC: {event_type=AUTHENTICATION_ERROR, username=admin, ip_address=192.168.1.100, user_agent=Mozilla/5.0..., timestamp=2025-01-15T10:30:00}
```

### Ejemplo de Log de Error Interno
```
2025-01-15 10:30:00 [http-nio-8080-exec-1] ERROR c.a.s.ErrorLoggingService - Internal server error on POST /api/admin/empleados: Database connection failed
MDC: {event_type=INTERNAL_SERVER_ERROR, endpoint=/api/admin/empleados, method=POST, ip_address=192.168.1.100, exception_class=SQLException, request_params={nombre=John, password=[REDACTED]}}
```

## Testing

### Backend Tests
- `GlobalExceptionHandlerTest` - Pruebas del manejador global
- `ErrorLoggingServiceTest` - Pruebas del servicio de logging

### Frontend Tests
- `useNotifications.test.ts` - Pruebas del sistema de notificaciones

## Configuración

### Variables de Entorno
```yaml
# Logging level
logging.level.com.asistencia: INFO
logging.level.com.asistencia.exception: WARN

# Log file configuration
logging.file.name: logs/asistencia.log
logging.file.max-size: 10MB
logging.file.max-history: 30
```

### Personalización de Notificaciones
```typescript
// Configurar timeout global por defecto
const customNotifications = useNotifications()
customNotifications.addNotification({
  message: 'Mensaje personalizado',
  type: 'info',
  timeout: 10000, // 10 segundos
  persistent: false
})
```

## Mejores Prácticas

### Backend
1. **Usar excepciones específicas** en lugar de genéricas
2. **Incluir contexto útil** en los mensajes de error
3. **No exponer información sensible** en los mensajes de error
4. **Usar logging apropiado** según la severidad del error

### Frontend
1. **Permitir que el interceptor maneje errores globales** automáticamente
2. **Manejar errores específicos** solo cuando se necesite lógica adicional
3. **Usar notificaciones apropiadas** según el contexto del usuario
4. **No mostrar errores técnicos** al usuario final

## Monitoreo y Análisis

### Métricas Importantes
- Frecuencia de errores por endpoint
- Tipos de errores más comunes
- Patrones de errores por IP/usuario
- Tiempo de respuesta en errores

### Alertas Recomendadas
- Más de 10 errores 500 en 5 minutos
- Más de 50 errores de autenticación por IP en 1 hora
- Errores de base de datos consecutivos
- Actividad sospechosa detectada

Este sistema proporciona una base sólida para el manejo, logging y monitoreo de errores en toda la aplicación.