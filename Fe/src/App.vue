<script setup lang="ts">
import { onMounted } from 'vue'
import BaseToast from '@/components/base/BaseToast.vue'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

const { initTheme } = useTheme()
const auth = useAuthStore()

onMounted(() => {
  initTheme()
  if (auth.isAuthenticated) auth.loadSession().catch(() => undefined)
})
</script>

<template>
  <div class="app-root">
    <div class="ambient-glow ambient-glow--1" aria-hidden="true" />
    <div class="ambient-glow ambient-glow--2" aria-hidden="true" />
    <RouterView />
    <BaseToast />
  </div>
</template>
