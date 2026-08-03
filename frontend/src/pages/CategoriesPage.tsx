import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getCategories, searchServices } from '../services/catalog'
import { getErrorMessage } from '../services/api'
import Card from '../components/Card'
import EmptyState from '../components/EmptyState'
import LoadingSpinner from '../components/LoadingSpinner'
import { TextInput } from '../components/FormField'
import { formatCurrency } from '../utils/format'
import type { Category, ServiceItem } from '../types'

const iconMap: Record<string, string> = {
  wrench: '🔧',
  zap: '⚡',
  sparkles: '✨',
  paintbrush: '🎨',
  snowflake: '❄️',
  hammer: '🔨',
}

export default function CategoriesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const activeCategory = searchParams.get('category')

  const [categories, setCategories] = useState<Category[]>([])
  const [services, setServices] = useState<ServiceItem[]>([])
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    Promise.all([getCategories(), searchServices()])
      .then(([cats, svcs]) => {
        if (cancelled) return
        setCategories(cats)
        setServices(svcs)
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

  const filtered = useMemo(() => {
    const cat = activeCategory ? Number(activeCategory) : null
    const kw = keyword.trim().toLowerCase()
    return services.filter((s) => {
      if (cat && s.categoryId !== cat) return false
      if (kw) {
        const haystack = `${s.name} ${s.description} ${s.categoryName}`.toLowerCase()
        if (!haystack.includes(kw)) return false
      }
      return true
    })
  }, [services, activeCategory, keyword])

  const activeCategoryName =
    categories.find((c) => String(c.id) === activeCategory)?.name ?? 'All services'

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Services</h1>
        <p className="mt-1 text-sm text-gray-500">
          Filter by category or search for a service
        </p>
      </div>

      <div className="mb-6 flex max-w-md">
        <TextInput
          type="search"
          placeholder="Search services…"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
      </div>

      {/* Category pills */}
      <div className="mb-8 flex flex-wrap gap-2">
        <button
          onClick={() => setSearchParams({})}
          className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
            !activeCategory
              ? 'bg-indigo-600 text-white shadow-sm'
              : 'bg-white text-gray-600 ring-1 ring-inset ring-gray-300 hover:bg-gray-50'
          }`}
        >
          All
        </button>
        {categories.map((category) => (
          <button
            key={category.id}
            onClick={() => setSearchParams({ category: String(category.id) })}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
              activeCategory === String(category.id)
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'bg-white text-gray-600 ring-1 ring-inset ring-gray-300 hover:bg-gray-50'
            }`}
          >
            {iconMap[category.icon] ?? '🛠️'} {category.name}
          </button>
        ))}
      </div>

      <h2 className="mb-4 text-lg font-semibold text-gray-900">
        {activeCategoryName} {keyword && `· “${keyword}”`}
        <span className="ml-2 text-sm font-normal text-gray-400">{filtered.length} results</span>
      </h2>

      {loading ? (
        <LoadingSpinner label="Loading services…" />
      ) : error ? (
        <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-center text-rose-700">
          {error}
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState
          icon="🔍"
          title="No services found"
          description="Try a different category or search term."
        />
      ) : (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((service) => (
            <Link key={service.id} to={`/services/${service.id}`} className="group">
              <Card className="flex h-full flex-col p-6 transition-all duration-200 group-hover:-translate-y-1 group-hover:border-indigo-200 group-hover:shadow-lg">
                <div className="flex items-start justify-between gap-3">
                  <h3 className="text-lg font-semibold text-gray-900 group-hover:text-indigo-700">
                    {service.name}
                  </h3>
                  <span className="shrink-0 rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-medium text-indigo-700">
                    {service.categoryName}
                  </span>
                </div>
                <p className="mt-2 line-clamp-3 flex-1 text-sm leading-relaxed text-gray-500">
                  {service.description}
                </p>
                <div className="mt-4 flex items-center justify-between border-t border-gray-100 pt-4">
                  <span className="text-lg font-bold text-gray-900">
                    {formatCurrency(service.basePrice)}
                  </span>
                  <span className="text-xs text-gray-400">~{service.durationMinutes} min</span>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
