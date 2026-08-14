import api from './api'
import type {
  Booking,
  BookingInput,
  BookingStatus,
  CreateRazorpayOrderInput,
  Payment,
  RazorpayOrder,
  VerifyPaymentInput,
} from '../types'

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

/** Step 1 of checkout: create a Razorpay Order for the booking. */
export async function createRazorpayOrder(payload: CreateRazorpayOrderInput): Promise<RazorpayOrder> {
  const { data } = await api.post<RazorpayOrder>('/payments/create-order', payload)
  return data
}

/** Step 2 of checkout: verify the payment signature server-side. */
export async function verifyPayment(payload: VerifyPaymentInput): Promise<Payment> {
  const { data } = await api.post<Payment>('/payments/verify', payload)
  return data
}

export async function getPaymentHistory(): Promise<Payment[]> {
  const { data } = await api.get<Payment[]>('/payments/history')
  return data
}
