import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../api/axios'
import Navbar from '../components/Navbar'
import StatsCard from '../components/StatsCard'
import StatusBadge from '../components/StatusBadge'
import Toast from '../components/Toast'
import ConfirmDialog from '../components/ConfirmDialog'
import { Input, Select } from '../components/FormElements'

const COLORS = ['#6366f1', '#8b5cf6', '#3b82f6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#ec4899']
const EMPTY = { company: '', role: '', status: 'APPLIED', appliedDate: new Date().toISOString().split('T')[0], notes: '' }

export default function Dashboard() {
  const { user } = useAuth()
  const [jobs, setJobs] = useState([])
  const [stats, setStats] = useState({ totalApplied: 0, totalInterviews: 0, totalOffers: 0, totalRejections: 0, offerRate: 0 })
  const [search, setSearch] = useState('')
  const [filterStatus, setFilter] = useState('')
  const [modal, setModal] = useState(false)
  const [editId, setEditId] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)
  const [toast, setToast] = useState(null)
  const [loading, setLoading] = useState(true)
  const [confirmDelete, setConfirmDelete] = useState(null)

  const showToast = (msg, type = 'success') => setToast({ msg, type })

  const loadData = useCallback(async () => {
    try {
      const [j, s] = await Promise.all([api.get('/jobs'), api.get('/jobs/stats')])
      setJobs(j.data)
      setStats(s.data)
    } catch (err) {
      console.error('Failed to load data:', err)
      showToast('Failed to load data', 'error')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadData() }, [loadData])

  const filtered = jobs.filter(j => {
    const q = search.toLowerCase()
    return (!q || j.company.toLowerCase().includes(q) || j.role.toLowerCase().includes(q))
      && (!filterStatus || j.status === filterStatus)
  })

  function openAdd() { setEditId(null); setForm(EMPTY); setModal(true) }
  function openEdit(j) { setEditId(j.id); setForm({ company: j.company, role: j.role, status: j.status, appliedDate: j.appliedDate, notes: j.notes || '' }); setModal(true) }
  function closeModal() { setModal(false) }

  async function handleSave() {
    if (!form.company || !form.role || !form.appliedDate) { showToast('Fill required fields', 'error'); return }
    setSaving(true)
    try {
      editId ? await api.put(`/jobs/${editId}`, form) : await api.post('/jobs', form)
      showToast(editId ? 'Application updated ✓' : 'Application added ✓')
      closeModal(); loadData()
    } catch { showToast('Failed to save', 'error') }
    finally { setSaving(false) }
  }

  async function handleDelete(id) {
    try { await api.delete(`/jobs/${id}`); showToast('Deleted'); loadData() }
    catch { showToast('Failed to delete', 'error') }
    finally { setConfirmDelete(null) }
  }

  const STATS_CONFIG = [
    { label: 'Total Applied', value: stats.totalApplied, sub: 'all time', accent: '#6366f1', glow: 'rgba(99,102,241,0.15)' },
    { label: 'Interviews', value: stats.totalInterviews, sub: 'in progress', accent: '#f59e0b', glow: 'rgba(245,158,11,0.15)' },
    { label: 'Offers', value: stats.totalOffers, sub: `${stats.offerRate?.toFixed(1)}% rate`, accent: '#10b981', glow: 'rgba(16,185,129,0.15)' },
    { label: 'Rejected', value: stats.totalRejections, sub: 'keep pushing', accent: '#ef4444', glow: 'rgba(239,68,68,0.15)' },
  ]

  return (
    <div style={{ minHeight: '100vh', background: '#080c14', fontFamily: "'Inter',system-ui,sans-serif" }}>

      <Navbar />

      <div style={{ maxWidth: '1160px', margin: '0 auto', padding: '36px 32px' }}>

        {/* ── PAGE HEADER ── */}
        <div style={{ marginBottom: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
            <h1 style={{ fontSize: '26px', fontWeight: 700, color: '#f1f5f9', letterSpacing: '-0.5px' }}>Job Applications</h1>
            <span style={{ padding: '3px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: 600, background: 'rgba(99,102,241,0.12)', color: '#818cf8', border: '1px solid rgba(99,102,241,0.2)' }}>{jobs.length} total</span>
          </div>
          <p style={{ fontSize: '14px', color: '#475569' }}>Welcome back, <span style={{ color: '#94a3b8', fontWeight: 500 }}>{user?.name?.split(' ')[0]}</span> — here's your job hunt overview.</p>
        </div>

        {/* ── STATS ── */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '28px' }}>
          {STATS_CONFIG.map((s, i) => <StatsCard key={i} {...s} />)}
        </div>

        {/* ── TOOLBAR ── */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '220px', position: 'relative' }}>
            <span style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', fontSize: '14px', color: '#334155', pointerEvents: 'none' }}>⌕</span>
            <input type="text" placeholder="Search company or role..." value={search} onChange={e => setSearch(e.target.value)}
              style={{ width: '100%', paddingLeft: '38px', paddingRight: '14px', paddingTop: '10px', paddingBottom: '10px', background: '#0d1117', border: '1px solid #161f2e', borderRadius: '10px', fontSize: '14px', color: '#f1f5f9', fontFamily: 'inherit', outline: 'none' }}
              onFocus={e => { e.target.style.borderColor = '#6366f1'; e.target.style.boxShadow = '0 0 0 3px rgba(99,102,241,0.08)' }}
              onBlur={e => { e.target.style.borderColor = '#161f2e'; e.target.style.boxShadow = 'none' }}
            />
          </div>
          <select value={filterStatus} onChange={e => setFilter(e.target.value)}
            style={{ padding: '10px 14px', background: '#0d1117', border: '1px solid #161f2e', borderRadius: '10px', fontSize: '13px', color: '#94a3b8', fontFamily: 'inherit', outline: 'none', cursor: 'pointer', minWidth: '130px' }}
            onFocus={e => e.target.style.borderColor = '#6366f1'}
            onBlur={e => e.target.style.borderColor = '#161f2e'}>
            <option value="">All Status</option>
            <option value="APPLIED">Applied</option>
            <option value="INTERVIEW">Interview</option>
            <option value="OFFER">Offer</option>
            <option value="REJECTED">Rejected</option>
          </select>
          <button onClick={openAdd} style={{ padding: '10px 22px', borderRadius: '10px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: 'white', fontSize: '13px', fontWeight: 600, border: 'none', cursor: 'pointer', fontFamily: 'inherit', boxShadow: '0 4px 20px rgba(99,102,241,0.3)', whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '7px', transition: 'all 0.2s' }}
            onMouseEnter={e => e.currentTarget.style.boxShadow = '0 6px 28px rgba(99,102,241,0.45)'}
            onMouseLeave={e => e.currentTarget.style.boxShadow = '0 4px 20px rgba(99,102,241,0.3)'}>
            <span style={{ fontSize: '16px', lineHeight: 1 }}>+</span> Add Application
          </button>
        </div>

        {/* ── TABLE ── */}
        <div style={{ background: '#0d1117', border: '1px solid #161f2e', borderRadius: '16px', overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#090d15', borderBottom: '1px solid #161f2e' }}>
                  {['Company & Role', 'Status', 'Applied Date', 'Notes', 'Actions'].map(h => (
                    <th key={h} style={{ padding: '13px 20px', textAlign: 'left', fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '1px', color: '#334155', whiteSpace: 'nowrap' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan={5} style={{ textAlign: 'center', padding: '60px', color: '#334155', fontSize: '14px' }}>Loading...</td></tr>
                ) : filtered.length === 0 ? (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', padding: '70px 20px' }}>
                      <div style={{ fontSize: '36px', marginBottom: '12px' }}>📋</div>
                      <div style={{ fontSize: '15px', fontWeight: 600, color: '#475569', marginBottom: '6px' }}>No applications found</div>
                      <div style={{ fontSize: '13px', color: '#334155' }}>{search || filterStatus ? 'Try adjusting your filters' : 'Click "+ Add Application" to get started'}</div>
                    </td>
                  </tr>
                ) : filtered.map((job, i) => {
                  const color = COLORS[i % COLORS.length]
                  const date = job.appliedDate
                    ? new Date(job.appliedDate + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
                    : '—'
                  return (
                    <tr key={job.id} style={{ borderBottom: '1px solid #0f1829', transition: 'background 0.15s', cursor: 'default' }}
                      onMouseEnter={e => e.currentTarget.style.background = '#0f1829'}
                      onMouseLeave={e => e.currentTarget.style.background = 'transparent'}>
                      <td style={{ padding: '16px 20px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div style={{ width: '38px', height: '38px', borderRadius: '10px', background: `${color}18`, border: `1px solid ${color}30`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', fontWeight: 700, color, flexShrink: 0 }}>
                            {job.company.substring(0, 2).toUpperCase()}
                          </div>
                          <div>
                            <div style={{ fontSize: '14px', fontWeight: 600, color: '#e2e8f0' }}>{job.company}</div>
                            <div style={{ fontSize: '12px', color: '#475569', marginTop: '2px' }}>{job.role}</div>
                          </div>
                        </div>
                      </td>
                      <td style={{ padding: '16px 20px' }}><StatusBadge status={job.status} /></td>
                      <td style={{ padding: '16px 20px', fontSize: '12px', fontFamily: 'monospace', color: '#475569', whiteSpace: 'nowrap' }}>{date}</td>
                      <td style={{ padding: '16px 20px', fontSize: '13px', color: '#475569', maxWidth: '160px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{job.notes || '—'}</td>
                      <td style={{ padding: '16px 20px' }}>
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button onClick={() => openEdit(job)} style={{ padding: '6px 14px', borderRadius: '8px', background: '#161f2e', border: '1px solid #1e293b', color: '#94a3b8', fontSize: '12px', fontWeight: 500, cursor: 'pointer', fontFamily: 'inherit', transition: 'all 0.15s' }}
                            onMouseEnter={e => { e.target.style.borderColor = '#6366f1'; e.target.style.color = '#818cf8' }}
                            onMouseLeave={e => { e.target.style.borderColor = '#1e293b'; e.target.style.color = '#94a3b8' }}>
                            Edit
                          </button>
                          <button onClick={() => setConfirmDelete(job.id)} style={{ padding: '6px 14px', borderRadius: '8px', background: 'rgba(239,68,68,0.06)', border: '1px solid rgba(239,68,68,0.15)', color: '#f87171', fontSize: '12px', fontWeight: 500, cursor: 'pointer', fontFamily: 'inherit', transition: 'all 0.15s' }}
                            onMouseEnter={e => e.target.style.background = 'rgba(239,68,68,0.14)'}
                            onMouseLeave={e => e.target.style.background = 'rgba(239,68,68,0.06)'}>
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>

          {filtered.length > 0 && (
            <div style={{ padding: '12px 20px', borderTop: '1px solid #0f1829', fontSize: '12px', color: '#334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>Showing <span style={{ color: '#64748b', fontWeight: 600 }}>{filtered.length}</span> of <span style={{ color: '#64748b', fontWeight: 600 }}>{jobs.length}</span> applications</span>
              {(search || filterStatus) && <button onClick={() => { setSearch(''); setFilter('') }} style={{ fontSize: '12px', color: '#6366f1', background: 'none', border: 'none', cursor: 'pointer', fontFamily: 'inherit' }}>Clear filters</button>}
            </div>
          )}
        </div>
      </div>

      {/* ── MODAL ── */}
      {modal && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 100, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px', background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(8px)' }}
          onClick={e => e.target === e.currentTarget && closeModal()}>
          <div style={{ width: '100%', maxWidth: '460px', background: '#0d1117', border: '1px solid #1e293b', borderRadius: '20px', padding: '32px', boxShadow: '0 40px 80px rgba(0,0,0,0.7)', animation: 'slideUp 0.25s ease' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '28px' }}>
              <div>
                <h3 style={{ fontSize: '18px', fontWeight: 700, color: '#f1f5f9' }}>{editId ? 'Edit Application' : 'New Application'}</h3>
                <p style={{ fontSize: '12px', color: '#475569', marginTop: '3px' }}>{editId ? 'Update the details below' : 'Track a new job application'}</p>
              </div>
              <button onClick={closeModal} style={{ width: '32px', height: '32px', borderRadius: '8px', background: '#161f2e', border: '1px solid #1e293b', color: '#64748b', fontSize: '16px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'inherit', transition: 'all 0.15s' }}
                onMouseEnter={e => { e.target.style.borderColor = '#ef4444'; e.target.style.color = '#f87171' }}
                onMouseLeave={e => { e.target.style.borderColor = '#1e293b'; e.target.style.color = '#64748b' }}>✕</button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <Input label="Company Name *" type="text" value={form.company} placeholder="e.g. Google, Swiggy, Razorpay" onChange={e => setForm({ ...form, company: e.target.value })} />
              <Input label="Role / Position *" type="text" value={form.role} placeholder="e.g. Backend Engineer" onChange={e => setForm({ ...form, role: e.target.value })} />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
                <Select label="Status" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                  <option value="APPLIED">Applied</option>
                  <option value="INTERVIEW">Interview</option>
                  <option value="OFFER">Offer</option>
                  <option value="REJECTED">Rejected</option>
                </Select>
                <Input label="Applied Date *" type="date" value={form.appliedDate} onChange={e => setForm({ ...form, appliedDate: e.target.value })} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.8px', color: '#64748b' }}>Notes (optional)</label>
                <textarea value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} placeholder="Any notes about this application..." rows={3}
                  style={{ width: '100%', background: '#0a0e1a', border: '1px solid #1e293b', borderRadius: '10px', padding: '11px 14px', fontSize: '14px', color: '#f1f5f9', fontFamily: 'inherit', outline: 'none', resize: 'vertical' }}
                  onFocus={e => { e.target.style.borderColor = '#6366f1'; e.target.style.boxShadow = '0 0 0 3px rgba(99,102,241,0.08)' }}
                  onBlur={e => { e.target.style.borderColor = '#1e293b'; e.target.style.boxShadow = 'none' }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '28px' }}>
              <button onClick={closeModal} style={{ flex: 1, padding: '12px', borderRadius: '10px', background: 'transparent', border: '1px solid #1e293b', color: '#64748b', fontSize: '14px', fontWeight: 500, cursor: 'pointer', fontFamily: 'inherit', transition: 'all 0.15s' }}
                onMouseEnter={e => e.target.style.borderColor = '#334155'}
                onMouseLeave={e => e.target.style.borderColor = '#1e293b'}>
                Cancel
              </button>
              <button onClick={handleSave} disabled={saving} style={{ flex: 1, padding: '12px', borderRadius: '10px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: 'white', fontSize: '14px', fontWeight: 600, border: 'none', cursor: saving ? 'not-allowed' : 'pointer', fontFamily: 'inherit', opacity: saving ? 0.7 : 1, boxShadow: '0 4px 20px rgba(99,102,241,0.3)', transition: 'all 0.15s' }}>
                {saving ? 'Saving...' : editId ? 'Update' : 'Save Application'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── CONFIRM DELETE ── */}
      {confirmDelete && (
        <ConfirmDialog
          title="Delete Application"
          message="Are you sure you want to delete this application? This action cannot be undone."
          onConfirm={() => handleDelete(confirmDelete)}
          onCancel={() => setConfirmDelete(null)}
        />
      )}

      {/* ── TOAST ── */}
      {toast && <Toast message={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      <style>{`
        @keyframes slideUp {
          from { opacity:0; transform:translateY(16px) scale(0.97); }
          to   { opacity:1; transform:translateY(0)    scale(1);    }
        }
        * { box-sizing: border-box; }
        input[type="date"]::-webkit-calendar-picker-indicator { filter: invert(0.4); cursor: pointer; }
        select option { background: #0a0e1a; }
      `}</style>
    </div>
  )
}