# MarcacionComponent

## Descripción

El componente `MarcacionComponent` implementa la interfaz de marcación de asistencia para el sistema MVP. Está diseñado para trabajar con el lector de códigos de barras ZKTeco ZKB202S y proporciona una experiencia de usuario optimizada para el registro rápido de asistencia.

## Características Principales

### 1. Captura Automática de DNI
- Input con autofocus que captura automáticamente los datos del lector ZKTeco
- Procesamiento inmediato cuando se detectan 8 dígitos (formato DNI peruano)
- No requiere presionar Enter o botones adicionales

### 2. Validación en Tiempo Real
- Validación de formato DNI (8 dígitos numéricos)
- Prevención de marcaciones duplicadas
- Manejo de empleados no encontrados

### 3. Retroalimentación Visual y Auditiva
- Mensajes contextuales de éxito, error y advertencia
- Efectos de sonido para confirmación (usando Web Audio API)
- Animaciones y transiciones suaves
- Indicadores de carga durante el procesamiento

### 4. Diseño Responsivo
- Interfaz adaptable a diferentes tamaños de pantalla
- Diseño centrado y fácil de usar
- Instrucciones claras para el usuario

## Uso

```vue
<template>
  <MarcacionComponent />
</template>

<script setup lang="ts">
import MarcacionComponent from '@/components/MarcacionComponent.vue'
</script>
```

## Composables Utilizados

### useZKTecoScanner
Maneja la lógica de captura y procesamiento de marcaciones:
- Validación de DNI
- Comunicación con la API backend
- Manejo de estados de carga y mensajes
- Control de marcaciones duplicadas

### useSoundEffects
Proporciona efectos de sonido usando Web Audio API:
- Sonido de éxito (tonos ascendentes)
- Sonido de error (tono grave)
- Inicialización automática del contexto de audio

## Integración con ZKTeco ZKB202S

El lector ZKTeco ZKB202S funciona como un dispositivo HID (Human Interface Device) que emula un teclado USB. Cuando se escanea un código de barras:

1. El lector envía los datos como entrada de teclado
2. El componente captura automáticamente la entrada en el campo de texto
3. Cuando se detectan 8 dígitos, se procesa inmediatamente la marcación
4. El campo se limpia automáticamente para la siguiente lectura

## API Backend

El componente se comunica con el endpoint:
```
POST /api/public/asistencia/marcar
{
  "dni": "12345678"
}
```

## Manejo de Errores

- **404**: Empleado no encontrado
- **409**: Marcación duplicada (dentro de 5 minutos)
- **500**: Error interno del servidor

## Pruebas

Las pruebas se encuentran en `__tests__/MarcacionComponent.test.ts` y cubren:
- Validación de formato DNI
- Funcionalidad básica del componente
- Integración con composables

Para ejecutar las pruebas:
```bash
npm run test
```