import { Link } from 'react-router-dom'
import Button from '../components/Button'

export default function UnauthorizedPage() {
  return (
    <div className="mx-auto flex max-w-7xl flex-col items-center justify-center px-4 py-24 text-center">
      <p className="text-7xl font-extrabold text-rose-200">403</p>
      <h1 className="mt-4 text-2xl font-bold text-gray-900">Not authorized</h1>
      <p className="mt-2 max-w-sm text-sm text-gray-500">
        Your account doesn&apos;t have permission to view this page.
      </p>
      <Link to="/" className="mt-6">
        <Button>Back to home</Button>
      </Link>
    </div>
  )
}
