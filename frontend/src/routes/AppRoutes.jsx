import React from 'react';
import { Routes, Route } from 'react-router-dom';

import PublicLayout from '../layouts/PublicLayout';
import AdminLayout from '../layouts/AdminLayout';
import StaffLayout from '../layouts/StaffLayout';
import ProtectedRoute from '../components/ProtectedRoute';

/* Public pages */
import HomePage from '../pages/public/HomePage';
import PoolsPage from '../pages/public/PoolsPage';
import PoolDetailPage from '../pages/public/PoolDetailPage';
import ServicesPage from '../pages/public/ServicesPage';
import SchedulesPage from '../pages/public/SchedulesPage';
import ScheduleDetailPage from '../pages/public/ScheduleDetailPage';

/* Auth pages */
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';

/* Customer pages */
import CheckoutPage from '../pages/customer/CheckoutPage';
import MyBookingsPage from '../pages/customer/MyBookingsPage';
import BookingDetailPage from '../pages/customer/BookingDetailPage';
import ProfilePage from '../pages/customer/ProfilePage';

/* Staff pages */
import CheckInPage from '../pages/staff/CheckInPage';

/* Admin pages */
import DashboardPage from '../pages/admin/DashboardPage';
import PoolsManagementPage from '../pages/admin/PoolsManagementPage';
import AreasManagementPage from '../pages/admin/AreasManagementPage';
import ServicesManagementPage from '../pages/admin/ServicesManagementPage';
import SchedulesManagementPage from '../pages/admin/SchedulesManagementPage';
import BookingsManagementPage from '../pages/admin/BookingsManagementPage';
import AccountsManagementPage from '../pages/admin/AccountsManagementPage';

export default function AppRoutes() {
  return (
    <Routes>
      {/* ─── PUBLIC + CUSTOMER layout ─── */}
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="pools" element={<PoolsPage />} />
        <Route path="pools/:id" element={<PoolDetailPage />} />
        <Route path="services" element={<ServicesPage />} />
        <Route path="schedules" element={<SchedulesPage />} />
        <Route path="schedules/:id" element={<ScheduleDetailPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        {/* Protected customer routes inside public layout */}
        <Route path="checkout/:scheduleId" element={
          <ProtectedRoute roles={['CUSTOMER']}><CheckoutPage /></ProtectedRoute>
        } />
        <Route path="my/bookings" element={
          <ProtectedRoute roles={['CUSTOMER']}><MyBookingsPage /></ProtectedRoute>
        } />
        <Route path="my/bookings/:id" element={
          <ProtectedRoute roles={['CUSTOMER']}><BookingDetailPage /></ProtectedRoute>
        } />
        <Route path="my/profile" element={
          <ProtectedRoute roles={['CUSTOMER']}><ProfilePage /></ProtectedRoute>
        } />
      </Route>

      {/* ─── STAFF layout ─── */}
      <Route path="staff" element={
        <ProtectedRoute roles={['STAFF', 'ADMIN']}><StaffLayout /></ProtectedRoute>
      }>
        <Route path="check-in" element={<CheckInPage />} />
      </Route>

      {/* ─── ADMIN layout ─── */}
      <Route path="admin" element={
        <ProtectedRoute roles={['ADMIN']}><AdminLayout /></ProtectedRoute>
      }>
        <Route index element={<DashboardPage />} />
        <Route path="pools" element={<PoolsManagementPage />} />
        <Route path="areas" element={<AreasManagementPage />} />
        <Route path="services" element={<ServicesManagementPage />} />
        <Route path="schedules" element={<SchedulesManagementPage />} />
        <Route path="bookings" element={<BookingsManagementPage />} />
        <Route path="accounts" element={<AccountsManagementPage />} />
      </Route>
    </Routes>
  );
}
