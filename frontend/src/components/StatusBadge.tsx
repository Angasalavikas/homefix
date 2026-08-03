const statusStyles: Record<string, { label: string; className: string }> = {
  PENDING: { label: 'Pending', className: 'bg-amber-50 text-amber-700 ring-amber-600/20' },
  ACCEPTED: { label: 'Accepted', className: 'bg-blue-50 text-blue-700 ring-blue-600/20' },
  ON_THE_WAY: { label: 'On the way', className: 'bg-sky-50 text-sky-700 ring-sky-600/20' },
  STARTED: { label: 'Started', className: 'bg-violet-50 text-violet-700 ring-violet-600/20' },
  COMPLETED: { label: 'Completed', className: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' },
  CANCELLED: { label: 'Cancelled', className: 'bg-rose-50 text-rose-700 ring-rose-600/20' },
  // Provider verification statuses
  VERIFIED: { label: 'Verified', className: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' },
  REJECTED: { label: 'Rejected', className: 'bg-rose-50 text-rose-700 ring-rose-600/20' },
}

export default function StatusBadge({ status }: { status: string }) {
  const s =
    statusStyles[status] ?? {
      label: status.replace(/_/g, ' '),
      className: 'bg-gray-100 text-gray-700 ring-gray-500/20',
    }
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${s.className}`}
    >
      <span className="h-1.5 w-1.5 rounded-full bg-current opacity-70" aria-hidden="true" />
      {s.label}
    </span>
  )
}
