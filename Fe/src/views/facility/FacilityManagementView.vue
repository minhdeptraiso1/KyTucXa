<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseLabel from '@/components/base/BaseLabel.vue'
import BaseField from '@/components/base/BaseField.vue'
import BaseCheckbox from '@/components/base/BaseCheckbox.vue'
import {
  facilityService,
  type Building,
  type Floor,
  type Room,
  type Bed,
  type PricePolicy,
  type FacilityOverviewStats,
} from '@/services/facilityService'
import { useToast } from '@/composables/useToast'

const toast = useToast()

// Active tab
const activeTab = ref<'tree' | 'rooms' | 'policies'>('tree')

// Overview stats
const stats = ref<FacilityOverviewStats>({
  totalBuildings: 0,
  totalFloors: 0,
  totalRooms: 0,
  availableRooms: 0,
  fullRooms: 0,
  maintenanceRooms: 0,
  totalBeds: 0,
  availableBeds: 0,
  occupiedBeds: 0,
  maintenanceBeds: 0,
  occupancyRate: 0,
})

// Loading states
const loading = ref(false)
const modalLoading = ref(false)

// Data lists
const buildings = ref<Building[]>([])

const floors = ref<Floor[]>([])
const rooms = ref<Room[]>([])
const allRooms = ref<Room[]>([])
const pricePolicies = ref<PricePolicy[]>([])

// Tree selections
const selectedBuildingId = ref<string>('')
const selectedFloorId = ref<string>('')

// Room & Bed detail modal
const showRoomDetailModal = ref(false)
const selectedRoom = ref<Room | null>(null)
const roomBeds = ref<Bed[]>([])
const bedsLoading = ref(false)

// Modals: Add Building
const showAddBuildingModal = ref(false)
const buildingForm = ref({
  code: '',
  name: '',
  address: '',
  genderType: 'MALE',
  totalFloors: '5',
  description: '',
})

// Modals: Add Floor
const showAddFloorModal = ref(false)
const floorForm = ref({
  floorNumber: '1',
  name: '',
  status: 'ACTIVE',
})

// Modals: Add Room
const showAddRoomModal = ref(false)
const roomForm = ref({
  floorId: '',
  roomNumber: '',
  roomType: 'STANDARD_8',
  capacity: '8',
  genderType: 'MALE',
  areaSqm: '32',
  pricePolicyId: '',
  imageUrl: '',
  description: '',
  autoGenerateBeds: true,
})

// Modals: Add Policy
const showAddPolicyModal = ref(false)
const editingPolicyId = ref('')
const policyForm = ref({
  name: '',
  roomType: 'STANDARD_8',
  pricePerMonth: '600000',
  effectiveFrom: new Date().toISOString().split('T')[0],
  effectiveTo: '',
  isActive: true,
  description: '',
})

// Filter states for Tab 2 (All rooms)
const roomFilterBuilding = ref('')
const roomFilterType = ref('')
const roomFilterStatus = ref('')
const roomFilterGender = ref('')
const roomSearchQuery = ref('')
const roomPricePolicyOptions = computed(() => [
  { label: 'Chưa gán bảng giá', value: '' },
  ...pricePolicies.value
    .filter((policy) => policy.isActive && policy.roomType === roomForm.value.roomType)
    .map((policy) => ({ label: `${policy.name} · ${formatCurrency(policy.pricePerMonth)}`, value: policy.id })),
])

