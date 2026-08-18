import { useState } from 'react'
import { Loader2, MapPin } from 'lucide-react'
import { reverseGeocode } from '../services/geolocation'
import type { AddressInput } from '../types'

interface LocatedResult {
  latitude: number
  longitude: number
  /** Best-effort reverse-geocoded fields; may be empty if geocoding fails. */
  address: Partial<AddressInput>
}

interface LocationPinButtonProps {
  onLocated: (result: LocatedResult) => void
  label?: string
}

const errorMessages: Record<number, string> = {
  1: 'Location permission was denied. Please allow location access and try again, or enter the address manually.',
  2: 'Your location is currently unavailable. Please try again or enter the address manually.',
  3: 'The location request timed out. Please try again or enter the address manually.',
}

/**
 * "Use my current location" button: reads the browser's geolocation, reverse
 * geocodes it into editable address fields, and hands the result to the parent
 * form. Always lets the user edit the prefilled fields before saving.
 */
export default function LocationPinButton({
  onLocated,
  label = 'Use my current location',
}: LocationPinButtonProps) {
  const [locating, setLocating] = useState(false)
  const [error, setError] = useState('')

  const handleClick = () => {
    setError('')

    if (!('geolocation' in navigator)) {
      setError(
        'Geolocation is not supported by this browser. Please enter your address manually.',
      )
      return
    }

    setLocating(true)
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude, longitude } = position.coords
        let address: Partial<AddressInput> = {}
        try {
          address = await reverseGeocode(latitude, longitude)
        } catch {
          // Coordinates are still useful even if reverse geocoding fails —
          // fields stay empty and the user fills them in manually.
        } finally {
          setLocating(false)
        }
        onLocated({ latitude, longitude, address })
      },
      (err) => {
        setLocating(false)
        setError(errorMessages[err.code] ?? 'Could not fetch your location. Please try again.')
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 },
    )
  }

  return (
    <div>
      <button
        type="button"
        onClick={handleClick}
        disabled={locating}
        className="inline-flex items-center gap-1.5 rounded-lg border border-indigo-200 bg-indigo-50 px-3 py-1.5 text-xs font-semibold text-indigo-700 transition-colors hover:bg-indigo-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {locating ? (
          <Loader2 className="h-3.5 w-3.5 animate-spin" aria-hidden="true" />
        ) : (
          <MapPin className="h-3.5 w-3.5" aria-hidden="true" />
        )}
        {locating ? 'Fetching your location…' : label}
      </button>
      {error && <p className="mt-2 text-xs font-medium text-rose-600">{error}</p>}
    </div>
  )
}
