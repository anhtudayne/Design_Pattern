import { BASE_URL, getAuthHeaders } from './api';

/**
 * Uploads a file to Cloudinary using secure, signed credentials from the backend.
 * @param {File} file The image file selected by the user.
 * @param {string} folder Optional folder on Cloudinary to organize assets.
 * @returns {Promise<string>} The secure HTTPS URL of the uploaded image.
 */
export const uploadToCloudinary = async (file, folder = 'general') => {
  if (!file) {
    throw new Error('Chưa chọn tệp ảnh để tải lên.');
  }

  try {
    // 1. Get credentials (signature, api_key, etc.) from backend
    // Backend endpoint: @PostMapping("/api/cloudinary/credentials")
    const res = await fetch(`${BASE_URL}/cloudinary/credentials`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ folder })
    });
    
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      throw new Error(errorData.message || 'Lỗi khi yêu cầu cấp quyền từ máy chủ.');
    }
    
    const creds = await res.json();
    if (!creds.signature || !creds.api_key || !creds.cloud_name) {
      throw new Error('Máy chủ trả về thông tin xác thực Cloudinary không hợp lệ.');
    }

    // 2. Construct FormData for Cloudinary signed upload
    const formData = new FormData();
    formData.append('file', file);
    formData.append('api_key', creds.api_key);
    formData.append('timestamp', creds.timestamp);
    formData.append('signature', creds.signature);
    if (creds.folder) formData.append('folder', creds.folder);

    // 3. Upload directly to Cloudinary's secure API
    const uploadUrl = `https://api.cloudinary.com/v1_1/${creds.cloud_name}/image/upload`;
    const cloudRes = await fetch(uploadUrl, {
      method: 'POST',
      body: formData
    });
    
    if (!cloudRes.ok) {
      const cloudError = await cloudRes.json().catch(() => ({}));
      throw new Error(cloudError.error?.message || 'Tải ảnh lên Cloudinary thất bại.');
    }
    
    const data = await cloudRes.json();
    return data.secure_url;
  } catch (err) {
    console.error('Cloudinary Upload Service Error:', err);
    throw err;
  }
};
