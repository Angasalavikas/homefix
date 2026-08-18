import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { cancelBooking, listBookings } from '../services/booking'
import { getErrorMessage } from '../services/api'
import Card from '../components/Card'
import Button from '../components/Button'
import StatusBadge from '../components/StatusBadge'
import EmptyState from '../components/EmptyState'
import LoadingSpinner from '../components/LoadingSpinner'
import { formatCurrency, formatDateTime } from '../utils/format'
import type { Booking } from '../types'

export default function BookingHistoryPage() {
  const [bookings, setBookings] = useState<Booking[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await listBookings('customer')
      setBookings(data)
    } catch (err) {
      // Surface the real failure — an empty array must NOT be confused with
      // an empty account. Log the raw error (status + body) for debugging.
      const status = (err as { response?: { status?: number } })?.response?.status
      const body = (err as { response?: { data?: unknown } })?.response?.data
      console.error(
        '[My Bookings] GET /bookings failed:',
        status != null ? `HTTP ${status}` : 'no response',
        body ?? (err instanceof Error ? err.message : err),
      )
      setBookings([])
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const handleCancel = async (id: number) => {
    setBusyId(id)
    setError('')
    try {
      const updated = await cancelBooking(id)
      setBookings((prev) => prev.map((b) => (b.id === id ? updated : b)))
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading your bookings…" />

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="mb-8 flex items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">My bookings</h1>
          <p className="mt-1 text-sm text-gray-500">
            Live status pulled from the booking service
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={load}>
          ↻ Refresh
        </Button>
      </div>

      {/* Error and empty states are mutually exclusive: a failed request shows
          ONLY the error (with Retry), never the "no bookings" empty state. */}
      {error ? (
        <Card className="p-8 text-center">
          <p className="text-base font-semibold text-gray-900">
            We couldn't load your bookings
          </p>
          <p className="mt-2 text-sm text-gray-500">{error}</p>
          <Button variant="secondary" size="sm" className="mt-4" onClick={load}>
            ↻ Retry
          </Button>
        </Card>
      ) : bookings.length === 0 ? (
        <EmptyState
          icon="📅"
          title="No bookings yet"
          description="Browse the catalog and book your first home service."
          action={
            <Link to="/categories">
              <Button>Browse services</Button>
            </Link>
          }
        />
      ) : (
        <div className="space-y-4">
          {bookings.map((booking) => {
            const cancellable = booking.status === 'PENDING' || booking.status === 'ACCEPTED'
            return (
              <Card key={booking.id} className="p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-3">
                      <h2 className="text-lg font-semibold text-gray-900">{booking.serviceName}</h2>
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

                <dl className="mt-4 grid grid-cols-1 gap-2 text-sm text-gray-600 sm:grid-cols-2">
                  <div className="flex gap-2">
                    <dt className="font-medium text-gray-500">Provider:</dt>
                    <dd>{booking.providerName || '—'}</dd>
                  </div>
                  <div className="flex gap-2">
                    <dt className="font-medium text-gray-500">Address:</dt>
                    <dd className="truncate">{booking.address}</dd>
                  </div>
                </dl>

                {cancellable && (
                  <div className="mt-4 border-t border-gray-100 pt-4">
                    <Button
                      variant="danger"
                      size="sm"
                      loading={busyId === booking.id}
                      onClick={() => handleCancel(booking.id)}
                    >
                      Cancel booking
                    </Button>
                  </div>
                )}
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}