// Helpers
function formatCurrency(amount: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

function getGenderBadge(gender: string) {
  switch (gender) {
    case 'MALE':
      return { text: 'Nam', color: 'info' }
    case 'FEMALE':
      return { text: 'Nữ', color: 'neutral' }
    default:
      return { text: 'Nam & Nữ', color: 'info' }
  }
}

function getRoomStatusBadge(status: string) {
  switch (status) {
    case 'AVAILABLE':
      return { text: 'Còn chỗ', color: 'success' }
    case 'FULL':
      return { text: 'Đã đầy', color: 'neutral' }
    case 'MAINTENANCE':
      return { text: 'Bảo trì', color: 'warning' }
    case 'INACTIVE':
      return { text: 'Ngưng dùng', color: 'danger' }
    default:
      return { text: status, color: 'neutral' }
  }
}

function getBedStatusBadge(status: string) {
  switch (status) {
    case 'AVAILABLE':
      return { text: 'Sẵn sàng', color: 'success' }
    case 'OCCUPIED':
      return { text: 'Đang ở', color: 'info' }
    case 'MAINTENANCE':
      return { text: 'Bảo trì', color: 'warning' }
    case 'INACTIVE':
      return { text: 'Khóa', color: 'danger' }
    default:
      return { text: status, color: 'neutral' }
  }
}

function getRoomTypeLabel(type: string) {
  switch (type) {
    case 'STANDARD_8':
      return 'Phòng tiêu chuẩn 8 người'
    case 'STANDARD_6':
      return 'Phòng tiêu chuẩn 6 người'
    case 'PREMIUM_4':
      return 'Phòng chất lượng cao 4 người'
    default:
      return type
  }
}

// Fetch Initial Data
async function loadFacilityData() {
  loading.value = true
  try {
    const bldRes = await facilityService.getBuildings({ page: 0, size: 50 })
    buildings.value = bldRes?.data?.content ?? []
    selectedBuildingId.value = buildings.value.some((item) => item.id === selectedBuildingId.value)
      ? selectedBuildingId.value
      : buildings.value[0]?.id ?? ''

    const [statsResult, policyResult] = await Promise.allSettled([
      facilityService.getOverviewStats(),
      facilityService.getAllPricePolicies(),
    ])
    if (statsResult.status === 'fulfilled' && statsResult.value?.data) {
      stats.value = statsResult.value.data
    }
    pricePolicies.value = policyResult.status === 'fulfilled' ? policyResult.value?.data ?? [] : []

    await loadFloorsForSelectedBuilding()
    await loadAllRooms()
  } catch (err) {
    buildings.value = []
    floors.value = []
    rooms.value = []
    allRooms.value = []
    toast.error('Không thể tải dữ liệu cơ sở vật chất từ máy chủ. Vui lòng kiểm tra backend.', 'Lỗi dữ liệu')
  } finally {
    loading.value = false
  }
}

async function loadFloorsForSelectedBuilding() {
  if (!selectedBuildingId.value) {
    floors.value = []
    rooms.value = []
    selectedFloorId.value = ''
    return
  }
  try {
    const res = await facilityService.getFloorsByBuilding(selectedBuildingId.value)
    floors.value = res?.data ?? []
    selectedFloorId.value = floors.value[0]?.id ?? ''
    await loadRoomsForSelectedFloor()
  } catch (err: any) {
    floors.value = []
    rooms.value = []
    selectedFloorId.value = ''
    toast.error(err.response?.data?.error?.message || 'Không thể tải danh sách tầng.')
  }
}

async function loadRoomsForSelectedFloor() {
  if (!selectedFloorId.value) {
    rooms.value = []
    return
  }
  try {
    const res = await facilityService.getRoomsByFloor(selectedFloorId.value)
    rooms.value = res?.data ?? []
  } catch (err: any) {
    rooms.value = []
    toast.error(err.response?.data?.error?.message || 'Không thể tải danh sách phòng của tầng.')
  }
}

async function loadAllRooms() {
  try {
    const res = await facilityService.getRooms({
      buildingId: roomFilterBuilding.value || undefined,
      status: roomFilterStatus.value || undefined,
      roomType: roomFilterType.value || undefined,
      genderType: roomFilterGender.value || undefined,
      search: roomSearchQuery.value || undefined,
      size: 100,
    })
    allRooms.value = res?.data?.content ?? []
  } catch (err: any) {
    allRooms.value = []
    toast.error(err.response?.data?.error?.message || 'Không thể tải danh sách phòng.')
  }
}

// Open Room Detail & Bed Management Modal
async function openRoomDetails(room: Room) {
  selectedRoom.value = room
  showRoomDetailModal.value = true
  bedsLoading.value = true
  try {
    const res = await facilityService.getBedsByRoom(room.id)
    roomBeds.value = res?.data ?? []
  } catch (err: any) {
    roomBeds.value = []
    toast.error(err.response?.data?.error?.message || 'Không thể tải danh sách giường.')
  } finally {
    bedsLoading.value = false
  }
}

async function toggleBedStatus(bed: Bed) {
  if (bed.status === 'OCCUPIED') {
    toast.warning('Giường đang có sinh viên ở, không thể đổi trực tiếp sang bảo trì.', 'Cảnh báo')
    return
  }

  const newStatus = bed.status === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE'
  try {
    await facilityService.updateBedStatus(bed.id, newStatus)
    bed.status = newStatus
    bed.isUsable = newStatus === 'AVAILABLE'
    toast.success(`Đã cập nhật trạng thái giường ${bed.bedNumber} thành ${newStatus === 'AVAILABLE' ? 'Sẵn sàng' : 'Bảo trì'}.`)
  } catch (err: any) {
    toast.error(err.response?.data?.error?.message || 'Không thể cập nhật trạng thái giường.')
  }
}

async function toggleRoomStatus(room: Room, newStatus: 'AVAILABLE' | 'MAINTENANCE' | 'INACTIVE') {
  try {
    await facilityService.updateRoomStatus(room.id, newStatus)
    room.status = newStatus
    toast.success(`Đã cập nhật trạng thái phòng ${room.roomNumber} thành ${getRoomStatusBadge(newStatus).text}.`)
  } catch (err: any) {
    toast.error(err.response?.data?.error?.message || 'Không thể cập nhật trạng thái phòng.')
  }
}

// Add Building Action
async function handleCreateBuilding() {
  modalLoading.value = true
  try {
    const data = {
      ...buildingForm.value,
      totalFloors: parseInt(buildingForm.value.totalFloors) || 5,
    }
    const res = await facilityService.createBuilding(data)
    if (res?.data) {
      buildings.value.push(res.data)
      selectedBuildingId.value = res.data.id
    }
    toast.success(`Tạo tòa nhà ${buildingForm.value.code} thành công!`)
    showAddBuildingModal.value = false
    buildingForm.value = { code: '', name: '', address: '', genderType: 'MALE', totalFloors: '5', description: '' }
  } catch (err: any) {
    toast.error(err.response?.data?.error?.message || 'Có lỗi xảy ra khi tạo tòa nhà.')
  } finally {
    modalLoading.value = false
  }
}

function openCreateRoom() {
  if (!selectedBuildingId.value) {
    toast.warning('Vui lòng chọn tòa nhà trước khi tạo phòng.')
    return
  }
  if (!selectedFloorId.value) {
    toast.warning('Tòa nhà chưa có tầng. Hãy thêm tầng trước khi tạo phòng.')
    return
  }
  showAddRoomModal.value = true
}

async function handleCreateFloor() {
  if (!selectedBuildingId.value) {
    toast.warning('Vui lòng chọn tòa nhà trước khi thêm tầng.')
    return
  }
  modalLoading.value = true
  try {
    const res = await facilityService.createFloor(selectedBuildingId.value, {
      floorNumber: Number(floorForm.value.floorNumber),
      name: floorForm.value.name || `Tầng ${floorForm.value.floorNumber}`,
      status: floorForm.value.status,
    })
    if (res?.data) {
      floors.value.push(res.data)
      floors.value.sort((left, right) => left.floorNumber - right.floorNumber)
      selectedFloorId.value = res.data.id
      rooms.value = []
    }
    showAddFloorModal.value = false
    floorForm.value = { floorNumber: String(floors.value.length + 1), name: '', status: 'ACTIVE' }
    toast.success('Đã thêm tầng bằng dữ liệu thật.')
  } catch (err: any) {
    toast.error(err.response?.data?.error?.message || 'Không thể tạo tầng.')
  } finally {
    modalLoading.value = false
  }
}

// Add Room Action
async function handleCreateRoom() {
  if (!selectedFloorId.value) {
    toast.warning('Vui lòng chọn tầng trước khi tạo phòng.')
    return
  }

  modalLoading.value = true
  try {
    const currentBld = buildings.value.find((b) => b.id === selectedBuildingId.value)
    const data = {
      ...roomForm.value,
      capacity: parseInt(roomForm.value.capacity) || 8,
      areaSqm: parseFloat(roomForm.value.areaSqm) || 32,
      floorId: selectedFloorId.value,
      pricePolicyId: roomForm.value.pricePolicyId || undefined,
      genderType: currentBld?.genderType === 'MIXED' ? roomForm.value.genderType : currentBld?.genderType || 'MALE',
    }
    const res = await facilityService.createRoom(data)
    if (res?.data) {
      rooms.value.push(res.data)
      allRooms.value.push(res.data)
    }
    toast.success(`Tạo phòng ${roomForm.value.roomNumber} thành công!`)
    showAddRoomModal.value = false
  } catch (err: any) {
    toast.error(err.response?.data?.error?.message || 'Có lỗi khi tạo phòng.')
  } finally {
    modalLoading.value = false
  }
}

function resetPolicyForm() {
  editingPolicyId.value = ''
  policyForm.value = {
    name: '',
    roomType: 'STANDARD_8',
    pricePerMonth: '600000',
    effectiveFrom: new Date().toISOString().split('T')[0],
    effectiveTo: '',
    isActive: true,
    description: '',
  }
}

function openCreatePolicy() {
  resetPolicyForm()
  showAddPolicyModal.value = true
}

function openEditPolicy(policy: PricePolicy) {
  editingPolicyId.value = policy.id
  policyForm.value = {
    name: policy.name,
    roomType: policy.roomType,
    pricePerMonth: String(policy.pricePerMonth),
    effectiveFrom: policy.effectiveFrom,
    effectiveTo: policy.effectiveTo || '',
    isActive: policy.isActive,
    description: policy.description || '',
  }
  showAddPolicyModal.value = true
}

async function handleSavePolicy() {
  modalLoading.value = true
  try {
    if (editingPolicyId.value) {
      await facilityService.updatePricePolicy(editingPolicyId.value, {
        name: policyForm.value.name,
        pricePerMonth: Number(policyForm.value.pricePerMonth),
        effectiveTo: policyForm.value.effectiveTo || undefined,
        isActive: policyForm.value.isActive,
        description: policyForm.value.description || undefined,
      })
    } else {
      await facilityService.createPricePolicy({
        name: policyForm.value.name,
        roomType: policyForm.value.roomType,
        pricePerMonth: Number(policyForm.value.pricePerMonth),
        effectiveFrom: policyForm.value.effectiveFrom,
        effectiveTo: policyForm.value.effectiveTo || undefined,
        isActive: policyForm.value.isActive,
        description: policyForm.value.description || undefined,
      })
    }
    const result = await facilityService.getAllPricePolicies()
    pricePolicies.value = result?.data ?? []
    showAddPolicyModal.value = false
    toast.success(editingPolicyId.value ? 'Đã cập nhật chính sách giá.' : 'Đã thêm chính sách giá mới.')
    resetPolicyForm()
  } catch (err: any) {
    toast.error(err.response?.data?.error?.message || 'Không thể lưu chính sách giá.')
  } finally {
    modalLoading.value = false
  }
}

async function togglePolicy(policy: PricePolicy) {
  try {
    await facilityService.updatePricePolicy(policy.id, {
      name: policy.name,
      pricePerMonth: policy.pricePerMonth,
      effectiveTo: policy.effectiveTo,
      isActive: !policy.isActive,
      description: policy.description,
    })
    policy.isActive = !policy.isActive
    toast.success(policy.isActive ? 'Đã áp dụng lại chính sách giá.' : 'Đã ngừng áp dụng chính sách giá.')
  } catch (err: any) {
    toast.error(err.response?.data?.error?.message || 'Không thể đổi trạng thái chính sách giá.')
  }
}

// Filtered rooms for Tab 2
const filteredAllRooms = computed(() => {
  return allRooms.value.filter((r) => {
    if (roomFilterBuilding.value && r.buildingId !== roomFilterBuilding.value) return false
    if (roomFilterType.value && r.roomType !== roomFilterType.value) return false
    if (roomFilterStatus.value && r.status !== roomFilterStatus.value) return false
    if (roomFilterGender.value && r.genderType !== roomFilterGender.value) return false
    if (roomSearchQuery.value) {
      const q = roomSearchQuery.value.toLowerCase()
      return r.roomNumber.toLowerCase().includes(q) || (r.description && r.description.toLowerCase().includes(q))
    }
    return true
  })
})

onMounted(() => {
  loadFacilityData()
})
</script>

<template>
  <AppLayout>
    <div class="facility-page">
      <!-- Header -->
      <header class="facility-header">
        <div>
          <div class="facility-header__badge">
            <span class="badge-dot" />
            <span>PHASE 2 — QUẢN LÝ CƠ SỞ VẬT CHẤT</span>
          </div>
          <h1 class="facility-header__title">Sơ đồ Cơ sở Vật chất KTX</h1>
          <p class="facility-header__subtitle">
            Quản trị tập trung Tòa nhà, Tầng, Phòng và Giường theo tiêu chuẩn chuẩn hóa BA & RBAC.
          </p>
        </div>

        <div class="facility-header__actions">
          <BaseButton variant="primary" @click="showAddBuildingModal = true">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            Thêm tòa nhà
          </BaseButton>
          <BaseButton variant="secondary" :disabled="!selectedBuildingId" @click="showAddFloorModal = true">
            Thêm tầng
          </BaseButton>
          <BaseButton variant="ghost" :disabled="!selectedFloorId" @click="openCreateRoom">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            Thêm phòng mới
          </BaseButton>
        </div>
      </header>

      <!-- KPI Overview Cards -->
      <section class="facility-stats">
        <BaseCard class="stat-card">
          <div class="stat-card__icon stat-card__icon--blue">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            </svg>
          </div>
          <div>
            <div class="stat-card__label">TỔNG TÒA NHÀ</div>
            <div class="stat-card__value">{{ stats.totalBuildings }} tòa</div>
            <div class="stat-card__sub">{{ stats.totalFloors }} tầng hoạt động</div>
          </div>
        </BaseCard>

        <BaseCard class="stat-card">
          <div class="stat-card__icon stat-card__icon--green">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M3 9h18M9 21V9" />
            </svg>
          </div>
          <div>
            <div class="stat-card__label">TỔNG PHÒNG Ở</div>
            <div class="stat-card__value">{{ stats.totalRooms }} phòng</div>
            <div class="stat-card__sub text-success">{{ stats.availableRooms }} phòng còn chỗ</div>
          </div>
        </BaseCard>

        <BaseCard class="stat-card">
          <div class="stat-card__icon stat-card__icon--purple">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M2 4v16M2 8h18a2 2 0 0 1 2 2v10M2 17h20M6 8v9" />
            </svg>
          </div>
          <div>
            <div class="stat-card__label">GIƯỜNG KHẢ DỤNG</div>
            <div class="stat-card__value text-success">{{ stats.availableBeds }} giường</div>
            <div class="stat-card__sub">/ {{ stats.totalBeds }} tổng số giường</div>
          </div>
        </BaseCard>

        <BaseCard class="stat-card">
          <div class="stat-card__icon stat-card__icon--amber">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.21 15.89A10 10 0 1 1 8 2.83" />
              <path d="M22 12A10 10 0 0 0 12 2v10z" />
            </svg>
          </div>
          <div>
            <div class="stat-card__label">TỶ LỆ LẤP ĐẦY</div>
            <div class="stat-card__value">{{ stats.occupancyRate }}%</div>
            <div class="stat-card__sub">{{ stats.occupiedBeds }} sinh viên lưu trú</div>
          </div>
        </BaseCard>
      </section>

      <!-- Main Navigation Tabs -->
      <div class="facility-tabs">
        <BaseButton
          type="button"
          variant="tertiary"
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'tree' }"
          @click="activeTab = 'tree'"
        >
          Sơ đồ tòa và phòng
        </BaseButton>

        <BaseButton
          type="button"
          variant="tertiary"
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'rooms' }"
          @click="activeTab = 'rooms'"
        >
          Danh sách phòng
        </BaseButton>

        <BaseButton
          type="button"
          variant="tertiary"
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === 'policies' }"
          @click="activeTab = 'policies'"
        >
          Bảng giá phòng
        </BaseButton>
      </div>

      <!-- TAB 1: TREE BROWSER -->
      <section v-if="activeTab === 'tree'" class="tab-content">
        <!-- Building & Floor Selectors -->
        <div class="browser-layout">
          <!-- Left: Buildings & Floors Tree -->
          <div class="browser-sidebar">
            <h3 class="browser-sidebar__title">Tòa nhà KTX</h3>
            <div class="building-pills">
              <BaseButton
                v-for="b in buildings"
                :key="b.id"
                type="button"
                variant="tertiary"
                class="building-pill"
                :class="{ 'building-pill--active': selectedBuildingId === b.id }"
                @click="
                  selectedBuildingId = b.id;
                  loadFloorsForSelectedBuilding()
                "
              >
                <div class="building-pill__header">
                  <span class="building-pill__code">{{ b.code }}</span>
                  <BaseBadge :tone="getGenderBadge(b.genderType).color as any" size="sm">
                    {{ getGenderBadge(b.genderType).text }}
                  </BaseBadge>
                </div>
                <div class="building-pill__name">{{ b.name }}</div>
                <div class="building-pill__stats">
                  <span>{{ b.totalFloors }} tầng</span> &bull;
                  <span>{{ b.availableBeds || 0 }} giường trống</span>
                </div>
              </BaseButton>
            </div>

            <!-- Floor List -->
            <div class="floor-selector">
              <div class="floor-selector__head">
                <h4>Danh sách Tầng</h4>
              </div>
              <div class="floor-pills">
                <BaseButton
                  v-for="fl in floors"
                  :key="fl.id"
                  type="button"
                  variant="tertiary"
                  class="floor-pill"
                  :class="{ 'floor-pill--active': selectedFloorId === fl.id }"
                  @click="
                    selectedFloorId = fl.id;
                    loadRoomsForSelectedFloor()
                  "
                >
                  <span class="floor-pill__num">Tầng {{ fl.floorNumber }}</span>
                  <span class="floor-pill__badge">{{ fl.roomCount || 4 }} phòng</span>
                </BaseButton>
              </div>
            </div>
          </div>

          <!-- Right: Rooms Grid on Selected Floor -->
          <div class="browser-main">
            <div class="browser-main__header">
              <div>
                <h2>Phòng thuộc Tầng {{ floors.find((f) => f.id === selectedFloorId)?.floorNumber || 1 }}</h2>
                <p class="text-muted">
                  Bấm vào từng phòng để xem chi tiết ma trận giường, phân bổ và cập nhật trạng thái bảo trì.
                </p>
              </div>

              <BaseButton variant="ghost" size="sm" @click="showAddRoomModal = true">
                + Thêm phòng vào tầng này
              </BaseButton>
            </div>

            <!-- Rooms Grid -->
            <div class="rooms-grid">
              <BaseCard
                v-for="room in rooms"
                :key="room.id"
                class="room-card"
                :class="{ 'room-card--maintenance': room.status === 'MAINTENANCE' }"
              >
                <div class="room-card__header">
                  <div class="room-card__number">Phòng {{ room.roomNumber }}</div>
                  <BaseBadge :tone="getRoomStatusBadge(room.status).color as any">
                    {{ getRoomStatusBadge(room.status).text }}
                  </BaseBadge>
                </div>

                <div class="room-card__type">
                  {{ getRoomTypeLabel(room.roomType) }}
                </div>

                <div class="room-card__occupancy">
                  <div class="occupancy-labels">
                    <span>Chỗ ở:</span>
                    <strong>{{ room.currentOccupancy }} / {{ room.capacity }}</strong>
                  </div>
                  <div class="occupancy-bar">
                    <div
                      class="occupancy-fill"
                      :style="{ width: `${(room.currentOccupancy / room.capacity) * 100}%` }"
                    />
                  </div>
                </div>

                <div class="room-card__meta">
                  <div>
                    <span class="meta-label">Giới tính:</span>
                    <BaseBadge :tone="getGenderBadge(room.genderType).color as any" size="sm">
                      {{ getGenderBadge(room.genderType).text }}
                    </BaseBadge>
                  </div>
                  <div>
                    <span class="meta-label">Giá:</span>
                    <span class="meta-price">{{ room.pricePerMonth != null ? `${formatCurrency(room.pricePerMonth)}/tháng` : 'Chưa gán bảng giá' }}</span>
                  </div>
                </div>

                <div class="room-card__actions">
                  <BaseButton variant="primary" size="sm" class="w-full" @click="openRoomDetails(room)">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="2" y="4" width="20" height="16" rx="2" />
                      <line x1="8" y1="2" x2="8" y2="6" />
                      <line x1="16" y1="2" x2="16" y2="6" />
                    </svg>
                    Quản lý Giường & Chi tiết
                  </BaseButton>
                </div>
              </BaseCard>
            </div>
          </div>
        </div>
      </section>

      <!-- TAB 2: ALL ROOMS LIST & ADVANCED FILTER -->
      <section v-if="activeTab === 'rooms'" class="tab-content">
        <BaseCard class="filter-card">
          <div class="filter-grid">
            <BaseInput
              id="room-search-input"
              v-model="roomSearchQuery"
              placeholder="Tìm theo số phòng..."
              @input="loadAllRooms"
            />
            <BaseSelect
              id="room-filter-type"
              v-model="roomFilterType"
              :options="[
                { label: 'Tất cả loại phòng', value: '' },
                { label: 'Tiêu chuẩn 8 người', value: 'STANDARD_8' },
                { label: 'Tiêu chuẩn 6 người', value: 'STANDARD_6' },
                { label: 'Chất lượng cao 4 người', value: 'PREMIUM_4' },
              ]"
              @change="loadAllRooms"
            />
            <BaseSelect
              id="room-filter-status"
              v-model="roomFilterStatus"
              :options="[
                { label: 'Tất cả trạng thái', value: '' },
                { label: 'Còn chỗ (AVAILABLE)', value: 'AVAILABLE' },
                { label: 'Đã đầy (FULL)', value: 'FULL' },
                { label: 'Bảo trì (MAINTENANCE)', value: 'MAINTENANCE' },
              ]"
              @change="loadAllRooms"
            />
            <BaseSelect
              id="room-filter-gender"
              v-model="roomFilterGender"
              :options="[
                { label: 'Tất cả đối tượng', value: '' },
                { label: 'Nam', value: 'MALE' },
                { label: 'Nữ', value: 'FEMALE' },
              ]"
              @change="loadAllRooms"
            />
          </div>
        </BaseCard>

        <!-- Rooms Table -->
        <BaseCard class="table-card">
          <div class="facility-table" role="table" aria-label="Danh sách phòng">
            <div class="facility-table__row facility-table__head" role="row">
              <div role="columnheader">Số phòng</div>
              <div role="columnheader">Tòa & Tầng</div>
              <div role="columnheader">Loại phòng</div>
              <div role="columnheader">Sức chứa & Lấp đầy</div>
              <div role="columnheader">Giới tính</div>
              <div role="columnheader">Giá thuê / tháng</div>
              <div role="columnheader">Trạng thái</div>
              <div class="text-right" role="columnheader">Thao tác</div>
            </div>
            <div v-for="r in filteredAllRooms" :key="r.id" class="facility-table__row" role="row">
                <div role="cell">
                  <strong>{{ r.roomNumber }}</strong>
                </div>
                <div role="cell">
                  <span>Tòa {{ r.buildingCode }} &bull; Tầng {{ r.floorNumber }}</span>
                </div>
                <div role="cell">{{ getRoomTypeLabel(r.roomType) }}</div>
                <div role="cell">
                  <span class="occupancy-pill">
                    {{ r.currentOccupancy }} / {{ r.capacity }} chỗ
                  </span>
                </div>
                <div role="cell">
                  <BaseBadge :tone="getGenderBadge(r.genderType).color as any" size="sm">
                    {{ getGenderBadge(r.genderType).text }}
                  </BaseBadge>
                </div>
                <div role="cell">{{ r.pricePerMonth != null ? formatCurrency(r.pricePerMonth) : 'Chưa gán' }}</div>
                <div role="cell">
                  <BaseBadge :tone="getRoomStatusBadge(r.status).color as any">
                    {{ getRoomStatusBadge(r.status).text }}
                  </BaseBadge>
                </div>
                <div class="text-right" role="cell">
                  <div class="action-buttons">
                    <BaseButton variant="ghost" size="sm" @click="openRoomDetails(r)">
                      Chi tiết
                    </BaseButton>
                    <BaseButton
                      v-if="r.status !== 'MAINTENANCE'"
                      variant="ghost"
                      size="sm"
                      class="text-warning"
                      @click="toggleRoomStatus(r, 'MAINTENANCE')"
                    >
                      Bảo trì
                    </BaseButton>
                    <BaseButton
                      v-else
                      variant="ghost"
                      size="sm"
                      class="text-success"
                      @click="toggleRoomStatus(r, 'AVAILABLE')"
                    >
                      Kích hoạt
                    </BaseButton>
                  </div>
                </div>
            </div>
          </div>
        </BaseCard>
      </section>

      <!-- TAB 3: PRICE POLICIES -->
      <section v-if="activeTab === 'policies'" class="tab-content">
        <div class="section-topbar">
          <h2>Chính sách Bảng giá Niêm yết</h2>
          <BaseButton variant="primary" @click="openCreatePolicy">
            + Thêm chính sách giá
          </BaseButton>
        </div>

        <div class="policies-grid">
          <BaseCard v-for="p in pricePolicies" :key="p.id" class="policy-card">
            <div class="policy-card__header">
              <span class="policy-badge">{{ getRoomTypeLabel(p.roomType) }}</span>
              <BaseBadge :tone="p.isActive ? 'success' : 'neutral'">
                {{ p.isActive ? 'ĐANG ÁP DỤNG' : 'TẠM KHÓA' }}
              </BaseBadge>
            </div>
            <div class="policy-card__title">{{ p.name }}</div>
            <div class="policy-card__price">
              {{ formatCurrency(p.pricePerMonth) }}
              <span class="unit">/ sinh viên / tháng</span>
            </div>
            <div class="policy-card__dates">
              Hiệu lực từ: <strong>{{ p.effectiveFrom }}</strong>
              <span v-if="p.effectiveTo"> đến {{ p.effectiveTo }}</span>
            </div>
            <div class="action-buttons policy-card__actions">
              <BaseButton variant="secondary" size="sm" @click="openEditPolicy(p)">Sửa bảng giá</BaseButton>
              <BaseButton variant="ghost" size="sm" @click="togglePolicy(p)">
                {{ p.isActive ? 'Ngừng áp dụng' : 'Áp dụng lại' }}
              </BaseButton>
            </div>
          </BaseCard>
        </div>
      </section>

      <!-- MODAL: ROOM DETAIL & BED MATRIX -->
      <BaseModal
        v-model="showRoomDetailModal"
        title="Chi tiết Phòng & Ma trận Giường"
        max-width="50rem"
      >
        <div v-if="selectedRoom" class="room-detail-modal">
          <div class="room-summary-box">
            <div class="summary-col">
              <span class="label">Phòng:</span>
              <strong>{{ selectedRoom.roomNumber }}</strong>
            </div>
            <div class="summary-col">
              <span class="label">Tòa / Tầng:</span>
              <span>Tòa {{ selectedRoom.buildingCode }}, Tầng {{ selectedRoom.floorNumber }}</span>
            </div>
            <div class="summary-col">
              <span class="label">Sức chứa:</span>
              <span>{{ selectedRoom.currentOccupancy }} / {{ selectedRoom.capacity }} chỗ</span>
            </div>
            <div class="summary-col">
              <span class="label">Trạng thái:</span>
              <BaseBadge :tone="getRoomStatusBadge(selectedRoom.status).color as any">
                {{ getRoomStatusBadge(selectedRoom.status).text }}
              </BaseBadge>
            </div>
          </div>

          <div class="beds-matrix-head">
            <h3>Danh sách Giường trong phòng</h3>
            <span class="text-muted">Quy tắc BR-FAC-06: Giường bảo trì không thể được phân bổ cho sinh viên</span>
          </div>

          <div v-if="bedsLoading" class="loading-state">Đang tải ma trận giường...</div>

          <div v-else class="beds-grid">
            <div
              v-for="b in roomBeds"
              :key="b.id"
              class="bed-card"
              :class="{
                'bed-card--available': b.status === 'AVAILABLE',
                'bed-card--occupied': b.status === 'OCCUPIED',
                'bed-card--maintenance': b.status === 'MAINTENANCE',
              }"
            >
              <div class="bed-card__head">
                <span class="bed-num">Giường {{ b.bedNumber }}</span>
                <BaseBadge :tone="getBedStatusBadge(b.status).color as any" size="sm">
                  {{ getBedStatusBadge(b.status).text }}
                </BaseBadge>
              </div>

              <div class="bed-card__body">
                <div class="bed-icon">
                  <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M2 4v16M2 8h18a2 2 0 0 1 2 2v10M2 17h20M6 8v9" />
                  </svg>
                </div>
                <div class="bed-desc">
                  {{ b.status === 'OCCUPIED' ? 'Đã có sinh viên' : b.status === 'AVAILABLE' ? 'Sẵn sàng xếp' : 'Bảo trì / Hỏng' }}
                </div>
              </div>

              <div class="bed-card__foot">
                <BaseButton
                  v-if="b.status !== 'OCCUPIED'"
                  variant="ghost"
                  size="sm"
                  class="w-full"
                  @click="toggleBedStatus(b)"
                >
                  {{ b.status === 'AVAILABLE' ? 'Chuyển bảo trì' : 'Kích hoạt giường' }}
                </BaseButton>
                <span v-else class="occupied-note">Đang có hợp đồng</span>
              </div>
            </div>
          </div>
        </div>
      </BaseModal>

      <!-- MODAL: ADD BUILDING -->
      <BaseModal
        v-model="showAddBuildingModal"
        title="Thêm Tòa Nhà Ký Túc Xá Mới"
      >
        <div class="form-grid">
          <BaseField id="bld-code" v-model="buildingForm.code" label="Mã tòa nhà (VD: D, E)" required />
          <BaseField id="bld-name" v-model="buildingForm.name" label="Tên tòa nhà (VD: Tòa D - Cao học)" required />
          <div class="field-item">
            <BaseLabel for-id="bld-gender" required>Loại đối tượng</BaseLabel>
            <BaseSelect
              id="bld-gender"
              v-model="buildingForm.genderType"
              :options="[
                { label: 'Dành cho Nam (MALE)', value: 'MALE' },
                { label: 'Dành cho Nữ (FEMALE)', value: 'FEMALE' },
                { label: 'Hỗn hợp Nam & Nữ (MIXED)', value: 'MIXED' },
              ]"
            />
          </div>
          <BaseField
            id="bld-floors"
            v-model="buildingForm.totalFloors"
            type="number"
            label="Số tầng"
            required
          />
          <BaseField id="bld-address" v-model="buildingForm.address" label="Địa chỉ tòa nhà" />
          <BaseField id="bld-desc" v-model="buildingForm.description" label="Mô tả" />
        </div>

        <template #footer>
          <div class="action-buttons">
            <BaseButton variant="ghost" @click="showAddBuildingModal = false">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="modalLoading" @click="handleCreateBuilding">
              Lưu tòa nhà
            </BaseButton>
          </div>
        </template>
      </BaseModal>

      <!-- MODAL: ADD FLOOR -->
      <BaseModal v-model="showAddFloorModal" title="Thêm Tầng Cho Tòa Nhà">
        <div class="form-grid">
          <BaseField id="floor-number" v-model="floorForm.floorNumber" type="number" label="Số tầng" required />
          <BaseField id="floor-name" v-model="floorForm.name" label="Tên tầng" placeholder="Ví dụ: Tầng 1" />
        </div>
        <template #footer>
          <div class="action-buttons">
            <BaseButton variant="ghost" @click="showAddFloorModal = false">Hủy</BaseButton>
            <BaseButton :loading="modalLoading" @click="handleCreateFloor">Tạo tầng</BaseButton>
          </div>
        </template>
      </BaseModal>

      <!-- MODAL: ADD ROOM -->
      <BaseModal
        v-model="showAddRoomModal"
        title="Thêm Phòng Mới"
      >
        <div class="form-grid">
          <BaseField id="rm-no" v-model="roomForm.roomNumber" label="Số phòng (VD: 201, 305)" required />
          <div class="field-item">
            <BaseLabel for-id="rm-type" required>Loại phòng</BaseLabel>
            <BaseSelect
              id="rm-type"
              v-model="roomForm.roomType"
              :options="[
                { label: 'Tiêu chuẩn 8 người (STANDARD_8)', value: 'STANDARD_8' },
                { label: 'Tiêu chuẩn 6 người (STANDARD_6)', value: 'STANDARD_6' },
                { label: 'Chất lượng cao 4 người (PREMIUM_4)', value: 'PREMIUM_4' },
              ]"
              @update:model-value="roomForm.pricePolicyId = ''"
            />
          </div>
          <BaseField
            id="rm-cap"
            v-model="roomForm.capacity"
            type="number"
            label="Sức chứa tối đa (người)"
            required
          />
          <BaseField
            id="rm-area"
            v-model="roomForm.areaSqm"
            type="number"
            label="Diện tích (m²)"
          />
          <div class="field-item">
            <BaseLabel for-id="rm-price-policy">Bảng giá phòng</BaseLabel>
            <BaseSelect id="rm-price-policy" v-model="roomForm.pricePolicyId" :options="roomPricePolicyOptions" />
          </div>
          <BaseField id="rm-image-url" v-model="roomForm.imageUrl" label="URL ảnh phòng (không bắt buộc)" placeholder="https://..." />
          <BaseField id="rm-desc" v-model="roomForm.description" label="Mô tả tiện nghi phòng" />
        </div>

        <template #footer>
          <div class="action-buttons">
            <BaseButton variant="ghost" @click="showAddRoomModal = false">Hủy</BaseButton>
            <BaseButton variant="primary" :loading="modalLoading" @click="handleCreateRoom">
              Tạo phòng
            </BaseButton>
          </div>
        </template>
      </BaseModal>

      <!-- MODAL: CREATE / EDIT PRICE POLICY -->
      <BaseModal
        v-model="showAddPolicyModal"
        :title="editingPolicyId ? 'Sửa Chính Sách Giá Phòng' : 'Thêm Chính Sách Giá Phòng'"
      >
        <div class="form-grid">
          <BaseField id="policy-name" v-model="policyForm.name" label="Tên chính sách" required />
          <div class="field-item">
            <BaseLabel for-id="policy-room-type" required>Loại phòng</BaseLabel>
            <BaseSelect
              id="policy-room-type"
              v-model="policyForm.roomType"
              :disabled="Boolean(editingPolicyId)"
              :options="[
                { label: 'Phòng tiêu chuẩn 8 người', value: 'STANDARD_8' },
                { label: 'Phòng tiêu chuẩn 6 người', value: 'STANDARD_6' },
                { label: 'Phòng chất lượng cao 4 người', value: 'PREMIUM_4' },
              ]"
            />
          </div>
          <BaseField id="policy-price" v-model="policyForm.pricePerMonth" type="number" label="Giá mỗi sinh viên / tháng" required />
          <BaseField v-if="!editingPolicyId" id="policy-from" v-model="policyForm.effectiveFrom" type="date" label="Hiệu lực từ" required />
          <div v-else class="field-item">
            <BaseLabel for-id="policy-effective-from">Hiệu lực từ</BaseLabel>
            <p id="policy-effective-from" class="policy-static-value">{{ policyForm.effectiveFrom }}</p>
          </div>
          <BaseField id="policy-to" v-model="policyForm.effectiveTo" type="date" label="Hiệu lực đến (không bắt buộc)" />
          <BaseField id="policy-description" v-model="policyForm.description" label="Mô tả" />
          <BaseCheckbox id="policy-active" v-model="policyForm.isActive" label="Đang áp dụng" />
        </div>
        <template #footer>
          <div class="action-buttons">
            <BaseButton variant="ghost" @click="showAddPolicyModal = false">Hủy</BaseButton>
            <BaseButton :loading="modalLoading" @click="handleSavePolicy">
              {{ editingPolicyId ? 'Lưu thay đổi' : 'Tạo chính sách' }}
            </BaseButton>
          </div>
        </template>
      </BaseModal>
    </div>
  </AppLayout>
