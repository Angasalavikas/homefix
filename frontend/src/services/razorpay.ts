import type { RazorpayOrder } from '../types'

const CHECKOUT_SCRIPT = 'https://checkout.razorpay.com/v1/checkout.js'

let scriptPromise: Promise<void> | null = null

/**
 * Load the Razorpay Checkout script once. Resolves when `window.Razorpay` is
 * available; rejects with a human-readable message if the script can't load.
 */
export function loadRazorpayCheckoutScript(): Promise<void> {
  if (typeof window !== 'undefined' && window.Razorpay) return Promise.resolve()
  if (scriptPromise) return scriptPromise

  scriptPromise = new Promise<void>((resolve, reject) => {
    const script = document.createElement('script')
    script.src = CHECKOUT_SCRIPT
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => {
      scriptPromise = null
      reject(
        new Error(
          'Could not load the Razorpay checkout script. Check your connection and try again.',
        ),
      )
    }
    document.head.appendChild(script)
  })
  return scriptPromise
}

export type CheckoutOutcome =
  | {
      status: 'success'
      razorpayPaymentId: string
      razorpayOrderId: string
      razorpaySignature: string
    }
  | { status: 'cancelled' }
  | { status: 'failed'; reason?: string }

/**
 * Open the Razorpay Checkout modal for an order created by
 * POST /payments/create-order. Resolves once the modal is dismissed, the user
 * cancels, or a payment attempt fails. The success `handler` only fires after a
 * real payment — the caller must still verify the signature server-side.
 */
export function openRazorpayCheckout(options: {
  order: RazorpayOrder
  bookingId: number
  prefill?: { name?: string; email?: string; contact?: string }
}): Promise<CheckoutOutcome> {
  return new Promise((resolve) => {
    let settled = false
    const finish = (outcome: CheckoutOutcome) => {
      if (settled) return
      settled = true
      resolve(outcome)
    }

    const RazorpayCtor = window.Razorpay
    if (!RazorpayCtor) {
      finish({ status: 'failed', reason: 'Razorpay checkout is unavailable. Please try again.' })
      return
    }

    const checkout = new RazorpayCtor({
      key: options.order.razorpayKeyId,
      amount: Math.round(options.order.amount * 100),
      currency: options.order.currency,
      name: 'HomeFix',
      description: `Booking #${options.bookingId}`,
      order_id: options.order.orderId,
      prefill: options.prefill,
      theme: { color: '#4f46e5' },
      handler: (response) => {
        finish({
          status: 'success',
          razorpayPaymentId: response.razorpay_payment_id,
          razorpayOrderId: response.razorpay_order_id,
          razorpaySignature: response.razorpay_signature,
        })
      },
      modal: {
        ondismiss: () => finish({ status: 'cancelled' }),
      },
    })

    checkout.on('payment.failed', (response: unknown) => {
      const error = (response as { error?: { description?: string } } | null)?.error
      finish({ status: 'failed', reason: error?.description })
    })

    checkout.open()
  })
}
