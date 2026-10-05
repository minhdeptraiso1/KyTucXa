<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseThemeToggle from '@/components/base/BaseThemeToggle.vue'
import BrandLogo from '@/components/brand/BrandLogo.vue'

const auth = useAuthStore()
const router = useRouter()
const toast = useToast()

const avatarLetter = computed(() => {
  const name = auth.displayName || 'KTX'
  const trimmed = name.trim().split(/\s+/)
  if (trimmed.length > 1) {
    const lastWord = trimmed[trimmed.length - 1]
    return (lastWord[0] || 'K').toUpperCase()
  }
  return (name[0] || 'K').toUpperCase()
})

const userEmail = computed(() => {
  return auth.currentUser?.email || ''
})

async function handleLogout() {
  await auth.logout()
  toast.info('Bạn đã đăng xuất khỏi hệ thống an toàn.', 'Đăng xuất')
  router.push('/login')
}
</script>

<template>
  <div class="app-layout">
    <!-- Fixed sidebar -->
    <aside class="app-sidebar" role="navigation" aria-label="Điều hướng hệ thống">
      <BrandLogo />

      <!-- Profile Card -->
      <section class="user-profile-widget" aria-label="Tài khoản đang đăng nhập">
        <div class="user-profile-widget__header">
          <div class="user-profile-widget__avatar" aria-hidden="true">
            {{ avatarLetter }}
            <span class="user-profile-widget__status-dot" title="Đang hoạt động" />
          </div>
          <div class="user-profile-widget__meta min-w-0">
            <p class="user-profile-widget__name" :title="auth.displayName">
              {{ auth.displayName }}
            </p>
            <p v-if="auth.studentCode" class="user-profile-widget__code">
              MSSV: <span>{{ auth.studentCode }}</span>
            </p>
            <p v-else-if="userEmail" class="user-profile-widget__email truncate" :title="userEmail">
              {{ userEmail }}
            </p>
          </div>
        </div>

        <div class="user-profile-widget__badge-row">
          <span class="user-profile-widget__role-tag">
            {{ auth.roleLabel }}
          </span>
          <span v-if="auth.studentCode && userEmail" class="user-profile-widget__email-sub truncate" :title="userEmail">
            {{ userEmail }}
          </span>
        </div>
      </section>

      <nav v-if="auth.role === 'USER'" class="app-nav" aria-label="Điều hướng sinh viên">
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/portal">
          Hồ sơ nội trú của tôi
        </RouterLink>
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/my-billing">
          Hóa đơn & thanh toán
        </RouterLink>
        <div class="app-nav__divider" />
        <RouterLink class="app-nav__link" to="/">Trang chủ KTX</RouterLink>
      </nav>

      <nav v-else class="app-nav" aria-label="Điều hướng quản lý">
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/dashboard">
          Tổng quan vận hành
        </RouterLink>
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/students">
          Hồ sơ sinh viên
        </RouterLink>
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/facilities">
          Sơ đồ phòng & giường
        </RouterLink>
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/registrations">
          Đăng ký & phân phòng
        </RouterLink>
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/contracts">
          Hợp đồng nội trú
        </RouterLink>
        <RouterLink class="app-nav__link" active-class="app-nav__link--active" to="/billing">
          Hóa đơn & Dịch vụ
        </RouterLink>

        <div class="app-nav__divider" />

        <RouterLink class="app-nav__link" to="/">
          Trang chủ KTX
        </RouterLink>
      </nav>

      <div class="app-sidebar__footer">
        <div class="app-sidebar__theme-row" aria-label="Chọn giao diện">
          <BaseThemeToggle />
        </div>
        <BaseButton type="button" variant="secondary" block @click="handleLogout">
          Đăng xuất
        </BaseButton>
      </div>
    </aside>

    <section class="app-content">
      <slot />
    </section>
  </div>
</template>

<style scoped>
.user-profile-widget {
  margin: 1.25rem 0.25rem 0.5rem;
  padding: 0.85rem;
  border-radius: var(--radius-lg, 0.75rem);
  background: var(--color-surface, #ffffff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: 0 2px 8px -2px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

[data-theme="dark"] .user-profile-widget {
  background: #111827;
  border-color: #1f2937;
  box-shadow: 0 4px 12px -2px rgba(0, 0, 0, 0.3);
}

.user-profile-widget__header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-profile-widget__avatar {
  position: relative;
  width: 2.5rem;
  height: 2.5rem;
  flex-shrink: 0;
  border-radius: 9999px;
  background: linear-gradient(135deg, #0d9488 0%, #3b82f6 100%);
  color: #ffffff;
  font-weight: 700;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(13, 148, 136, 0.3);
}

.user-profile-widget__status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 0.625rem;
  height: 0.625rem;
  border-radius: 9999px;
  background-color: #10b981;
  border: 2px solid var(--color-surface, #ffffff);
}

[data-theme="dark"] .user-profile-widget__status-dot {
  border-color: #111827;
}

.user-profile-widget__meta {
  flex: 1;
  overflow: hidden;
}

.user-profile-widget__name {
  font-size: 0.88rem;
  font-weight: 700;
  line-height: 1.25;
  color: var(--color-ink, #0f172a);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-profile-widget__code {
  font-size: 0.75rem;
  color: var(--color-muted, #64748b);
  margin-top: 0.15rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-profile-widget__code span {
  font-weight: 600;
  color: var(--color-primary, #0d9488);
}

.user-profile-widget__email {
  font-size: 0.75rem;
  color: var(--color-muted, #64748b);
  margin-top: 0.15rem;
}

.user-profile-widget__badge-row {
  margin-top: 0.6rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--color-border, #f1f5f9);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

[data-theme="dark"] .user-profile-widget__badge-row {
  border-top-color: #1f2937;
}

.user-profile-widget__role-tag {
  display: inline-flex;
  align-items: center;
  border-radius: 9999px;
  background-color: var(--color-primary-soft, #f0fdfa);
  color: var(--color-primary, #0d9488);
  font-size: 0.68rem;
  font-weight: 700;
  padding: 0.15rem 0.55rem;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

[data-theme="dark"] .user-profile-widget__role-tag {
  background-color: rgba(13, 148, 136, 0.2);
  color: #2dd4bf;
}

.user-profile-widget__email-sub {
  font-size: 0.7rem;
  color: var(--color-muted, #94a3b8);
  max-width: 9.5rem;
}
</style>

