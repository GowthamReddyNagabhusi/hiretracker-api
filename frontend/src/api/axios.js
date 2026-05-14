import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
})

// ── Request Interceptor: attach access token ──
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('ht_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── Response Interceptor: auto-refresh on 401/403 ──
let isRefreshing = false
let failedQueue = []

function processQueue(error, token = null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // If 401/403 and we haven't retried yet, try refreshing the token
    if (
      (error.response?.status === 401 || error.response?.status === 403) &&
      !originalRequest._retry
    ) {
      // Don't try to refresh if the failing request IS the refresh request
      if (originalRequest.url === '/auth/refresh' || originalRequest.url === '/auth/login') {
        clearAuth()
        return Promise.reject(error)
      }

      if (isRefreshing) {
        // Queue this request until refresh completes
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = localStorage.getItem('ht_refresh_token')
        if (!refreshToken) {
          clearAuth()
          return Promise.reject(error)
        }

        const res = await api.post('/auth/refresh', { refreshToken })
        const { token, refreshToken: newRefreshToken } = res.data

        localStorage.setItem('ht_token', token)
        localStorage.setItem('ht_refresh_token', newRefreshToken)

        processQueue(null, token)

        originalRequest.headers.Authorization = `Bearer ${token}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        clearAuth()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

function clearAuth() {
  localStorage.removeItem('ht_token')
  localStorage.removeItem('ht_refresh_token')
  localStorage.removeItem('ht_user')
  window.location.href = '/login'
}

export default api