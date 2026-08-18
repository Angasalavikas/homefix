import api from './api'
import type { AdminBooking, AdminCustomer, AdminProvider, Dashboard } from '../types'

export async function getDashboard(): Promise<Dashboard> {
  const { data } = await api.get<Dashboard>('/admin/dashboard')
  return data
}

export async function getAdminCustomers(): Promise<AdminCustomer[]> {
  const { data } = await api.get<AdminCustomer[]>('/admin/customers')
  return data
}

export async function getAdminProviders(): Promise<AdminProvider[]> {
  const { data } = await api.get<AdminProvider[]>('/admin/providers')
  return data
}

export async function getAdminBookings(): Promise<AdminBooking[]> {
  const { data } = await api.get<AdminBooking[]>('/admin/bookings')
  return data
}

export async function verifyProvider(id: number): Promise<string> {
  const { data } = await api.put<string>(`/admin/providers/${id}/verify`)
  return data
}
