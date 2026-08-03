import api from './api'
import type { Category, ServiceItem } from '../types'

export async function getCategories(): Promise<Category[]> {
  const { data } = await api.get<Category[]>('/catalog/categories')
  return data
}

export async function searchServices(params?: {
  category?: number
  keyword?: string
}): Promise<ServiceItem[]> {
  const { data } = await api.get<ServiceItem[]>('/catalog/services/search', { params })
  return data
}

export async function getServiceById(id: number | string): Promise<ServiceItem> {
  const { data } = await api.get<ServiceItem>(`/catalog/services/${id}`)
  return data
}
