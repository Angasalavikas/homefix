import api from './api'
import type { AvailabilityStatus, Provider, ProviderRegistrationInput } from '../types'

export async function getAvailableProviders(): Promise<Provider[]> {
  const { data } = await api.get<Provider[]>('/providers/available')
  return data
}

export async function getMyProviderProfile(): Promise<Provider> {
  const { data } = await api.get<Provider>('/providers/me')
  return data
}

export async function registerProviderProfile(payload: ProviderRegistrationInput): Promise<Provider> {
  const { data } = await api.post<Provider>('/providers/profile', payload)
  return data
}

export async function updateAvailability(availability: AvailabilityStatus): Promise<Provider> {
  const { data } = await api.put<Provider>('/providers/me/availability', { availability })
  return data
}
