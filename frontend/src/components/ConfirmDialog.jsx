export default function ConfirmDialog({ title, message, onConfirm, onCancel }) {
  return (
    <div
      style={{
        position: 'fixed', inset: 0, zIndex: 150,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)'
      }}
      onClick={e => e.target === e.currentTarget && onCancel()}
    >
      <div style={{
        width: '100%', maxWidth: '380px',
        background: '#0d1117', border: '1px solid #1e293b',
        borderRadius: '16px', padding: '28px',
        boxShadow: '0 40px 80px rgba(0,0,0,0.7)',
        animation: 'slideUp 0.2s ease'
      }}>
        <h3 style={{ fontSize: '16px', fontWeight: 600, color: '#f1f5f9', marginBottom: '8px' }}>
          {title}
        </h3>
        <p style={{ fontSize: '14px', color: '#64748b', marginBottom: '24px' }}>
          {message}
        </p>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            onClick={onCancel}
            style={{
              flex: 1, padding: '10px', borderRadius: '10px',
              background: 'transparent', border: '1px solid #1e293b',
              color: '#64748b', fontSize: '14px', fontWeight: 500,
              cursor: 'pointer', fontFamily: 'inherit'
            }}
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            style={{
              flex: 1, padding: '10px', borderRadius: '10px',
              background: 'rgba(239,68,68,0.15)', border: '1px solid rgba(239,68,68,0.3)',
              color: '#f87171', fontSize: '14px', fontWeight: 600,
              cursor: 'pointer', fontFamily: 'inherit'
            }}
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  )
}
