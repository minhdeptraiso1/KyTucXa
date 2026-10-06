const enumLabels: Record<string, string> = {
  // Tài khoản và trạng thái chung
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Tạm ngưng',
  PENDING: 'Đang xử lý',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Đã từ chối',
  CANCELLED: 'Đã hủy',
  DRAFT: 'Bản nháp',
  EXPIRED: 'Hết hạn',
  TERMINATED: 'Đã thanh lý',
  ENDED: 'Đã kết thúc',

  // Phòng và giường
  AVAILABLE: 'Còn chỗ',
  FULL: 'Đã đầy',
  MAINTENANCE: 'Bảo trì',
  OCCUPIED: 'Đang sử dụng',
  STANDARD_8: 'Phòng tiêu chuẩn 8 người',
  STANDARD_6: 'Phòng tiêu chuẩn 6 người',
  PREMIUM_4: 'Phòng chất lượng cao 4 người',
  MALE: 'Nam',
  FEMALE: 'Nữ',
  MIXED: 'Nam và nữ',

  // Tài chính
  ISSUED: 'Chưa thanh toán',
  PARTIALLY_PAID: 'Thanh toán một phần',
  PAID: 'Đã thanh toán',
  OVERDUE: 'Quá hạn',
  SUCCESS: 'Thành công',
  FAILED: 'Thất bại',
  CASH: 'Tiền mặt',
  VNPAY: 'VNPay',
  BANK_TRANSFER: 'Chuyển khoản',
  ELECTRICITY: 'Điện',
  WATER: 'Nước',
}

export function enumLabel(value?: string | null, fallback = 'Chưa cập nhật') {
  if (!value) return fallback
  return enumLabels[value] ?? value
}

export function formatDateVi(value?: string | null, fallback = 'Chưa xác định') {
  if (!value) return fallback
  const datePart = value.slice(0, 10)
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(datePart)
  if (match) return `${match[3]}/${match[2]}/${match[1]}`

  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) return value
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(parsed)
}

export function formatMonthVi(value?: string | null, fallback = 'Chưa xác định') {
  if (!value) return fallback
  const match = /^(\d{4})-(\d{2})/.exec(value)
  if (match) return `Tháng ${match[2]}/${match[1]}`
  return formatDateVi(value, fallback)
}

