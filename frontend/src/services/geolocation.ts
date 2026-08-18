/**
 * Reverse geocoding via the OpenStreetMap Nominatim public API — free, no API
 * key required, CORS enabled. Respect the usage policy (max ~1 request/sec,
 * lightweight use) — fine for a manual "use my location" button.
 *
 * Alternative without a street field: BigDataCloud's free client endpoint
 * (https://api.bigdatacloud.net/data/reverse-geocode-client) — no key, no usage
 * policy, but returns city/state/postcode without street-level data.
 */

export interface ReverseGeocodeResult {
  street: string
  city: string
  state: string
  zip: string
}

export async function reverseGeocode(
  latitude: number,
  longitude: number,
): Promise<ReverseGeocodeResult> {
  const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&addressdetails=1&zoom=18&lat=${latitude}&lon=${longitude}`
  const res = await fetch(url, { headers: { Accept: 'application/json' } })
  if (!res.ok) {
    throw new Error('Could not reverse-geocode your location')
  }
  const data = (await res.json()) as { address?: Record<string, string> }
  const addr = data.address ?? {}

  const road = [addr.road, addr.suburb, addr.neighbourhood].filter(Boolean).join(', ')
  return {
    street: road || addr.house_number || '',
    city: addr.city ?? addr.town ?? addr.village ?? addr.municipality ?? '',
    state: addr.state ?? addr.region ?? '',
    zip: addr.postcode ?? '',
  }
}
