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

const studentCode = ref('')
const fullName = ref('')
const email = ref('')
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const message = ref('')
const error = ref('')
const submitting = ref(false)

async function submit() {
  error.value = ''
  message.value = ''

  if (!studentCode.value || !fullName.value || !email.value || !phone.value || !password.value || !confirmPassword.value) {
    error.value = 'Vui lòng điền đầy đủ tất cả các trường thông tin bắt buộc (*).'
    toast.warning(error.value, 'Thiếu thông tin')
    return
  }

  if (password.value !== confirmPassword.value) {
    error.value = 'Mật khẩu xác nhận không khớp. Vui lòng kiểm tra lại.'
    toast.warning(error.value, 'Mật khẩu không khớp')
    return
  }

  if (password.value.length < 8) {
    error.value = 'Mật khẩu phải chứa ít nhất 8 ký tự.'
    toast.warning(error.value, 'Mật khẩu quá ngắn')
    return
  }

  submitting.value = true
  try {
    toast.info('Đang đối chiếu thông tin với danh sách sinh viên trường...', 'Xác thực hồ sơ')
    const response = await auth.register({
      studentCode: studentCode.value.trim().toUpperCase(),
      fullName: fullName.value.trim(),
      email: email.value.trim().toLowerCase(),
      phone: phone.value.trim(),
      password: password.value,
      confirmPassword: confirmPassword.value,
    })
    message.value = response.message || 'Đăng ký tài khoản thành công! Vui lòng kiểm tra email để kích hoạt.'
    toast.success(message.value, 'Đăng ký thành công')
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (cause: unknown) {
    const errorMsg =
      (cause as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ||
      'Không thể đăng ký tài khoản. Vui lòng kiểm tra lại thông tin.'
    error.value = errorMsg
    toast.error(errorMsg, 'Từ chối đăng ký')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <AuthLayout>
    <BaseCard class="login-card animate-fade-in">
      <div class="login-card__header">
        <span class="eyebrow">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <line x1="19" y1="8" x2="19" y2="14" />
            <line x1="22" y1="11" x2="16" y2="11" />
          </svg>
          HỒ SƠ SINH VIÊN
        </span>
        <h2>Tạo tài khoản KTX</h2>
        <p>Đăng ký tài khoản để nộp đơn xét duyệt và quản lý chỗ ở nội trú.</p>
      </div>

      <!-- Information alert for student registry matching rule -->
      <div style="background: var(--color-primary-soft); border: 1px solid color-mix(in srgb, var(--color-primary) 30%, transparent); border-radius: var(--radius-md); padding: 0.85rem 1rem; margin-bottom: 1.25rem; font-size: 0.82rem; line-height: 1.5; color: var(--color-ink);">
        <strong style="color: var(--color-primary); display: flex; align-items: center; gap: 0.35rem; margin-bottom: 0.25rem;">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
          Quy định xác thực sinh viên:
        </strong>
        Thông tin đăng ký (Mã SV, Họ tên, Email, Số điện thoại) phải trùng khớp chính xác với hồ sơ trường đã cung cấp cho ký túc xá.
      </div>

      <BaseForm labelled-by="register-title" @submit="submit">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr)); gap: var(--space-4);">
          <BaseField
            id="register-student-code"
            v-model="studentCode"
            label="Mã sinh viên"
            placeholder="VD: SV001 hoặc B20DCCN001"
            required
          />
          <BaseField
            id="register-full-name"
            v-model="fullName"
            label="Họ và tên"
            placeholder="Nguyễn Văn A"
            required
          />
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr)); gap: var(--space-4);">
          <BaseField
            id="register-email"
            v-model="email"
            label="Email sinh viên trường"
            type="email"
            autocomplete="email"
            placeholder="nguyenvana@abc.edu.vn"
            required
          />
          <BaseField
            id="register-phone"
            v-model="phone"
            label="Số điện thoại"
            type="tel"
            autocomplete="tel"
            placeholder="0901234567"
            required
          />
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr)); gap: var(--space-4);">
          <BaseField
            id="register-password"
            v-model="password"
            label="Mật khẩu"
            type="password"
            autocomplete="new-password"
            placeholder="Tối thiểu 8 ký tự"
            :show-toggle="true"
            required
          />
          <BaseField
            id="register-confirm-password"
            v-model="confirmPassword"
            label="Xác nhận mật khẩu"
            type="password"
            autocomplete="new-password"
            placeholder="Nhập lại mật khẩu"
            :show-toggle="true"
            required
          />
        </div>

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

        <BaseButton type="submit" block :loading="submitting">
          Xác thực & Tạo tài khoản KTX
        </BaseButton>
      </BaseForm>

      <div class="login-card__footer">
        <span>Đã có tài khoản sinh viên?</span>
        <BaseButton type="button" variant="secondary" @click="router.push('/login')">
          Đăng nhập ngay
        </BaseButton>
      </div>
    </BaseCard>
  </AuthLayout>
</template>
