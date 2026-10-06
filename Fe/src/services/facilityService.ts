import { api } from './api'

export interface Building {
  id: string
  code: string
  name: string
  address?: string
  genderType: 'MALE' | 'FEMALE' | 'MIXED'
  totalFloors: number
  status: 'ACTIVE' | 'MAINTENANCE' | 'INACTIVE'
  description?: string
  floorCount?: number
  roomCount?: number
  totalBeds?: number
  availableBeds?: number
  occupiedBeds?: number
  createdAt?: string
  updatedAt?: string
}

export interface Floor {
  id: string
  buildingId: string
  buildingCode: string
  buildingName: string
  floorNumber: number
  name: string
  status: 'ACTIVE' | 'MAINTENANCE' | 'INACTIVE'
  roomCount?: number
  totalBeds?: number
  availableBeds?: number
  occupiedBeds?: number
  createdAt?: string
  updatedAt?: string
}

export interface Room {
  id: string
  floorId: string
  floorNumber: number
  floorName?: string
  buildingId: string
  buildingCode: string
  buildingName: string
  roomNumber: string
  roomType: 'STANDARD_8' | 'STANDARD_6' | 'PREMIUM_4'
  capacity: number
  currentOccupancy: number
  availableBeds: number
  genderType: 'MALE' | 'FEMALE' | 'MIXED'
  status: 'AVAILABLE' | 'FULL' | 'MAINTENANCE' | 'INACTIVE'
  areaSqm?: number
  pricePolicyId?: string
  pricePolicyName?: string
  pricePerMonth?: number
  imageUrl?: string
  description?: string
  notes?: string
  bedCount?: number
  createdAt?: string
  updatedAt?: string
}

export interface PublicRoomSummary {
  id: string
  buildingCode: string
  buildingName: string
  floorNumber: number
  roomNumber: string
  roomType: 'STANDARD_8' | 'STANDARD_6' | 'PREMIUM_4'
  capacity: number
  availableBeds: number
  genderType: 'MALE' | 'FEMALE' | 'MIXED'
  status: 'AVAILABLE'
  areaSqm?: number
  pricePerMonth?: number
  imageUrl?: string
  description?: string
}

export interface Bed {
  id: string
  roomId: string
  roomNumber: string
  floorNumber: number
  buildingCode: string
  buildingName: string
  bedNumber: string
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'INACTIVE'
  isUsable: boolean
  notes?: string
  createdAt?: string
  updatedAt?: string
}

export interface PricePolicy {
  id: string
  name: string
  roomType: 'STANDARD_8' | 'STANDARD_6' | 'PREMIUM_4'
  pricePerMonth: number
  effectiveFrom: string
  effectiveTo?: string
  isActive: boolean
  description?: string
  createdAt?: string
  updatedAt?: string
}

export interface FacilityOverviewStats {
  totalBuildings: number
  totalFloors: number
  totalRooms: number
  availableRooms: number
  fullRooms: number
  maintenanceRooms: number
  totalBeds: number
  availableBeds: number
  occupiedBeds: number
  maintenanceBeds: number
  occupancyRate: number
}

