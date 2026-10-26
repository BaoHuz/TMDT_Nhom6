import api from '../api/axiosClient';
export const profileService={get:()=>api.get('/profile'),update:data=>api.put('/profile',data)};
