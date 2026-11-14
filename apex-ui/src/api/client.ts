import axios from 'axios';

const DEV_TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJFTVAtMDAxIiwicm9sZXMiOiJST0xFX0FETUlOLFJPTEVfTUFOQUdFUiIsImNvc3RfY2VudGVyX2lkIjoiQ0MtTUZHLTAxIiwiaWF0IjoxNzk0Njc3ODcxLCJleHAiOjE4MjYyMTM4NzF9.-4bnbBA0M5wnprAbpPqoynpIfICSq7iQGEQJRxQi70xsBqVGjU9kpg8icKvNgFSgQKyOW7UEG8z5bnvtMifpgw';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('apex_auth_token') || DEV_TOKEN;
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.warn('Session expired or unauthorized');
    }
    return Promise.reject(error);
  }
);

export default api;

