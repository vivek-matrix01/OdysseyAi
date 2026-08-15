import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { apiRequest } from '../api/client';
import { parseJwt, getTokenTimeRemaining } from '../utils/jwt';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem('odyssey_access_token') || '');
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem('odyssey_refresh_token') || '');
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [timeRemaining, setTimeRemaining] = useState(0);
  const [lastApiLog, setLastApiLog] = useState(null);

  // Sync tokens to localStorage
  const saveTokens = (access, refresh) => {
    setAccessToken(access);
    setRefreshToken(refresh);
    if (access) localStorage.setItem('odyssey_access_token', access);
    else localStorage.removeItem('odyssey_access_token');
    
    if (refresh) localStorage.setItem('odyssey_refresh_token', refresh);
    else localStorage.removeItem('odyssey_refresh_token');
  };

  // Decode user roles / email from JWT if available
  const decodedToken = parseJwt(accessToken);

  // Timer countdown for JWT expiration
  useEffect(() => {
    if (!accessToken) {
      setTimeRemaining(0);
      return;
    }

    const updateTimer = () => {
      const remaining = getTokenTimeRemaining(accessToken);
      setTimeRemaining(remaining);
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);
    return () => clearInterval(interval);
  }, [accessToken]);

  // Fetch logged in user profile (/api/users/me)
  const fetchProfile = useCallback(async (tokenOverride = null) => {
    const activeToken = tokenOverride || accessToken;
    if (!activeToken) {
      setUser(null);
      return null;
    }
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/users/me',
      method: 'GET',
      token: activeToken,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok && res.data?.data) {
      setUser(res.data.data);
      return res.data.data;
    } else {
      // If token expired or invalid, user profile fetch failed
      if (res.status === 401 || res.status === 403) {
        setUser(null);
      }
      return null;
    }
  }, [accessToken]);

  // Fetch profile when accessToken changes
  useEffect(() => {
    if (accessToken) {
      fetchProfile();
    } else {
      setUser(null);
    }
  }, [accessToken, fetchProfile]);

  // Handle Login success
  const login = async (accessTok, refreshTok) => {
    saveTokens(accessTok, refreshTok);
    await fetchProfile(accessTok);
  };

  // Refresh Token call
  const refreshSession = async () => {
    if (!refreshToken) return { ok: false, message: 'No refresh token stored' };
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/auth/refresh',
      method: 'POST',
      body: { refreshToken },
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok && res.data?.data) {
      const newAccess = res.data.data.accessToken;
      const newRefresh = res.data.data.refreshToken;
      saveTokens(newAccess, newRefresh);
      await fetchProfile(newAccess);
      return { ok: true, data: res.data };
    } else {
      return { ok: false, error: res.data?.message || 'Refresh failed' };
    }
  };

  // Logout call
  const logout = async () => {
    if (accessToken) {
      const res = await apiRequest({
        endpoint: '/api/auth/logout',
        method: 'POST',
        token: accessToken,
      });
      setLastApiLog(res);
    }
    saveTokens('', '');
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        accessToken,
        refreshToken,
        user,
        setUser,
        loading,
        timeRemaining,
        decodedToken,
        saveTokens,
        login,
        logout,
        refreshSession,
        fetchProfile,
        lastApiLog,
        setLastApiLog,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