</template>

<style scoped>
.facility-page {
  padding: 24px 32px 48px;
  max-width: 1400px;
  margin: 0 auto;
}

.facility-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.facility-header__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--color-primary-soft, rgba(59, 130, 246, 0.12));
  color: var(--color-primary, #3b82f6);
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
}

.facility-header__title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
}

.facility-header__subtitle {
  color: var(--color-text-muted, #94a3b8);
  font-size: 14px;
  margin: 0;
}

.facility-header__actions {
  display: flex;
  gap: 12px;
}

/* Stats */
.facility-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card__icon--blue {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}

.stat-card__icon--green {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}

.stat-card__icon--purple {
  background: rgba(168, 85, 247, 0.12);
  color: #a855f7;
}

.stat-card__icon--amber {
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
}

.stat-card__label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--color-text-muted, #94a3b8);
  margin-bottom: 4px;
}

.stat-card__value {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.stat-card__sub {
  font-size: 12px;
  color: var(--color-text-muted, #94a3b8);
}

/* Tabs */
.facility-tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  border-bottom: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  margin-bottom: 24px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  color: var(--color-text-muted, #94a3b8);
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
}

.tab-btn :deep(.base-button__content) {
  width: 100%;
}

.tab-btn:hover {
  color: var(--color-text-primary, #ffffff);
}

.tab-btn--active {
  color: var(--color-primary, #3b82f6);
  border-bottom-color: var(--color-primary, #3b82f6);
}

/* Tree Browser */
.browser-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
}

.browser-sidebar {
  background: var(--color-surface, #1e293b);
  border: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  border-radius: 12px;
  padding: 20px;
}

.browser-sidebar__title {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 16px 0;
  color: var(--color-text-muted, #94a3b8);
}

.building-pills {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
}

.building-pill {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  border-radius: 8px;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
}

.building-pill :deep(.base-button__content) {
  display: block;
  min-width: 0;
  text-align: left;
  width: 100%;
}

.building-pill:hover {
  border-color: var(--color-primary, #3b82f6);
  transform: translateX(2px);
}

.building-pill--active {
  border-color: var(--color-primary, #3b82f6);
  background: rgba(59, 130, 246, 0.1);
}

.building-pill__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.building-pill__code {
  font-weight: 800;
  font-size: 16px;
}

.building-pill__name {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.building-pill__stats {
  font-size: 12px;
  color: var(--color-text-muted, #94a3b8);
  display: flex;
  flex-wrap: wrap;
  gap: 2px 4px;
}

.floor-selector {
  border-top: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  padding-top: 16px;
}

.floor-selector__head h4 {
  font-size: 13px;
  margin: 0 0 12px 0;
}

.floor-pills {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.floor-pill {
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
  transition: all 0.2s ease;
  width: 100%;
}

.floor-pill :deep(.base-button__content) {
  align-items: flex-start;
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
}

.floor-pill:hover {
  border-color: var(--color-primary, #3b82f6);
}

.floor-pill--active {
  border-color: var(--color-primary, #3b82f6);
  background: rgba(59, 130, 246, 0.12);
}

.floor-pill__num {
  font-size: 12px;
  font-weight: 700;
}

.floor-pill__badge {
  font-size: 10px;
  color: var(--color-text-muted, #94a3b8);
}

/* Browser Main & Rooms Grid */
.browser-main__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.browser-main__header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px 0;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.room-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.room-card--maintenance {
  border-color: rgba(245, 158, 11, 0.3);
}

.room-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-card__number {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.room-card__type {
  font-size: 13px;
  color: var(--color-text-muted, #94a3b8);
}

.room-card__occupancy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.occupancy-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.occupancy-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.occupancy-fill {
  height: 100%;
  background: var(--color-primary, #3b82f6);
  border-radius: 999px;
}

.room-card__meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  padding: 8px 0;
  border-top: 1px dashed var(--color-border, rgba(255, 255, 255, 0.08));
}

.meta-label {
  color: var(--color-text-muted, #94a3b8);
  margin-right: 4px;
}

.meta-price {
  font-weight: 700;
  color: #10b981;
}

/* Tab 2: Filter & Table */
.filter-card {
  padding: 16px;
  margin-bottom: 20px;
}

.filter-grid {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1.5fr 1.5fr;
  gap: 12px;
}

.table-card {
  padding: 0;
  overflow-x: auto;
}

.facility-table {
  width: 100%;
  text-align: left;
  font-size: 13px;
  min-width: 72rem;
}

.facility-table__row {
  align-items: center;
  border-bottom: 1px solid var(--color-border, rgba(255, 255, 255, 0.06));
  display: grid;
  grid-template-columns: 0.8fr 1.1fr 1.25fr 1.15fr 0.8fr 1.1fr 1fr 1.4fr;
}

.facility-table__row > div {
  padding: 14px 16px;
}

.facility-table__head {
  background: rgba(255, 255, 255, 0.02);
  font-weight: 600;
  color: var(--color-text-muted, #94a3b8);
  border-bottom: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
}

.occupancy-pill {
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
}

.action-buttons {
  display: inline-flex;
  gap: 6px;
}

/* Tab 3: Policies */
.section-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.policies-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.policy-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.policy-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.policy-badge {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-primary, #3b82f6);
}

.policy-card__title {
  font-size: 18px;
  font-weight: 700;
}

.policy-card__price {
  font-size: 26px;
  font-weight: 800;
  color: #10b981;
}

.policy-card__price .unit {
  font-size: 12px;
  color: var(--color-text-muted, #94a3b8);
  font-weight: 400;
}

.policy-card__dates {
  font-size: 12px;
  color: var(--color-text-muted, #94a3b8);
  border-top: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  padding-top: 12px;
}

.policy-card__actions {
  margin-top: auto;
  padding-top: 4px;
  flex-wrap: wrap;
}

.policy-static-value {
  min-height: 42px;
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0 12px;
  border: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  border-radius: 8px;
  color: var(--color-text-muted, #94a3b8);
}

/* Modal: Room detail & beds */
.room-summary-box {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  background: rgba(255, 255, 255, 0.03);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.summary-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.summary-col .label {
  font-size: 11px;
  color: var(--color-text-muted, #94a3b8);
}

.beds-matrix-head {
  margin-bottom: 16px;
}

.beds-matrix-head h3 {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 4px 0;
}

.beds-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(10.5rem, 1fr));
  gap: 12px;
}

.bed-card {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--color-border, rgba(255, 255, 255, 0.08));
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.bed-card--available {
  border-color: rgba(16, 185, 129, 0.4);
}

.bed-card--occupied {
  border-color: rgba(59, 130, 246, 0.4);
}

.bed-card--maintenance {
  border-color: rgba(245, 158, 11, 0.4);
}

.bed-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.bed-num {
  flex: 1 1 auto;
  font-weight: 800;
  font-size: 13px;
  min-width: 0;
  white-space: nowrap;
}

.bed-card__body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bed-desc {
  font-size: 11px;
  color: var(--color-text-muted, #94a3b8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bed-card__foot {
  margin-top: auto;
  white-space: nowrap;
}

.occupied-note {
  font-size: 11px;
  color: #3b82f6;
  font-weight: 600;
  text-align: center;
  display: block;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.text-success {
  color: #10b981;
}

.text-warning {
  color: #f59e0b;
}

.text-muted {
  color: var(--color-text-muted, #94a3b8);
  font-size: 12px;
}

.w-full {
  width: 100%;
}

.text-right {
  text-align: right;
}

@media (max-width: 1100px) {
  .browser-layout {
    grid-template-columns: 15rem minmax(0, 1fr);
    gap: 16px;
  }

  .facility-tabs {
    gap: 4px;
  }

  .tab-btn {
    font-size: 12px;
    padding-inline: 10px;
  }
}

@media (max-width: 760px) {
  .facility-tabs,
  .browser-layout,
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .tab-btn {
    border: 1px solid var(--color-border);
    border-radius: var(--radius-sm);
  }

  .tab-btn--active {
    border-color: var(--color-primary);
  }

  .floor-pills {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
