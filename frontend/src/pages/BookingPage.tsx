import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getServiceById } from '../services/catalog'
import { getAvailableProviders } from '../services/provider'
import { getMyAddresses } from '../services/customer'
import { createBooking, createRazorpayOrder, verifyPayment } from '../services/booking'
import { loadRazorpayCheckoutScript, openRazorpayCheckout } from '../services/razorpay'
import { getErrorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Card from '../components/Card'
import Button from '../components/Button'
import LocationPinButton from '../components/LocationPinButton'
import { Field, Select, TextInput } from '../components/FormField'
import LoadingSpinner from '../components/LoadingSpinner'
import StatusBadge from '../components/StatusBadge'
import { formatCurrency, toLocalInputValue } from '../utils/format'
import type { Address, Booking, Payment, Provider, ServiceItem } from '../types'

interface FieldErrors {
  provider?: string
  bookingDate?: string
  address?: string
  newAddress?: string
}

/**
 * datetime-local gives "YYYY-MM-DDTHH:mm" (local, no timezone). Send it as-is
 * (with seconds appended) so the backend's LocalDateTime deserializer stores
 * the exact local wall-clock time the user picked — no UTC timezone shift.
 */
function normalizeLocalDateTime(value: string): string {
  return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value) ? `${value}:00` : value
}

