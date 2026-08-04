import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { Role } from '../types'

interface ProtectedRouteProps {
  children: ReactNode
  roles?: Role[]
}

export default function ProtectedRoute({ children, roles }: ProtectedRouteProps) {
  const { isAuthenticated, user, token } = useAuth()
  const location = useLocation()

  console.log("====== Protected Route ======")
  console.log("Path:", location.pathname)
  console.log("Token:", token)
  console.log("User:", user)
  console.log("isAuthenticated:", isAuthenticated)

  if (!isAuthenticated) {
    console.log("Redirecting to Login")
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  if (roles && user && !roles.includes(user.role)) {
    console.log("Redirecting to Unauthorized")
    return <Navigate to="/unauthorized" replace />
  }

  console.log("Access Granted")
  return <>{children}</>
}