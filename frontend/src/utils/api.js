export const BASE_URL = 'http://localhost:8080/api';

export const getAuthHeaders = () => {
  const token = localStorage.getItem('starcine_token');
  return { 
    'Content-Type': 'application/json', 
    ...(token && token !== 'null' ? { Authorization: `Bearer ${token}` } : {}) 
  };
};

/**
 * PROXY PATTERN (Structural Pattern)
 * Đối tượng Proxy đại diện cho 'fetch' API gốc của trình duyệt.
 * Nó chặn (intercept) các request để tự động kiểm tra lỗi 401 (Unauthorized).
 * Nếu phát hiện token hết hạn/lỗi, nó tự động dọn dẹp localStorage và đưa user về trang login.
 */
export const ApiProxy = new Proxy(window.fetch, {
  apply: async function (target, thisArg, argumentsList) {
    try {
      // Gọi fetch gốc
      const response = await target.apply(thisArg, argumentsList);
      
      // Hook xử lý lỗi bảo mật toàn cục
      if (response.status === 401) {
        console.warn('[Proxy Pattern] Phát hiện lỗi 401 Unauthorized. Tự động Clear Token!');
        localStorage.removeItem('starcine_token');
        localStorage.removeItem('starcine_user');
        // Ép Reload để xóa trạng thái bị treo trong Redux/UI
        window.location.href = '/login';
      }
      return response;
    } catch (error) {
      throw error;
    }
  }
});

