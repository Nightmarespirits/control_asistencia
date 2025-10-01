<template>
  <div class="horarios-container">
    <!-- Header con título y botón nuevo -->
    <v-card class="mb-4">
      <v-card-title class="d-flex justify-space-between align-center">
        <span class="text-h5">Configuración de Horarios</span>
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="abrirDialogoNuevo"
        >
          Nuevo Horario
        </v-btn>
      </v-card-title>
    </v-card>

    <!-- Vista previa de horarios configurados -->
    <v-card class="mb-4">
      <v-card-title>
        <v-icon class="mr-2">mdi-clock-outline</v-icon>
        Vista Previa de Horarios
      </v-card-title>
      <v-card-text>
        <v-row>
          <v-col
            v-for="tipo in tiposMarcacion"
            :key="tipo.value"
            cols="12"
            md="6"
            lg="3"
          >
            <v-card
              :color="
                getHorarioByTipo(tipo.value) ? 'success' : 'grey-lighten-3'
              "
              :variant="getHorarioByTipo(tipo.value) ? 'tonal' : 'outlined'"
              class="text-center"
            >
              <v-card-title class="text-subtitle-1">
                <v-icon class="mr-1">{{ tipo.icon }}</v-icon>
                {{ tipo.text }}
              </v-card-title>
              <v-card-text>
                <div v-if="getHorarioByTipo(tipo.value)" class="text-h6">
                  {{ formatTimeRange(getHorarioByTipo(tipo.value)!) }}
                </div>
                <div v-else class="text-body-2 text-grey">No configurado</div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Tabla de horarios -->
    <v-card>
      <v-card-title>
        <v-icon class="mr-2">mdi-table</v-icon>
        Lista de Horarios
      </v-card-title>
      <v-data-table
        :headers="headers"
        :items="horarios"
        :loading="loading"
        class="elevation-0"
        item-key="id"
      >
        <template #[`item.tipo`]="{ item }">
          <v-chip :color="getTipoColor(item.tipo)" size="small" variant="tonal">
            <v-icon start>{{ getTipoIcon(item.tipo) }}</v-icon>
            {{ getTipoText(item.tipo) }}
          </v-chip>
        </template>

        <template #[`item.horario`]="{ item }">
          <span class="font-weight-medium">
            {{ formatTimeRange(item) }}
          </span>
        </template>

        <template #[`item.activo`]="{ item }">
          <v-chip
            :color="item.activo ? 'success' : 'error'"
            size="small"
            variant="tonal"
          >
            {{ item.activo ? "Activo" : "Inactivo" }}
          </v-chip>
        </template>

        <template #[`item.actions`]="{ item }">
          <v-btn
            icon="mdi-pencil"
            size="small"
            variant="text"
            @click="editarHorario(item)"
          />
          <v-btn
            icon="mdi-delete"
            size="small"
            variant="text"
            color="error"
            @click="confirmarEliminar(item)"
          />
        </template>
      </v-data-table>
    </v-card>

    <!-- Diálogo para crear/editar horario -->
    <v-dialog v-model="dialogo" max-width="600px" persistent>
      <v-card>
        <v-card-title>
          <span class="text-h5">
            {{ editando ? "Editar Horario" : "Nuevo Horario" }}
          </span>
        </v-card-title>

        <v-card-text>
          <v-form ref="form" v-model="formularioValido">
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="horarioForm.nombre"
                  label="Nombre del horario"
                  :rules="[rules.required, rules.maxLength(50)]"
                  prepend-icon="mdi-tag"
                  variant="outlined"
                />
              </v-col>

              <v-col cols="12">
                <v-select
                  v-model="horarioForm.tipo"
                  :items="tiposMarcacion"
                  label="Tipo de marcación"
                  :rules="[rules.required]"
                  prepend-icon="mdi-clock-outline"
                  variant="outlined"
                  @update:model-value="onTipoChange"
                >
                  <template #item="{ props, item }">
                    <v-list-item v-bind="props">
                      <template #prepend>
                        <v-icon>{{ item.raw.icon }}</v-icon>
                      </template>
                    </v-list-item>
                  </template>
                </v-select>
              </v-col>

              <v-col cols="6">
                <v-text-field
                  v-model="horarioForm.horaInicio"
                  label="Hora de inicio"
                  type="time"
                  :rules="[rules.required, rules.timeFormat]"
                  prepend-icon="mdi-clock-start"
                  variant="outlined"
                  @blur="validarRangoHorario"
                />
              </v-col>

              <v-col cols="6">
                <v-text-field
                  v-model="horarioForm.horaFin"
                  label="Hora de fin"
                  type="time"
                  :rules="[rules.required, rules.timeFormat, rules.timeRange]"
                  prepend-icon="mdi-clock-end"
                  variant="outlined"
                  @blur="validarRangoHorario"
                />
              </v-col>
            </v-row>

            <!-- Mensaje de validación de solapamiento -->
            <v-alert
              v-if="mensajeSolapamiento"
              type="warning"
              variant="tonal"
              class="mt-3"
            >
              {{ mensajeSolapamiento }}
            </v-alert>

            <!-- Vista previa del horario -->
            <v-card
              v-if="horarioForm.horaInicio && horarioForm.horaFin"
              variant="tonal"
              color="info"
              class="mt-3"
            >
              <v-card-text class="text-center">
                <v-icon class="mr-2">mdi-eye</v-icon>
                <strong>Vista previa:</strong>
                {{ horarioForm.nombre || "Nuevo horario" }} -
                {{ formatTime(horarioForm.horaInicio) }} a
                {{ formatTime(horarioForm.horaFin) }}
                <br />
                <small class="text-medium-emphasis">
                  Duración: {{ calcularDuracion() }}
                </small>
              </v-card-text>
            </v-card>
          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn text="Cancelar" @click="cerrarDialogo" />
          <v-btn
            color="primary"
            text="Guardar"
            :loading="guardando"
            :disabled="!formularioValido || !!mensajeSolapamiento"
            @click="guardarHorario"
          />
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Diálogo de confirmación para eliminar -->
    <v-dialog v-model="dialogoEliminar" max-width="400px">
      <v-card>
        <v-card-title class="text-h5"> Confirmar eliminación </v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar el horario "{{
            horarioAEliminar?.nombre
          }}"? <br /><br />
          <small class="text-medium-emphasis">
            Esta acción desactivará el horario pero mantendrá los registros
            históricos.
          </small>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text="Cancelar" @click="dialogoEliminar = false" />
          <v-btn
            color="error"
            text="Eliminar"
            :loading="eliminando"
            @click="eliminarHorario"
          />
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar para mensajes -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000">
      {{ snackbar.message }}
      <template #actions>
        <v-btn variant="text" @click="snackbar.show = false"> Cerrar </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { horarioService } from "@/services/horarioService";
