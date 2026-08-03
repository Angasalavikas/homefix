import api from './api'
import type { Booking, BookingInput, BookingStatus, Payment, PaymentInput } from '../types'

export async function createBooking(payload: BookingInput): Promise<Booking> {
  const { data } = await api.post<Booking>('/bookings', payload)
  return data
}

export async function listBookings(
  role: 'customer' | 'provider' = 'customer',
  status?: BookingStatus,
): Promise<Booking[]> {
  const { data } = await api.get<Booking[]>('/bookings', { params: { role, status } })
  return data
}

export async function updateBookingStatus(id: number, status: BookingStatus): Promise<Booking> {
  const { data } = await api.put<Booking>(`/bookings/${id}/status`, { status })
  return data
}

export async function cancelBooking(id: number): Promise<Booking> {
  const { data } = await api.put<Booking>(`/bookings/${id}/cancel`)
  return data
}

export async function processPayment(payload: PaymentInput): Promise<Payment> {
  const { data } = await api.post<Payment>('/payments', payload)
  return data
}

export async function getPaymentHistory(): Promise<Payment[]> {
  const { data } = await api.get<Payment[]>('/payments/history')
  return data
}
