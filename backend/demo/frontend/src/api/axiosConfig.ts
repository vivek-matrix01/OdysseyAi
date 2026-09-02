import axios from 'axios';
import toast from 'react-hot-toast';

// Get API base URL from .env file (single point of configuration)
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to add JWT token if available
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor to handle responses and token expiration
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    // Global Network or Server Error Toasts
    if (!error.response) {
      toast.error('Network error. Please check your connection.');
    } else if (error.response.status >= 500) {
      toast.error('Server error. Our team has been notified.');
    }
    
    // If error is 401 and we haven't retried yet
    if (error.response && error.response.status === 401 && !originalRequest._retry && originalRequest.url !== '/auth/login') {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('No refresh token');
        
        // Call refresh endpoint
        const res = await axios.post(`${baseURL}/auth/refresh`, { refreshToken });
        
        // Save new tokens
        const { accessToken, refreshToken: newRefreshToken } = res.data.data;
        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        
        // Update header and retry original request
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axiosInstance(originalRequest);
        
      } catch (refreshError) {
        // Refresh failed, clear everything and go to login
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        toast.error('Session expired. Please log in again.');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
