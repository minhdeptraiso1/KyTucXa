<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthLayout from '@/layouts/AuthLayout.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseField from '@/components/base/BaseField.vue'
import BaseForm from '@/components/base/BaseForm.vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const auth = useAuthStore()
const router = useRouter()
const toast = useToast()

const email = ref('')
const message = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  message.value = ''

  if (!email.value) {
    error.value = 'Vui lòng nhập địa chỉ email đã đăng ký.'
    toast.warning(error.value, 'Thiếu thông tin')
    return
  }

  loading.value = true
  try {
    toast.info('Đang gửi yêu cầu đặt lại mật khẩu...', 'Khôi phục')
    const response = await auth.forgotPassword(email.value)
    message.value = response.message || 'Đã gửi hướng dẫn khôi phục mật khẩu vào hòm thư của bạn.'
    toast.success(message.value, 'Đã gửi thành công')
  } catch (cause: unknown) {
    const errorMsg =
      (cause as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ||
      'Không thể gửi yêu cầu đặt lại mật khẩu. Vui lòng kiểm tra lại email.'
    error.value = errorMsg
    toast.error(errorMsg, 'Lỗi khôi phục')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthLayout>
    <BaseCard class="login-card animate-fade-in">
      <div class="login-card__header">
        <span class="eyebrow">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.2">
            <circle cx="12" cy="12" r="10" />
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
          KHÔI PHỤC TÀI KHOẢN
        </span>
        <h2>Quên mật khẩu</h2>
        <p>Nhập email tài khoản của bạn để nhận liên kết xác thực đặt lại mật khẩu.</p>
      </div>

      <BaseForm labelled-by="forgot-title" @submit="submit">
        <BaseField
          id="forgot-email"
          v-model="email"
          label="Email đã đăng ký"
          type="email"
          autocomplete="email"
          placeholder="sinhvien@campus.edu.vn"
          required
        />

        <p v-if="message" class="form-success" role="status">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
          </svg>
          {{ message }}
        </p>

        <p v-if="error" class="form-error" role="alert">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          {{ error }}
        </p>

        <BaseButton type="submit" block :loading="loading">
          Gửi mã khôi phục mật khẩu
        </BaseButton>
      </BaseForm>

      <div class="login-card__footer">
        <span>Đã nhớ lại mật khẩu?</span>
        <BaseButton type="button" variant="secondary" @click="router.push('/login')">
          Quay lại đăng nhập
        </BaseButton>
      </div>
    </BaseCard>
  </AuthLayout>
</template>
