export function apiErrorMessage(cause: unknown, fallback = 'Không thể xử lý yêu cầu.') {
  return (cause as { response?: { data?: { error?: { message?: string } } } })
    .response?.data?.error?.message || fallback
}
