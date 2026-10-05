<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseField from '@/components/base/BaseField.vue'
import BaseLabel from '@/components/base/BaseLabel.vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import { useToast } from '@/composables/useToast'
import { registrationService, type Assignment, type Bed, type Registration, type Room } from '@/services/registrationService'
import { apiErrorMessage } from '@/utils/apiError'

const toast = useToast()
const loading = ref(false)
const items = ref<Registration[]>([])
const assignments = ref<Assignment[]>([])
const rooms = ref<Room[]>([])
const beds = ref<Bed[]>([])
const status = ref('')
const keyword = ref('')
const selected = ref<Registration | null>(null)
const rejectOpen = ref(false)
const assignOpen = ref(false)
const rejectionReason = ref('')
const assignment = reactive({ roomId: '', bedId: '', startDate: new Date().toISOString().slice(0, 10), endDate: '' })

const statusOptions = [
  { value: '', label: 'Tất cả trạng thái' }, { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' }, { value: 'REJECTED', label: 'Từ chối' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]
const compatibleRooms = computed(() => rooms.value.filter((room) => !selected.value
  || (room.roomType === selected.value.requestedRoomType && room.genderType === selected.value.requestedGenderType)))
const roomOptions = computed(() => [
  { value: '', label: 'Chọn phòng phù hợp' },
  ...compatibleRooms.value.map((room) => ({ value: room.id, label: `${room.buildingCode} · ${room.roomNumber} (${room.availableBeds} chỗ)` })),
])
const bedOptions = computed(() => [
  { value: '', label: 'Chọn giường' }, ...beds.value.map((bed) => ({ value: bed.id, label: `Giường ${bed.bedNumber}` })),
])
const assignedRegistrationIds = computed(() => new Set(assignments.value.map((item) => item.registrationId)))

function tone(value: string): 'info' | 'success' | 'warning' | 'neutral' | 'danger' {
  if (value === 'APPROVED') return 'success'
  if (value === 'PENDING') return 'warning'
  if (value === 'REJECTED') return 'danger'
  return 'neutral'
}
function label(value: string) {
  return ({ PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối', CANCELLED: 'Đã hủy' } as Record<string, string>)[value] || value
}

async function load() {
  loading.value = true
  try {
    const [result, assignmentItems] = await Promise.all([
      registrationService.queue(status.value, keyword.value), registrationService.assignments(),
    ])
    items.value = result.content
    assignments.value = assignmentItems
  } catch (cause) {
    toast.error(apiErrorMessage(cause, 'Không tải được hàng đợi đăng ký.'))
  } finally {
    loading.value = false
  }
}

async function approve(item: Registration) {
  try {
    await registrationService.review(item.id, 'APPROVED')
    toast.success(`Đã duyệt hồ sơ của ${item.fullName}.`)
    await load()
  } catch (cause) { toast.error(apiErrorMessage(cause)) }
}

function openReject(item: Registration) {
  selected.value = item
  rejectionReason.value = ''
  rejectOpen.value = true
}

async function reject() {
  if (!selected.value) return
  try {
    await registrationService.review(selected.value.id, 'REJECTED', rejectionReason.value)
    rejectOpen.value = false
    toast.info('Đã từ chối hồ sơ và lưu lý do.')
    await load()
  } catch (cause) { toast.error(apiErrorMessage(cause)) }
}

async function openAssign(item: Registration) {
  selected.value = item
  assignment.roomId = ''
  assignment.bedId = ''
  assignment.startDate = item.preferredStartDate
  assignment.endDate = item.preferredEndDate || ''
  try {
    rooms.value = await registrationService.rooms()
    assignOpen.value = true
  } catch (cause) { toast.error(apiErrorMessage(cause)) }
}

async function loadBeds() {
  assignment.bedId = ''
  beds.value = assignment.roomId ? await registrationService.beds(assignment.roomId) : []
}

async function assign() {
  if (!selected.value || !assignment.bedId) return
  try {
    await registrationService.assign({ registrationId: selected.value.id, bedId: assignment.bedId,
      startDate: assignment.startDate, endDate: assignment.endDate || null })
    assignOpen.value = false
    toast.success('Đã phân giường và khóa chỗ thành công.')
    await load()
  } catch (cause) { toast.error(apiErrorMessage(cause)) }
}

onMounted(load)
</script>

<template>
  <AppLayout>
    <main class="mx-auto w-full max-w-[1500px] p-5 sm:p-7 lg:p-10">
      <header class="mb-7 flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <span class="font-mono text-xs font-bold uppercase tracking-[0.2em] text-app-primary">Phase 3 / Registration</span>
          <h1 class="mt-2 font-display text-3xl font-extrabold tracking-tight text-app-ink sm:text-5xl">Đăng ký & phân phòng</h1>
          <p class="mt-2 text-app-muted">Duyệt nhu cầu trước, sau đó gán đúng phòng và giường còn trống.</p>
        </div>
        <BaseBadge tone="info">{{ items.length }} hồ sơ</BaseBadge>
      </header>

      <BaseCard class="mb-5 !rounded-app-lg !border-app-border !bg-app-surface !p-4">
        <form class="grid gap-3 md:grid-cols-[minmax(0,1fr)_240px_auto]" @submit.prevent="load">
          <BaseField id="registration-keyword" v-model="keyword" label="Tìm sinh viên" placeholder="Mã SV, họ tên hoặc email" />
          <div>
            <BaseLabel for-id="registration-status">Trạng thái</BaseLabel>
            <BaseSelect id="registration-status" v-model="status" :options="statusOptions" />
          </div>
          <div class="flex items-end"><BaseButton type="submit" :loading="loading">Lọc danh sách</BaseButton></div>
        </form>
      </BaseCard>

      <section class="grid gap-3">
        <BaseCard v-for="item in items" :key="item.id" class="!rounded-app-lg !border-app-border !bg-app-surface !p-5">
          <article class="grid gap-5 lg:grid-cols-[1.2fr_1fr_auto] lg:items-center">
            <div>
              <div class="flex flex-wrap items-center gap-3"><h2 class="text-lg font-bold text-app-ink">{{ item.fullName }}</h2><BaseBadge :tone="tone(item.status)">{{ label(item.status) }}</BaseBadge></div>
              <p class="mt-1 text-sm text-app-muted">{{ item.studentCode || 'Chưa có mã SV' }} · {{ item.email }}</p>
            </div>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div><span class="block text-xs text-app-muted">Nhu cầu</span><strong class="text-app-ink">{{ item.requestedRoomType }}</strong></div>
              <div><span class="block text-xs text-app-muted">Đối tượng</span><strong class="text-app-ink">{{ item.requestedGenderType }}</strong></div>
              <div><span class="block text-xs text-app-muted">Nhận phòng</span><strong class="text-app-ink">{{ item.preferredStartDate }}</strong></div>
              <div><span class="block text-xs text-app-muted">Kết thúc</span><strong class="text-app-ink">{{ item.preferredEndDate || 'Chưa xác định' }}</strong></div>
            </div>
            <div class="flex flex-wrap justify-end gap-2">
              <template v-if="item.status === 'PENDING'">
                <BaseButton variant="secondary" size="sm" @click="openReject(item)">Từ chối</BaseButton>
                <BaseButton size="sm" @click="approve(item)">Duyệt hồ sơ</BaseButton>
              </template>
              <BaseBadge v-if="item.status === 'APPROVED' && assignedRegistrationIds.has(item.id)" tone="success">Đã phân giường</BaseBadge>
              <BaseButton v-else-if="item.status === 'APPROVED'" size="sm" @click="openAssign(item)">Phân giường</BaseButton>
            </div>
          </article>
        </BaseCard>
        <div v-if="!loading && !items.length" class="rounded-app-lg border border-dashed border-app-border bg-app-surface p-10 text-center text-app-muted">Không có hồ sơ phù hợp bộ lọc.</div>
      </section>
    </main>

    <BaseModal v-model="rejectOpen" title="Từ chối hồ sơ">
      <BaseLabel for-id="rejection-reason" required>Lý do từ chối</BaseLabel>
      <BaseTextarea id="rejection-reason" v-model="rejectionReason" placeholder="Nêu rõ thông tin cần sinh viên bổ sung..." />
      <template #footer><BaseButton variant="secondary" @click="rejectOpen = false">Đóng</BaseButton><BaseButton variant="danger" :disabled="!rejectionReason.trim()" @click="reject">Xác nhận từ chối</BaseButton></template>
    </BaseModal>

    <BaseModal v-model="assignOpen" title="Phân phòng và giường" max-width="46rem">
      <div class="grid gap-5">
        <div><BaseLabel for-id="assignment-room" required>Phòng tương thích</BaseLabel><BaseSelect id="assignment-room" v-model="assignment.roomId" :options="roomOptions" @update:model-value="loadBeds" /></div>
        <div><BaseLabel for-id="assignment-bed" required>Giường còn trống</BaseLabel><BaseSelect id="assignment-bed" v-model="assignment.bedId" :options="bedOptions" :disabled="!assignment.roomId" /></div>
        <div class="grid gap-4 sm:grid-cols-2"><BaseField id="assignment-start" v-model="assignment.startDate" label="Ngày bắt đầu" type="date" required /><BaseField id="assignment-end" v-model="assignment.endDate" label="Ngày kết thúc dự kiến" type="date" /></div>
      </div>
      <template #footer><BaseButton variant="secondary" @click="assignOpen = false">Đóng</BaseButton><BaseButton :disabled="!assignment.bedId" @click="assign">Xác nhận phân giường</BaseButton></template>
    </BaseModal>
  </AppLayout>
</template>
