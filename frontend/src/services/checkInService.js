import api from '../api/axiosClient';
export const checkInService={preview:qr=>api.get('/check-in/preview',{params:{qr}}),check:data=>api.post('/check-in',data)};
