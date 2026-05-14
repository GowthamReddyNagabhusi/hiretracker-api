export default function StatsCard({ label, value, sub, accent, glow }) {
  return (
    <div
      style={{
        background: '#0d1117', border: '1px solid #161f2e', borderRadius: '16px',
        padding: '22px 24px', position: 'relative', overflow: 'hidden',
        cursor: 'default', transition: 'transform 0.2s, border-color 0.2s'
      }}
      onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.borderColor = accent + '44' }}
      onMouseLeave={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.borderColor = '#161f2e' }}
    >
      {/* glow */}
      <div style={{ position: 'absolute', top: 0, right: 0, width: '80px', height: '80px', background: glow, borderRadius: '0 16px 0 80px', pointerEvents: 'none' }} />
      {/* accent bar */}
      <div style={{ position: 'absolute', top: 0, left: 0, width: '3px', height: '100%', background: accent, borderRadius: '16px 0 0 16px' }} />
      <div style={{ fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '1.2px', color: '#334155', marginBottom: '14px' }}>{label}</div>
      <div style={{ fontSize: '42px', fontWeight: 700, lineHeight: 1, color: accent, fontFamily: 'monospace', marginBottom: '8px' }}>{value}</div>
      <div style={{ fontSize: '12px', color: '#334155' }}>{sub}</div>
    </div>
  )
}
