import api from '../api/axiosClient';
export const catalogService={pools:()=>api.get('/pools'),pool:id=>api.get(`/pools/${id}`),areas:poolId=>api.get('/areas',{params:{poolId}}),services:()=>api.get('/services')};