import type { Horario } from "@/types";
import { TipoMarcacion } from "@/types";

// Estado reactivo
const horarios = ref<Horario[]>([]);
const loading = ref(false);
const dialogo = ref(false);
const dialogoEliminar = ref(false);
const editando = ref(false);
const guardando = ref(false);
const eliminando = ref(false);
const formularioValido = ref(false);
const mensajeSolapamiento = ref("");
const horarioAEliminar = ref<Horario | null>(null);

// Referencias del formulario
const form = ref();

// Formulario reactivo
const horarioForm = reactive<Omit<Horario, "id">>({
  nombre: "",
  horaInicio: "",
  horaFin: "",
  tipo: TipoMarcacion.ENTRADA,
  activo: true,
});

// Snackbar para mensajes
const snackbar = reactive({
  show: false,
  message: "",
  color: "success",
});

// Configuración de la tabla
const headers = [
  { title: "Nombre", key: "nombre", sortable: true },
  { title: "Tipo", key: "tipo", sortable: true },
  { title: "Horario", key: "horario", sortable: false },
  { title: "Estado", key: "activo", sortable: true },
  { title: "Acciones", key: "actions", sortable: false, width: "120px" },
];

// Tipos de marcación con iconos
const tiposMarcacion = [
  {
    value: TipoMarcacion.ENTRADA,
    text: "Entrada",
    icon: "mdi-login",
    color: "green",
  },
  {
    value: TipoMarcacion.SALIDA_ALMUERZO,
    text: "Salida a almuerzo",
    icon: "mdi-food",
    color: "orange",
  },
  {
    value: TipoMarcacion.RETORNO_ALMUERZO,
    text: "Retorno de almuerzo",
    icon: "mdi-food-off",
    color: "blue",
  },
  {
    value: TipoMarcacion.SALIDA,
    text: "Salida",
    icon: "mdi-logout",
    color: "red",
  },
];

