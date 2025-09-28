<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="text-center pa-8" elevation="4">
          <v-card-text>
            <v-icon size="120" color="grey-lighten-1" class="mb-4">
              mdi-file-question-outline
            </v-icon>
            
            <h1 class="text-h3 font-weight-bold mb-4">404</h1>
            
            <h2 class="text-h5 mb-4">Página no encontrada</h2>
            
            <p class="text-body-1 mb-6">
              Lo sentimos, la página que estás buscando no existe o ha sido movida.
            </p>
            
            <div class="d-flex flex-column flex-sm-row gap-3 justify-center">
              <v-btn
                color="primary"
                variant="elevated"
                size="large"
                to="/"
                prepend-icon="mdi-home"
              >
                Ir al Inicio
              </v-btn>
              
              <v-btn
                color="secondary"
                variant="outlined"
                size="large"
                @click="goBack"
                prepend-icon="mdi-arrow-left"
              >
                Volver Atrás
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.fill-height {
  min-height: calc(100vh - 64px); /* Subtract app bar height */
}

.gap-3 {
  gap: 12px;
}
</style>