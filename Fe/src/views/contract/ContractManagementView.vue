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
import { contractService, type Contract } from '@/services/contractService'
import { registrationService, type Assignment } from '@/services/registrationService'
import { apiErrorMessage } from '@/utils/apiError'

const toast = useToast()
const loading = ref(false)
const contracts = ref<Contract[]>([])
const assignments = ref<Assignment[]>([])
const selected = ref<Contract | null>(null)
const status = ref('')
const createOpen = ref(false)
const activateOpen = ref(false)
const renewOpen = ref(false)
const terminateOpen = ref(false)
const createForm = reactive({ assignmentId: '', startDate: '', endDate: '', deposit: '0' })
const newEndDate = ref('')
const terminationReason = ref('')

const statusOptions = [{ value: '', label: 'Tất cả trạng thái' }, { value: 'DRAFT', label: 'Bản nháp' },
  { value: 'ACTIVE', label: 'Đang hiệu lực' }, { value: 'EXPIRED', label: 'Hết hạn' }, { value: 'TERMINATED', label: 'Đã thanh lý' }]
const assignmentOptions = computed(() => [{ value: '', label: 'Chọn phân phòng đang hoạt động' }, ...assignments.value
  .filter((item) => item.status === 'ACTIVE' && !contracts.value.some((contract) => contract.assignmentId === item.id))
  .map((item) => ({ value: item.id, label: `${item.studentCode || item.fullName} · ${item.buildingCode}-${item.roomNumber}/${item.bedNumber}` }))])

function tone(value: string): 'info' | 'success' | 'warning' | 'neutral' | 'danger' {
  if (value === 'ACTIVE') return 'success'
  if (value === 'DRAFT') return 'warning'
  if (value === 'TERMINATED') return 'danger'
  return 'neutral'
}
function label(value: string) { return ({ DRAFT: 'Bản nháp', ACTIVE: 'Đang hiệu lực', EXPIRED: 'Hết hạn', TERMINATED: 'Đã thanh lý' } as Record<string, string>)[value] || value }

async function load() {
  loading.value = true
  try {
    ;[contracts.value, assignments.value] = await Promise.all([contractService.all(status.value), registrationService.assignments()])
  } catch (cause) { toast.error(apiErrorMessage(cause, 'Không tải được danh sách hợp đồng.')) }
  finally { loading.value = false }
}

function openCreate() {
  Object.assign(createForm, { assignmentId: '', startDate: new Date().toISOString().slice(0, 10), endDate: '', deposit: '0' })
  createOpen.value = true
}
async function create() {
  try {
    await contractService.create({ ...createForm, deposit: Number(createForm.deposit) })
    createOpen.value = false
    toast.success('Đã tạo hợp đồng nháp từ phân phòng.')
    await load()
  } catch (cause) { toast.error(apiErrorMessage(cause)) }
}
function openActivate(item: Contract) { selected.value = item; activateOpen.value = true }
async function activate() {
  if (!selected.value) return
  try { await contractService.activate(selected.value.id); activateOpen.value = false; toast.success(`Hợp đồng ${selected.value.contractCode} đã có hiệu lực.`); await load() }
  catch (cause) { toast.error(apiErrorMessage(cause)) }
}
function openRenew(item: Contract) { selected.value = item; newEndDate.value = item.endDate; renewOpen.value = true }
async function renew() {
  if (!selected.value) return
  try { await contractService.renew(selected.value.id, newEndDate.value); renewOpen.value = false; toast.success('Đã gia hạn hợp đồng.'); await load() }
  catch (cause) { toast.error(apiErrorMessage(cause)) }
}
function openTerminate(item: Contract) { selected.value = item; terminationReason.value = ''; terminateOpen.value = true }
async function terminate() {
  if (!selected.value) return
  try { await contractService.terminate(selected.value.id, terminationReason.value); terminateOpen.value = false; toast.info('Đã thanh lý hợp đồng và trả giường.'); await load() }
  catch (cause) { toast.error(apiErrorMessage(cause)) }
}
onMounted(load)
</script>

