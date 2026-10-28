import React, { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

const KEYS = ['pool_access_token', 'pool_refresh_token', 'pool_user'];
const clear = () => KEYS.forEach(key => localStorage.removeItem(key));

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('pool_user'));
    } catch {
      return null;
    }
  });

  const saveTokenData = useCallback((token) => {
    if (!token) return null;
    localStorage.setItem('pool_access_token', token.accessToken);
    localStorage.setItem('pool_refresh_token', token.refreshToken);
    const next = {
      role: token.role,
      fullName: token.fullName,
      username: token.username
    };
    localStorage.setItem('pool_user', JSON.stringify(next));
    setUser(next);
    return next;
  }, []);

  const login = useCallback(async (data) => {
    // If token data is passed directly
    if (data?.accessToken) {
      return saveTokenData(data);
    }
    // Otherwise treat as login credentials { email, password }
    const res = await authService.login(data);
    return saveTokenData(res.data.data);
  }, [saveTokenData]);

  const register = useCallback(async (data) => {
    // If token data is passed directly
    if (data?.accessToken) {
      return saveTokenData(data);
    }
    // Otherwise treat as registration form data
    const res = await authService.register(data);
    return saveTokenData(res.data.data);
  }, [saveTokenData]);

  const logout = useCallback(() => {
    clear();
    setUser(null);
  }, []);

  useEffect(() => {
    const expired = () => {
      clear();
      setUser(null);
    };
    window.addEventListener('aquaflow:session-expired', expired);
    return () => window.removeEventListener('aquaflow:session-expired', expired);
  }, []);

  const value = useMemo(() => ({
    user,
    isAuthenticated: !!user,
    login,
    register,
    logout,
  }), [user, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
