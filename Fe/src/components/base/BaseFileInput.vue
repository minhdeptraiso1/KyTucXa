<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(
  defineProps<{
    id: string
    accept?: string
    disabled?: boolean
    label?: string
    hint?: string
  }>(),
  {
    accept: '.csv',
    disabled: false,
    label: 'Chọn file CSV',
    hint: 'Hỗ trợ định dạng .csv (tối đa 5MB)',
  },
)

const emit = defineEmits<{
  change: [file: File | null]
}>()

const fileName = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

function onFileSelected(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files && target.files.length > 0 ? target.files[0] : null
  fileName.value = file ? file.name : ''
  emit('change', file)
}

function clear() {
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  fileName.value = ''
  emit('change', null)
}

defineExpose({ clear })
</script>

<template>
  <div class="base-file-input">
    <input
      :id="id"
      ref="fileInput"
      type="file"
      class="base-file-input__native"
      :accept="accept"
      :disabled="disabled"
      @change="onFileSelected"
    />
    <label :for="id" class="base-file-input__dropzone" :class="{ 'has-file': Boolean(fileName) }">
      <div class="base-file-input__icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
          <polyline points="17 8 12 3 7 8" />
          <line x1="12" y1="3" x2="12" y2="15" />
        </svg>
      </div>
      <div class="base-file-input__info">
        <strong v-if="fileName" class="base-file-input__filename">{{ fileName }}</strong>
        <strong v-else class="base-file-input__title">{{ label }}</strong>
        <span class="base-file-input__hint">{{ hint }}</span>
      </div>
    </label>
  </div>
</template>
