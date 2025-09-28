<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card>
          <v-card-title class="text-center">
            <h1>Sistema de Control de Asistencia</h1>
          </v-card-title>
          <v-card-text class="text-center">
            <p>Bienvenido al sistema MVP de control de asistencia</p>
            <p>Configuración inicial completada</p>
            
            <div class="mt-6">
              <v-btn
                color="primary"
                size="large"
                :to="{ name: 'marcacion' }"
                prepend-icon="mdi-qrcode-scan"
              >
                Ir a Marcación de Asistencia
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
// Home view component
</script>