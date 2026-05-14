import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    try {
      await api.post('/auth/logout')
    } catch {
      // Logout even if API call fails
    }
    logout()
    navigate('/login')
  }

  return (
    <nav style={{
      position: 'sticky', top: 0, zIndex: 50, height: '60px',
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 32px',
      background: 'rgba(8,12,20,0.92)', backdropFilter: 'blur(24px)',
      borderBottom: '1px solid #0f1e35'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <div style={{
          width: '34px', height: '34px', borderRadius: '10px',
          background: 'linear-gradient(135deg,#6366f1,#8b5cf6)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '16px', boxShadow: '0 0 20px rgba(99,102,241,0.4)'
        }}>🎯</div>
        <span style={{ fontFamily: 'monospace', fontWeight: 700, fontSize: '14px', letterSpacing: '3px', color: '#e2e8f0' }}>HIRETRACK</span>
        <span style={{
          marginLeft: '8px', padding: '2px 10px', borderRadius: '20px',
          fontSize: '10px', fontWeight: 600,
          background: 'rgba(99,102,241,0.15)', color: '#818cf8',
          border: '1px solid rgba(99,102,241,0.25)', letterSpacing: '1px'
        }}>BETA</span>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div style={{
          display: 'flex', alignItems: 'center', gap: '8px',
          padding: '6px 14px', borderRadius: '30px',
          background: '#0f1829', border: '1px solid #1e293b',
          fontSize: '13px', color: '#94a3b8'
        }}>
          <div style={{
            width: '26px', height: '26px', borderRadius: '50%',
            background: 'linear-gradient(135deg,#6366f1,#8b5cf6)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '11px', fontWeight: 700, color: 'white'
          }}>
            {user?.name?.charAt(0).toUpperCase()}
          </div>
          <span style={{ color: '#cbd5e1', fontWeight: 500 }}>{user?.name}</span>
        </div>
        <button
          onClick={handleLogout}
          style={{
            padding: '7px 16px', borderRadius: '8px',
            background: 'transparent', border: '1px solid #1e293b',
            color: '#475569', fontSize: '13px', cursor: 'pointer',
            fontFamily: 'inherit', transition: 'all 0.2s'
          }}
          onMouseEnter={e => { e.target.style.borderColor = '#ef4444'; e.target.style.color = '#f87171' }}
          onMouseLeave={e => { e.target.style.borderColor = '#1e293b'; e.target.style.color = '#475569' }}
        >
          Logout
        </button>
      </div>
    </nav>
  )
}
