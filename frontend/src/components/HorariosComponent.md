# HorariosComponent

## Descripción

El componente `HorariosComponent` proporciona una interfaz completa para la gestión de horarios de trabajo en el sistema de control de asistencia. Permite configurar los intervalos de tiempo para cada tipo de marcación (entrada, salida a almuerzo, retorno de almuerzo, y salida).

## Características

### Vista Previa de Horarios
- Muestra una vista visual de todos los tipos de marcación configurados
- Indica qué horarios están configurados y cuáles faltan
- Usa colores y iconos distintivos para cada tipo

### Gestión de Horarios
- **Crear**: Formulario para crear nuevos horarios con validaciones
- **Editar**: Modificar horarios existentes
- **Eliminar**: Soft delete que mantiene registros históricos
- **Listar**: Tabla con todos los horarios y su estado

### Validaciones
- **Formato de hora**: Valida formato HH:mm
- **Rango temporal**: La hora de fin debe ser posterior a la de inicio
- **Solapamientos**: Previene conflictos entre horarios del mismo tipo
- **Campos obligatorios**: Nombre, tipo, hora inicio y fin

### Funcionalidades Avanzadas
- **Vista previa en tiempo real**: Muestra cómo se verá el horario mientras se edita
- **Cálculo de duración**: Muestra automáticamente la duración del intervalo
- **Sugerencia de nombres**: Propone nombres basados en el tipo seleccionado
- **Mensajes contextuales**: Feedback claro sobre errores y éxitos

## Tipos de Marcación Soportados

| Tipo | Descripción | Icono | Color |
|------|-------------|-------|-------|
| ENTRADA | Entrada al trabajo | mdi-login | Verde |
| SALIDA_ALMUERZO | Salida a almuerzo | mdi-food | Naranja |
| RETORNO_ALMUERZO | Retorno de almuerzo | mdi-food-off | Azul |
| SALIDA | Salida del trabajo | mdi-logout | Rojo |

## Uso

### En una Vista
```vue
<template>
  <div>
    <HorariosComponent />
  </div>
</template>

<script setup lang="ts">
import HorariosComponent from '@/components/HorariosComponent.vue'
</script>
```

### Como Ruta
```typescript
{
  path: '/horarios',
  name: 'horarios',
  component: () => import('@/views/HorariosView.vue'),
  meta: { requiresAuth: true }
}
```

## API Backend Requerida

El componente espera que existan los siguientes endpoints en el backend:

- `GET /api/admin/horarios` - Obtener todos los horarios
- `GET /api/admin/horarios/activos` - Obtener horarios activos
- `GET /api/admin/horarios/{id}` - Obtener horario por ID
- `POST /api/admin/horarios` - Crear nuevo horario
- `PUT /api/admin/horarios/{id}` - Actualizar horario
- `DELETE /api/admin/horarios/{id}` - Eliminar horario (soft delete)
- `GET /api/admin/horarios/check-overlap` - Verificar solapamientos

## Estructura de Datos

### Horario
```typescript
interface Horario {
  id?: number;
  nombre: string;
  horaInicio: string; // Format: "HH:mm"
  horaFin: string; // Format: "HH:mm"
  tipo: TipoMarcacion;
  activo?: boolean;
  fechaCreacion?: string;
}
```

## Dependencias

- Vue 3 con Composition API
- Vuetify 3 para componentes UI
- Axios para llamadas HTTP
- TypeScript para tipado

## Testing

El componente incluye tests unitarios que cubren:
- Validaciones de formato de hora
- Cálculo de duración
- Formateo de tiempo
- Lógica de obtención de horarios por tipo
- Funciones de utilidad

Para ejecutar los tests:
```bash
npm test -- HorariosComponent.test.ts
```

## Notas de Implementación

- El componente usa `reactive` para el formulario y `ref` para estados simples
- Las validaciones se ejecutan en tiempo real
- Los mensajes de error se muestran usando Vuetify's snackbar
- El componente es completamente responsivo
- Soporta temas de Vuetify automáticamente