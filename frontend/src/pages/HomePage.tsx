import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCategories } from '../services/catalog'
import { getErrorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Card from '../components/Card'
import LoadingSpinner from '../components/LoadingSpinner'
import type { Category } from '../types'

const iconMap: Record<string, string> = {
  wrench: '🔧',
  zap: '⚡',
  sparkles: '✨',
  paintbrush: '🎨',
  snowflake: '❄️',
  hammer: '🔨',
}

function categoryIcon(icon: string): string {
  return iconMap[icon] ?? '🛠️'
}

export default function HomePage() {
  const { isAuthenticated, user } = useAuth()
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    getCategories()
      .then((data) => {
        if (!cancelled) setCategories(data)
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
  }, [])

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-indigo-600 via-indigo-500 to-violet-600">
        <div className="pointer-events-none absolute -right-24 -top-24 h-72 w-72 rounded-full bg-white/10 blur-2xl" />
        <div className="pointer-events-none absolute -bottom-32 -left-16 h-80 w-80 rounded-full bg-violet-400/20 blur-3xl" />
        <div className="relative mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8 lg:py-28">
          <div className="max-w-2xl">
            <span className="inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-1.5 text-sm font-medium text-indigo-50 ring-1 ring-inset ring-white/25 backdrop-blur">
              <span className="h-2 w-2 animate-pulse rounded-full bg-emerald-300" />
              Trusted home service pros
            </span>
            <h1 className="mt-6 text-4xl font-extrabold tracking-tight text-white sm:text-5xl lg:text-6xl">
              Home repairs, <span className="text-indigo-100">fixed in a tap.</span>
            </h1>
            <p className="mt-4 text-lg leading-relaxed text-indigo-100">
              Book verified plumbers, electricians, cleaners, painters, AC technicians and carpenters
              — all from your phone.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link
                to="/categories"
                className="rounded-xl bg-white px-6 py-3 text-base font-semibold text-indigo-700 shadow-lg transition-all hover:-translate-y-0.5 hover:shadow-xl"
              >
                Browse services
              </Link>
              {isAuthenticated ? (
                <Link
                  to={user?.role === 'CUSTOMER' ? '/bookings' : '/profile'}
                  className="rounded-xl bg-indigo-800/60 px-6 py-3 text-base font-semibold text-white ring-1 ring-inset ring-white/30 transition-colors hover:bg-indigo-800/80"
                >
                  Go to dashboard
                </Link>
              ) : (
                <Link
                  to="/register"
                  className="rounded-xl bg-indigo-800/60 px-6 py-3 text-base font-semibold text-white ring-1 ring-inset ring-white/30 transition-colors hover:bg-indigo-800/80"
                >
                  Create an account
                </Link>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Categories */}
      <section className="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
        <div className="mb-8 flex items-end justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">What do you need done?</h2>
            <p className="mt-1 text-sm text-gray-500">
              Pick a category to see available services and pricing
            </p>
          </div>
          <Link
            to="/categories"
            className="hidden shrink-0 text-sm font-semibold text-indigo-600 hover:text-indigo-500 sm:block"
          >
            View all →
          </Link>
        </div>

        {loading ? (
          <LoadingSpinner label="Loading categories…" />
        ) : error ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-center text-rose-700">
            Couldn&apos;t load categories. {error}
            <p className="mt-1 text-sm text-rose-600">Is the backend running?</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {categories.map((category) => (
              <Link key={category.id} to={`/categories?category=${category.id}`} className="group">
                <Card className="flex h-full items-start gap-4 p-6 transition-all duration-200 group-hover:-translate-y-1 group-hover:border-indigo-200 group-hover:shadow-lg">
                  <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-2xl transition-transform duration-200 group-hover:scale-110">
                    {categoryIcon(category.icon)}
                  </span>
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 group-hover:text-indigo-700">
                      {category.name}
                    </h3>
                    <p className="mt-1 line-clamp-2 text-sm leading-relaxed text-gray-500">
                      {category.description}
                    </p>
                    <span className="mt-3 inline-block text-sm font-semibold text-indigo-600 opacity-0 transition-opacity duration-200 group-hover:opacity-100">
                      Explore services →
                    </span>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
