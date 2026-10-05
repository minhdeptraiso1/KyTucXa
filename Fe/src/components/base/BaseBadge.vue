<script setup lang="ts">
withDefaults(defineProps<{
  tone?: 'info' | 'success' | 'warning' | 'neutral' | 'danger'
  size?: 'sm' | 'md'
}>(), {
  tone: 'info',
  size: 'md',
})
</script>

<template>
  <span class="base-badge" :class="[`base-badge--${tone}`, `base-badge--${size}`]">
    <slot />
  </span>
</template>
