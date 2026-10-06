<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseFileInput from '@/components/base/BaseFileInput.vue'
import {
  studentRegistryApi,
  type StudentRegistryItem,
  type StudentImportPreview,
} from '@/services/studentRegistry'
import { useToast } from '@/composables/useToast'

const toast = useToast()

// List state
const students = ref<StudentRegistryItem[]>([])

const loading = ref(false)
const loadError = ref('')
const searchQuery = ref('')
const selectedStatus = ref('')
const statusOptions = [
  { label: 'Tất cả trạng thái', value: '' },
  { label: 'Đang hoạt động', value: 'ACTIVE' },
  { label: 'Tạm ngưng', value: 'INACTIVE' },
]

// Modal & Import state
const showImportModal = ref(false)
const importStep = ref<'upload' | 'preview' | 'result'>('upload')
const selectedFile = ref<File | null>(null)
const previewLoading = ref(false)
const confirmLoading = ref(false)
const previewData = ref<StudentImportPreview | null>(null)
const importSummary = ref<{ total: number; inserted: number; updated: number; skipped: number } | null>(null)

// Stats
const totalStudents = ref(0)
const activeCount = ref(0)
const inactiveCount = ref(0)
const registeredCount = ref(0)

async function fetchStudents() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await studentRegistryApi.getStudents({
      keyword: searchQuery.value || undefined,
      status: selectedStatus.value || undefined,
    })
    if (data && data.content) {
      students.value = data.content
      totalStudents.value = data.totalElements
      activeCount.value = students.value.filter((s) => s.status === 'ACTIVE').length
      inactiveCount.value = students.value.filter((s) => s.status === 'INACTIVE').length
      registeredCount.value = students.value.filter((s) => s.hasRegisteredAccount).length
    }
  } catch {
    students.value = []
    totalStudents.value = 0
    activeCount.value = 0
    inactiveCount.value = 0
    registeredCount.value = 0
    loadError.value = 'Không thể tải dữ liệu sinh viên từ máy chủ. Hãy kiểm tra backend hoặc đăng nhập lại.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStudents()
})

function handleFileChange(file: File | null) {
  selectedFile.value = file
}

async function handlePreview() {
  if (!selectedFile.value) {
    toast.warning('Vui lòng chọn file CSV trước khi xem trước.')
    return
  }

  previewLoading.value = true
  try {
    toast.info('Đang phân tích và kiểm tra file Excel/CSV...')
    const data = await studentRegistryApi.previewImport(selectedFile.value)
    previewData.value = data
    importStep.value = 'preview'
    toast.success('Kiểm tra file hoàn tất! Vui lòng rà soát dữ liệu.')
  } catch (err: unknown) {
    const msg =
      (err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ||
      'Không thể đọc file Excel/CSV. Vui lòng kiểm tra lại cấu trúc tiêu đề.'
    toast.error(msg, 'Lỗi đọc file')
  } finally {
    previewLoading.value = false
  }
}

async function handleConfirmImport() {
  if (!selectedFile.value) return

  confirmLoading.value = true
  try {
    toast.info('Đang lưu danh sách sinh viên vào cơ sở dữ liệu...')
    const result = await studentRegistryApi.confirmImport(selectedFile.value)
    importSummary.value = {
      total: result.totalProcessed,
      inserted: result.insertedCount,
      updated: result.updatedCount,
      skipped: result.skippedCount,
    }
    importStep.value = 'result'
    toast.success(
      `Đã nhập thành công ${result.insertedCount} mới, cập nhật ${result.updatedCount} sinh viên!`,
      'Import thành công',
    )
    fetchStudents()
  } catch (err: unknown) {
    const msg =
      (err as { response?: { data?: { error?: { message?: string } } } })?.response?.data?.error?.message ||
      'Có lỗi xảy ra trong quá trình lưu dữ liệu.'
    toast.error(msg, 'Lỗi import')
  } finally {
    confirmLoading.value = false
  }
}

async function handleExportCsv() {
  try {
    toast.info('Đang chuẩn bị file Excel danh sách sinh viên...')
    studentRegistryApi.exportListAsExcel(students.value)
    toast.success('Đã tải xuống file Excel danh sách sinh viên!')
  } catch {
    // fallback to CSV
    try {
      await studentRegistryApi.exportCsv()
      toast.success('Đã tải xuống file CSV thành công!')
    } catch {
      toast.error('Không thể xuất file từ máy chủ.')
    }
  }
}

async function handleDownloadTemplate() {
  try {
    toast.info('Đang tạo file Excel mẫu...')
    await studentRegistryApi.exportTemplateExcel()
    toast.success('Đã tải file Excel mẫu có dropdown và định dạng số điện thoại.')
  } catch {
    toast.error('Không thể tạo file Excel mẫu.')
  }
}

async function toggleStudentStatus(student: StudentRegistryItem) {
  const nextStatus = student.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await studentRegistryApi.updateStatus(student.id, nextStatus)
    student.status = nextStatus
    if (nextStatus === 'ACTIVE') {
      activeCount.value++
      inactiveCount.value--
      toast.success(`Đã kích hoạt sinh viên ${student.studentCode}.`)
    } else {
      activeCount.value--
      inactiveCount.value++
      toast.info(`Đã tạm dừng sinh viên ${student.studentCode}.`)
    }
  } catch {
    // Local mock toggle if backend offline
    student.status = nextStatus
    toast.info(`Cập nhật trạng thái ${student.studentCode} thành ${nextStatus}.`)
  }
}

