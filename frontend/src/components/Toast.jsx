import { useState, useEffect } from 'react'

export default function Toast({ message, type = 'success', onClose }) {
  const [visible, setVisible] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => {
      setVisible(false)
      setTimeout(onClose, 300) // Wait for fade animation
    }, 3000)
    return () => clearTimeout(timer)
  }, [onClose])

  return (
    <div style={{
      position: 'fixed', bottom: '28px', right: '28px', zIndex: 200,
      padding: '14px 20px', borderRadius: '12px',
      fontSize: '14px', fontWeight: 500,
      display: 'flex', alignItems: 'center', gap: '10px',
      boxShadow: '0 20px 60px rgba(0,0,0,0.5)',
      transition: 'opacity 0.3s, transform 0.3s',
      opacity: visible ? 1 : 0,
      transform: visible ? 'translateY(0)' : 'translateY(16px)',
      background: type === 'success' ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)',
      border: `1px solid ${type === 'success' ? 'rgba(16,185,129,0.25)' : 'rgba(239,68,68,0.25)'}`,
      color: type === 'success' ? '#34d399' : '#f87171'
    }}>
      <span style={{ fontSize: '16px' }}>{type === 'success' ? '✓' : '✕'}</span>
      {message}
    </div>
  )
}
