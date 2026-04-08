export const BASE_URL = 'http://localhost:8080/api';

export const getAuthHeaders = () => {
  const token = localStorage.getItem('starcine_token');
  return { 
    'Content-Type': 'application/json', 
    ...(token ? { Authorization: `Bearer ${token}` } : {}) 
  };
};
