import * as XLSX from 'xlsx'
import type { Row, Worksheet } from 'exceljs'
import { api } from './api'

export interface StudentRegistryItem {
  id: string
  studentCode: string
  fullName: string
  email: string
  phone: string
  className: string
  status: 'ACTIVE' | 'INACTIVE'
  hasRegisteredAccount: boolean
  createdAt: string
  updatedAt: string
}

export interface StudentRowPreview {
  rowNumber: number
  studentCode: string
  email: string
  phone: string
  fullName: string
  className: string
  status: 'ACTIVE' | 'INACTIVE'
  isValid: boolean
  isDuplicate: boolean
  isExisting: boolean
  error?: string
}

export interface StudentImportPreview {
  totalRows: number
  validRows: number
  invalidRows: number
  duplicateRows: number
  newCount: number
  updateCount: number
  errors: string[]
  previewRows: StudentRowPreview[]
}

export interface StudentImportResult {
  totalProcessed: number
  insertedCount: number
  updatedCount: number
  skippedCount: number
  messages: string[]
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const studentRegistryApi = {
  async getStudents(params: {
    keyword?: string
    status?: string
    hasAccount?: boolean
    page?: number
    size?: number
  }) {
    const { data } = await api.get<{ data: PageResult<StudentRegistryItem> }>('/student-registry', {
      params,
    })
    return data.data
  },

  async getStudentById(id: string) {
    const { data } = await api.get<{ data: StudentRegistryItem }>(`/student-registry/${id}`)
    return data.data
  },

  async updateStatus(id: string, status: 'ACTIVE' | 'INACTIVE') {
    const { data } = await api.patch<{ data: StudentRegistryItem }>(`/student-registry/${id}/status`, { status })
    return data.data
  },

  async previewImport(file: File) {
    const uploadFile = await normalizeStudentImportFile(file)
    const formData = new FormData()
    formData.append('file', uploadFile)
    const { data } = await api.post<{ data: StudentImportPreview }>(
      '/student-registry/import/preview',
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      },
    )
    return data.data
  },

