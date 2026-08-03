import { useCallback, useEffect, useState } from 'react'
import {
  getAdminBookings,
  getAdminCustomers,
  getAdminProviders,
  getDashboard,
  verifyProvider,
} from '../services/admin'
import { getErrorMessage } from '../services/api'
import Card from '../components/Card'
import Button from '../components/Button'
import StatusBadge from '../components/StatusBadge'
import LoadingSpinner from '../components/LoadingSpinner'
import { formatCurrency, formatDate } from '../utils/format'
import type { AdminBooking, AdminCustomer, AdminProvider, Dashboard } from '../types'

const statusOrder = ['PENDING', 'ACCEPTED', 'ON_THE_WAY', 'STARTED', 'COMPLETED', 'CANCELLED']

export default function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [customers, setCustomers] = useState<AdminCustomer[]>([])
  const [providers, setProviders] = useState<AdminProvider[]>([])
  const [bookings, setBookings] = useState<AdminBooking[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [verifyBusy, setVerifyBusy] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [dash, custs, provs, book] = await Promise.all([
        getDashboard(),
        getAdminCustomers(),
        getAdminProviders(),
        getAdminBookings(),
      ])
      setDashboard(dash)
      setCustomers(custs)
      setProviders(provs)
      setBookings(book)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const handleVerify = async (id: number) => {
    setVerifyBusy(id)
    setError('')
    try {
      await verifyProvider(id)
      setProviders((prev) =>
        prev.map((p) => (p.id === id ? { ...p, verificationStatus: 'VERIFIED' } : p)),
      )
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setVerifyBusy(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading admin dashboard…" />

  if (error) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-20 text-center">
        <p className="text-lg font-medium text-rose-600">{error}</p>
      </div>
    )
  }

  const stats = [
    { label: 'Customers', value: String(dashboard?.totalCustomers ?? 0), icon: '👥' },
    { label: 'Providers', value: String(dashboard?.totalProviders ?? 0), icon: '🧑‍🔧' },
    { label: 'Revenue', value: formatCurrency(dashboard?.totalRevenue ?? 0), icon: '💰' },
    {
      label: 'Total bookings',
      value: String(
        Object.values(dashboard?.bookingsByStatus ?? {}).reduce((a, b) => a + b, 0),
      ),
      icon: '📅',
    },
  ]

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="mb-8 flex items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Admin dashboard</h1>
          <p className="mt-1 text-sm text-gray-500">
            Live overview aggregated from all services
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={load}>
          ↻ Refresh
        </Button>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {stats.map((stat) => (
          <Card key={stat.label} className="p-5">
            <div className="flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 text-xl">
                {stat.icon}
              </span>
              <div>
                <p className="text-xs font-medium uppercase tracking-wide text-gray-500">
                  {stat.label}
                </p>
                <p className="text-xl font-bold text-gray-900">{stat.value}</p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Booking status breakdown */}
      <Card className="mt-6 p-5">
        <h2 className="mb-3 text-sm font-semibold text-gray-900">Bookings by status</h2>
        <div className="flex flex-wrap gap-2">
          {statusOrder.map((status) => {
            const count = dashboard?.bookingsByStatus[status] ?? 0
            return (
              <span key={status} className="inline-flex items-center gap-2">
                <StatusBadge status={status} />
                <span className="text-sm font-semibold text-gray-700">{count}</span>
              </span>
            )
          })}
        </div>
      </Card>

      {/* Providers */}
      <section className="mt-10">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          Providers{' '}
          <span className="text-sm font-normal text-gray-400">({providers.length})</span>
        </h2>
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">Name</th>
                  <th className="hidden px-4 py-3 md:table-cell">Skills</th>
                  <th className="hidden px-4 py-3 sm:table-cell">Availability</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {providers.map((provider) => (
                  <tr key={provider.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-500">#{provider.id}</td>
                    <td className="px-4 py-3 font-medium text-gray-900">{provider.name}</td>
                    <td className="hidden px-4 py-3 text-gray-500 md:table-cell">
                      {provider.skills.slice(0, 3).join(', ') || '—'}
                    </td>
                    <td className="hidden px-4 py-3 text-gray-500 sm:table-cell">
                      {provider.availability}
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={provider.verificationStatus} />
                    </td>
                    <td className="px-4 py-3">
                      {provider.verificationStatus === 'PENDING' ? (
                        <Button
                          size="sm"
                          loading={verifyBusy === provider.id}
                          onClick={() => handleVerify(provider.id)}
                        >
                          Verify
                        </Button>
                      ) : (
                        <span className="text-xs text-gray-400">—</span>
                      )}
                    </td>
                  </tr>
                ))}
                {providers.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                      No providers registered yet
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </Card>
      </section>

      {/* Customers */}
      <section className="mt-10">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          Customers{' '}
          <span className="text-sm font-normal text-gray-400">({customers.length})</span>
        </h2>
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">Name</th>
                  <th className="hidden px-4 py-3 sm:table-cell">Email</th>
                  <th className="hidden px-4 py-3 md:table-cell">Phone</th>
                  <th className="px-4 py-3">Joined</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {customers.map((customer) => (
                  <tr key={customer.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-500">#{customer.id}</td>
                    <td className="px-4 py-3 font-medium text-gray-900">{customer.fullName}</td>
                    <td className="hidden px-4 py-3 text-gray-500 sm:table-cell">
                      {customer.email}
                    </td>
                    <td className="hidden px-4 py-3 text-gray-500 md:table-cell">
                      {customer.phone}
                    </td>
                    <td className="px-4 py-3 text-gray-500">{formatDate(customer.createdAt)}</td>
                  </tr>
                ))}
                {customers.length === 0 && (
                  <tr>
                    <td colSpan={5} className="px-4 py-8 text-center text-gray-400">
                      No customers yet
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </Card>
      </section>

      {/* Bookings */}
      <section className="mt-10">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          Bookings{' '}
          <span className="text-sm font-normal text-gray-400">({bookings.length})</span>
        </h2>
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">Service</th>
                  <th className="hidden px-4 py-3 md:table-cell">Customer</th>
                  <th className="hidden px-4 py-3 lg:table-cell">Provider</th>
                  <th className="px-4 py-3">Price</th>
                  <th className="px-4 py-3">When</th>
                  <th className="px-4 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {bookings.map((booking) => (
                  <tr key={booking.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-500">#{booking.id}</td>
                    <td className="px-4 py-3 font-medium text-gray-900">{booking.serviceName}</td>
                    <td className="hidden px-4 py-3 text-gray-500 md:table-cell">
                      {booking.customerName || '—'}
                    </td>
                    <td className="hidden px-4 py-3 text-gray-500 lg:table-cell">
                      {booking.providerName || '—'}
                    </td>
                    <td className="px-4 py-3 text-gray-900">
                      {formatCurrency(booking.servicePrice)}
                    </td>
                    <td className="px-4 py-3 text-gray-500">{formatDate(booking.bookingDate)}</td>
                    <td className="px-4 py-3">
                      <StatusBadge status={booking.status} />
                    </td>
                  </tr>
                ))}
                {bookings.length === 0 && (
                  <tr>
                    <td colSpan={7} className="px-4 py-8 text-center text-gray-400">
                      No bookings yet
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </Card>
      </section>
    </div>
  )
}