// Reglas de validación
const rules = {
  required: (value: string) => !!value || "Este campo es obligatorio",
  maxLength: (max: number) => (value: string) =>
    !value || value.length <= max || `Máximo ${max} caracteres`,
  timeFormat: (value: string) => {
    if (!value) return true;
    const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
    return timeRegex.test(value) || "Formato de hora inválido (HH:mm)";
  },
  timeRange: (value: string) => {
    if (!value || !horarioForm.horaInicio) return true;
    return (
      horarioForm.horaInicio < value ||
      "La hora de fin debe ser posterior a la hora de inicio"
    );
  },
};

// Computed properties
const horariosActivos = computed(() => horarios.value.filter((h) => h.activo));

// Métodos
const cargarHorarios = async () => {
  try {
    loading.value = true;
    horarios.value = await horarioService.getAll();
  } catch (error) {
    mostrarMensaje("Error al cargar horarios", "error");
    console.error("Error:", error);
  } finally {
    loading.value = false;
  }
};

const abrirDialogoNuevo = () => {
  editando.value = false;
  resetearFormulario();
  dialogo.value = true;
};

const editarHorario = (horario: Horario) => {
  editando.value = true;
  Object.assign(horarioForm, {
    nombre: horario.nombre,
    horaInicio: horario.horaInicio,
    horaFin: horario.horaFin,
    tipo: horario.tipo,
    activo: horario.activo,
  });
  dialogo.value = true;
};

const resetearFormulario = () => {
  Object.assign(horarioForm, {
    nombre: "",
    horaInicio: "",
    horaFin: "",
    tipo: TipoMarcacion.ENTRADA,
    activo: true,
  });
  mensajeSolapamiento.value = "";
  if (form.value) {
    form.value.resetValidation();
  }
};

const cerrarDialogo = () => {
  dialogo.value = false;
  resetearFormulario();
};

const onTipoChange = () => {
  // Limpiar mensaje de solapamiento al cambiar tipo
  mensajeSolapamiento.value = "";
  // Sugerir nombre basado en el tipo
  if (!horarioForm.nombre) {
    const tipoInfo = tiposMarcacion.find((t) => t.value === horarioForm.tipo);
    if (tipoInfo) {
      horarioForm.nombre = `Horario ${tipoInfo.text}`;
    }
  }
};

const validarRangoHorario = async () => {
  if (!horarioForm.horaInicio || !horarioForm.horaFin || !horarioForm.tipo) {
    mensajeSolapamiento.value = "";
    return;
  }

  try {
    const excludeId = editando.value
      ? horarios.value.find((h) => h.tipo === horarioForm.tipo)?.id
      : undefined;

    const hasOverlap = await horarioService.checkOverlap(
      horarioForm,
      excludeId
    );

    if (hasOverlap) {
      mensajeSolapamiento.value = `Ya existe un horario que se solapa con este rango para el tipo ${getTipoText(
        horarioForm.tipo
      )}`;
    } else {
      mensajeSolapamiento.value = "";
    }
  } catch (error) {
    console.error("Error validando solapamiento:", error);
  }
};

