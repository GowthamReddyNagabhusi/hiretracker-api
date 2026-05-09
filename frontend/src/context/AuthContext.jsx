import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('ht_token'))
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('ht_user') || 'null'))

  function login(data) {
    localStorage.setItem('ht_token', data.token)
    localStorage.setItem('ht_user', JSON.stringify({ name: data.name, email: data.email }))
    setToken(data.token)
    setUser({ name: data.name, email: data.email })
  }

  function logout() {
    localStorage.removeItem('ht_token')
    localStorage.removeItem('ht_user')
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ token, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}