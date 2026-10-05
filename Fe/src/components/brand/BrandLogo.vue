<script setup lang="ts">
withDefaults(defineProps<{
  tone?: 'default' | 'inverse'
  compact?: boolean
}>(), {
  tone: 'default',
  compact: false,
})
</script>

<template>
  <div class="brand-logo" :class="[`brand-logo--${tone}`, { 'brand-logo--compact': compact }]" aria-label="KTX UET Campus Living">
    <svg class="brand-logo__mark" viewBox="0 0 44 44" aria-hidden="true">
      <rect x="2" y="2" width="40" height="40" rx="12" fill="currentColor" opacity="0.12" />
      <path d="M11 31V17l11-6 11 6v14" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" />
      <path d="M16 31V20h4v11m4 0V20h4v11" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
      <path d="M9 31h26" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
      <circle cx="22" cy="11" r="2.2" fill="currentColor" />
    </svg>
    <span class="brand-logo__copy">
      <strong>KTX UET</strong>
      <span v-if="!compact">Campus Living</span>
    </span>
  </div>
</template>

<style scoped>
.brand-logo {
  align-items: center;
  color: var(--color-primary);
  display: inline-flex;
  gap: 0.7rem;
  min-width: 0;
}

.brand-logo--inverse {
  color: #5eead4;
}

.brand-logo__mark {
  flex: 0 0 auto;
  height: 2.4rem;
  width: 2.4rem;
}

.brand-logo__copy {
  display: flex;
  flex-direction: column;
  line-height: 1.05;
}

.brand-logo__copy strong {
  color: var(--color-ink);
  font: 800 0.94rem var(--font-display);
  letter-spacing: 0.045em;
}

.brand-logo--inverse .brand-logo__copy strong {
  color: #ffffff;
}

.brand-logo__copy span {
  color: var(--color-muted);
  font: 600 0.62rem "DM Mono", monospace;
  letter-spacing: 0.13em;
  margin-top: 0.24rem;
  text-transform: uppercase;
}

.brand-logo--inverse .brand-logo__copy span {
  color: #94a3b8;
}

.brand-logo--compact .brand-logo__mark {
  height: 2rem;
  width: 2rem;
}
</style>
