<script setup lang="ts">
import { ref } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const buildings = ref<Array<{
  name: string
  total: number
  occupied: number
  status: string
  tone: 'success' | 'warning' | 'info'
}>>([
  { name: 'Tòa A - Nam sinh viên', total: 400, occupied: 368, status: '92% Lấp đầy', tone: 'success' },
  { name: 'Tòa B - Nữ sinh viên', total: 450, occupied: 412, status: '91% Lấp đầy', tone: 'success' },
  { name: 'Tòa C - Chất lượng cao', total: 200, occupied: 146, status: '73% Còn chỗ', tone: 'warning' },
])

function triggerToastDemo(type: 'success' | 'warning' | 'error' | 'info') {
  if (type === 'success') toast.success('Đã cập nhật chỉ số vận hành KTX thành công!')
  if (type === 'warning') toast.warning('Có 3 phòng tại Tòa A báo hỏng điều hòa cần bảo trì!')
  if (type === 'error') toast.error('Mất kết nối với công tơ điện tử Tòa C - Đang kết nối lại...')
  if (type === 'info') toast.info('Hệ thống sẽ bảo trì định kỳ lúc 02:00 sáng chủ nhật.')
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div>
        <span class="eyebrow">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <polygon points="12 6 12 12 16 14" />
          </svg>
          STAFF / OVERVIEW
        </span>
        <h1>Trung tâm vận hành số KTX</h1>
        <p>Giám sát thời gian thực phòng ở, hợp đồng sinh viên và tài chính.</p>
      </div>
      <div style="display: flex; gap: 0.6rem; align-items: center;">
        <BaseBadge tone="success">Hệ sinh thái ổn định</BaseBadge>
      </div>
    </header>

    <!-- Key Metrics Grid -->
    <section class="metric-grid" aria-label="Chỉ số vận hành">
      <BaseCard>
        <div class="metric-header">
          <span class="metric-label">Tỷ lệ lấp đầy KTX</span>
          <span style="font-size: 1.25rem;">🏢</span>
        </div>
        <strong class="metric-value">88.2%</strong>
        <span class="metric-trend">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
            <polyline points="17 6 23 6 23 12" />
          </svg>
          +5.4% so với tháng trước
        </span>
      </BaseCard>

      <BaseCard>
        <div class="metric-header">
          <span class="metric-label">Giường còn trống</span>
          <span style="font-size: 1.25rem;">🛏️</span>
        </div>
        <strong class="metric-value">124</strong>
        <span class="metric-trend">Sẵn sàng đón tân sinh viên</span>
      </BaseCard>

      <BaseCard>
        <div class="metric-header">
          <span class="metric-label">Hóa đơn chờ thu</span>
          <span style="font-size: 1.25rem;">⚡</span>
        </div>
        <strong class="metric-value">₫184.2M</strong>
        <span class="metric-trend metric-trend--warning">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          18 hóa đơn điện nước quá hạn
        </span>
      </BaseCard>
    </section>

    <!-- Building Status Showcase -->
    <section style="margin-top: 2rem;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2 style="font-size: 1.4rem;">Tình trạng các khu nhà ký túc xá</h2>
        <span style="font-size: 0.82rem; color: var(--color-muted);">Cập nhật 2 phút trước</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(18rem, 1fr)); gap: 1rem;">
        <BaseCard v-for="b in buildings" :key="b.name" style="padding: 1.25rem;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
            <strong>{{ b.name }}</strong>
            <BaseBadge :tone="b.tone">{{ b.status }}</BaseBadge>
          </div>
          <div style="font-size: 0.82rem; color: var(--color-muted); margin-bottom: 0.5rem;">
            Đã ở: {{ b.occupied }} / {{ b.total }} chỗ (Còn {{ b.total - b.occupied }} chỗ)
          </div>
          <div style="background: var(--color-border); border-radius: 999px; height: 6px; overflow: hidden;">
            <div
              :style="{
                width: `${(b.occupied / b.total) * 100}%`,
                background: 'var(--color-primary)',
                height: '100%',
                borderRadius: '999px'
              }"
            />
          </div>
        </BaseCard>
      </div>
    </section>

    <!-- Interactive Toast Tester & System Tools -->
    <BaseCard class="dashboard-note" style="margin-top: 2rem;">
      <div>
        <span class="eyebrow">TOAST NOTIFICATIONS</span>
        <h2>Thử nghiệm hệ thống thông báo Toast</h2>
        <p>Kiểm tra các trạng thái thông báo phản hồi người dùng thời gian thực.</p>
        <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; margin-top: 1rem;">
          <BaseButton type="button" @click="triggerToastDemo('success')">Toast Thành công</BaseButton>
          <BaseButton type="button" variant="ghost" @click="triggerToastDemo('info')">Toast Thông tin</BaseButton>
          <BaseButton type="button" variant="ghost" @click="triggerToastDemo('warning')">Toast Cảnh báo</BaseButton>
          <BaseButton type="button" variant="danger" @click="triggerToastDemo('error')">Toast Lỗi</BaseButton>
        </div>
      </div>
    </BaseCard>
  </AppLayout>
</template>
