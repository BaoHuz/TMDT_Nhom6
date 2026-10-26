import api from '../api/axiosClient';
export const bookingService={create:data=>api.post('/bookings',data),mine:()=>api.get('/bookings/my'),get:id=>api.get(`/bookings/${id}`),cancelPreview:id=>api.get(`/bookings/${id}/cancel-preview`),cancel:(id,reason)=>api.post(`/bookings/${id}/cancel`,{reason})};
