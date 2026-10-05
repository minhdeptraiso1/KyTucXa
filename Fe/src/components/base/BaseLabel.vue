<script setup lang="ts">
defineProps<{ forId: string; required?: boolean }>()
</script>

<template>
  <label class="base-label" :for="forId">
    <slot />
    <span v-if="required" class="base-label__required" aria-hidden="true">*</span>
  </label>
</template>