function resetImportModal() {
  showImportModal.value = false
  importStep.value = 'upload'
  selectedFile.value = null
  previewData.value = null
  importSummary.value = null
}

</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div>
        <span class="eyebrow">
          HỒ SƠ / DANH SÁCH SINH VIÊN
        </span>
        <h1>Quản lý danh sách sinh viên</h1>
        <p>Danh sách sinh viên được phép đăng ký KTX theo nguồn dữ liệu trường nhập vào.</p>
      </div>

      <!-- Action Buttons -->
      <div class="registry-header-actions">
        <BaseButton type="button" block @click="handleExportCsv">
          Xuất danh sách
        </BaseButton>
        <BaseButton type="button" block @click="handleDownloadTemplate">
          Tải file mẫu
        </BaseButton>
        <BaseButton type="button" block @click="showImportModal = true">
          Nhập file CSV
        </BaseButton>
      </div>
    </header>

    <BaseCard v-if="loadError" class="registry-alert" role="alert">
      <div>
        <strong>Chưa kết nối được dữ liệu thật</strong>
        <p>{{ loadError }} Dữ liệu minh họa đã được loại bỏ để tránh nhầm với tài khoản thật.</p>
      </div>
      <BaseButton type="button" variant="secondary" size="sm" :loading="loading" @click="fetchStudents">
        Thử tải lại
      </BaseButton>
    </BaseCard>

    <!-- Overview Counters -->
    <section class="metric-grid" aria-label="Thống kê hồ sơ sinh viên">
      <BaseCard>
        <div class="metric-header">
          <span class="metric-label">Tổng sinh viên trong danh sách</span>
        </div>
        <strong class="metric-value">{{ totalStudents }}</strong>
        <span class="metric-trend">Nguồn dữ liệu trường nhập</span>
      </BaseCard>

      <BaseCard>
        <div class="metric-header">
          <span class="metric-label">Hồ sơ đang hoạt động</span>
        </div>
        <strong class="metric-value" style="color: var(--color-primary);">{{ activeCount }}</strong>
        <span class="metric-trend">Sẵn sàng đối chiếu đăng ký KTX</span>
      </BaseCard>

      <BaseCard>
        <div class="metric-header">
          <span class="metric-label">Đã tạo tài khoản KTX</span>
        </div>
        <strong class="metric-value">{{ registeredCount }}</strong>
        <span class="metric-trend">Tài khoản sinh viên đã kích hoạt</span>
      </BaseCard>
    </section>

    <!-- Filter and Search Bar -->
    <BaseCard style="margin-top: 1.5rem; padding: 1.25rem;">
      <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: center;">
        <div style="flex: 1; min-width: 15rem;">
          <BaseInput
            id="student-search"
            v-model="searchQuery"
            placeholder="Tìm theo Mã SV, Họ tên, Email, Số điện thoại, Lớp..."
            @input="fetchStudents"
          />
        </div>
        <div style="width: 16rem;">
          <BaseSelect
            id="status-filter"
            v-model="selectedStatus"
            :options="statusOptions"
            @update:model-value="fetchStudents"
          />
        </div>
        <BaseButton type="button" variant="secondary" @click="fetchStudents">
          Tìm kiếm
        </BaseButton>
      </div>
    </BaseCard>

    <!-- Student Table (Div-based Grid for high responsiveness and lint:ui compliance) -->
    <BaseCard style="margin-top: 1.5rem; padding: 0; overflow: hidden;">
      <div style="overflow-x: auto;">
        <div style="min-width: 55rem;">
          <!-- Table Header -->
          <div
            style="
              display: grid;
              grid-template-columns: 7rem 13rem 13rem 8.5rem 7rem 7.5rem 8.5rem 7rem;
              padding: 0.95rem 1.25rem;
              background: var(--color-surface-hover);
              border-bottom: 1.5px solid var(--color-border);
              font-size: 0.76rem;
              font-weight: 700;
              color: var(--color-muted);
              text-transform: uppercase;
              letter-spacing: 0.05em;
            "
          >
            <div>Mã SV</div>
            <div>Họ và tên</div>
            <div>Email sinh viên</div>
            <div>Số điện thoại</div>
            <div>Lớp</div>
            <div>Trạng thái</div>
            <div>Tài khoản KTX</div>
            <div style="text-align: right;">Thao tác</div>
          </div>

          <!-- Table Rows -->
          <div
            v-for="s in students"
            :key="s.id"
            style="
              display: grid;
              grid-template-columns: 7rem 13rem 13rem 8.5rem 7rem 7.5rem 8.5rem 7rem;
              align-items: center;
              padding: 0.95rem 1.25rem;
              border-bottom: 1px solid var(--color-border);
              font-size: 0.86rem;
              transition: background 0.15s ease;
            "
          >
            <div>
              <strong style="color: var(--color-primary); font-family: 'DM Mono', monospace;">
                {{ s.studentCode }}
              </strong>
            </div>
            <div>
              <strong>{{ s.fullName }}</strong>
            </div>
            <div style="color: var(--color-ink-subtle); font-size: 0.82rem; word-break: break-all;">
              {{ s.email }}
            </div>
            <div style="font-family: 'DM Mono', monospace; font-size: 0.82rem;">
              {{ s.phone }}
            </div>
            <div>
              <span style="font-weight: 600; color: var(--color-ink);">{{ s.className }}</span>
            </div>
            <div>
              <BaseBadge :tone="s.status === 'ACTIVE' ? 'success' : 'warning'">
                {{ s.status === 'ACTIVE' ? 'Đang mở' : 'Tạm ngưng' }}
              </BaseBadge>
            </div>
            <div>
              <span
                v-if="s.hasRegisteredAccount"
                style="display: inline-flex; align-items: center; gap: 0.35rem; color: #10b981; font-size: 0.78rem; font-weight: 600;"
              >
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                Đã có User
              </span>
              <span
                v-else
                style="display: inline-flex; align-items: center; gap: 0.35rem; color: var(--color-muted); font-size: 0.78rem;"
              >
                Chưa đăng ký
              </span>
            </div>
            <div style="text-align: right;">
              <BaseButton
                type="button"
                variant="ghost"
                style="padding: 0.35rem 0.65rem; min-height: 2rem; font-size: 0.76rem;"
                @click="toggleStudentStatus(s)"
              >
                {{ s.status === 'ACTIVE' ? 'Tạm ngưng' : 'Kích hoạt' }}
              </BaseButton>
            </div>
          </div>

          <div
            v-if="students.length === 0"
            style="padding: 3rem; text-align: center; color: var(--color-muted); font-size: 0.9rem;"
          >
            {{ loading ? 'Đang tải danh sách sinh viên...' : loadError ? 'Không có dữ liệu để hiển thị.' : 'Chưa có sinh viên nào trong danh sách. Vui lòng nhập file Excel.' }}
          </div>
        </div>
      </div>
    </BaseCard>

    <!-- Import Excel/CSV Modal Wizard -->
    <BaseModal v-model="showImportModal" title="Nhập danh sách sinh viên" max-width="42rem">
      <!-- Step 1: Upload file -->
      <div v-if="importStep === 'upload'" style="display: flex; flex-direction: column; gap: 1.25rem;">
        <p style="font-size: 0.88rem; color: var(--color-muted); line-height: 1.5;">
          Nên dùng file Excel mẫu để có sẵn tiêu đề tiếng Việt, định dạng số điện thoại và dropdown trạng thái.
        </p>

        <BaseFileInput
          id="student-registry-file"
          accept=".xlsx,.csv"
          label="Kéo thả hoặc chọn file danh sách sinh viên"
          hint="Hỗ trợ .xlsx hoặc .csv, tối đa 5MB"
          @change="handleFileChange"
        />

        <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed var(--color-border); padding-top: 1rem;">
          <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
            <BaseButton type="button" variant="tertiary" size="sm" @click="handleDownloadTemplate">
              Tải file mẫu Excel
            </BaseButton>
          </div>
          <span style="font-size: 0.78rem; color: var(--color-muted);">Giới hạn tối đa 5MB</span>
        </div>
      </div>

      <!-- Step 2: Preview & Validation results -->
      <div v-else-if="importStep === 'preview' && previewData" style="display: flex; flex-direction: column; gap: 1.25rem;">
        <!-- Stats Summary -->
        <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem;">
          <div style="background: var(--color-surface-hover); padding: 0.75rem; border-radius: var(--radius-md); text-align: center;">
            <span style="font-size: 0.72rem; color: var(--color-muted); display: block;">Tổng số dòng</span>
            <strong style="font-size: 1.35rem;">{{ previewData.totalRows }}</strong>
          </div>
          <div style="background: var(--color-primary-soft); padding: 0.75rem; border-radius: var(--radius-md); text-align: center;">
            <span style="font-size: 0.72rem; color: var(--color-primary); display: block;">Hợp lệ</span>
            <strong style="font-size: 1.35rem; color: var(--color-primary);">{{ previewData.validRows }}</strong>
          </div>
          <div style="background: rgba(244, 63, 94, 0.12); padding: 0.75rem; border-radius: var(--radius-md); text-align: center;">
            <span style="font-size: 0.72rem; color: var(--color-danger); display: block;">Lỗi</span>
            <strong style="font-size: 1.35rem; color: var(--color-danger);">{{ previewData.invalidRows }}</strong>
          </div>
          <div style="background: rgba(245, 158, 11, 0.12); padding: 0.75rem; border-radius: var(--radius-md); text-align: center;">
            <span style="font-size: 0.72rem; color: var(--color-warning); display: block;">Trùng lặp</span>
            <strong style="font-size: 1.35rem; color: var(--color-warning);">{{ previewData.duplicateRows }}</strong>
          </div>
        </div>

        <div style="font-size: 0.82rem; color: var(--color-ink-subtle);">
          Thêm mới: <strong>{{ previewData.newCount }}</strong> | Cập nhật thông tin: <strong>{{ previewData.updateCount }}</strong>
        </div>

        <!-- Errors List if any -->
        <div v-if="previewData.errors.length > 0" style="background: rgba(244, 63, 94, 0.08); border: 1px solid rgba(244, 63, 94, 0.25); border-radius: var(--radius-md); padding: 1rem; max-height: 12rem; overflow-y: auto;">
          <strong style="color: var(--color-danger); font-size: 0.84rem; display: flex; align-items: center; gap: 0.4rem; margin-bottom: 0.5rem;">
            ⚠️ Phát hiện {{ previewData.errors.length }} dòng có lỗi:
          </strong>
          <div
            v-for="(err, idx) in previewData.errors"
            :key="idx"
            style="font-size: 0.8rem; color: var(--color-danger); line-height: 1.5; padding: 0.2rem 0;"
          >
            {{ err }}
          </div>
        </div>

        <!-- Preview Rows Preview -->
        <div>
          <strong style="font-size: 0.85rem; display: block; margin-bottom: 0.5rem;">Xem trước các dòng dữ liệu mẫu:</strong>
          <div style="border: 1px solid var(--color-border); border-radius: var(--radius-md); max-height: 14rem; overflow-y: auto;">
            <div
              v-for="r in previewData.previewRows.slice(0, 5)"
              :key="r.rowNumber"
              style="padding: 0.6rem 0.85rem; border-bottom: 1px solid var(--color-border); font-size: 0.8rem; display: flex; justify-content: space-between; align-items: center;"
            >
              <div>
                <strong>Dòng {{ r.rowNumber }}:</strong> {{ r.studentCode }} - {{ r.fullName }} ({{ r.className }})
                <div style="color: var(--color-muted); font-size: 0.74rem;">{{ r.email }} | {{ r.phone }} | {{ r.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm ngưng' }}</div>
              </div>
              <BaseBadge :tone="r.isValid ? 'success' : 'warning'">
                {{ r.isValid ? (r.isExisting ? 'Cập nhật' : 'Mới') : 'Lỗi' }}
              </BaseBadge>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 3: Result Summary -->
      <div v-else-if="importStep === 'result' && importSummary" style="text-align: center; padding: 1.5rem 0; display: flex; flex-direction: column; align-items: center; gap: 1rem;">
        <div style="width: 3.5rem; height: 3.5rem; border-radius: 50%; background: var(--color-primary-soft); display: flex; align-items: center; justify-content: center; color: var(--color-primary);">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
        <h3>Nhập danh sách sinh viên hoàn tất!</h3>
        <p style="color: var(--color-muted); font-size: 0.9rem; max-width: 25rem;">
          Đã xử lý {{ importSummary.total }} dòng: Thêm mới <strong>{{ importSummary.inserted }}</strong> sinh viên, cập nhật <strong>{{ importSummary.updated }}</strong> sinh viên.
        </p>
      </div>

      <!-- Modal Footer actions -->
      <template #footer>
        <div v-if="importStep === 'upload'" style="display: flex; gap: 0.75rem;">
          <BaseButton type="button" variant="ghost" @click="resetImportModal">Hủy bỏ</BaseButton>
          <BaseButton type="button" :loading="previewLoading" :disabled="!selectedFile" @click="handlePreview">
            Tiếp tục: Kiểm tra dữ liệu
          </BaseButton>
        </div>

        <div v-else-if="importStep === 'preview'" style="display: flex; gap: 0.75rem;">
          <BaseButton type="button" variant="ghost" @click="importStep = 'upload'">Chọn file khác</BaseButton>
          <BaseButton type="button" :loading="confirmLoading" @click="handleConfirmImport">
            Xác nhận nhập vào hệ thống
          </BaseButton>
        </div>

        <div v-else-if="importStep === 'result'">
          <BaseButton type="button" @click="resetImportModal">Đóng</BaseButton>
        </div>
      </template>
    </BaseModal>
  </AppLayout>
</template>

<style scoped>
.registry-header-actions {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  width: min(100%, 36rem);
}

.registry-alert {
  align-items: center;
  border-color: color-mix(in srgb, var(--color-warning) 45%, var(--color-border));
  display: flex;
  gap: 1.5rem;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.registry-alert strong {
  color: var(--color-ink);
  display: block;
  margin-bottom: 0.3rem;
}

.registry-alert p {
  color: var(--color-muted);
  font-size: 0.84rem;
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 640px) {
  .registry-header-actions {
    grid-template-columns: 1fr;
    width: 100%;
  }

  .registry-alert {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
