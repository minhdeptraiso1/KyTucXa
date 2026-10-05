<script setup lang="ts">
withDefaults(
  defineProps<{
    id: string
    modelValue: boolean
    label?: string
    disabled?: boolean
  }>(),
  {
    label: '',
    disabled: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<template>
  <label :for="id" class="base-checkbox" :class="{ 'base-checkbox--disabled': disabled }">
    <span class="base-checkbox__input-wrapper">
      <input
        :id="id"
        type="checkbox"
        class="base-checkbox__native"
        :checked="modelValue"
        :disabled="disabled"
        @change="emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
      />
      <span class="base-checkbox__custom" aria-hidden="true">
        <svg viewBox="0 0 12 10" class="base-checkbox__check-icon" fill="none" stroke="currentColor">
          <path d="M1.5 5.2L4.2 8L10.5 1.5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </span>
    </span>
    <span v-if="label || $slots.default" class="base-checkbox__label">
      <slot>{{ label }}</slot>
    </span>
  </label>
</template>