export default function BookingPage() {
  const { serviceId } = useParams<{ serviceId: string }>()
  const { user } = useAuth()

  const [service, setService] = useState<ServiceItem | null>(null)
  const [providers, setProviders] = useState<Provider[]>([])
  const [addresses, setAddresses] = useState<Address[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [providerId, setProviderId] = useState('')
  const [bookingDate, setBookingDate] = useState('')
  const [addressMode, setAddressMode] = useState<'saved' | 'new'>('saved')
  const [savedAddressId, setSavedAddressId] = useState('')
  const [newAddress, setNewAddress] = useState({ label: '', street: '', city: '', state: '', zip: '' })
  const [errors, setErrors] = useState<FieldErrors>({})
  const [submitting, setSubmitting] = useState(false)

  const [createdBooking, setCreatedBooking] = useState<Booking | null>(null)
  const [paying, setPaying] = useState(false)
  const [payment, setPayment] = useState<Payment | null>(null)
  const [paymentSkipped, setPaymentSkipped] = useState(false)
  const [actionError, setActionError] = useState('')

  const minDate = useMemo(() => toLocalInputValue(new Date(Date.now() + 60 * 60 * 1000)), [])

  useEffect(() => {
    let cancelled = false
    Promise.allSettled([getServiceById(serviceId ?? ''), getAvailableProviders(Number(serviceId)), getMyAddresses()])
      .then(([svc, provs, addrs]) => {
        if (cancelled) return
        if (svc.status === 'fulfilled') setService(svc.value)
        if (provs.status === 'fulfilled') {
          setProviders(provs.value)
          // Only pre-select an AVAILABLE provider — never a busy/offline one,
          // since their option is disabled in the dropdown.
          const firstAvailable = provs.value.find((p) => p.availability === 'AVAILABLE')
          if (firstAvailable) setProviderId(String(firstAvailable.id))
        }
        if (addrs.status === 'fulfilled' && addrs.value.length > 0) {
          setAddresses(addrs.value)
          const def = addrs.value.find((a) => a.isDefault) ?? addrs.value[0]
          setSavedAddressId(String(def.id))
        } else {
          setAddressMode('new')
        }
        if (svc.status === 'rejected') {
          setError(getErrorMessage(svc.reason))
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [serviceId])

  const resolvedAddress = useMemo(() => {
    if (addressMode === 'saved') {
      const a = addresses.find((x) => String(x.id) === savedAddressId)
      return a ? `${a.label}: ${a.street}, ${a.city}, ${a.state} ${a.zip}` : ''
    }
    return [newAddress.label, newAddress.street, newAddress.city, newAddress.state, newAddress.zip]
      .filter(Boolean)
      .join(', ')
  }, [addressMode, addresses, savedAddressId, newAddress])

  const selectedProvider = providers.find((p) => String(p.id) === providerId)

  const validate = () => {
    const next: FieldErrors = {}
    if (!providerId) next.provider = 'Please choose a provider'
    else if (selectedProvider?.availability !== 'AVAILABLE')
      next.provider = 'That provider is not available right now'
    if (!bookingDate) next.bookingDate = 'Please pick a date and time'
    else if (new Date(bookingDate).getTime() <= Date.now())
      next.bookingDate = 'Booking date must be in the future'
    if (addressMode === 'saved' && !savedAddressId) next.address = 'Please choose an address'
    if (addressMode === 'new') {
      if (!newAddress.label.trim()) next.newAddress = 'Label is required'
      if (!newAddress.street.trim()) next.newAddress = 'Street is required'
      if (!newAddress.city.trim()) next.newAddress = 'City is required'
      if (!newAddress.state.trim()) next.newAddress = 'State is required'
      if (!newAddress.zip.trim()) next.newAddress = 'ZIP code is required'
    }
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!service || !validate()) return
    setSubmitting(true)
    setActionError('')
    try {
      const booking = await createBooking({
        providerId: Number(providerId),
        serviceId: service.id,
        bookingDate: normalizeLocalDateTime(bookingDate),
        address: resolvedAddress || '',
      })
      setCreatedBooking(booking)
    } catch (err) {
      setActionError(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  const handlePay = async () => {
    if (!createdBooking) return
    setPaying(true)
    setActionError('')
    setPaymentSkipped(false)
    try {
      // 1) Create a Razorpay Order server-side
      const order = await createRazorpayOrder({
        bookingId: createdBooking.id,
        amount: createdBooking.servicePrice,
      })
      // 2) Open Razorpay Checkout with the returned order details
      await loadRazorpayCheckoutScript()
      const outcome = await openRazorpayCheckout({
        order,
        bookingId: createdBooking.id,
        prefill: { name: user?.fullName, email: user?.email },
      })

      if (outcome.status === 'success') {
        // 3) Verify the signature server-side — never trust the callback alone
        const verified = await verifyPayment({
          razorpayOrderId: outcome.razorpayOrderId,
          razorpayPaymentId: outcome.razorpayPaymentId,
          razorpaySignature: outcome.razorpaySignature,
        })
        setPayment(verified)
      } else if (outcome.status === 'cancelled') {
        // User closed the modal — booking stays confirmed, payment stays PENDING
        setPaymentSkipped(true)
      } else {
        setActionError(outcome.reason ?? 'Payment failed. Please try again.')
        setPaymentSkipped(true)
      }
    } catch (err) {
      setActionError(getErrorMessage(err))
    } finally {
      setPaying(false)
    }
  }

  if (loading) return <LoadingSpinner label="Preparing your booking…" />

  if (error || !service) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-20 text-center">
        <p className="text-lg font-medium text-rose-600">{error || 'Service not found'}</p>
        <Link
          to="/categories"
          className="mt-4 inline-block text-sm font-semibold text-indigo-600 hover:text-indigo-500"
        >
          ← Back to services
        </Link>
      </div>
    )
  }

  // ---------- Confirmation + payment state ----------
  if (createdBooking) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-10 sm:px-6 lg:px-8">
        <Card className="p-8">
          <div className="flex items-center gap-3">
            <span className="flex h-11 w-11 items-center justify-center rounded-full bg-emerald-100 text-xl">
              ✅
            </span>
            <div>
              <h1 className="text-xl font-bold text-gray-900">Booking confirmed!</h1>
              <p className="text-sm text-gray-500">
                Booking #{createdBooking.id} · {createdBooking.serviceName}
              </p>
            </div>
          </div>

          <dl className="mt-6 space-y-3 rounded-xl bg-gray-50 p-4 text-sm">
            <div className="flex justify-between">
              <dt className="text-gray-500">Provider</dt>
              <dd className="font-semibold text-gray-900">{createdBooking.providerName}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">Status</dt>
              <dd>
                <StatusBadge status={createdBooking.status} />
              </dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">Address</dt>
              <dd className="max-w-[60%] text-right text-gray-900">{createdBooking.address}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">Amount</dt>
              <dd className="font-bold text-gray-900">
                {formatCurrency(createdBooking.servicePrice)}
              </dd>
            </div>
          </dl>

          {/* Payment */}
          {payment ? (
            <div
              className={`mt-6 rounded-xl p-4 text-sm ${
                payment.status === 'SUCCESS'
                  ? 'bg-emerald-50 text-emerald-800'
                  : payment.status === 'FAILED'
                    ? 'bg-rose-50 text-rose-700'
                    : 'bg-amber-50 text-amber-800'
              }`}
            >
              <p className="font-semibold">
                Payment {payment.status === 'SUCCESS' ? 'successful' : payment.status.toLowerCase()} —
                {payment.method ? payment.method.toUpperCase() : 'Online'} ·{' '}
                {formatCurrency(payment.amount)}
              </p>
              <p className="mt-1 text-xs opacity-80">
                Payment #{payment.id} · {new Date(payment.createdAt).toLocaleString()}
              </p>
              {payment.razorpayPaymentId && (
                <p className="mt-1 text-xs opacity-80">
                  Razorpay payment ID: {payment.razorpayPaymentId}
                </p>
              )}
            </div>
          ) : (
            <div className="mt-6">
              <h2 className="text-base font-semibold text-gray-900">Pay now</h2>
              <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center">
                <Button onClick={handlePay} loading={paying}>
                  Pay {formatCurrency(createdBooking.servicePrice)}
                </Button>
                <p className="text-xs text-gray-400">
                  Secure payment via Razorpay (test mode — no real charge).
                </p>
              </div>
              {paymentSkipped && (
                <div className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
                  Payment not completed — no charge was made. Your booking stays confirmed and
                  you can retry the payment anytime.
                </div>
              )}
            </div>
          )}

          {actionError && (
            <div className="mt-4 rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
              {actionError}
            </div>
          )}

          <div className="mt-8 flex flex-wrap gap-3">
            <Link to="/bookings">
              <Button variant="primary">View my bookings</Button>
            </Link>
            <Link to="/categories">
              <Button variant="secondary">Book another service</Button>
            </Link>
          </div>
        </Card>
      </div>
    )
  }

  // ---------- Booking form ----------
  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        {/* Service summary */}
        <Card className="h-fit p-6 lg:sticky lg:top-24">
          <span className="inline-flex rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700">
            {service.categoryName}
          </span>
          <h1 className="mt-3 text-2xl font-bold text-gray-900">{service.name}</h1>
          <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-gray-500">
            {service.description}
          </p>
          <div className="mt-4 flex items-baseline gap-1">
            <span className="text-2xl font-bold text-gray-900">
              {formatCurrency(service.basePrice)}
            </span>
            <span className="text-sm text-gray-500">/ visit</span>
          </div>
          <div className="mt-4 border-t border-gray-100 pt-4 text-sm text-gray-500">
            ⏱ ~{service.durationMinutes} minutes
          </div>
        </Card>

        {/* Booking form */}
        <Card className="p-6 lg:col-span-2">
          {actionError && (
            <div className="mb-4 rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
              {actionError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            <Field
              label="Provider"
              error={errors.provider}
              hint={selectedProvider ? undefined : 'No verified providers available yet'}
            >
              <Select value={providerId} onChange={(e) => setProviderId(e.target.value)}>
                <option value="">Choose a provider…</option>
                {providers.map((provider) => (
                  <option key={provider.id} value={provider.id} disabled={provider.availability !== 'AVAILABLE'}>
                    {provider.name} · {provider.experienceYears} yrs ·{' '}
                    {provider.availability === 'AVAILABLE' ? 'Available' : provider.availability}
                    {provider.skills.length > 0 ? ` · ${provider.skills.join(', ')}` : ''}
                  </option>
                ))}
              </Select>
            </Field>

            <Field label="When?" error={errors.bookingDate}>
              <TextInput
                type="datetime-local"
                min={minDate}
                value={bookingDate}
                onChange={(e) => setBookingDate(e.target.value)}
              />
            </Field>

            <Field label="Address" error={errors.address}>
              {addresses.length > 0 ? (
                <div className="space-y-2">
                  <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-gray-200 px-3 py-2 text-sm">
                    <input
                      type="radio"
                      name="addressMode"
                      checked={addressMode === 'saved'}
                      onChange={() => setAddressMode('saved')}
                    />
                    Use a saved address
                  </label>
                  {addressMode === 'saved' && (
                    <Select value={savedAddressId} onChange={(e) => setSavedAddressId(e.target.value)}>
                      {addresses.map((address) => (
                        <option key={address.id} value={address.id}>
                          {address.label}: {address.street}, {address.city}, {address.state} {address.zip}
                          {address.isDefault ? ' (default)' : ''}
                        </option>
                      ))}
                    </Select>
                  )}
                  <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-gray-200 px-3 py-2 text-sm">
                    <input
                      type="radio"
                      name="addressMode"
                      checked={addressMode === 'new'}
                      onChange={() => setAddressMode('new')}
                    />
                    Enter a new address
                  </label>
                </div>
              ) : (
                <p className="text-sm text-gray-500">
                  No saved addresses yet — add a new one below.
                </p>
              )}
            </Field>

            {addressMode === 'new' && (
              <div className="space-y-4 rounded-xl bg-gray-50 p-4">
                <div className="flex justify-end">
                  <LocationPinButton
                    onLocated={({ address }) =>
                      setNewAddress((prev) => ({
                        ...prev,
                        street: address.street || prev.street,
                        city: address.city || prev.city,
                        state: address.state || prev.state,
                        zip: address.zip || prev.zip,
                      }))
                    }
                  />
                </div>
                <Field label="Address label" error={errors.newAddress}>
                  <TextInput
                    placeholder="e.g. Home, Office"
                    value={newAddress.label}
                    onChange={(e) => setNewAddress({ ...newAddress, label: e.target.value })}
                  />
                </Field>
                <Field label="Street">
                  <TextInput
                    placeholder="123 Main Street"
                    value={newAddress.street}
                    onChange={(e) => setNewAddress({ ...newAddress, street: e.target.value })}
                  />
                </Field>
                <div className="grid grid-cols-2 gap-4">
                  <Field label="City">
                    <TextInput
                      placeholder="City"
                      value={newAddress.city}
                      onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
                    />
                  </Field>
                  <Field label="State">
                    <TextInput
                      placeholder="State"
                      value={newAddress.state}
                      onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
                    />
                  </Field>
                </div>
                <Field label="ZIP code">
                  <TextInput
                    placeholder="ZIP"
                    value={newAddress.zip}
                    onChange={(e) => setNewAddress({ ...newAddress, zip: e.target.value })}
                  />
                </Field>
              </div>
            )}

            <div className="flex items-center justify-between border-t border-gray-100 pt-5">
              <span className="text-sm text-gray-500">
                You&apos;ll pay {formatCurrency(service.basePrice)} after confirmation
              </span>
              <Button type="submit" size="lg" loading={submitting}>
                Confirm booking
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  )
}
