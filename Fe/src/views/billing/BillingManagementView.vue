<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseCheckbox from '@/components/base/BaseCheckbox.vue'
import BaseField from '@/components/base/BaseField.vue'
import BaseLabel from '@/components/base/BaseLabel.vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import { useToast } from '@/composables/useToast'
import { apiErrorMessage } from '@/utils/apiError'
import { billingService, type Invoice, type Meter, type MeterReading, type Payment, type Tariff } from '@/services/billingService'
import { facilityService, type Room } from '@/services/facilityService'
import { contractService, type Contract } from '@/services/contractService'

const toast = useToast()
const loading = ref(true)
const saving = ref(false)
const activeTab = ref<'invoices' | 'meters' | 'payments'>('invoices')
const invoices = ref<Invoice[]>([])
const payments = ref<Payment[]>([])
const meters = ref<Meter[]>([])
const readings = ref<MeterReading[]>([])
const tariffs = ref<Tariff[]>([])
const rooms = ref<Room[]>([])
const contracts = ref<Contract[]>([])
const dialog = ref<'' | 'meter' | 'reading' | 'tariff' | 'invoice' | 'payment'>('')
const editingId = ref('')

const monthStart = new Date().toISOString().slice(0, 7) + '-01'
const forms = reactive({
  meter: { roomId: '', meterCode: '', utilityType: 'ELECTRICITY', unit: 'kWh', status: 'ACTIVE' },
  reading: { meterId: '', billingPeriod: monthStart, currentValue: '', resetRecorded: false, note: '' },
  tariff: { utilityType: 'ELECTRICITY', unitPrice: '', effectiveFrom: monthStart, effectiveTo: '', active: true },
  invoice: { contractId: '', billingPeriod: monthStart, dueDate: monthStart, serviceFee: '0', discount: '0', fineAmount: '0' },
  payment: { invoiceId: '', amount: '' },
})

const roomOptions = computed(() => rooms.value.map((item) => ({ value: item.id, label: `${item.buildingCode} · ${item.roomNumber}` })))
const meterOptions = computed(() => meters.value.map((item) => ({ value: item.id, label: `${item.meterCode} · ${item.roomNumber} · ${utilityLabel(item.utilityType)}` })))
const contractOptions = computed(() => contracts.value.filter((item) => item.status === 'ACTIVE').map((item) => ({ value: item.id, label: `${item.contractCode} · ${item.studentCode || item.fullName} · ${item.roomNumber}` })))
const unpaidInvoiceOptions = computed(() => invoices.value.filter((item) => item.remainingAmount > 0 && item.status !== 'CANCELLED').map((item) => ({ value: item.id, label: `${item.invoiceCode} · còn ${money(item.remainingAmount)}` })))
const totalDebt = computed(() => invoices.value.reduce((sum, item) => sum + Number(item.remainingAmount), 0))
const totalPaid = computed(() => payments.value.filter((item) => item.status === 'SUCCESS').reduce((sum, item) => sum + Number(item.amount), 0))
const dialogTitle = computed(() => {
  const createTitles = { meter: 'Thêm công tơ', reading: 'Nhập chỉ số', tariff: 'Thêm đơn giá', invoice: 'Phát hành hóa đơn', payment: 'Thanh toán trực tiếp' }
  const editTitles = { meter: 'Sửa công tơ', reading: 'Sửa chỉ số', tariff: 'Sửa đơn giá' }
  return editingId.value
    ? editTitles[dialog.value as keyof typeof editTitles] || 'Sửa dữ liệu'
    : createTitles[dialog.value as keyof typeof createTitles] || ''
})

function money(value: number) { return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + ' đ' }
function utilityLabel(value: string) { return value === 'ELECTRICITY' ? 'Điện' : 'Nước' }
function statusLabel(value: string) {
  return ({ ISSUED: 'Chưa thanh toán', PARTIALLY_PAID: 'Thanh toán một phần', PAID: 'Đã thanh toán', OVERDUE: 'Quá hạn', CANCELLED: 'Đã hủy', SUCCESS: 'Thành công', PENDING: 'Đang xử lý', FAILED: 'Thất bại' } as Record<string, string>)[value] || value
}
function tone(value: string): 'success' | 'warning' | 'danger' | 'neutral' | 'info' {
  if (['PAID', 'SUCCESS', 'ACTIVE'].includes(value)) return 'success'
  if (['OVERDUE', 'FAILED'].includes(value)) return 'danger'
  if (['ISSUED', 'PARTIALLY_PAID', 'PENDING'].includes(value)) return 'warning'
  return 'neutral'
}

