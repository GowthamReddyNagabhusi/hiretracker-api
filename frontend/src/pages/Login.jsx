import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/auth/login', form)
      login(res.data)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4" style={{background: 'radial-gradient(ellipse at top, #0f1f3d 0%, #080c14 70%)'}}>
      <div className="w-full max-w-md">

        {/* Logo */}
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl mb-4 text-2xl" style={{background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)', boxShadow: '0 0 40px rgba(59,130,246,0.3)'}}>
            🎯
          </div>
          <h1 className="text-2xl font-bold tracking-widest" style={{fontFamily: 'monospace', color: '#f1f5f9'}}>HIRETRACK</h1>
          <p className="text-xs mt-1 tracking-widest uppercase" style={{color: '#475569'}}>Job Application Tracker</p>
        </div>

        {/* Card */}
        <div className="rounded-2xl p-8" style={{background: '#111827', border: '1px solid #1e2d45', boxShadow: '0 25px 60px rgba(0,0,0,0.5)'}}>
          <h2 className="text-xl font-semibold mb-1">Welcome back</h2>
          <p className="text-sm mb-6" style={{color: '#64748b'}}>Sign in to continue tracking</p>

          {error && (
            <div className="mb-4 p-3 rounded-lg text-sm" style={{background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', color: '#fca5a5'}}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium mb-2 uppercase tracking-wider" style={{color: '#94a3b8'}}>Email</label>
              <input
                type="email" required
                value={form.email}
                onChange={e => setForm({...form, email: e.target.value})}
                placeholder="you@example.com"
                className="w-full px-4 py-3 rounded-xl text-sm outline-none transition-all"
                style={{background: '#0f1525', border: '1px solid #1e2d45', color: '#f1f5f9'}}
                onFocus={e => e.target.style.borderColor = '#3b82f6'}
                onBlur={e => e.target.style.borderColor = '#1e2d45'}
              />
            </div>
            <div>
              <label className="block text-xs font-medium mb-2 uppercase tracking-wider" style={{color: '#94a3b8'}}>Password</label>
              <input
                type="password" required
                value={form.password}
                onChange={e => setForm({...form, password: e.target.value})}
                placeholder="••••••••"
                className="w-full px-4 py-3 rounded-xl text-sm outline-none transition-all"
                style={{background: '#0f1525', border: '1px solid #1e2d45', color: '#f1f5f9'}}
                onFocus={e => e.target.style.borderColor = '#3b82f6'}
                onBlur={e => e.target.style.borderColor = '#1e2d45'}
              />
            </div>
            <button
              type="submit" disabled={loading}
              className="w-full py-3 rounded-xl font-semibold text-sm text-white transition-all mt-2"
              style={{background: 'linear-gradient(135deg, #3b82f6, #2563eb)', boxShadow: '0 4px 20px rgba(59,130,246,0.3)', opacity: loading ? 0.7 : 1}}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        </div>

        <p className="text-center mt-5 text-sm" style={{color: '#475569'}}>
          No account? <Link to="/register" className="font-medium" style={{color: '#60a5fa'}}>Create one</Link>
        </p>
      </div>
    </div>
  )
}