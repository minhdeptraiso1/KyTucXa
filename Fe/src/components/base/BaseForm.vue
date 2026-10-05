<script setup lang="ts">
defineProps<{ labelledBy?: string }>()
defineEmits<{ submit: [] }>()
</script>

<template>
  <form class="base-form" :aria-labelledby="labelledBy" @submit.prevent="$emit('submit')">
    <slot />
  </form>
</template>
