/**
 * Centralized Axios API client.
 *
 * WHY THIS FILE EXISTS:
 * Previously every component had: fetch('http://localhost:8080/api/...')
 * That's hardcoded, untestable, and breaks in production.
 * Now every request goes through this single client which:
 *   1. Automatically attaches the JWT Bearer token
 *   2. Auto-refreshes the token on 401 responses
 *   3. Uses the VITE_API_URL env variable (never hardcoded)
 *
 * Usage in components:
 *   import api from '../../lib/api';
 *   const data = await api.post('/auth/customer/login', { email, password });
 */

import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // 15s timeout
});

// ─── Request Interceptor ──────────────────────────────────────────────────────
// Automatically attach JWT Bearer token to every outgoing request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response Interceptor ─────────────────────────────────────────────────────
// On 401 (token expired), attempt to refresh the token automatically
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Queue this request while refresh is in progress
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        // No refresh token — force logout
        clearAuth();
        window.location.href = '/';
        return Promise.reject(error);
      }

      try {
        const response = await axios.post(
          `${import.meta.env.VITE_API_URL}/auth/refresh`,
          { refreshToken }
        );
        const { accessToken } = response.data.data;
        localStorage.setItem('accessToken', accessToken);
        api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
        processQueue(null, accessToken);
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearAuth();
        window.location.href = '/';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// ─── Auth Helpers ─────────────────────────────────────────────────────────────

export const saveAuth = ({ accessToken, refreshToken, role, userId, name, email }) => {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
  localStorage.setItem('userRole', role);
  localStorage.setItem('userId', String(userId));
  localStorage.setItem('userName', name);
  localStorage.setItem('userEmail', email);

  // Legacy compatibility: components checking 'customerId' still work
  if (role === 'CUSTOMER') localStorage.setItem('customerId', String(userId));
  if (role === 'AGENT')    localStorage.setItem('agentId', String(userId));
  if (role === 'ADMIN')    localStorage.setItem('adminId', String(userId));
};

export const clearAuth = () => {
  ['accessToken', 'refreshToken', 'userRole', 'userId', 'userName', 'userEmail',
   'customerId', 'agentId', 'adminId'].forEach((k) => localStorage.removeItem(k));
};

export const getUser = () => ({
  userId: parseInt(localStorage.getItem('userId') || '0'),
  role:   localStorage.getItem('userRole') || '',
  name:   localStorage.getItem('userName') || '',
  email:  localStorage.getItem('userEmail') || '',
});

export const isAuthenticated = () => !!localStorage.getItem('accessToken');

export default api;
