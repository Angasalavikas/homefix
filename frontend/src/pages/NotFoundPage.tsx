import { Link } from 'react-router-dom'
import Button from '../components/Button'

export default function NotFoundPage() {
  return (
    <div className="mx-auto flex max-w-7xl flex-col items-center justify-center px-4 py-24 text-center">
      <p className="text-7xl font-extrabold text-indigo-200">404</p>
      <h1 className="mt-4 text-2xl font-bold text-gray-900">Page not found</h1>
      <p className="mt-2 max-w-sm text-sm text-gray-500">
        The page you&apos;re looking for doesn&apos;t exist or has moved.
      </p>
      <Link to="/" className="mt-6">
        <Button>Back to home</Button>
      </Link>
    </div>
  )
}
