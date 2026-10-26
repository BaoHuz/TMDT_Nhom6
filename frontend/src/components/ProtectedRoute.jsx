import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ roles, children }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`} replace />;
  }

  if (roles && !roles.includes(user.role)) {
    const home = user.role === 'ADMIN' ? '/admin' : user.role === 'STAFF' ? '/staff/check-in' : '/';
    return <Navigate to={home} replace />;
  }

  return children;
}
