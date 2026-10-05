<script setup lang="ts">
withDefaults(defineProps<{
  id: string
  modelValue: string
  placeholder?: string
  rows?: number
  invalid?: boolean
}>(), { placeholder: '', rows: 4, invalid: false })

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
</script>

<template>
  <textarea
    :id="id"
    class="base-input min-h-24 resize-y"
    :class="{ 'base-input--invalid': invalid }"
    :value="modelValue"
    :placeholder="placeholder"
    :rows="rows"
    :aria-invalid="invalid"
    @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
  />
</template>
