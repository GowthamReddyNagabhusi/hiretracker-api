export function Input({ label, ...props }) {
  return (
    <div>
      {label && <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.8px', color: '#64748b' }}>{label}</label>}
      <input
        {...props}
        style={{
          width: '100%', background: '#0a0e1a', border: '1px solid #1e293b',
          borderRadius: '10px', padding: '11px 14px', fontSize: '14px',
          color: '#f1f5f9', fontFamily: 'inherit', outline: 'none',
          ...props.style
        }}
        onFocus={e => { e.target.style.borderColor = '#3b82f6'; e.target.style.boxShadow = '0 0 0 3px rgba(59,130,246,0.1)' }}
        onBlur={e => { e.target.style.borderColor = '#1e293b'; e.target.style.boxShadow = 'none' }}
      />
    </div>
  )
}

export function Select({ label, children, ...props }) {
  return (
    <div>
      {label && <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.8px', color: '#64748b' }}>{label}</label>}
      <select
        {...props}
        style={{
          width: '100%', background: '#0a0e1a', border: '1px solid #1e293b',
          borderRadius: '10px', padding: '11px 14px', fontSize: '14px',
          color: '#f1f5f9', fontFamily: 'inherit', outline: 'none', cursor: 'pointer',
          ...props.style
        }}
        onFocus={e => { e.target.style.borderColor = '#3b82f6' }}
        onBlur={e => { e.target.style.borderColor = '#1e293b' }}
      >{children}</select>
    </div>
  )
}
