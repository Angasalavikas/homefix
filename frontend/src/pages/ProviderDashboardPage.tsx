import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { listBookings, updateBookingStatus } from '../services/booking'
import { getMyProviderProfile } from '../services/provider'
import { getErrorMessage } from '../services/api'
import Card from '../components/Card'
import Button from '../components/Button'
import StatusBadge from '../components/StatusBadge'
import EmptyState from '../components/EmptyState'
import LoadingSpinner from '../components/LoadingSpinner'
import { formatCurrency, formatDateTime } from '../utils/format'
import type { Booking, BookingStatus, Provider } from '../types'

const activeStatuses: BookingStatus[] = ['ACCEPTED', 'ON_THE_WAY', 'STARTED']

export default function ProviderDashboardPage() {
  const [provider, setProvider] = useState<Provider | null>(null)
  const [providerMissing, setProviderMissing] = useState(false)
  const [bookings, setBookings] = useState<Booking[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  // Build the profile line only from fields that are actually present — never
  // render "· yrs experience · ·" while the profile is missing or loading.
  const profileMeta = useMemo(() => {
    if (!provider) return null
    return [
      provider.name,
      provider.experienceYears != null ? `${provider.experienceYears} yrs experience` : null,
      provider.verificationStatus ? provider.verificationStatus.toLowerCase() : null,
      provider.availability ? provider.availability.toLowerCase() : null,
    ]
      .filter((part): part is string => Boolean(part))
      .join(' · ')
  }, [provider])

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      // Fetch profile and bookings independently: a bookings failure must not
      // blank out the provider header (and vice versa).
      const [profResult, bookResult] = await Promise.allSettled([
        getMyProviderProfile(),
        listBookings('provider'),
      ])

      if (profResult.status === 'fulfilled') {
        setProvider(profResult.value)
      } else {
        // A 404 (provider profile missing) shows the register prompt instead of an error banner.
        const status = (profResult.reason as { response?: { status?: number } })?.response?.status
        if (status === 404) {
          setProviderMissing(true)
        } else {
          console.error('[Provider Dashboard] GET /providers/me failed:', profResult.reason)
          setError(getErrorMessage(profResult.reason))
        }
      }

      if (bookResult.status === 'fulfilled') {
        setBookings(bookResult.value)
      } else {
        console.error('[Provider Dashboard] GET /bookings failed:', bookResult.reason)
        setBookings([])
        setError(getErrorMessage(bookResult.reason))
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const handleStatus = async (id: number, status: BookingStatus) => {
    setBusyId(id)
    setError('')
    try {
      const updated = await updateBookingStatus(id, status)
      setBookings((prev) => prev.map((b) => (b.id === id ? updated : b)))
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading provider dashboard…" />

  if (providerMissing) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-20">
        <EmptyState
          icon="🧑‍🔧"
          title="Register as a provider first"
          description="You need a provider profile to receive and manage bookings."
          action={
            <Link to="/profile">
              <Button>Go to profile</Button>
            </Link>
          }
        />
      </div>
    )
  }

  const newRequests = bookings.filter((b) => b.status === 'PENDING')
  const active = bookings.filter((b) => activeStatuses.includes(b.status))
  // History is purely lifecycle-driven — a paid booking stays in New Requests
  // (or In progress) until the provider advances its status to COMPLETED.
  const past = bookings.filter((b) => b.status === 'COMPLETED' || b.status === 'CANCELLED')

  const nextAction = (status: BookingStatus): { label: string; next: BookingStatus } | null => {
    switch (status) {
      case 'PENDING':
        return { label: 'Accept', next: 'ACCEPTED' }
      case 'ACCEPTED':
        return { label: 'On the way', next: 'ON_THE_WAY' }
      case 'ON_THE_WAY':
        return { label: 'Start work', next: 'STARTED' }
      case 'STARTED':
        return { label: 'Mark complete', next: 'COMPLETED' }
      default:
        return null
    }
  }

  const renderBookingRow = (booking: Booking) => {
    const action = nextAction(booking.status)
    return (
      <Card key={booking.id} className="p-5">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <div className="flex items-center gap-3">
              <h3 className="text-lg font-semibold text-gray-900">{booking.serviceName}</h3>
              <StatusBadge status={booking.status} />
              <StatusBadge status={booking.paymentStatus} />
            </div>
            <p className="mt-1 text-sm text-gray-500">
              Booking #{booking.id} · {formatDateTime(booking.bookingDate)}
            </p>
          </div>
          <span className="text-lg font-bold text-gray-900">
            {formatCurrency(booking.servicePrice)}
          </span>
        </div>
        <dl className="mt-3 grid grid-cols-1 gap-2 text-sm text-gray-600 sm:grid-cols-2">
          <div className="flex gap-2">
            <dt className="font-medium text-gray-500">Customer:</dt>
            <dd>{booking.customerName || '—'}</dd>
          </div>
          <div className="flex gap-2">
            <dt className="font-medium text-gray-500">Address:</dt>
            <dd className="truncate">{booking.address}</dd>
          </div>
        </dl>
        {action && (
          <div className="mt-4 border-t border-gray-100 pt-4">
            <Button
              size="sm"
              variant={booking.status === 'PENDING' ? 'primary' : 'secondary'}
              loading={busyId === booking.id}
              onClick={() => handleStatus(booking.id, action.next)}
            >
              {action.label}
            </Button>
          </div>
        )}
      </Card>
    )
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="mb-8 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Provider dashboard</h1>
          {profileMeta && <p className="mt-1 text-sm text-gray-500">{profileMeta}</p>}
        </div>
        <Button variant="secondary" size="sm" onClick={load}>
          ↻ Refresh
        </Button>
      </div>

      {error && (
        <div className="mb-4 rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
          {error}
        </div>
      )}

      <section>
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          New requests{' '}
          <span className="text-sm font-normal text-gray-400">({newRequests.length})</span>
        </h2>
        {newRequests.length === 0 ? (
          <p className="rounded-xl border border-dashed border-gray-300 bg-white/70 px-4 py-8 text-center text-sm text-gray-500">
            No new booking requests 🎉
          </p>
        ) : (
          <div className="space-y-4">{newRequests.map(renderBookingRow)}</div>
        )}
      </section>

      <section className="mt-10">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          In progress{' '}
          <span className="text-sm font-normal text-gray-400">({active.length})</span>
        </h2>
        {active.length === 0 ? (
          <p className="rounded-xl border border-dashed border-gray-300 bg-white/70 px-4 py-8 text-center text-sm text-gray-500">
            Nothing in progress
          </p>
        ) : (
          <div className="space-y-4">{active.map(renderBookingRow)}</div>
        )}
      </section>

      {past.length > 0 && (
        <section className="mt-10">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">
            History <span className="text-sm font-normal text-gray-400">({past.length})</span>
          </h2>
          <div className="space-y-4">{past.map(renderBookingRow)}</div>
        </section>
      )}
    </div>
  )
}