<template>
  <AppLayout>
    <main class="mx-auto w-full max-w-[1500px] p-5 sm:p-7 lg:p-10">
      <header class="mb-7 flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
        <div><span class="font-mono text-xs font-bold uppercase tracking-[0.2em] text-app-primary">Phase 4 / Contract</span><h1 class="mt-2 font-display text-3xl font-extrabold tracking-tight text-app-ink sm:text-5xl">Hợp đồng nội trú</h1><p class="mt-2 text-app-muted">Tạo từ phân phòng, kích hoạt, gia hạn và thanh lý theo một vòng đời rõ ràng.</p></div>
        <BaseButton @click="openCreate">Tạo hợp đồng</BaseButton>
      </header>

      <BaseCard class="mb-5 !rounded-app-lg !border-app-border !bg-app-surface !p-4">
        <div class="max-w-xs"><BaseLabel for-id="contract-status">Lọc trạng thái</BaseLabel><BaseSelect id="contract-status" v-model="status" :options="statusOptions" @update:model-value="load" /></div>
      </BaseCard>

      <section class="grid gap-4 lg:grid-cols-2">
        <BaseCard v-for="item in contracts" :key="item.id" class="!rounded-app-lg !border-app-border !bg-app-surface !p-5">
          <article class="grid gap-5">
            <div class="flex flex-wrap items-start justify-between gap-3"><div><p class="font-mono text-xs font-bold text-app-primary">{{ item.contractCode }}</p><h2 class="mt-1 text-xl font-bold text-app-ink">{{ item.fullName }}</h2><p class="mt-1 text-sm text-app-muted">{{ item.studentCode || 'Chưa có mã SV' }} · {{ item.buildingCode }}-{{ item.roomNumber }}/{{ item.bedNumber }}</p></div><BaseBadge :tone="tone(item.status)">{{ label(item.status) }}</BaseBadge></div>
            <div class="grid grid-cols-2 gap-3 rounded-app-md bg-app-bg p-4 text-sm"><div><span class="block text-xs text-app-muted">Thời hạn</span><strong class="text-app-ink">{{ item.startDate }} → {{ item.endDate }}</strong></div><div><span class="block text-xs text-app-muted">Giá tháng / Cọc</span><strong class="text-app-ink">{{ Number(item.rentalPrice).toLocaleString('vi-VN') }} / {{ Number(item.deposit).toLocaleString('vi-VN') }} đ</strong></div></div>
            <div class="flex flex-wrap justify-end gap-2"><BaseButton v-if="item.status === 'DRAFT'" size="sm" @click="openActivate(item)">Kích hoạt</BaseButton><BaseButton v-if="item.status === 'ACTIVE'" variant="secondary" size="sm" @click="openRenew(item)">Gia hạn</BaseButton><BaseButton v-if="['DRAFT', 'ACTIVE'].includes(item.status)" variant="danger" size="sm" @click="openTerminate(item)">Thanh lý</BaseButton></div>
          </article>
        </BaseCard>
      </section>
      <div v-if="!loading && !contracts.length" class="rounded-app-lg border border-dashed border-app-border bg-app-surface p-10 text-center text-app-muted">Chưa có hợp đồng phù hợp.</div>
    </main>

    <BaseModal v-model="createOpen" title="Tạo hợp đồng nháp" max-width="46rem"><div class="grid gap-5"><div><BaseLabel for-id="contract-assignment" required>Phân phòng</BaseLabel><BaseSelect id="contract-assignment" v-model="createForm.assignmentId" :options="assignmentOptions" /></div><div class="grid gap-4 sm:grid-cols-2"><BaseField id="contract-start" v-model="createForm.startDate" label="Ngày bắt đầu" type="date" required /><BaseField id="contract-end" v-model="createForm.endDate" label="Ngày kết thúc" type="date" required /></div><BaseField id="contract-deposit" v-model="createForm.deposit" label="Tiền đặt cọc" type="number" /></div><template #footer><BaseButton variant="secondary" @click="createOpen = false">Đóng</BaseButton><BaseButton :disabled="!createForm.assignmentId || !createForm.endDate" @click="create">Tạo bản nháp</BaseButton></template></BaseModal>
    <BaseModal v-model="activateOpen" title="Kích hoạt hợp đồng"><p class="text-sm leading-6 text-app-muted">Sau khi kích hoạt, hợp đồng sẽ có hiệu lực và hệ thống không cho phép sinh viên có thêm hợp đồng ACTIVE khác. Bạn xác nhận tiếp tục?</p><template #footer><BaseButton variant="secondary" @click="activateOpen = false">Đóng</BaseButton><BaseButton @click="activate">Xác nhận kích hoạt</BaseButton></template></BaseModal>
    <BaseModal v-model="renewOpen" title="Gia hạn hợp đồng"><BaseField id="contract-new-end" v-model="newEndDate" label="Ngày kết thúc mới" type="date" required /><template #footer><BaseButton variant="secondary" @click="renewOpen = false">Đóng</BaseButton><BaseButton @click="renew">Xác nhận gia hạn</BaseButton></template></BaseModal>
    <BaseModal v-model="terminateOpen" title="Thanh lý hợp đồng"><BaseLabel for-id="contract-termination" required>Lý do thanh lý</BaseLabel><BaseTextarea id="contract-termination" v-model="terminationReason" placeholder="Nhập lý do và căn cứ thanh lý..." /><template #footer><BaseButton variant="secondary" @click="terminateOpen = false">Đóng</BaseButton><BaseButton variant="danger" :disabled="!terminationReason.trim()" @click="terminate">Xác nhận thanh lý</BaseButton></template></BaseModal>
  </AppLayout>
</template>