async function load() {
  loading.value = true
  try {
    const [invoiceData, paymentData, meterData, readingData, tariffData, roomData, contractData] = await Promise.all([
      billingService.invoices(), billingService.payments(), billingService.meters(), billingService.readings(),
      billingService.tariffs(), facilityService.getRooms({ size: 500 }), contractService.all(),
    ])
    invoices.value = invoiceData
    payments.value = paymentData
    meters.value = meterData
    readings.value = readingData
    tariffs.value = tariffData
    rooms.value = roomData?.data?.content ?? []
    contracts.value = contractData
  } catch (cause) {
    toast.error(apiErrorMessage(cause, 'Không tải được dữ liệu hóa đơn.'))
  } finally { loading.value = false }
}

function open(next: typeof dialog.value) {
  editingId.value = ''
  dialog.value = next
  if (next === 'meter') forms.meter.roomId = forms.meter.roomId || roomOptions.value[0]?.value || ''
  if (next === 'reading') forms.reading.meterId = forms.reading.meterId || meterOptions.value[0]?.value || ''
  if (next === 'invoice') forms.invoice.contractId = forms.invoice.contractId || contractOptions.value[0]?.value || ''
  if (next === 'payment') {
    forms.payment.invoiceId = forms.payment.invoiceId || unpaidInvoiceOptions.value[0]?.value || ''
    const invoice = invoices.value.find((item) => item.id === forms.payment.invoiceId)
    forms.payment.amount = String(invoice?.remainingAmount || '')
  }
}

function editMeter(item: Meter) {
  editingId.value = item.id
  forms.meter = {
    roomId: item.roomId,
    meterCode: item.meterCode,
    utilityType: item.utilityType,
    unit: item.unit,
    status: item.status,
  }
  dialog.value = 'meter'
}

function editReading(item: MeterReading) {
  editingId.value = item.id
  forms.reading = {
    meterId: item.meterId,
    billingPeriod: item.billingPeriod,
    currentValue: String(item.currentValue),
    resetRecorded: item.resetRecorded,
    note: item.note || '',
  }
  dialog.value = 'reading'
}

function editTariff(item: Tariff) {
  editingId.value = item.id
  forms.tariff = {
    utilityType: item.utilityType,
    unitPrice: String(item.unitPrice),
    effectiveFrom: item.effectiveFrom,
    effectiveTo: item.effectiveTo || '',
    active: item.active,
  }
  dialog.value = 'tariff'
}

async function submit() {
  saving.value = true
  try {
    if (dialog.value === 'meter') {
      if (editingId.value) await billingService.updateMeter(editingId.value, { meterCode: forms.meter.meterCode, unit: forms.meter.unit, status: forms.meter.status as Meter['status'] })
      else await billingService.createMeter(forms.meter)
    }
    if (dialog.value === 'reading') {
      const readingPayload = { currentValue: Number(forms.reading.currentValue), resetRecorded: forms.reading.resetRecorded, note: forms.reading.note }
      if (editingId.value) await billingService.updateReading(editingId.value, readingPayload)
      else await billingService.createReading({ ...forms.reading, currentValue: Number(forms.reading.currentValue) })
    }
    if (dialog.value === 'tariff') {
      const tariffPayload = { unitPrice: Number(forms.tariff.unitPrice), effectiveFrom: forms.tariff.effectiveFrom, effectiveTo: forms.tariff.effectiveTo || null, active: forms.tariff.active }
      if (editingId.value) await billingService.updateTariff(editingId.value, tariffPayload)
      else await billingService.createTariff({ ...forms.tariff, unitPrice: Number(forms.tariff.unitPrice), effectiveTo: forms.tariff.effectiveTo || null })
    }
    if (dialog.value === 'invoice') await billingService.generateInvoice({ ...forms.invoice, serviceFee: Number(forms.invoice.serviceFee), discount: Number(forms.invoice.discount), fineAmount: Number(forms.invoice.fineAmount) })
    if (dialog.value === 'payment') await billingService.directPayment({ invoiceId: forms.payment.invoiceId, amount: Number(forms.payment.amount), idempotencyKey: crypto.randomUUID() })
    toast.success('Dữ liệu tài chính đã được cập nhật.', 'Thành công')
    dialog.value = ''
    editingId.value = ''
    await load()
  } catch (cause) { toast.error(apiErrorMessage(cause)) }
  finally { saving.value = false }
}

