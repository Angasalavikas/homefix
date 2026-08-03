import api from './api'
import type { AppNotification } from '../types'

export async function getNotifications(): Promise<AppNotification[]> {
  const { data } = await api.get<AppNotification[]>('/notifications')
  return data
}

export async function markNotificationRead(id: number): Promise<AppNotification> {
  const { data } = await api.put<AppNotification>(`/notifications/${id}/read`)
  return data
}
