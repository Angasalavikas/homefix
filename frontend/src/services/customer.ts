import api from './api'
import type { Address, AddressInput, Customer, CustomerInput } from '../types'

export async function getMyCustomerProfile(): Promise<Customer> {
  const { data } = await api.get<Customer>('/customers/me')
  return data
}

export async function updateMyCustomerProfile(payload: CustomerInput): Promise<Customer> {
  const { data } = await api.put<Customer>('/customers/me', payload)
  return data
}

export async function getMyAddresses(): Promise<Address[]> {
  const { data } = await api.get<Address[]>('/customers/me/addresses')
  return data
}

export async function addAddress(payload: AddressInput): Promise<Address> {
  const { data } = await api.post<Address>('/customers/me/addresses', payload)
  return data
}

export async function deleteAddress(id: number): Promise<void> {
  await api.delete(`/customers/me/addresses/${id}`)
}
