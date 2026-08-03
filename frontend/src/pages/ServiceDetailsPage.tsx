import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getServiceById } from '../services/catalog'
import { getErrorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Card from '../components/Card'
import Button from '../components/Button'
import LoadingSpinner from '../components/LoadingSpinner'
import { formatCurrency } from '../utils/format'
import type { ServiceItem } from '../types'

export default function ServiceDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  const [service, setService] = useState<ServiceItem | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    let cancelled = false
    getServiceById(id)
      .then((data) => {
        if (!cancelled) setService(data)
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  const handleBookNow = () => {
    navigate(`/booking/${id}`)
  }

  if (loading) return <LoadingSpinner label="Loading service…" />

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

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <nav className="mb-6 text-sm text-gray-500">
        <Link to="/" className="hover:text-indigo-600">
          Home
        </Link>
        <span className="mx-2">/</span>
        <Link to="/categories" className="hover:text-indigo-600">
          Categories
        </Link>
        <span className="mx-2">/</span>
        <Link to={`/categories?category=${service.categoryId}`} className="hover:text-indigo-600">
          {service.categoryName}
        </Link>
        <span className="mx-2">/</span>
        <span className="text-gray-900">{service.name}</span>
      </nav>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        <Card className="p-8 lg:col-span-2">
          <span className="inline-flex rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700">
            {service.categoryName}
          </span>
          <h1 className="mt-3 text-3xl font-bold text-gray-900">{service.name}</h1>
          <p className="mt-4 text-base leading-relaxed text-gray-600">{service.description}</p>

          <dl className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div className="rounded-xl bg-gray-50 p-4">
              <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">Price</dt>
              <dd className="mt-1 text-xl font-bold text-gray-900">
                {formatCurrency(service.basePrice)}
              </dd>
            </div>
            <div className="rounded-xl bg-gray-50 p-4">
              <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">Duration</dt>
              <dd className="mt-1 text-xl font-bold text-gray-900">~{service.durationMinutes} min</dd>
            </div>
            <div className="rounded-xl bg-gray-50 p-4">
              <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">Category</dt>
              <dd className="mt-1 text-xl font-bold text-gray-900">{service.categoryName}</dd>
            </div>
          </dl>
        </Card>

        <Card className="h-fit p-6 lg:sticky lg:top-24">
          <h2 className="text-lg font-semibold text-gray-900">Book this service</h2>
          <p className="mt-2 text-sm leading-relaxed text-gray-500">
            Choose a date, a verified provider and your address. The provider will confirm shortly
            after you book.
          </p>
          <div className="mt-4 flex items-baseline gap-1">
            <span className="text-2xl font-bold text-gray-900">
              {formatCurrency(service.basePrice)}
            </span>
            <span className="text-sm text-gray-500">/ visit</span>
          </div>
          <Button className="mt-5 w-full" size="lg" onClick={handleBookNow}>
            Book now
          </Button>
          {!isAuthenticated && (
            <p className="mt-3 text-center text-xs text-gray-400">
              You&apos;ll be asked to log in before booking.
            </p>
          )}
        </Card>
      </div>
    </div>
  )
}
