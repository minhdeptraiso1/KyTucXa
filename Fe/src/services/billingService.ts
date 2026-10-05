import { api } from '@/services/api'

export type UtilityType = 'ELECTRICITY' | 'WATER'
export type InvoiceStatus = 'ISSUED' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'CANCELLED'
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED'

export type Meter = {
  id: string; roomId: string; roomNumber: string; buildingCode: string; meterCode: string
  utilityType: UtilityType; unit: string; status: 'ACTIVE' | 'INACTIVE'
}
export type MeterReading = {
  id: string; meterId: string; meterCode: string; utilityType: UtilityType; billingPeriod: string
  previousValue: number; currentValue: number; consumption: number; resetRecorded: boolean; note?: string; readAt: string
}
export type Tariff = {
  id: string; utilityType: UtilityType; unitPrice: number; effectiveFrom: string; effectiveTo?: string; active: boolean
}
export type InvoiceItem = {
  id: string; itemType: 'ROOM' | 'ELECTRICITY' | 'WATER' | 'SERVICE' | 'FINE'
  description: string; quantity: number; unitPrice: number; amount: number
}
export type Invoice = {
  id: string; invoiceCode: string; userId: string; studentCode?: string; studentName: string
  contractId: string; contractCode: string; roomNumber: string; billingPeriod: string; dueDate: string
  subtotal: number; discount: number; fineAmount: number; totalAmount: number; paidAmount: number
  remainingAmount: number; status: InvoiceStatus; items: InvoiceItem[]
}
export type Payment = {
  id: string; paymentCode: string; invoiceId: string; invoiceCode: string; userId: string
  amount: number; method: 'CASH' | 'VNPAY'; status: PaymentStatus
  externalReference?: string; paidAt?: string; createdAt: string
}
type Page<T> = { content: T[]; totalElements: number }

export const billingService = {
  async meters() {
    const { data } = await api.get<{ data: Meter[] }>('/meters')
    return data.data
  },
  async createMeter(payload: Record<string, unknown>) {
    const { data } = await api.post<{ data: Meter }>('/meters', payload)
    return data.data
  },
  async updateMeter(id: string, payload: { meterCode: string; unit: string; status: Meter['status'] }) {
    const { data } = await api.put<{ data: Meter }>(`/meters/${id}`, payload)
    return data.data
  },
  async readings() {
    const { data } = await api.get<{ data: MeterReading[] }>('/meter-readings')
    return data.data
  },
  async createReading(payload: Record<string, unknown>) {
    const { data } = await api.post<{ data: MeterReading }>('/meter-readings', payload)
    return data.data
  },
  async updateReading(id: string, payload: { currentValue: number; resetRecorded: boolean; note?: string }) {
    const { data } = await api.put<{ data: MeterReading }>(`/meter-readings/${id}`, payload)
    return data.data
  },
  async tariffs() {
    const { data } = await api.get<{ data: Tariff[] }>('/utility-tariffs')
    return data.data
  },
  async createTariff(payload: Record<string, unknown>) {
    const { data } = await api.post<{ data: Tariff }>('/utility-tariffs', payload)
    return data.data
  },
  async updateTariff(id: string, payload: { unitPrice: number; effectiveFrom: string; effectiveTo: string | null; active: boolean }) {
    const { data } = await api.put<{ data: Tariff }>(`/utility-tariffs/${id}`, payload)
    return data.data
  },
  async invoices(status = '') {
    const { data } = await api.get<{ data: Page<Invoice> }>('/invoices', {
      params: { status: status || undefined, size: 200, sort: 'billingPeriod,desc' },
    })
    return data.data.content
  },
  async myInvoices() {
    const { data } = await api.get<{ data: Invoice[] }>('/invoices/my')
    return data.data
  },
  async generateInvoice(payload: Record<string, unknown>) {
    const { data } = await api.post<{ data: Invoice }>('/invoices/generate', payload)
    return data.data
  },
  async payments() {
    const { data } = await api.get<{ data: Payment[] }>('/payments')
    return data.data
  },
  async myPayments() {
    const { data } = await api.get<{ data: Payment[] }>('/payments/my')
    return data.data
  },
  async directPayment(payload: { invoiceId: string; amount: number; idempotencyKey: string }) {
    const { data } = await api.post<{ data: Payment }>('/payments/direct', payload)
    return data.data
  },
  async createVnPay(payload: { invoiceId: string; amount: number; idempotencyKey: string }) {
    const { data } = await api.post<{ data: { paymentId: string; paymentCode: string; paymentUrl: string } }>('/payments/vnpay/create', payload)
    return data.data
  },
  async finishVnPay(params: URLSearchParams) {
    const { data } = await api.get<{ data: Payment }>('/payments/vnpay/callback', { params })
    return data.data
  },
}

