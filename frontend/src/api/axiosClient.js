import axios from 'axios';
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const axiosClient=axios.create({baseURL:API_URL,headers:{'Content-Type':'application/json'}});
axiosClient.interceptors.request.use(config=>{const token=localStorage.getItem('pool_access_token');if(token)config.headers.Authorization=`Bearer ${token}`;return config;});
let refreshing=null;
axiosClient.interceptors.response.use(response=>response,async error=>{
  const original=error.config;
  if(error.response?.status!==401||original?._retry||original?.url==='/auth/refresh')throw error;
  const refreshToken=localStorage.getItem('pool_refresh_token');
  if(!refreshToken)throw error;
  original._retry=true;
  refreshing??=axios.post(`${API_URL}/auth/refresh`,{refreshToken}).finally(()=>{refreshing=null});
  try{const response=await refreshing;const tokens=response.data.data;localStorage.setItem('pool_access_token',tokens.accessToken);localStorage.setItem('pool_refresh_token',tokens.refreshToken);original.headers.Authorization=`Bearer ${tokens.accessToken}`;return axiosClient(original)}
  catch(refreshError){localStorage.removeItem('pool_access_token');localStorage.removeItem('pool_refresh_token');localStorage.removeItem('pool_user');window.dispatchEvent(new Event('aquaflow:session-expired'));throw refreshError}
});
export default axiosClient;
