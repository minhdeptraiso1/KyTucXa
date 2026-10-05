<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthLayout from '@/layouts/AuthLayout.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseField from '@/components/base/BaseField.vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { apiErrorMessage } from '@/utils/apiError'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const loading = ref(false)
const form = reactive({ password: '', confirm: '' })

async function submit() {
  if (form.password.length < 8 || form.password !== form.confirm) {
    toast.error('Mật khẩu cần ít nhất 8 ký tự và hai ô phải trùng nhau.')
    return
  }
  loading.value = true
  try {
    await auth.resetPassword(String(route.query.token || ''), form.password)
    toast.success('Mật khẩu đã được đặt lại.')
    router.push('/login')
  } catch (cause) {
    toast.error(apiErrorMessage(cause, 'Không thể đặt lại mật khẩu.'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout>
    <section class="mx-auto max-w-lg rounded-app-lg border border-app-border bg-app-surface p-8 shadow-xl">
      <p class="text-xs font-bold uppercase tracking-[0.16em] text-app-primary">Khôi phục tài khoản</p>
      <h1 class="mt-3 text-3xl font-bold text-app-ink">Đặt mật khẩu mới</h1>
      <form class="mt-7 grid gap-5" @submit.prevent="submit">
        <BaseField id="reset-password" v-model="form.password" type="password" label="Mật khẩu mới" show-toggle required />
        <BaseField id="reset-confirm" v-model="form.confirm" type="password" label="Nhập lại mật khẩu" show-toggle required />
        <BaseButton type="submit" block :loading="loading">Cập nhật mật khẩu</BaseButton>
      </form>
    </section>
  </AuthLayout>
</template>
