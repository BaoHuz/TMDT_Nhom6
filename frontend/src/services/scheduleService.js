import api from '../api/axiosClient';
export const scheduleService={list:params=>api.get('/schedules',{params}),get:id=>api.get(`/schedules/${id}`)};
