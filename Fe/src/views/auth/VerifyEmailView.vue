<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AuthLayout from '@/layouts/AuthLayout.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { useAuthStore } from '@/stores/auth'
import { apiErrorMessage } from '@/utils/apiError'

const route = useRoute()
const auth = useAuthStore()
const state = ref<'loading' | 'success' | 'error'>('loading')
const message = ref('Đang xác minh email...')

onMounted(async () => {
  try {
    const token = String(route.query.token || '')
    if (!token) throw new Error('missing token')
    message.value = (await auth.verifyEmail(token)).message
    state.value = 'success'
  } catch (cause) {
    message.value = apiErrorMessage(cause, 'Liên kết xác minh không hợp lệ hoặc đã hết hạn.')
    state.value = 'error'
  }
})
</script>

<template>
  <AuthLayout>
    <section class="mx-auto max-w-lg rounded-app-lg border border-app-border bg-app-surface p-8 text-center shadow-xl">
      <p class="text-xs font-bold uppercase tracking-[0.16em] text-app-primary">Xác minh tài khoản</p>
      <h1 class="mt-3 text-3xl font-bold text-app-ink">{{ state === 'success' ? 'Email đã xác minh' : state === 'error' ? 'Không thể xác minh' : 'Đang xử lý' }}</h1>
      <p class="my-6 text-sm leading-6 text-app-muted">{{ message }}</p>
      <RouterLink v-if="state !== 'loading'" to="/login"><BaseButton>Đến trang đăng nhập</BaseButton></RouterLink>
    </section>
  </AuthLayout>
</template>
