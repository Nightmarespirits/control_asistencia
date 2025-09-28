<template>
  <v-app>
    <!-- Simple App Bar for public pages -->
    <v-app-bar app color="primary" dark elevation="1">
      <v-app-bar-title>Sistema de Control de Asistencia</v-app-bar-title>
      
      <v-spacer />
      
      <!-- Navigation buttons -->
      <v-btn to="/" text>
        <v-icon left>mdi-home</v-icon>
        Inicio
      </v-btn>
      
      <v-btn to="/marcacion" text>
        <v-icon left>mdi-clock-check</v-icon>
        Marcación
      </v-btn>
      
      <!-- Login button -->
      <v-btn to="/login" text>
        <v-icon left>mdi-login</v-icon>
        Iniciar Sesión
      </v-btn>
    </v-app-bar>

    <!-- Main Content -->
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
// No additional logic needed for public layout
</script>

<style scoped>
/* Public layout specific styles */
</style>