onMounted(load)
</script>

<template>
  <AppLayout>
    <main class="mx-auto w-full max-w-[1500px] p-5 sm:p-7 lg:p-10">
      <header class="mb-7 flex flex-col justify-between gap-5 xl:flex-row xl:items-end">
        <div>
          <p class="font-mono text-xs font-bold uppercase tracking-[0.2em] text-app-primary">Phase 5 / Billing</p>
          <h1 class="mt-2 font-display text-3xl font-extrabold tracking-tight text-app-ink sm:text-5xl">Hóa đơn & Thanh toán</h1>
          <p class="mt-2 max-w-3xl text-app-muted">Quản lý công tơ, chỉ số, đơn giá, công nợ và giao dịch trên dữ liệu PostgreSQL.</p>
        </div>
        <div class="flex flex-wrap gap-2">
          <BaseButton variant="secondary" @click="open('meter')">Thêm công tơ</BaseButton>
          <BaseButton variant="secondary" @click="open('reading')">Nhập chỉ số</BaseButton>
          <BaseButton variant="secondary" @click="open('tariff')">Thêm đơn giá</BaseButton>
          <BaseButton @click="open('invoice')">Phát hành hóa đơn</BaseButton>
        </div>
      </header>

      <section class="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <BaseCard class="!p-5"><p class="text-xs font-bold uppercase text-app-muted">Tổng hóa đơn</p><p class="mt-2 text-3xl font-extrabold text-app-ink">{{ invoices.length }}</p></BaseCard>
        <BaseCard class="!p-5"><p class="text-xs font-bold uppercase text-app-muted">Công nợ còn lại</p><p class="mt-2 text-2xl font-extrabold text-amber-500">{{ money(totalDebt) }}</p></BaseCard>
        <BaseCard class="!p-5"><p class="text-xs font-bold uppercase text-app-muted">Đã thu</p><p class="mt-2 text-2xl font-extrabold text-emerald-500">{{ money(totalPaid) }}</p></BaseCard>
        <BaseCard class="!p-5"><p class="text-xs font-bold uppercase text-app-muted">Công tơ hoạt động</p><p class="mt-2 text-3xl font-extrabold text-app-ink">{{ meters.filter(item => item.status === 'ACTIVE').length }}</p></BaseCard>
      </section>

      <div class="mb-5 flex flex-wrap gap-2 rounded-app-lg border border-app-border bg-app-surface p-2">
        <BaseButton :variant="activeTab === 'invoices' ? 'primary' : 'ghost'" @click="activeTab = 'invoices'">Hóa đơn</BaseButton>
        <BaseButton :variant="activeTab === 'meters' ? 'primary' : 'ghost'" @click="activeTab = 'meters'">Điện nước</BaseButton>
        <BaseButton :variant="activeTab === 'payments' ? 'primary' : 'ghost'" @click="activeTab = 'payments'">Giao dịch</BaseButton>
      </div>

      <section v-if="loading" class="rounded-app-lg border border-app-border bg-app-surface p-10 text-center text-app-muted">Đang tải dữ liệu thật...</section>
      <section v-else-if="activeTab === 'invoices'" class="grid gap-4">
        <div class="flex justify-end"><BaseButton variant="secondary" :disabled="!unpaidInvoiceOptions.length" @click="open('payment')">Ghi nhận thanh toán trực tiếp</BaseButton></div>
        <article v-for="item in invoices" :key="item.id" class="grid gap-4 rounded-app-lg border border-app-border bg-app-surface p-5 lg:grid-cols-[1.2fr_1fr_1fr_auto] lg:items-center">
          <div><strong class="text-app-ink">{{ item.invoiceCode }}</strong><p class="mt-1 text-sm text-app-muted">{{ item.studentCode || '—' }} · {{ item.studentName }} · Phòng {{ item.roomNumber }}</p></div>
          <div><p class="text-xs text-app-muted">Kỳ / hạn thanh toán</p><p class="font-semibold text-app-ink">{{ item.billingPeriod.slice(0, 7) }} · {{ item.dueDate }}</p></div>
          <div><p class="text-xs text-app-muted">Còn phải thu</p><p class="font-bold text-app-ink">{{ money(item.remainingAmount) }}</p></div>
          <BaseBadge :tone="tone(item.status)">{{ statusLabel(item.status) }}</BaseBadge>
        </article>
        <p v-if="!invoices.length" class="rounded-app-lg border border-dashed border-app-border p-10 text-center text-app-muted">Chưa có hóa đơn.</p>
      </section>

      <section v-else-if="activeTab === 'meters'" class="grid gap-6 xl:grid-cols-2">
        <BaseCard class="!p-5"><h2 class="mb-4 text-xl font-bold text-app-ink">Công tơ theo phòng</h2><div class="grid gap-3"><div v-for="item in meters" :key="item.id" class="flex items-center justify-between gap-3 rounded-app-md bg-app-bg p-4"><div><strong class="text-app-ink">{{ item.meterCode }}</strong><p class="text-sm text-app-muted">{{ item.buildingCode }} · {{ item.roomNumber }} · {{ utilityLabel(item.utilityType) }}</p></div><div class="flex items-center gap-2"><BaseBadge :tone="tone(item.status)">{{ item.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm ngưng' }}</BaseBadge><BaseButton size="sm" variant="ghost" @click="editMeter(item)">Sửa</BaseButton></div></div><p v-if="!meters.length" class="text-app-muted">Chưa có công tơ.</p></div></BaseCard>
        <BaseCard class="!p-5"><h2 class="mb-4 text-xl font-bold text-app-ink">Chỉ số gần đây</h2><div class="grid gap-3"><div v-for="item in readings" :key="item.id" class="rounded-app-md bg-app-bg p-4"><div class="flex justify-between gap-3"><strong class="text-app-ink">{{ item.meterCode }} · {{ item.billingPeriod.slice(0, 7) }}</strong><span class="font-bold text-app-primary">{{ item.consumption }} {{ item.utilityType === 'ELECTRICITY' ? 'kWh' : 'm³' }}</span></div><div class="mt-1 flex items-center justify-between gap-3"><p class="text-sm text-app-muted">{{ item.previousValue }} → {{ item.currentValue }}</p><BaseButton size="sm" variant="ghost" @click="editReading(item)">Sửa chỉ số</BaseButton></div></div><p v-if="!readings.length" class="text-app-muted">Chưa có chỉ số.</p></div></BaseCard>
        <BaseCard class="!p-5 xl:col-span-2"><h2 class="mb-4 text-xl font-bold text-app-ink">Đơn giá điện nước</h2><div class="flex flex-wrap gap-3"><div v-for="item in tariffs" :key="item.id" class="min-w-64 rounded-app-md border border-app-border px-4 py-3"><div class="flex items-center justify-between gap-3"><strong class="text-app-ink">{{ utilityLabel(item.utilityType) }}</strong><BaseBadge :tone="item.active ? 'success' : 'neutral'">{{ item.active ? 'Đang áp dụng' : 'Ngừng áp dụng' }}</BaseBadge></div><p class="mt-2 text-sm text-app-muted">{{ money(item.unitPrice) }}/đơn vị · từ {{ item.effectiveFrom }}<span v-if="item.effectiveTo"> đến {{ item.effectiveTo }}</span></p><div class="mt-3"><BaseButton size="sm" variant="ghost" @click="editTariff(item)">Sửa đơn giá</BaseButton></div></div><p v-if="!tariffs.length" class="text-app-muted">Chưa có đơn giá điện nước.</p></div></BaseCard>
      </section>

      <section v-else class="grid gap-3">
        <article v-for="item in payments" :key="item.id" class="grid gap-3 rounded-app-lg border border-app-border bg-app-surface p-5 sm:grid-cols-[1fr_1fr_auto] sm:items-center"><div><strong class="text-app-ink">{{ item.paymentCode }}</strong><p class="text-sm text-app-muted">{{ item.invoiceCode }} · {{ item.method }}</p></div><p class="font-bold text-app-ink">{{ money(item.amount) }}</p><BaseBadge :tone="tone(item.status)">{{ statusLabel(item.status) }}</BaseBadge></article>
        <p v-if="!payments.length" class="rounded-app-lg border border-dashed border-app-border p-10 text-center text-app-muted">Chưa có giao dịch.</p>
      </section>
    </main>

    <BaseModal :model-value="Boolean(dialog)" :title="dialogTitle" @update:model-value="dialog = ''">
      <form class="grid gap-4" @submit.prevent="submit">
        <template v-if="dialog === 'meter'">
          <div><BaseLabel for-id="meter-room" required>Phòng</BaseLabel><BaseSelect id="meter-room" v-model="forms.meter.roomId" :options="roomOptions" :disabled="Boolean(editingId)" /></div>
          <BaseField id="meter-code" v-model="forms.meter.meterCode" label="Mã công tơ" required />
          <div><BaseLabel for-id="meter-type" required>Loại</BaseLabel><BaseSelect id="meter-type" v-model="forms.meter.utilityType" :disabled="Boolean(editingId)" :options="[{ value: 'ELECTRICITY', label: 'Điện' }, { value: 'WATER', label: 'Nước' }]" @update:model-value="forms.meter.unit = $event === 'ELECTRICITY' ? 'kWh' : 'm3'" /></div>
          <BaseField id="meter-unit" v-model="forms.meter.unit" label="Đơn vị" required />
          <div v-if="editingId"><BaseLabel for-id="meter-status" required>Trạng thái</BaseLabel><BaseSelect id="meter-status" v-model="forms.meter.status" :options="[{ value: 'ACTIVE', label: 'Đang hoạt động' }, { value: 'INACTIVE', label: 'Tạm ngưng' }]" /></div>
        </template>
        <template v-if="dialog === 'reading'">
          <div><BaseLabel for-id="reading-meter" required>Công tơ</BaseLabel><BaseSelect id="reading-meter" v-model="forms.reading.meterId" :options="meterOptions" :disabled="Boolean(editingId)" /></div>
          <BaseField id="reading-period" v-model="forms.reading.billingPeriod" label="Kỳ ghi chỉ số" type="date" required />
          <BaseField id="reading-current" v-model="forms.reading.currentValue" label="Chỉ số hiện tại" type="number" required />
          <BaseCheckbox id="reading-reset" v-model="forms.reading.resetRecorded" label="Công tơ vừa reset/thay mới" />
          <BaseField id="reading-note" v-model="forms.reading.note" label="Ghi chú" />
        </template>
        <template v-if="dialog === 'tariff'">
          <div><BaseLabel for-id="tariff-type" required>Loại</BaseLabel><BaseSelect id="tariff-type" v-model="forms.tariff.utilityType" :disabled="Boolean(editingId)" :options="[{ value: 'ELECTRICITY', label: 'Điện' }, { value: 'WATER', label: 'Nước' }]" /></div>
          <BaseField id="tariff-price" v-model="forms.tariff.unitPrice" label="Đơn giá" type="number" required />
          <div class="grid gap-4 sm:grid-cols-2"><BaseField id="tariff-from" v-model="forms.tariff.effectiveFrom" label="Hiệu lực từ" type="date" required /><BaseField id="tariff-to" v-model="forms.tariff.effectiveTo" label="Đến ngày" type="date" /></div>
          <BaseCheckbox v-if="editingId" id="tariff-active" v-model="forms.tariff.active" label="Đang áp dụng" />
        </template>
        <template v-if="dialog === 'invoice'">
          <div><BaseLabel for-id="invoice-contract" required>Hợp đồng đang hiệu lực</BaseLabel><BaseSelect id="invoice-contract" v-model="forms.invoice.contractId" :options="contractOptions" /></div>
          <div class="grid gap-4 sm:grid-cols-2"><BaseField id="invoice-period" v-model="forms.invoice.billingPeriod" label="Kỳ hóa đơn" type="date" required /><BaseField id="invoice-due" v-model="forms.invoice.dueDate" label="Hạn thanh toán" type="date" required /></div>
          <div class="grid gap-4 sm:grid-cols-3"><BaseField id="invoice-service" v-model="forms.invoice.serviceFee" label="Phí dịch vụ" type="number" /><BaseField id="invoice-discount" v-model="forms.invoice.discount" label="Giảm trừ" type="number" /><BaseField id="invoice-fine" v-model="forms.invoice.fineAmount" label="Khoản phạt" type="number" /></div>
        </template>
        <template v-if="dialog === 'payment'">
          <div><BaseLabel for-id="payment-invoice" required>Hóa đơn</BaseLabel><BaseSelect id="payment-invoice" v-model="forms.payment.invoiceId" :options="unpaidInvoiceOptions" /></div>
          <BaseField id="payment-amount" v-model="forms.payment.amount" label="Số tiền nhận" type="number" required />
        </template>
        <BaseButton type="submit" block :loading="saving">{{ editingId ? 'Lưu thay đổi' : 'Lưu dữ liệu' }}</BaseButton>
      </form>
    </BaseModal>
  </AppLayout>
</template>

