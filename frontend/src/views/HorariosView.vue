<template>
  <div class="horarios-view">
    <HorariosComponent />
  </div>
</template>

<script setup lang="ts">
import HorariosComponent from '@/components/HorariosComponent.vue'
</script>

<style scoped>
.horarios-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>