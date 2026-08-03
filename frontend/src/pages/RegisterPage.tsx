import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { register } from '../services/auth'
import { getErrorMessage } from '../services/api'
import Button from '../components/Button'
import { Field, Select, TextInput } from '../components/FormField'
import Card from '../components/Card'
import type { Role } from '../types'

interface FieldErrors {
  fullName?: string
  email?: string
  phone?: string
  password?: string
  confirm?: string
}

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PHONE_RE = /^\+?[1-9]\d{1,14}$/
const PASSWORD_RE = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/

export default function RegisterPage() {
  const { login: loginContext } = useAuth()
  const navigate = useNavigate()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [role, setRole] = useState<Role>('CUSTOMER')
  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState('')
  const [loading, setLoading] = useState(false)

  const validate = () => {
    const next: FieldErrors = {}
    if (fullName.trim().length < 2) next.fullName = 'Full name must be at least 2 characters'
    if (!email.trim()) next.email = 'Email is required'
    else if (!EMAIL_RE.test(email.trim())) next.email = 'Enter a valid email address'
    if (!phone.trim()) next.phone = 'Phone number is required'
    else if (!PHONE_RE.test(phone.trim()))
      next.phone = 'Use E.164 format, e.g. +1234567890'
    if (password.length < 8) next.password = 'Password must be at least 8 characters'
    else if (!PASSWORD_RE.test(password))
      next.password = 'Needs 1 digit, 1 lowercase, 1 uppercase, and 1 special character (@#$%^&+=!)'
    if (confirm !== password) next.confirm = 'Passwords do not match'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    setFormError('')
    try {
      const auth = await register({
        fullName: fullName.trim(),
        email: email.trim(),
        phone: phone.trim(),
        password,
        role,
      })
      loginContext(auth)
      navigate(role === 'PROVIDER' ? '/provider-dashboard' : '/', { replace: true })
    } catch (err) {
      setFormError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto flex max-w-7xl items-center justify-center px-4 py-16 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        <Card className="p-8">
          <div className="mb-6 text-center">
            <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600 text-2xl text-white shadow">
              🔧
            </span>
            <h1 className="mt-4 text-2xl font-bold text-gray-900">Create your account</h1>
            <p className="mt-1 text-sm text-gray-500">Join HomeFix in under a minute</p>
          </div>

          {formError && (
            <div className="mb-4 rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
              {formError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <Field label="Full name" error={errors.fullName}>
              <TextInput
                placeholder="Jane Doe"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            </Field>
            <Field label="Email" error={errors.email}>
              <TextInput
                type="email"
                autoComplete="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </Field>
            <Field label="Phone" error={errors.phone} hint="E.164 format, e.g. +1234567890">
              <TextInput
                type="tel"
                autoComplete="tel"
                placeholder="+1234567890"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
            </Field>
            <Field label="Password" error={errors.password}>
              <TextInput
                type="password"
                autoComplete="new-password"
                placeholder="At least 8 characters"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </Field>
            <Field label="Confirm password" error={errors.confirm}>
              <TextInput
                type="password"
                autoComplete="new-password"
                placeholder="Repeat your password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
              />
            </Field>
            <Field label="I want to" hint="You can also book services with a CUSTOMER account">
              <Select value={role} onChange={(e) => setRole(e.target.value as Role)}>
                <option value="CUSTOMER">Book home services (Customer)</option>
                <option value="PROVIDER">Offer home services (Provider)</option>
              </Select>
            </Field>
            <Button type="submit" className="w-full" size="lg" loading={loading}>
              Create account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-500">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-indigo-600 hover:text-indigo-500">
              Log in
            </Link>
          </p>
        </Card>
      </div>
    </div>
  )
}
