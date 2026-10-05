<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseField from '@/components/base/BaseField.vue'
import BaseLabel from '@/components/base/BaseLabel.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import { useToast } from '@/composables/useToast'
import { registrationService, type Assignment, type Registration } from '@/services/registrationService'
import { contractService, type Contract } from '@/services/contractService'
import { apiErrorMessage } from '@/utils/apiError'

const toast = useToast()
const loading = ref(true)
const submitting = ref(false)
const registrations = ref<Registration[]>([])
const assignments = ref<Assignment[]>([])
const contracts = ref<Contract[]>([])
const form = reactive({
  requestedRoomType: 'STANDARD_8',
  requestedGenderType: 'MALE',
  preferredStartDate: new Date().toISOString().slice(0, 10),
  preferredEndDate: '',
  reason: '',
})

const roomTypes = [
  { value: 'STANDARD_8', label: 'Phòng tiêu chuẩn 8 người' },
  { value: 'STANDARD_6', label: 'Phòng tiêu chuẩn 6 người' },
  { value: 'PREMIUM_4', label: 'Phòng chất lượng cao 4 người' },
]
const genderTypes = [
  { value: 'MALE', label: 'Phòng nam' },
  { value: 'FEMALE', label: 'Phòng nữ' },
  { value: 'MIXED', label: 'Phòng hỗn hợp' },
]
const activeRegistration = computed(() => registrations.value.find((item) => ['PENDING', 'APPROVED'].includes(item.status)))

function badgeTone(status: string): 'info' | 'success' | 'warning' | 'neutral' | 'danger' {
  if (['APPROVED', 'ACTIVE'].includes(status)) return 'success'
  if (status === 'PENDING' || status === 'DRAFT') return 'warning'
  if (['REJECTED', 'TERMINATED'].includes(status)) return 'danger'
  return 'neutral'
}

function statusLabel(status: string) {
  return ({ PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối', CANCELLED: 'Đã hủy',
    ACTIVE: 'Đang hiệu lực', ENDED: 'Đã kết thúc', DRAFT: 'Bản nháp', EXPIRED: 'Hết hạn', TERMINATED: 'Đã thanh lý' } as Record<string, string>)[status] || status
}

async function load() {
  loading.value = true
  try {
    ;[registrations.value, assignments.value, contracts.value] = await Promise.all([
      registrationService.mine(), registrationService.myAssignments(), contractService.mine(),
    ])
  } catch (cause) {
    toast.error(apiErrorMessage(cause, 'Không tải được hồ sơ nội trú.'))
  } finally {
    loading.value = false
  }
}

async function submit() {
  submitting.value = true
  try {
    await registrationService.createMine({
      ...form,
      preferredEndDate: form.preferredEndDate || null,
      reason: form.reason || null,
    })
    toast.success('Hồ sơ đã được gửi đến ban quản lý.', 'Đăng ký thành công')
    await load()
  } catch (cause) {
    toast.error(apiErrorMessage(cause))
  } finally {
    submitting.value = false
  }
}

async function cancel(id: string) {
  try {
    await registrationService.cancel(id)
    toast.info('Hồ sơ chờ duyệt đã được hủy.')
    await load()
  } catch (cause) {
    toast.error(apiErrorMessage(cause))
  }
}

onMounted(load)
</script>

