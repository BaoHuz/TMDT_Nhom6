export const getAccessToken = () => localStorage.getItem('pool_access_token');
export const getRefreshToken = () => localStorage.getItem('pool_refresh_token');
export const getStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem('pool_user') || 'null');
  } catch {
    return null;
  }
};
export const clearAuthStorage = () => {
  localStorage.removeItem('pool_access_token');
  localStorage.removeItem('pool_refresh_token');
  localStorage.removeItem('pool_user');
};
