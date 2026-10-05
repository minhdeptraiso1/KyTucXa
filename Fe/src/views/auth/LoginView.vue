<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AuthLayout from '@/layouts/AuthLayout.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseField from '@/components/base/BaseField.vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseCheckbox from '@/components/base/BaseCheckbox.vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const auth = useAuthStore()
const router = useRouter()
const toast = useToast()

const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const localError = ref('')

onMounted(() => {
  try {
    const isRemembered = localStorage.getItem('ktx.rememberMe') === 'true'
    if (isRemembered) {
      rememberMe.value = true
      const savedEmail = localStorage.getItem('ktx.savedEmail')
      if (savedEmail) {
        email.value = savedEmail
      }
      const savedPassword = localStorage.getItem('ktx.savedPassword')
      if (savedPassword) {
        try {
          password.value = atob(savedPassword)
        } catch {
          password.value = savedPassword
        }
      }
    }
  } catch {
    // Ignore storage issues
  }
})

function handleRememberMeToggle(value: boolean) {
  rememberMe.value = value
  if (!value) {
    try {
      localStorage.removeItem('ktx.rememberMe')
      localStorage.removeItem('ktx.savedEmail')
      localStorage.removeItem('ktx.savedPassword')
      toast.info('Đã xóa thông tin đăng nhập đã lưu trên thiết bị.')
    } catch {
      // Ignore
    }
  }
}

async function submit() {
  localError.value = ''
  if (!email.value || !password.value) {
    localError.value = 'Vui lòng nhập đầy đủ Email và Mật khẩu.'
    toast.warning('Vui lòng nhập đầy đủ Email và Mật khẩu.', 'Thiếu thông tin')
    return
  }

  try {
    toast.info('Đang kiểm tra thông tin đăng nhập...', 'Xác thực')
    await auth.login(email.value, password.value)

    // Save or clear remember credentials
    if (rememberMe.value) {
      try {
        localStorage.setItem('ktx.rememberMe', 'true')
        localStorage.setItem('ktx.savedEmail', email.value)
        localStorage.setItem('ktx.savedPassword', btoa(password.value))
      } catch {
        // Storage issue fallback
      }
    } else {
      try {
        localStorage.removeItem('ktx.rememberMe')
        localStorage.removeItem('ktx.savedEmail')
        localStorage.removeItem('ktx.savedPassword')
      } catch {
        // Storage issue fallback
      }
    }

    toast.success('Đăng nhập thành công! Đang chuyển hướng...', 'Chào mừng trở lại')
    setTimeout(() => {
      router.push(auth.homePath)
    }, 600)
  } catch (err: unknown) {
    const errorMsg =
      (err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ||
      'Không thể kết nối đến máy chủ hoặc sai thông tin đăng nhập.'
    localError.value = errorMsg
    toast.error(errorMsg, 'Đăng nhập thất bại')
  }
}
</script>

<template>
  <AuthLayout>
    <BaseCard class="login-card animate-fade-in">
      <div class="login-card__header">
        <span class="eyebrow">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
          XÁC THỰC AN TOÀN
        </span>
        <h2>Chào mừng trở lại</h2>
        <p>Đăng nhập để quản lý phòng, dịch vụ và hợp đồng KTX của bạn.</p>
      </div>

      <BaseForm labelled-by="login-title" @submit="submit">
        <BaseField
          id="email"
          v-model="email"
          label="Email sinh viên / Quản lý"
          type="email"
          autocomplete="email"
          placeholder="sinhvien@campus.edu.vn"
          required
        />

        <BaseField
          id="password"
          v-model="password"
          label="Mật khẩu"
          type="password"
          autocomplete="current-password"
          placeholder="••••••••"
          :show-toggle="true"
          required
        />

        <!-- Options: Remember password checkbox & Forgot password -->
        <div class="login-card__options">
          <BaseCheckbox
            id="remember-me"
            :model-value="rememberMe"
            label="Ghi nhớ tài khoản & mật khẩu"
            @update:model-value="handleRememberMeToggle"
          />
          <BaseButton
            type="button"
            variant="tertiary"
            size="sm"
            @click="router.push('/forgot-password')"
          >
            Quên mật khẩu?
          </BaseButton>
        </div>

        <p v-if="localError || auth.error" class="form-error" role="alert">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          {{ localError || auth.error }}
        </p>

        <BaseButton type="submit" block :loading="auth.loading">
          Đăng nhập vào hệ thống
        </BaseButton>
      </BaseForm>

      <div class="login-card__footer">
        <span>Chưa có tài khoản sinh viên?</span>
        <BaseButton type="button" variant="secondary" @click="router.push('/register')">
          Đăng ký tài khoản
        </BaseButton>
      </div>
    </BaseCard>
  </AuthLayout>
</template>
