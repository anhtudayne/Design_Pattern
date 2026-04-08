import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { BASE_URL } from '../utils/api';

const API_BASE = 'http://localhost:8080/api/auth';

// ── Helpers ─────────────────────────────────────────────────────────────────
const saveToLocalStorage = (authData) => {
  localStorage.setItem('starcine_token', authData.token);
  localStorage.setItem('starcine_user', JSON.stringify({
    id: authData.id,
    email: authData.email,
    roles: authData.roles,
    type: authData.type,
  }));
};

const clearLocalStorage = () => {
  localStorage.removeItem('starcine_token');
  localStorage.removeItem('starcine_user');
};

const loadFromLocalStorage = () => {
  try {
    const token = localStorage.getItem('starcine_token');
    const user = JSON.parse(localStorage.getItem('starcine_user'));
    if (token && user) return { token, user };
  } catch (_) {}
  return null;
};

// ── Async Thunks ─────────────────────────────────────────────────────────────

/**
 * POST /api/auth/login
 * Body: { email, password }
 * Response: { token, type, id, email, roles }
 */
export const loginUser = createAsyncThunk(
  'auth/login',
  async ({ email, password }, { rejectWithValue }) => {
    try {
      const res = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Đăng nhập thất bại');
      return data; // JwtResponse: { token, type, id, email, roles }
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);



/**
 * POST /api/auth/google-login
 * Body: { idToken }
 */
export const googleLogin = createAsyncThunk(
  'auth/googleLogin',
  async (idToken, { rejectWithValue }) => {
    try {
      const res = await fetch(`${API_BASE}/google-login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idToken }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Đăng nhập Google thất bại');
      return data;
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);

/**
 * POST /api/auth/register
 * Body: { fullname, email, password, phone }
 * Response: { message }
 */
export const registerUser = createAsyncThunk(
  'auth/register',
  async ({ fullName, email, password, phone }, { rejectWithValue }) => {
    try {
      const res = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullname: fullName, email, password, phone }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Đăng ký thất bại');
      return data; // MessageResponse: { message }
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);

/**
 * GET /api/users/me
 * Headers: { Authorization }
 */
export const fetchProfile = createAsyncThunk(
  'auth/fetchProfile',
  async (_, { getState, rejectWithValue }) => {
    try {
      const state = getState();
      const token = state.auth.token;
      if (!token) throw new Error('No token found');
      
      const res = await fetch(`${BASE_URL}/users/me`, {
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Lấy profile thất bại');
      return data;
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);

// ── Initial State ─────────────────────────────────────────────────────────────
const persisted = loadFromLocalStorage();

const initialState = {
  token: persisted?.token ?? null,
  user: persisted?.user ?? null,        // { id, email, roles, type, fullname }
  isAuthenticated: !!persisted?.token,
  status: 'idle',                        // 'idle' | 'loading' | 'succeeded' | 'failed'
  error: null,
  registerStatus: 'idle',
  registerError: null,
  registerSuccess: null,
};

// ── Slice ─────────────────────────────────────────────────────────────────────
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout(state) {
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      state.status = 'idle';
      state.error = null;
      clearLocalStorage();
    },
    clearAuthError(state) {
      state.error = null;
      state.registerError = null;
    },
    clearRegisterStatus(state) {
      state.registerStatus = 'idle';
      state.registerError = null;
      state.registerSuccess = null;
    },
  },
  extraReducers: (builder) => {
    // ── Login ─────────────────────────
    builder
      .addCase(loginUser.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        const { token, type, id, email, roles } = action.payload;
        state.status = 'succeeded';
        state.token = token;
        state.user = { id, email, roles, type };
        state.isAuthenticated = true;
        state.error = null;
        saveToLocalStorage({ token, type, id, email, roles });
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
        state.isAuthenticated = false;
      })
      // ── Google Login ──────────────────
      .addCase(googleLogin.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(googleLogin.fulfilled, (state, action) => {
        const { token, type, id, email, roles } = action.payload;
        state.status = 'succeeded';
        state.token = token;
        state.user = { id, email, roles, type };
        state.isAuthenticated = true;
        state.error = null;
        saveToLocalStorage({ token, type, id, email, roles });
      })
      .addCase(googleLogin.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
        state.isAuthenticated = false;
      });

    // ── Register ──────────────────────
    builder
      .addCase(registerUser.pending, (state) => {
        state.registerStatus = 'loading';
        state.registerError = null;
        state.registerSuccess = null;
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.registerStatus = 'succeeded';
        state.registerSuccess = action.payload.message;
        state.registerError = null;
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.registerStatus = 'failed';
        state.registerError = action.payload;
        state.registerSuccess = null;
      });

    // ── Fetch Profile ──────────────────
    builder
      .addCase(fetchProfile.fulfilled, (state, action) => {
        state.user = { 
          ...state.user, 
          fullname: action.payload.fullname,
          phone: action.payload.phone,
          tierName: action.payload.tierName
        };
      });
  },
});

export const { logout, clearAuthError, clearRegisterStatus } = authSlice.actions;

// ── Selectors ─────────────────────────────────────────────────────────────────
export const selectIsAuthenticated = (state) => state.auth.isAuthenticated;
export const selectCurrentUser = (state) => state.auth.user;
export const selectToken = (state) => state.auth.token;
export const selectAuthStatus = (state) => state.auth.status;
export const selectAuthError = (state) => state.auth.error;
export const selectRegisterStatus = (state) => state.auth.registerStatus;
export const selectRegisterError = (state) => state.auth.registerError;
export const selectRegisterSuccess = (state) => state.auth.registerSuccess;

export default authSlice.reducer;
