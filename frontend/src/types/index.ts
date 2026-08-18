// ---------- Auth ----------
export type Role = 'CUSTOMER' | 'PROVIDER' | 'ADMIN'

export interface AuthUser {
  id: number
  fullName: string
  email: string
  role: Role
}

export interface AuthResponse extends AuthUser {
  token: string
  tokenType: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  fullName: string
  email: string
  phone: string
  password: string
  role: Role
}

export interface UserProfile {
  id: number
  fullName: string
  email: string
  phone: string
  role: Role
  createdAt: string
}

// ---------- Catalog ----------
export interface Category {
  id: number
  name: string
  description: string
  icon: string
  createdAt: string
}

export interface ServiceItem {
  id: number
  name: string
  description: string
  categoryId: number
  categoryName: string
  basePrice: number
  durationMinutes: number
  createdAt: string
}

// ---------- Customer ----------
export interface Address {
  id: number
  label: string
  street: string
  city: string
  state: string
  zip: string
  isDefault: boolean
  latitude?: number | null
  longitude?: number | null
}

export interface AddressInput {
  label: string
  street: string
  city: string
  state: string
  zip: string
  isDefault?: boolean
  latitude?: number | null
  longitude?: number | null
}

export interface Customer {
  id: number
  userId: number
  fullName: string
  email: string
  phone: string
  createdAt: string
  addresses: Address[]
}

export interface CustomerInput {
  fullName?: string
  email?: string
  phone?: string
}

// ---------- Provider ----------
export type AvailabilityStatus = 'AVAILABLE' | 'BUSY' | 'OFFLINE'
export type VerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED'

export interface Provider {
  id: number
  userId: number
  name: string
  experienceYears: number
  skills: string[]
  availability: AvailabilityStatus
  verificationStatus: VerificationStatus
  createdAt: string
}

export interface ProviderRegistrationInput {
  name: string
  experienceYears: number
  serviceId: number
  skills: string[]
}

// ---------- Booking ----------
export type BookingStatus =
  | 'PENDING'
  | 'ACCEPTED'
  | 'ON_THE_WAY'
  | 'STARTED'
  | 'COMPLETED'
  | 'CANCELLED'

/**
 * Payment state of a booking — tracked separately from {@link BookingStatus}.
 * A booking can be PENDING+PAID, ACCEPTED+PAID, etc.; paying never advances
 * the lifecycle status.
 */
export type BookingPaymentStatus = 'UNPAID' | 'PAID'

export interface Booking {
  id: number
  customerId: number
  customerName: string
  customerAddress: string
  providerId: number
  providerName: string
  serviceId: number
  serviceName: string
  servicePrice: number
  bookingDate: string
  address: string
  status: BookingStatus
  paymentStatus: BookingPaymentStatus
  createdAt: string
  updatedAt: string
}

export interface BookingInput {
  providerId: number
  serviceId: number
  bookingDate: string
  address: string
}

// ---------- Payment ----------
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED'

export interface Payment {
  id: number
  bookingId: number
  customerId: number
  amount: number
  currency?: string | null
  status: PaymentStatus
  method?: string | null
  razorpayOrderId?: string | null
  razorpayPaymentId?: string | null
  razorpaySignature?: string | null
  transactionDate: string
  createdAt: string
}

export interface CreateRazorpayOrderInput {
  bookingId: number
  amount: number
}

export interface RazorpayOrder {
  orderId: string
  amount: number
  currency: string
  razorpayKeyId: string
}

export interface VerifyPaymentInput {
  razorpayOrderId: string
  razorpayPaymentId: string
  razorpaySignature: string
}

// ---------- Notifications ----------
export interface AppNotification {
  id: number
  recipientId: number
  type: string
  message: string
  isRead: boolean
  createdAt: string
}

// ---------- Admin ----------
export interface Dashboard {
  totalCustomers: number
  totalProviders: number
  bookingsByStatus: Record<string, number>
  totalRevenue: number
}

export interface AdminCustomer {
  id: number
  userId: number
  fullName: string
  email: string
  phone: string
  createdAt: string
}

export interface AdminProvider {
  id: number
  userId: number
  name: string
  experienceYears: number
  skills: string[]
  availability: string
  verificationStatus: string
  createdAt: string
}

export interface AdminBooking {
  id: number
  customerId: number
  customerName: string
  providerId: number
  providerName: string
  serviceId: number
  serviceName: string
  servicePrice: number
  bookingDate: string
  address: string
  status: string
  paymentStatus: string
  createdAt: string
  updatedAt: string
}
