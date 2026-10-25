import api from '../api/axiosClient';
export const authService={register:data=>api.post('/auth/register',data),login:data=>api.post('/auth/login',data)};
export const scheduleService={list:params=>api.get('/schedules',{params})};
export const bookingService={create:data=>api.post('/bookings',data),mine:()=>api.get('/bookings/my'),cancel:id=>api.put(`/bookings/${id}/cancel`)};
export const paymentService={pay:data=>api.post('/payments',data)};
export const checkInService={check:data=>api.post('/check-in',data)};
export const adminService={dashboard:()=>api.get('/admin/dashboard'),bookings:()=>api.get('/admin/bookings')};