<template>
  <AppLayout>
    <main class="mx-auto w-full max-w-[1440px] p-5 sm:p-7 lg:p-10">
      <header class="mb-7 flex flex-col gap-2">
        <span class="font-mono text-xs font-bold uppercase tracking-[0.2em] text-app-primary">Sinh viên / Nội trú</span>
        <h1 class="font-display text-3xl font-extrabold tracking-tight text-app-ink sm:text-5xl">Hồ sơ nội trú của tôi</h1>
        <p class="max-w-3xl text-sm leading-6 text-app-muted sm:text-base">Theo dõi từ lúc gửi đăng ký, được phân giường đến khi hợp đồng có hiệu lực.</p>
      </header>

      <section v-if="loading" class="rounded-app-lg border border-app-border bg-app-surface p-8 text-app-muted">Đang tải dữ liệu...</section>
      <section v-else class="grid gap-6 xl:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
        <BaseCard class="!rounded-app-lg !border-app-border !bg-app-surface !p-6">
          <div class="mb-6 flex items-start justify-between gap-4">
            <div>
              <p class="text-xs font-bold uppercase tracking-[0.16em] text-app-primary">Đơn đăng ký mới</p>
              <h2 class="mt-2 text-xl font-bold text-app-ink">Nhu cầu phòng ở</h2>
            </div>
            <BaseBadge v-if="activeRegistration" :tone="badgeTone(activeRegistration.status)">{{ statusLabel(activeRegistration.status) }}</BaseBadge>
          </div>

          <form class="grid gap-5" @submit.prevent="submit">
            <div>
              <BaseLabel for-id="portal-room-type" required>Loại phòng</BaseLabel>
              <BaseSelect id="portal-room-type" v-model="form.requestedRoomType" :options="roomTypes" :disabled="Boolean(activeRegistration)" />
            </div>
            <div>
              <BaseLabel for-id="portal-gender" required>Đối tượng phòng</BaseLabel>
              <BaseSelect id="portal-gender" v-model="form.requestedGenderType" :options="genderTypes" :disabled="Boolean(activeRegistration)" />
            </div>
            <div class="grid gap-4 sm:grid-cols-2">
              <BaseField id="portal-start" v-model="form.preferredStartDate" label="Ngày muốn nhận phòng" type="date" required />
              <BaseField id="portal-end" v-model="form.preferredEndDate" label="Ngày dự kiến kết thúc" type="date" />
            </div>
            <div>
              <BaseLabel for-id="portal-reason">Ghi chú nhu cầu</BaseLabel>
              <BaseTextarea id="portal-reason" v-model="form.reason" placeholder="Ví dụ: ưu tiên tầng thấp..." />
            </div>
            <BaseButton type="submit" block :loading="submitting" :disabled="Boolean(activeRegistration)">
              {{ activeRegistration ? 'Bạn đang có hồ sơ được xử lý' : 'Gửi hồ sơ đăng ký' }}
            </BaseButton>
          </form>
        </BaseCard>

        <div class="grid content-start gap-6">
          <BaseCard class="!rounded-app-lg !border-app-border !bg-app-surface !p-6">
            <div class="mb-5 flex items-center justify-between gap-3">
              <h2 class="text-xl font-bold text-app-ink">Tiến trình đăng ký</h2>
              <span class="text-xs text-app-muted">{{ registrations.length }} hồ sơ</span>
            </div>
            <div v-if="!registrations.length" class="rounded-app-md border border-dashed border-app-border p-6 text-sm text-app-muted">Chưa có hồ sơ đăng ký.</div>
            <div v-else class="grid gap-3">
              <article v-for="item in registrations" :key="item.id" class="rounded-app-md border border-app-border bg-app-bg p-4">
                <div class="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p class="font-bold text-app-ink">{{ item.requestedRoomType }} · {{ item.requestedGenderType }}</p>
                    <p class="mt-1 text-xs text-app-muted">Nhận phòng từ {{ item.preferredStartDate }}</p>
                  </div>
                  <BaseBadge :tone="badgeTone(item.status)">{{ statusLabel(item.status) }}</BaseBadge>
                </div>
                <p v-if="item.rejectionReason" class="mt-3 rounded-app-sm bg-red-50 p-3 text-sm text-red-700">{{ item.rejectionReason }}</p>
                <div v-if="item.status === 'PENDING'" class="mt-3 flex justify-end">
                  <BaseButton variant="danger" size="sm" @click="cancel(item.id)">Hủy hồ sơ</BaseButton>
                </div>
              </article>
            </div>
          </BaseCard>

          <section class="grid gap-6 lg:grid-cols-2">
            <BaseCard class="!rounded-app-lg !border-app-border !bg-app-surface !p-6">
              <p class="text-xs font-bold uppercase tracking-[0.16em] text-app-primary">Phân phòng</p>
              <h2 class="mt-2 text-lg font-bold text-app-ink">Giường của tôi</h2>
              <div v-if="assignments.length" class="mt-4 grid gap-3">
                <div v-for="item in assignments" :key="item.id" class="rounded-app-md bg-app-bg p-4">
                  <div class="flex justify-between gap-3"><strong class="text-app-ink">{{ item.buildingCode }} · {{ item.roomNumber }} · Giường {{ item.bedNumber }}</strong><BaseBadge :tone="badgeTone(item.status)">{{ statusLabel(item.status) }}</BaseBadge></div>
                  <p class="mt-2 text-xs text-app-muted">Tầng {{ item.floorNumber }} · từ {{ item.startDate }}</p>
                </div>
              </div>
              <p v-else class="mt-4 text-sm text-app-muted">Chưa được phân giường.</p>
            </BaseCard>

            <BaseCard class="!rounded-app-lg !border-app-border !bg-app-surface !p-6">
              <p class="text-xs font-bold uppercase tracking-[0.16em] text-app-primary">Hợp đồng</p>
              <h2 class="mt-2 text-lg font-bold text-app-ink">Hợp đồng nội trú</h2>
              <div v-if="contracts.length" class="mt-4 grid gap-3">
                <div v-for="item in contracts" :key="item.id" class="rounded-app-md bg-app-bg p-4">
                  <div class="flex flex-wrap justify-between gap-3"><strong class="text-app-ink">{{ item.contractCode }}</strong><BaseBadge :tone="badgeTone(item.status)">{{ statusLabel(item.status) }}</BaseBadge></div>
                  <p class="mt-2 text-xs text-app-muted">{{ item.startDate }} → {{ item.endDate }}</p>
                  <p class="mt-2 font-semibold text-app-ink">{{ Number(item.rentalPrice).toLocaleString('vi-VN') }} đ/tháng</p>
                </div>
              </div>
              <p v-else class="mt-4 text-sm text-app-muted">Chưa có hợp đồng.</p>
            </BaseCard>
          </section>
        </div>
      </section>
    </main>
  </AppLayout>
</template>
