import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  addAddress,
  deleteAddress,
  getMyCustomerProfile,
  updateMyCustomerProfile,
} from '../services/customer'
import {
  getMyProviderProfile,
  registerProviderProfile,
  updateAvailability,
} from '../services/provider'
import { getErrorMessage } from '../services/api'
import Card from '../components/Card'
import Button from '../components/Button'
import Modal from '../components/Modal'
import LoadingSpinner from '../components/LoadingSpinner'
import LocationPinButton from '../components/LocationPinButton'
import { Field, TextInput } from '../components/FormField'
import StatusBadge from '../components/StatusBadge'
import type { Address, AddressInput, AvailabilityStatus, Customer, Provider } from '../types'

const availabilityLabels: Record<AvailabilityStatus, string> = {
  AVAILABLE: 'Available',
  BUSY: 'Busy',
  OFFLINE: 'Offline',
}

const emptyAddress: AddressInput = {
  label: '',
  street: '',
  city: '',
  state: '',
  zip: '',
  isDefault: false,
  latitude: null,
  longitude: null,
}

export default function ProfilePage() {
  const { user } = useAuth()

  // ---------- Customer state ----------
  const [customer, setCustomer] = useState<Customer | null>(null)
  const [customerMissing, setCustomerMissing] = useState(false)
  const [profileForm, setProfileForm] = useState({ fullName: '', email: '', phone: '' })
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileMsg, setProfileMsg] = useState('')

  const [addressModalOpen, setAddressModalOpen] = useState(false)
  const [addressForm, setAddressForm] = useState<AddressInput>(emptyAddress)
  const [savingAddress, setSavingAddress] = useState(false)
  const [addressError, setAddressError] = useState('')

  // ---------- Provider state ----------
  const [provider, setProvider] = useState<Provider | null>(null)
  const [providerMissing, setProviderMissing] = useState(false)
  const [regForm, setRegForm] = useState({ name: '', experienceYears: '', skills: '', serviceId: '', })
  const [savingReg, setSavingReg] = useState(false)
  const [regError, setRegError] = useState('')
  const [availabilityBusy, setAvailabilityBusy] = useState(false)

  const [loading, setLoading] = useState(true)

  const isCustomer = user?.role === 'CUSTOMER'
  const isProvider = user?.role === 'PROVIDER'

  const loadCustomer = useCallback(async () => {
    try {
      const data = await getMyCustomerProfile()
      setCustomer(data)
      setCustomerMissing(false)
      setProfileForm({ fullName: data.fullName, email: data.email, phone: data.phone })
    } catch {
      setCustomerMissing(true)
    }
  }, [])

  const loadProvider = useCallback(async () => {
    try {
      setProvider(await getMyProviderProfile())
      setProviderMissing(false)
    } catch {
      setProviderMissing(true)
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    const work = async () => {
      setLoading(true)
      if (isCustomer) await loadCustomer()
      if (isProvider) await loadProvider()
      if (!cancelled) setLoading(false)
    }
    work()
    return () => {
      cancelled = true
    }
  }, [isCustomer, isProvider, loadCustomer, loadProvider])

  // ---------- Customer actions ----------
  const handleSaveProfile = async (e: FormEvent) => {
    e.preventDefault()
    setSavingProfile(true)
    setProfileMsg('')
    try {
      const updated = await updateMyCustomerProfile(profileForm)
      setCustomer(updated)
      setCustomerMissing(false)
      setProfileMsg('Profile saved ✓')
    } catch (err) {
      setProfileMsg(getErrorMessage(err))
    } finally {
      setSavingProfile(false)
    }
  }

  const handleAddAddress = async (e: FormEvent) => {
    e.preventDefault()
    setSavingAddress(true)
    setAddressError('')
    try {
      const created = await addAddress(addressForm)
      setCustomer((prev) =>
        prev ? { ...prev, addresses: [...prev.addresses, created] } : prev,
      )
      setAddressModalOpen(false)
      setAddressForm(emptyAddress)
    } catch (err) {
      setAddressError(getErrorMessage(err))
    } finally {
      setSavingAddress(false)
    }
  }

  const handleDeleteAddress = async (address: Address) => {
    setAddressError('')
    try {
      await deleteAddress(address.id)
      setCustomer((prev) =>
        prev ? { ...prev, addresses: prev.addresses.filter((a) => a.id !== address.id) } : prev,
      )
    } catch (err) {
      setAddressError(getErrorMessage(err))
    }
  }

  // ---------- Provider actions ----------
  const handleRegister = async (e: FormEvent) => {
    e.preventDefault()
    setSavingReg(true)
    setRegError('')
    try {
      const created = await registerProviderProfile({
        name: regForm.name.trim(),
        experienceYears: Number(regForm.experienceYears),
        serviceId: Number(regForm.serviceId),
        skills: regForm.skills
          .split(',')
          .map((s) => s.trim())
          .filter(Boolean),
      })
      setProvider(created)
      setProviderMissing(false)
    } catch (err) {
      setRegError(getErrorMessage(err))
    } finally {
      setSavingReg(false)
    }
  }

  const handleAvailability = async (availability: AvailabilityStatus) => {
    setAvailabilityBusy(true)
    setRegError('')
    try {
      setProvider(await updateAvailability(availability))
    } catch (err) {
      setRegError(getErrorMessage(err))
    } finally {
      setAvailabilityBusy(false)
    }
  }

  if (loading) return <LoadingSpinner label="Loading profile…" />

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="mb-8 text-3xl font-bold text-gray-900">My profile</h1>

      {/* ================= Customer branch ================= */}
      {isCustomer && (
        <div className="space-y-8">
          {customerMissing ? (
            <Card className="p-6">
              <h2 className="text-lg font-semibold text-gray-900">Complete your profile</h2>
              <p className="mt-1 text-sm text-gray-500">
                A few details so providers know who to contact.
              </p>
              <form onSubmit={handleSaveProfile} className="mt-4 space-y-4">
                <Field label="Full name">
                  <TextInput
                    value={profileForm.fullName}
                    onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })}
                  />
                </Field>
                <Field label="Email">
                  <TextInput
                    type="email"
                    value={profileForm.email}
                    onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                  />
                </Field>
                <Field label="Phone">
                  <TextInput
                    value={profileForm.phone}
                    onChange={(e) => setProfileForm({ ...profileForm, phone: e.target.value })}
                  />
                </Field>
                <Button type="submit" loading={savingProfile}>
                  Save profile
                </Button>
              </form>
            </Card>
          ) : (
            customer && (
              <>
                <Card className="p-6">
                  <h2 className="text-lg font-semibold text-gray-900">Contact details</h2>
                  <p className="text-sm text-gray-500">Customer profile #{customer.id}</p>
                  {profileMsg && (
                    <p className="mt-2 text-sm font-medium text-emerald-600">{profileMsg}</p>
                  )}
                  <form onSubmit={handleSaveProfile} className="mt-4 space-y-4">
                    <Field label="Full name">
                      <TextInput
                        value={profileForm.fullName}
                        onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })}
                      />
                    </Field>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <Field label="Email">
                        <TextInput
                          type="email"
                          value={profileForm.email}
                          onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                        />
                      </Field>
                      <Field label="Phone">
                        <TextInput
                          value={profileForm.phone}
                          onChange={(e) => setProfileForm({ ...profileForm, phone: e.target.value })}
                        />
                      </Field>
                    </div>
                    <Button type="submit" loading={savingProfile}>
                      Save changes
                    </Button>
                  </form>
                </Card>

                <div>
                  <div className="mb-4 flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-gray-900">Saved addresses</h2>
                    <Button size="sm" onClick={() => setAddressModalOpen(true)}>
                      + Add address
                    </Button>
                  </div>
                  {addressError && (
                    <p className="mb-3 text-sm font-medium text-rose-600">{addressError}</p>
                  )}
                  {customer.addresses.length === 0 ? (
                    <p className="rounded-xl border border-dashed border-gray-300 bg-white/70 px-4 py-8 text-center text-sm text-gray-500">
                      No saved addresses. Add one to speed up booking.
                    </p>
                  ) : (
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      {customer.addresses.map((address) => (
                        <Card key={address.id} className="p-4">
                          <div className="flex items-center justify-between gap-2">
                            <span className="font-semibold text-gray-900">
                              {address.label}
                              {address.isDefault && (
                                <span className="ml-2 rounded-full bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700">
                                  default
                                </span>
                              )}
                            </span>
                            <button
                              onClick={() => handleDeleteAddress(address)}
                              className="rounded-md p-1 text-gray-400 transition-colors hover:bg-rose-50 hover:text-rose-600"
                              aria-label="Delete address"
                            >
                              <svg
                                className="h-4 w-4"
                                fill="none"
                                viewBox="0 0 24 24"
                                stroke="currentColor"
                                strokeWidth="2"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  d="M6 18L18 6M6 6l12 12"
                                />
                              </svg>
                            </button>
                          </div>
                          <p className="mt-1 text-sm text-gray-500">
                            {address.street}, {address.city}, {address.state} {address.zip}
                          </p>
                        </Card>
                      ))}
                    </div>
                  )}
                </div>
              </>
            )
          )}

          {/* Add address modal */}
          <Modal open={addressModalOpen} onClose={() => setAddressModalOpen(false)} title="Add address">
            <form onSubmit={handleAddAddress} className="space-y-4">
              {addressError && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
                  {addressError}
                </p>
              )}
              <div className="flex justify-end">
                <LocationPinButton
                  onLocated={({ latitude, longitude, address }) =>
                    setAddressForm((prev) => ({
                      ...prev,
                      latitude,
                      longitude,
                      street: address.street || prev.street,
                      city: address.city || prev.city,
                      state: address.state || prev.state,
                      zip: address.zip || prev.zip,
                    }))
                  }
                />
              </div>
              <Field label="Label">
                <TextInput
                  placeholder="e.g. Home"
                  value={addressForm.label}
                  onChange={(e) => setAddressForm({ ...addressForm, label: e.target.value })}
                />
              </Field>
              <Field label="Street">
                <TextInput
                  placeholder="123 Main Street"
                  value={addressForm.street}
                  onChange={(e) => setAddressForm({ ...addressForm, street: e.target.value })}
                />
              </Field>
              <div className="grid grid-cols-2 gap-4">
                <Field label="City">
                  <TextInput
                    value={addressForm.city}
                    onChange={(e) => setAddressForm({ ...addressForm, city: e.target.value })}
                  />
                </Field>
                <Field label="State">
                  <TextInput
                    value={addressForm.state}
                    onChange={(e) => setAddressForm({ ...addressForm, state: e.target.value })}
                  />
                </Field>
              </div>
              <Field label="ZIP code">
                <TextInput
                  value={addressForm.zip}
                  onChange={(e) => setAddressForm({ ...addressForm, zip: e.target.value })}
                />
              </Field>
              <label className="flex items-center gap-2 text-sm text-gray-700">
                <input
                  type="checkbox"
                  checked={Boolean(addressForm.isDefault)}
                  onChange={(e) => setAddressForm({ ...addressForm, isDefault: e.target.checked })}
                />
                Set as default address
              </label>
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="secondary" onClick={() => setAddressModalOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" loading={savingAddress}>
                  Save address
                </Button>
              </div>
            </form>
          </Modal>
        </div>
      )}

      {/* ================= Provider branch ================= */}
      {isProvider && (
        <div className="space-y-8">
          {providerMissing ? (
            <Card className="p-6">
              <h2 className="text-lg font-semibold text-gray-900">Register as a provider</h2>
              <p className="mt-1 text-sm text-gray-500">
                Tell customers about yourself. You&apos;ll appear in booking results once an admin
                verifies you.
              </p>
              {regError && (
                <p className="mt-3 rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
                  {regError}
                </p>
              )}
              <form onSubmit={handleRegister} className="mt-4 space-y-4">
                <Field label="Name">
                  <TextInput
                    value={regForm.name}
                    onChange={(e) => setRegForm({ ...regForm, name: e.target.value })}
                  />
                </Field>
                <Field label="Years of experience">
                  <TextInput
                    type="number"
                    min={0}
                    value={regForm.experienceYears}
                    onChange={(e) =>
                      setRegForm({ ...regForm, experienceYears: e.target.value })
                    }
                  />
                </Field>
                <Field label="Service">
                  <select
                      className="w-full rounded-lg border border-gray-300 px-3 py-2"
                      value={regForm.serviceId}
                      onChange={(e) =>
                          setRegForm({ ...regForm, serviceId: e.target.value })
                      }
                  >
                    <option value="">Select Service</option>
                    <option value="1">Plumbing</option>
                    <option value="2">Electrical</option>
                    <option value="3">Painting</option>
                    <option value="4">Cleaning</option>
                    <option value="5">AC Repair</option>
                  </select>
                </Field>
                <Field label="Skills" hint="Comma-separated, e.g. plumbing, faucet repair">
                  <TextInput
                    placeholder="plumbing, faucet repair"
                    value={regForm.skills}
                    onChange={(e) => setRegForm({ ...regForm, skills: e.target.value })}
                  />
                </Field>
                <Button type="submit" loading={savingReg}>
                  Register profile
                </Button>
              </form>
            </Card>
          ) : (
            provider && (
              <>
                <Card className="p-6">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h2 className="text-lg font-semibold text-gray-900">{provider.name}</h2>
                      <p className="text-sm text-gray-500">
                        Provider profile #{provider.id} · {provider.experienceYears} years experience
                      </p>
                    </div>
                    <StatusBadge status={provider.verificationStatus} />
                  </div>

                  <div className="mt-4 flex flex-wrap gap-2">
                    {provider.skills.length > 0 ? (
                      provider.skills.map((skill) => (
                        <span
                          key={skill}
                          className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700"
                        >
                          {skill}
                        </span>
                      ))
                    ) : (
                      <span className="text-sm text-gray-400">No skills listed</span>
                    )}
                  </div>

                  <div className="mt-6">
                    <h3 className="text-sm font-semibold text-gray-900">Availability</h3>
                    <div className="mt-2 flex flex-wrap gap-2">
                      {(Object.keys(availabilityLabels) as AvailabilityStatus[]).map((status) => (
                        <button
                          key={status}
                          disabled={availabilityBusy}
                          onClick={() => handleAvailability(status)}
                          className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors disabled:opacity-50 ${
                            provider.availability === status
                              ? 'bg-indigo-600 text-white shadow-sm'
                              : 'bg-white text-gray-600 ring-1 ring-inset ring-gray-300 hover:bg-gray-50'
                          }`}
                        >
                          {availabilityLabels[status]}
                        </button>
                      ))}
                    </div>
                    {regError && (
                      <p className="mt-3 text-sm font-medium text-rose-600">{regError}</p>
                    )}
                  </div>
                </Card>

                <Card className="p-6">
                  <h3 className="text-sm font-semibold text-gray-900">How verification works</h3>
                  <p className="mt-2 text-sm leading-relaxed text-gray-500">
                    Your profile is currently{' '}
                    <span className="font-medium text-gray-700">
                      {provider.verificationStatus.toLowerCase()}
                    </span>
                    . Only verified providers appear in customer booking results — an admin can
                    verify you from the admin dashboard.
                  </p>
                </Card>
              </>
            )
          )}
        </div>
      )}
    </div>
  )
}