const guardarHorario = async () => {
  if (!form.value.validate() || mensajeSolapamiento.value) return;

  try {
    guardando.value = true;

    if (editando.value) {
      const horarioExistente = horarios.value.find(
        (h) => h.tipo === horarioForm.tipo
      );
      if (horarioExistente?.id) {
        await horarioService.update(horarioExistente.id, horarioForm);
        mostrarMensaje("Horario actualizado correctamente", "success");
      }
    } else {
      await horarioService.create(horarioForm);
      mostrarMensaje("Horario creado correctamente", "success");
    }

    await cargarHorarios();
    cerrarDialogo();
  } catch (error: any) {
    const mensaje = error.response?.data?.mensaje || "Error al guardar horario";
    mostrarMensaje(mensaje, "error");
    console.error("Error:", error);
  } finally {
    guardando.value = false;
  }
};

const confirmarEliminar = (horario: Horario) => {
  horarioAEliminar.value = horario;
  dialogoEliminar.value = true;
};

const eliminarHorario = async () => {
  if (!horarioAEliminar.value?.id) return;

  try {
    eliminando.value = true;
    await horarioService.delete(horarioAEliminar.value.id);
    mostrarMensaje("Horario eliminado correctamente", "success");
    await cargarHorarios();
    dialogoEliminar.value = false;
    horarioAEliminar.value = null;
  } catch (error: any) {
    const mensaje =
      error.response?.data?.mensaje || "Error al eliminar horario";
    mostrarMensaje(mensaje, "error");
    console.error("Error:", error);
  } finally {
    eliminando.value = false;
  }
};

const mostrarMensaje = (message: string, color: string) => {
  snackbar.message = message;
  snackbar.color = color;
  snackbar.show = true;
};

// Funciones de utilidad
const getHorarioByTipo = (tipo: TipoMarcacion): Horario | undefined => {
  return horariosActivos.value.find((h) => h.tipo === tipo);
};

const getTipoText = (tipo: TipoMarcacion): string => {
  return tiposMarcacion.find((t) => t.value === tipo)?.text || tipo;
};

const getTipoIcon = (tipo: TipoMarcacion): string => {
  return tiposMarcacion.find((t) => t.value === tipo)?.icon || "mdi-clock";
};

const getTipoColor = (tipo: TipoMarcacion): string => {
  return tiposMarcacion.find((t) => t.value === tipo)?.color || "primary";
};

const formatTime = (time: string): string => {
  if (!time) return "";
  const [hours, minutes] = time.split(":");
  return `${hours}:${minutes}`;
};

const formatTimeRange = (horario: Horario): string => {
  return `${formatTime(horario.horaInicio)} - ${formatTime(horario.horaFin)}`;
};

const calcularDuracion = (): string => {
  if (!horarioForm.horaInicio || !horarioForm.horaFin) return "";

  const [startHours, startMinutes] = horarioForm.horaInicio
    .split(":")
    .map(Number);
  const [endHours, endMinutes] = horarioForm.horaFin.split(":").map(Number);

  const startTotalMinutes = startHours * 60 + startMinutes;
  const endTotalMinutes = endHours * 60 + endMinutes;

  const durationMinutes = endTotalMinutes - startTotalMinutes;

  if (durationMinutes <= 0) return "Rango inválido";

  const hours = Math.floor(durationMinutes / 60);
  const minutes = durationMinutes % 60;

  if (hours === 0) return `${minutes} min`;
  if (minutes === 0) return `${hours}h`;
  return `${hours}h ${minutes}min`;
};

// Lifecycle
onMounted(() => {
  cargarHorarios();
});
</script>

<style scoped>
.horarios-container {
  padding: 16px;
}

.v-card-title {
  font-weight: 500;
}

.text-h5 {
  font-weight: 500;
}
</style>