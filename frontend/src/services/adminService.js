import api from '../api/axiosClient';
export const adminService={
  dashboard:()=>api.get('/admin/dashboard'),
  bookings:()=>api.get('/admin/bookings'),
  pools:()=>api.get('/admin/pools'),
  areas:()=>api.get('/admin/areas'),
  accounts:()=>api.get('/admin/accounts'),
  services:()=>api.get('/admin/services'),
  schedules:()=>api.get('/admin/schedules'),
  createService:data=>api.post('/admin/services',data),
  updateService:(id,data)=>api.put(`/admin/services/${id}`,data),
  createSchedule:data=>api.post('/admin/schedules',data),
  closeSchedule:id=>api.put(`/admin/schedules/${id}/close`),
  cancelSchedule:(id,reason)=>api.post(`/admin/schedules/${id}/cancel`,{reason}),
  createPool:data=>api.post('/admin/pools',data),
  updatePool:(id,data)=>api.put(`/admin/pools/${id}`,data),
  createArea:data=>api.post('/admin/areas',data),
  updateArea:(id,data)=>api.put(`/admin/areas/${id}`,data),
  createStaff:data=>api.post('/admin/accounts/staff',data),
  lockAccount:id=>api.put(`/admin/accounts/${id}/lock`),
  unlockAccount:id=>api.put(`/admin/accounts/${id}/unlock`)
};
