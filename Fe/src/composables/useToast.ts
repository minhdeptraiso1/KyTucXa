import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastItem {
  id: string
  type: ToastType
  title?: string
  message: string
  duration: number
  createdAt: number
}

const toasts = ref<ToastItem[]>([])

export function useToast() {
  const remove = (id: string) => {
    const idx = toasts.value.findIndex((t) => t.id === id)
    if (idx !== -1) {
      toasts.value.splice(idx, 1)
    }
  }

  const add = (message: string, type: ToastType = 'info', title?: string, duration = 4500) => {
    const id = `toast-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`
    const toast: ToastItem = {
      id,
      type,
      title,
      message,
      duration,
      createdAt: Date.now(),
    }
    toasts.value.push(toast)

    if (duration > 0) {
      setTimeout(() => {
        remove(id)
      }, duration)
    }

    return id
  }

  const success = (message: string, title: string = 'Thành công') => add(message, 'success', title)
  const error = (message: string, title: string = 'Lỗi') => add(message, 'error', title, 6000)
  const warning = (message: string, title: string = 'Cảnh báo') => add(message, 'warning', title)
  const info = (message: string, title: string = 'Thông báo') => add(message, 'info', title)

  const clearAll = () => {
    toasts.value = []
  }

  return {
    toasts,
    add,
    remove,
    clearAll,
    success,
    error,
    warning,
    info,
  }
}
