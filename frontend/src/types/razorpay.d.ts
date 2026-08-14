/**
 * Minimal typings for the Razorpay Checkout script
 * (https://checkout.razorpay.com/v1/checkout.js), which is loaded at runtime.
 */
export {}

declare global {
  interface RazorpayCheckoutOptions {
    key: string
    /** Amount in the smallest currency unit (paise for INR). */
    amount: number
    currency: string
    name: string
    description?: string
    order_id: string
    image?: string
    prefill?: {
      name?: string
      email?: string
      contact?: string
    }
    theme?: {
      color?: string
    }
    handler?: (response: {
      razorpay_payment_id: string
      razorpay_order_id: string
      razorpay_signature: string
    }) => void
    modal?: {
      ondismiss?: () => void
      confirm_close?: boolean
    }
  }

  interface RazorpayCheckout {
    open: () => void
    close: () => void
    on: (event: string, callback: (response: unknown) => void) => void
  }

  interface Window {
    Razorpay?: new (options: RazorpayCheckoutOptions) => RazorpayCheckout
  }
}