  async confirmImport(file: File) {
    const uploadFile = await normalizeStudentImportFile(file)
    const formData = new FormData()
    formData.append('file', uploadFile)
    const { data } = await api.post<{ data: StudentImportResult }>(
      '/student-registry/import',
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      },
    )
    return data.data
  },

  /**
   * Xuất danh sách sinh viên từ API (CSV)
   */
  async exportCsv() {
    const response = await api.get('/student-registry/export', {
      responseType: 'blob',
    })
    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `danh_sach_sinh_vien_${new Date().toISOString().slice(0, 10)}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  },

  /**
   * Xuất danh sách sinh viên dạng Excel với header tiếng Việt
   */
  exportListAsExcel(students: StudentRegistryItem[]) {
    const rows = students.map((s) => ({
      'Mã sinh viên': s.studentCode,
      'Họ và tên': s.fullName,
      'Email sinh viên': s.email,
      'Số điện thoại': s.phone,
      'Lớp / Khóa': s.className,
      'Trạng thái': s.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm ngưng',
      'Đã có tài khoản KTX': s.hasRegisteredAccount ? 'Có' : 'Chưa',
      'Ngày thêm vào': new Date(s.createdAt).toLocaleDateString('vi-VN'),
    }))

    const worksheet = XLSX.utils.json_to_sheet(rows)

    worksheet['!cols'] = Object.keys(rows[0] || {}).map((header) => ({
      wch: Math.min(45, Math.max(14, header.length + 2, ...rows.map((row) => String(row[header as keyof typeof row] ?? '').length + 2))),
    }))
    for (let row = 2; row <= rows.length + 1; row += 1) {
      const cell = worksheet[`D${row}`]
      if (cell) {
        cell.t = 's'
        cell.z = '@'
      }
    }

    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Danh sách sinh viên')

    const filename = `danh_sach_sinh_vien_${new Date().toISOString().slice(0, 10)}.xlsx`
    XLSX.writeFile(workbook, filename)
  },

  /**
   * Xuất file mẫu Excel để nhập liệu
   * - Header tiếng Việt
   * - Cột "Trạng thái" có dropdown validation
   * - Ghi chú hướng dẫn ở sheet thứ 2
   */
  async exportTemplateExcel() {
    const { default: ExcelJS } = await import('exceljs')
    const workbook = new ExcelJS.Workbook()
    workbook.creator = 'KTX UET'
    workbook.created = new Date()

    const worksheet = workbook.addWorksheet('Nhập liệu sinh viên', {
      views: [{ state: 'frozen', ySplit: 1, showGridLines: false }],
    })
    worksheet.columns = [
      { header: 'Mã sinh viên (*)', key: 'studentCode', width: 20 },
      { header: 'Họ và tên (*)', key: 'fullName', width: 26 },
      { header: 'Email sinh viên (*)', key: 'email', width: 32 },
      { header: 'Số điện thoại (*)', key: 'phone', width: 20, style: { numFmt: '@' } },
      { header: 'Lớp / Khóa (*)', key: 'className', width: 18 },
      { header: 'Trạng thái', key: 'status', width: 22 },
    ]
    styleHeader(worksheet.getRow(1))
    worksheet.autoFilter = 'A1:F1001'
    worksheet.getColumn('phone').numFmt = '@'
    for (let row = 2; row <= 1001; row += 1) {
      worksheet.getCell(`D${row}`).numFmt = '@'
      worksheet.getCell(`F${row}`).dataValidation = {
        type: 'list',
        allowBlank: true,
        formulae: ['"Đang hoạt động,Tạm ngưng"'],
        showErrorMessage: true,
        errorStyle: 'stop',
        errorTitle: 'Trạng thái không hợp lệ',
        error: 'Hãy chọn Đang hoạt động hoặc Tạm ngưng từ danh sách.',
        showInputMessage: true,
        promptTitle: 'Chọn trạng thái',
        prompt: 'Chọn một giá trị trong danh sách.',
      }
    }
    autoFitColumns(worksheet, 14, 42)

    const guide = workbook.addWorksheet('Hướng dẫn', { views: [{ showGridLines: false }] })
    guide.addRows([
      ['HƯỚNG DẪN NHẬP DANH SÁCH SINH VIÊN'],
      [],
      ['Cột', 'Tên trường', 'Bắt buộc', 'Ghi chú'],
      ['A', 'Mã sinh viên', 'Có', 'Ví dụ: SV001, B20DCCN001'],
      ['B', 'Họ và tên', 'Có', 'Nhập đầy đủ họ tên có dấu'],
      ['C', 'Email sinh viên', 'Có', 'Email trường cấp, ví dụ: ten@abc.edu.vn'],
      ['D', 'Số điện thoại', 'Có', 'Cột dạng văn bản nên giữ nguyên số 0 ở đầu'],
      ['E', 'Lớp / Khóa', 'Có', 'Ví dụ: CNTT01, KTPM02'],
      ['F', 'Trạng thái', 'Không', 'Chọn từ dropdown. Để trống sẽ dùng trạng thái đang hoạt động'],
      [],
      ['Lưu ý'],
      ['Không xóa hoặc đổi tên dòng tiêu đề.'],
      ['Các cột có dấu (*) là bắt buộc.'],
      ['Mỗi email, số điện thoại và mã sinh viên phải là duy nhất.'],
      ['Hệ thống nhận file .xlsx hoặc .csv, tối đa 5 MB.'],
    ])
    guide.mergeCells('A1:D1')
    guide.getCell('A1').font = { name: 'Aptos', size: 14, bold: true, color: { argb: 'FF0F172A' } }
    styleHeader(guide.getRow(3))
    autoFitColumns(guide, 10, 60)

    const buffer = await workbook.xlsx.writeBuffer()
    downloadBlob(new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }), 'mau_nhap_danh_sach_sinh_vien.xlsx')
  },
}

const VIETNAMESE_TO_API_HEADER: Record<string, string> = {
  'mã sinh viên (*)': 'student_code',
  'mã sinh viên': 'student_code',
  'họ và tên (*)': 'full_name',
  'họ và tên': 'full_name',
  'email sinh viên (*)': 'email',
  'email sinh viên': 'email',
  'số điện thoại (*)': 'phone',
  'số điện thoại': 'phone',
  'lớp / khóa (*)': 'class_name',
  'lớp / khóa': 'class_name',
  'trạng thái': 'status',
}

async function normalizeStudentImportFile(file: File) {
  if (!file.name.toLowerCase().endsWith('.xlsx')) return file
  const workbook = XLSX.read(await file.arrayBuffer(), { type: 'array' })
  const worksheet = workbook.Sheets[workbook.SheetNames[0]]
  if (!worksheet) throw new Error('File Excel không có sheet dữ liệu.')
  const rows = XLSX.utils.sheet_to_json<Array<string | number>>(worksheet, { header: 1, raw: false, defval: '' })
  if (!rows.length) throw new Error('File Excel không có dữ liệu.')

  const headers = rows[0].map((value) => VIETNAMESE_TO_API_HEADER[String(value).trim().toLowerCase()] || String(value).trim().toLowerCase())
  const normalizedRows = rows.slice(1).map((row) => headers.map((header, index) => {
    let value = String(row[index] ?? '').trim()
    if (header === 'phone' && /^\d{8,9}$/.test(value)) value = value.padStart(10, '0')
    if (header === 'status') value = value === 'Tạm ngưng' ? 'INACTIVE' : 'ACTIVE'
    return value
  }))
  const csv = XLSX.utils.sheet_to_csv(XLSX.utils.aoa_to_sheet([headers, ...normalizedRows]))
  return new File([`\uFEFF${csv}`], file.name.replace(/\.xlsx$/i, '.csv'), { type: 'text/csv;charset=utf-8' })
}

function styleHeader(row: Row) {
  row.height = 26
  row.eachCell((cell) => {
    cell.font = { name: 'Aptos', size: 10, bold: true, color: { argb: 'FFFFFFFF' } }
    cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF0F766E' } }
    cell.alignment = { horizontal: 'center', vertical: 'middle' }
    cell.border = { bottom: { style: 'thin', color: { argb: 'FFCBD5E1' } } }
  })
}

function autoFitColumns(worksheet: Worksheet, minWidth: number, maxWidth: number) {
  worksheet.columns.forEach((column) => {
    let longest = 0
    column.eachCell?.({ includeEmpty: false }, (cell) => {
      const value = cell.value == null ? '' : String(cell.value)
      longest = Math.max(longest, value.length)
    })
    column.width = Math.min(maxWidth, Math.max(minWidth, longest + 3))
  })
}

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}