export const facilityService = {
  // Buildings
  async getBuildings(params?: { search?: string; status?: string; genderType?: string; page?: number; size?: number }) {
    const res = await api.get('/buildings', { params })
    return res.data
  },

  async getBuildingById(id: string) {
    const res = await api.get(`/buildings/${id}`)
    return res.data
  },

  async getActiveBuildings() {
    const res = await api.get('/buildings/active')
    return res.data
  },

  async createBuilding(data: {
    code: string
    name: string
    address?: string
    genderType: string
    totalFloors: number
    status?: string
    description?: string
  }) {
    const res = await api.post('/buildings', data)
    return res.data
  },

  async updateBuilding(id: string, data: {
    name: string
    address?: string
    genderType: string
    totalFloors: number
    status: string
    description?: string
  }) {
    const res = await api.put(`/buildings/${id}`, data)
    return res.data
  },

  async deleteBuilding(id: string) {
    const res = await api.delete(`/buildings/${id}`)
    return res.data
  },

  // Floors
  async getFloorsByBuilding(buildingId: string) {
    const res = await api.get(`/floors/buildings/${buildingId}`)
    return res.data
  },

  async createFloor(buildingId: string, data: { floorNumber: number; name?: string; status?: string }) {
    const res = await api.post(`/floors/buildings/${buildingId}`, data)
    return res.data
  },

  async updateFloor(id: string, data: { name?: string; status: string }) {
    const res = await api.put(`/floors/${id}`, data)
    return res.data
  },

  async deleteFloor(id: string) {
    const res = await api.delete(`/floors/${id}`)
    return res.data
  },

  // Rooms
  async getRooms(params?: {
    buildingId?: string
    floorId?: string
    status?: string
    roomType?: string
    genderType?: string
    search?: string
    page?: number
    size?: number
  }) {
    const res = await api.get('/rooms', { params })
    return res.data
  },

  async getRoomsByFloor(floorId: string) {
    const res = await api.get(`/rooms/floors/${floorId}`)
    return res.data
  },

  async getRoomById(id: string) {
    const res = await api.get(`/rooms/${id}`)
    return res.data
  },

  async createRoom(data: {
    floorId: string
    roomNumber: string
    roomType: string
    capacity: number
    genderType: string
    areaSqm?: number
    pricePolicyId?: string
    imageUrl?: string
    description?: string
    notes?: string
    autoGenerateBeds?: boolean
  }) {
    const res = await api.post('/rooms', data)
    return res.data
  },

  async uploadRoomImage(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await api.post<{ data: { imageUrl: string } }>('/rooms/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return res.data.data
  },

  async updateRoom(id: string, data: {
    roomNumber: string
    roomType: string
    capacity: number
    genderType: string
    status: string
    areaSqm?: number
    pricePolicyId?: string
    imageUrl?: string
    description?: string
    notes?: string
  }) {
    const res = await api.put(`/rooms/${id}`, data)
    return res.data
  },

  async updateRoomStatus(id: string, status: string) {
    const res = await api.patch(`/rooms/${id}/status`, null, { params: { status } })
    return res.data
  },

  async deleteRoom(id: string) {
    const res = await api.delete(`/rooms/${id}`)
    return res.data
  },

  // Beds
  async getBedsByRoom(roomId: string) {
    const res = await api.get(`/beds/rooms/${roomId}`)
    return res.data
  },

  async createBed(data: { roomId: string; bedNumber: string; notes?: string }) {
    const res = await api.post('/beds', data)
    return res.data
  },

  async batchCreateBeds(data: { roomId: string; prefix?: string; count: number }) {
    const res = await api.post('/beds/batch', data)
    return res.data
  },

  async updateBed(id: string, data: { bedNumber: string; status: string; notes?: string }) {
    const res = await api.put(`/beds/${id}`, data)
    return res.data
  },

  async updateBedStatus(id: string, status: string) {
    const res = await api.patch(`/beds/${id}/status`, null, { params: { status } })
    return res.data
  },

  async deleteBed(id: string) {
    const res = await api.delete(`/beds/${id}`)
    return res.data
  },

  // Price Policies
  async getAllPricePolicies() {
    const res = await api.get('/price-policies')
    return res.data
  },

  async getActivePricePolicies() {
    const res = await api.get('/price-policies/active')
    return res.data
  },

  async createPricePolicy(data: {
    name: string
    roomType: string
    pricePerMonth: number
    effectiveFrom: string
    effectiveTo?: string
    isActive?: boolean
    description?: string
  }) {
    const res = await api.post('/price-policies', data)
    return res.data
  },

  async updatePricePolicy(id: string, data: {
    name: string
    pricePerMonth: number
    effectiveTo?: string
    isActive: boolean
    description?: string
  }) {
    const res = await api.put(`/price-policies/${id}`, data)
    return res.data
  },

  async deletePricePolicy(id: string) {
    const res = await api.delete(`/price-policies/${id}`)
    return res.data
  },

  // Stats
  async getOverviewStats() {
    const res = await api.get('/facility-stats/overview')
    return res.data
  },

  // Public APIs
  async getPublicAvailableRooms(params?: { roomType?: string; genderType?: string }) {
    const res = await api.get<{ data: PublicRoomSummary[] }>('/public/facilities/rooms', { params })
    return res.data
  },

  async getPublicOverview() {
    const res = await api.get<{ data: FacilityOverviewStats }>('/public/facilities/overview')
    return res.data
  },

  async getPublicActiveBuildings() {
    const res = await api.get('/public/facilities/buildings')
    return res.data
  },

  async getPublicActivePricePolicies() {
    const res = await api.get('/public/facilities/price-policies')
    return res.data
  }
}
