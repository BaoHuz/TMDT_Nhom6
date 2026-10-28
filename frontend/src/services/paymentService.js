import api from '../api/axiosClient';
export const paymentService={pay:data=>api.post('/payments',data),createVnPay:maDat=>api.post('/payments/vnpay',null,{params:{maDat}})};
