<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { isDark, toggleTheme } = useTheme()
</script>

<template>
  <button
    type="button"
    class="base-theme-toggle"
    :title="isDark ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối'"
    :aria-label="isDark ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối'"
    @click="toggleTheme"
  >
    <div class="base-theme-toggle__track">
      <!-- Sun Icon for Light Mode -->
      <span class="base-theme-toggle__icon base-theme-toggle__icon--sun" :class="{ 'is-active': !isDark }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="5" />
          <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
      </span>
      <!-- Moon Icon for Dark Mode -->
      <span class="base-theme-toggle__icon base-theme-toggle__icon--moon" :class="{ 'is-active': isDark }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      </span>
      <span class="base-theme-toggle__thumb" :class="{ 'base-theme-toggle__thumb--dark': isDark }" />
    </div>
  </button>
</template>
