import { api } from '@/services/api'

export type RegistrationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
export type AssignmentStatus = 'ACTIVE' | 'ENDED' | 'CANCELLED'

export type Registration = {
  id: string
  userId: string
  studentCode?: string
  fullName: string
  email: string
  requestedRoomType: string
  requestedGenderType: string
  preferredStartDate: string
  preferredEndDate?: string
  reason?: string
  status: RegistrationStatus
  rejectionReason?: string
  createdAt: string
}

export type Assignment = {
  id: string
  registrationId: string
  userId: string
  studentCode?: string
  fullName: string
  bedId: string
  bedNumber: string
  roomNumber: string
  buildingCode: string
  floorNumber: number
  startDate: string
  endDate?: string
  status: AssignmentStatus
  createdAt: string
}

export type Room = {
  id: string
  buildingCode: string
  roomNumber: string
  roomType: string
  genderType: string
  availableBeds: number
  status: string
}

export type Bed = { id: string; bedNumber: string; status: string }
type Page<T> = { content: T[]; totalElements: number; totalPages: number; number: number }

export const registrationService = {
  async createMine(payload: Record<string, unknown>) {
    const { data } = await api.post<{ data: Registration }>('/registrations/my', payload)
    return data.data
  },
  async mine() {
    const { data } = await api.get<{ data: Registration[] }>('/registrations/my')
    return data.data
  },
  async cancel(id: string) {
    const { data } = await api.patch<{ data: Registration }>(`/registrations/${id}/cancel`)
    return data.data
  },
  async queue(status = '', keyword = '') {
    const { data } = await api.get<{ data: Page<Registration> }>('/registrations', {
      params: { status: status || undefined, keyword: keyword || undefined, size: 100, sort: 'createdAt,desc' },
    })
    return data.data
  },
  async review(id: string, decision: 'APPROVED' | 'REJECTED', rejectionReason?: string) {
    const { data } = await api.patch<{ data: Registration }>(`/registrations/${id}/review`, { decision, rejectionReason })
    return data.data
  },
  async rooms() {
    const { data } = await api.get<{ data: Page<Room> }>('/rooms', { params: { status: 'AVAILABLE', size: 500 } })
    return data.data.content
  },
  async beds(roomId: string) {
    const { data } = await api.get<{ data: Bed[] }>(`/beds/rooms/${roomId}`)
    return data.data.filter((bed) => bed.status === 'AVAILABLE')
  },
  async assign(payload: Record<string, unknown>) {
    const { data } = await api.post<{ data: Assignment }>('/room-assignments', payload)
    return data.data
  },
  async myAssignments() {
    const { data } = await api.get<{ data: Assignment[] }>('/room-assignments/my')
    return data.data
  },
  async assignments() {
    const { data } = await api.get<{ data: Page<Assignment> }>('/room-assignments', { params: { size: 200, sort: 'createdAt,desc' } })
    return data.data.content
  },
  async endAssignment(id: string) {
    const { data } = await api.patch<{ data: Assignment }>(`/room-assignments/${id}/end`)
    return data.data
  },
}
