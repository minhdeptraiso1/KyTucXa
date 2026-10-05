<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseButton from '@/components/base/BaseButton.vue'
import BrandLogo from '@/components/brand/BrandLogo.vue'
import { billingService } from '@/services/billingService'

const route = useRoute()
const router = useRouter()
const state = ref<'loading' | 'success' | 'error'>('loading')
const message = ref('Đang xác minh kết quả thanh toán với VNPay...')

onMounted(async () => {
  try {
    const params = new URLSearchParams()
    Object.entries(route.query).forEach(([key, value]) => { if (typeof value === 'string') params.set(key, value) })
    const payment = await billingService.finishVnPay(params)
    state.value = payment.status === 'SUCCESS' ? 'success' : 'error'
    message.value = payment.status === 'SUCCESS' ? 'Thanh toán đã được ghi nhận thành công.' : 'Giao dịch chưa thành công.'
  } catch { state.value = 'error'; message.value = 'Không thể xác minh giao dịch hoặc chữ ký không hợp lệ.' }
})
</script>

<template>
  <main class="grid min-h-screen place-items-center bg-app-bg p-5">
    <section class="w-full max-w-xl rounded-app-xl border border-app-border bg-app-surface p-8 text-center shadow-xl">
      <BrandLogo class="mx-auto mb-8 w-fit" />
      <p class="font-mono text-xs font-bold uppercase tracking-[0.18em] text-app-primary">VNPay Sandbox</p>
      <h1 class="mt-3 text-3xl font-extrabold text-app-ink">Kết quả thanh toán</h1>
      <p class="mt-4 text-app-muted">{{ message }}</p>
      <div class="mt-8"><BaseButton @click="router.push('/my-billing')">Về lịch sử hóa đơn</BaseButton></div>
    </section>
  </main>
</template>

