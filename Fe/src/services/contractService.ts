import { api } from '@/services/api'

export type ContractStatus = 'DRAFT' | 'ACTIVE' | 'EXPIRED' | 'TERMINATED'
export type Contract = {
  id: string
  contractCode: string
  userId: string
  studentCode?: string
  fullName: string
  assignmentId: string
  roomNumber: string
  bedNumber: string
  buildingCode: string
  startDate: string
  endDate: string
  rentalPrice: number
  deposit: number
  status: ContractStatus
  terminatedAt?: string
  terminationReason?: string
  createdAt: string
}

type Page<T> = { content: T[]; totalElements: number }

export const contractService = {
  async mine() {
    const { data } = await api.get<{ data: Contract[] }>('/contracts/my')
    return data.data
  },
  async all(status = '') {
    const { data } = await api.get<{ data: Page<Contract> }>('/contracts', {
      params: { status: status || undefined, size: 200, sort: 'createdAt,desc' },
    })
    return data.data.content
  },
  async create(payload: Record<string, unknown>) {
    const { data } = await api.post<{ data: Contract }>('/contracts', payload)
    return data.data
  },
  async activate(id: string) {
    const { data } = await api.patch<{ data: Contract }>(`/contracts/${id}/activate`)
    return data.data
  },
  async renew(id: string, newEndDate: string) {
    const { data } = await api.patch<{ data: Contract }>(`/contracts/${id}/renew`, { newEndDate })
    return data.data
  },
  async terminate(id: string, reason: string) {
    const { data } = await api.patch<{ data: Contract }>(`/contracts/${id}/terminate`, { reason })
    return data.data
  },
}
