<script setup lang="ts">
withDefaults(defineProps<{
  id: string
  modelValue: string
  type?: string
  placeholder?: string
  autocomplete?: string
  invalid?: boolean
  disabled?: boolean
}>(), { type: 'text', placeholder: '', autocomplete: 'off', invalid: false, disabled: false })

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
</script>

<template>
  <input
    :id="id"
    class="base-input"
    :class="{ 'base-input--invalid': invalid }"
    :type="type"
    :value="modelValue"
    :placeholder="placeholder"
    :autocomplete="autocomplete"
    :aria-invalid="invalid"
    :disabled="disabled"
    @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
  />
</template>
