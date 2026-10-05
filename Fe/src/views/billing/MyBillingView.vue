<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import { useToast } from '@/composables/useToast'
import { apiErrorMessage } from '@/utils/apiError'
import { billingService, type Invoice, type Payment } from '@/services/billingService'

const toast = useToast()
const loading = ref(true)
const payingId = ref('')
const invoices = ref<Invoice[]>([])
const payments = ref<Payment[]>([])
const debt = computed(() => invoices.value.reduce((sum, item) => sum + Number(item.remainingAmount), 0))

function money(value: number) { return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + ' đ' }
function statusLabel(value: string) {
  return ({ ISSUED: 'Chưa thanh toán', PARTIALLY_PAID: 'Thanh toán một phần', PAID: 'Đã thanh toán', OVERDUE: 'Quá hạn', CANCELLED: 'Đã hủy', SUCCESS: 'Thành công', PENDING: 'Đang xử lý', FAILED: 'Thất bại' } as Record<string, string>)[value] || value
}
function tone(value: string): 'success' | 'warning' | 'danger' | 'neutral' {
  if (['PAID', 'SUCCESS'].includes(value)) return 'success'
  if (['OVERDUE', 'FAILED'].includes(value)) return 'danger'
  if (['ISSUED', 'PARTIALLY_PAID', 'PENDING'].includes(value)) return 'warning'
  return 'neutral'
}

async function load() {
  loading.value = true
  try { [invoices.value, payments.value] = await Promise.all([billingService.myInvoices(), billingService.myPayments()]) }
  catch (cause) { toast.error(apiErrorMessage(cause, 'Không tải được lịch sử tài chính.')) }
  finally { loading.value = false }
}

async function payWithVnPay(invoice: Invoice) {
  payingId.value = invoice.id
  try {
    const payment = await billingService.createVnPay({ invoiceId: invoice.id, amount: invoice.remainingAmount, idempotencyKey: crypto.randomUUID() })
    window.location.assign(payment.paymentUrl)
  } catch (cause) { toast.error(apiErrorMessage(cause)) }
  finally { payingId.value = '' }
}

onMounted(load)
</script>

<template>
  <AppLayout>
    <main class="mx-auto w-full max-w-[1400px] p-5 sm:p-7 lg:p-10">
      <header class="mb-7">
        <p class="font-mono text-xs font-bold uppercase tracking-[0.2em] text-app-primary">Sinh viên / Tài chính</p>
        <h1 class="mt-2 font-display text-3xl font-extrabold tracking-tight text-app-ink sm:text-5xl">Hóa đơn của tôi</h1>
        <p class="mt-2 text-app-muted">Theo dõi đầy đủ tiền phòng, điện nước, dịch vụ và lịch sử thanh toán.</p>
      </header>

      <section class="mb-6 grid gap-4 sm:grid-cols-3">
        <BaseCard class="!p-5"><p class="text-xs font-bold uppercase text-app-muted">Tổng hóa đơn</p><p class="mt-2 text-3xl font-extrabold text-app-ink">{{ invoices.length }}</p></BaseCard>
        <BaseCard class="!p-5"><p class="text-xs font-bold uppercase text-app-muted">Công nợ hiện tại</p><p class="mt-2 text-2xl font-extrabold text-amber-500">{{ money(debt) }}</p></BaseCard>
        <BaseCard class="!p-5"><p class="text-xs font-bold uppercase text-app-muted">Giao dịch thành công</p><p class="mt-2 text-3xl font-extrabold text-emerald-500">{{ payments.filter(item => item.status === 'SUCCESS').length }}</p></BaseCard>
      </section>

      <section v-if="loading" class="rounded-app-lg border border-app-border bg-app-surface p-10 text-center text-app-muted">Đang tải lịch sử...</section>
      <section v-else class="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]">
        <div class="grid content-start gap-4">
          <h2 class="text-xl font-bold text-app-ink">Lịch sử hóa đơn</h2>
          <article v-for="item in invoices" :key="item.id" class="rounded-app-lg border border-app-border bg-app-surface p-5">
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div><strong class="text-app-ink">{{ item.invoiceCode }} · Phòng {{ item.roomNumber }}</strong><p class="mt-1 text-sm text-app-muted">Kỳ {{ item.billingPeriod.slice(0, 7) }} · hạn {{ item.dueDate }}</p></div>
              <BaseBadge :tone="tone(item.status)">{{ statusLabel(item.status) }}</BaseBadge>
            </div>
            <div class="mt-4 grid gap-2 rounded-app-md bg-app-bg p-4">
              <div v-for="detail in item.items" :key="detail.id" class="flex justify-between gap-4 text-sm"><span class="text-app-muted">{{ detail.description }}</span><strong class="whitespace-nowrap text-app-ink">{{ money(detail.amount) }}</strong></div>
              <div class="mt-2 flex justify-between border-t border-app-border pt-3"><strong class="text-app-ink">Còn phải trả</strong><strong class="text-lg text-app-primary">{{ money(item.remainingAmount) }}</strong></div>
            </div>
            <div v-if="item.remainingAmount > 0 && item.status !== 'CANCELLED'" class="mt-4 flex justify-end"><BaseButton :loading="payingId === item.id" @click="payWithVnPay(item)">Thanh toán qua VNPay Sandbox</BaseButton></div>
          </article>
          <p v-if="!invoices.length" class="rounded-app-lg border border-dashed border-app-border p-10 text-center text-app-muted">Bạn chưa có hóa đơn.</p>
        </div>

        <div class="grid content-start gap-4">
          <h2 class="text-xl font-bold text-app-ink">Lịch sử thanh toán</h2>
          <article v-for="item in payments" :key="item.id" class="rounded-app-lg border border-app-border bg-app-surface p-5"><div class="flex justify-between gap-3"><strong class="text-app-ink">{{ item.paymentCode }}</strong><BaseBadge :tone="tone(item.status)">{{ statusLabel(item.status) }}</BaseBadge></div><p class="mt-2 text-sm text-app-muted">{{ item.invoiceCode }} · {{ item.method }}</p><p class="mt-3 text-xl font-bold text-app-ink">{{ money(item.amount) }}</p></article>
          <p v-if="!payments.length" class="rounded-app-lg border border-dashed border-app-border p-8 text-center text-app-muted">Chưa có giao dịch.</p>
        </div>
      </section>
    </main>
  </AppLayout>
</template>

