const STATUS = {
  APPLIED:   { bg: 'rgba(59,130,246,0.12)',  text: '#60a5fa', border: 'rgba(59,130,246,0.25)', dot: '#3b82f6' },
  INTERVIEW: { bg: 'rgba(245,158,11,0.12)',  text: '#fbbf24', border: 'rgba(245,158,11,0.25)', dot: '#f59e0b' },
  OFFER:     { bg: 'rgba(16,185,129,0.12)',  text: '#34d399', border: 'rgba(16,185,129,0.25)', dot: '#10b981' },
  REJECTED:  { bg: 'rgba(239,68,68,0.12)',   text: '#f87171', border: 'rgba(239,68,68,0.25)',  dot: '#ef4444' },
}

export default function StatusBadge({ status }) {
  const s = STATUS[status] || STATUS.APPLIED
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: '6px',
      padding: '5px 12px', borderRadius: '20px',
      fontSize: '11px', fontWeight: 600, letterSpacing: '0.5px',
      background: s.bg, color: s.text, border: `1px solid ${s.border}`
    }}>
      <span style={{ width: '5px', height: '5px', borderRadius: '50%', background: s.dot, flexShrink: 0 }} />
      {status}
    </span>
  )
}
