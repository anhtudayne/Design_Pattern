import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { 
    googleLogin, loginUser, registerUser, clearAuthError, clearRegisterStatus,
    selectAuthStatus, selectAuthError, selectRegisterStatus, selectRegisterError, selectRegisterSuccess, selectCurrentUser
} from '../store/authSlice';
// import { useGoogleLogin, GoogleLogin } from '@react-oauth/google';

function FormInput({ id, icon, label, type = 'text', placeholder, value, onChange, required, minLength, error, children }) {
  return (
    <div className="space-y-1.5 w-full">
      {label && <label className="text-[11px] font-black text-slate-400 tracking-wider uppercase" htmlFor={id}>{label}</label>}
      <div className="relative">
        <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xl pointer-events-none">{icon}</span>
        <input id={id} type={type} required={required} minLength={minLength} placeholder={placeholder} value={value} onChange={onChange}
          className={`w-full pl-12 pr-12 py-4 rounded-[1.25rem] bg-white border ${error ? 'border-red-500/60 focus:border-red-500' : 'border-slate-100 focus:border-orange-500/50'} text-slate-800 placeholder-slate-300 text-sm focus:outline-none shadow-sm focus:shadow-md transition-all duration-300`} />
        {children}
      </div>
    </div>
  );
}

function TogglePassword({ show, onClick, id }) {
  return (
    <button type="button" id={id} tabIndex={-1} onClick={onClick} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors">
      <span className="material-symbols-outlined text-xl">{show ? 'visibility_off' : 'visibility'}</span>
    </button>
  );
}

const Spinner = () => (
  <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
  </svg>
);

export default function Login() {
  const [activeTab, setActiveTab] = useState('login');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();

  const loginStatus = useSelector(selectAuthStatus);
  const loginError = useSelector(selectAuthError);
  const registerStatus = useSelector(selectRegisterStatus);
  const registerError = useSelector(selectRegisterError);
  const registerSuccess = useSelector(selectRegisterSuccess);
  const isLoginLoading = loginStatus === 'loading';
  const isRegisterLoading = registerStatus === 'loading';

  const [loginForm, setLoginForm] = useState({ email: '', password: '', remember: false });
  const [registerForm, setRegisterForm] = useState({ fullName: '', email: '', phone: '', password: '', confirmPassword: '', agreeTerms: false });

  const currentUser = useSelector(selectCurrentUser);

  useEffect(() => {
    if (loginStatus === 'succeeded' && currentUser) {
      const returnTo = typeof location.state?.from === 'string' ? location.state.from : null;
      if (currentUser.roles?.includes('ROLE_ADMIN')) {
        navigate('/admin');
      } else if (currentUser.roles?.includes('ROLE_STAFF')) {
        navigate('/staff');
      } else if (returnTo) {
        navigate(returnTo);
      } else {
        navigate('/');
      }
    }
  }, [loginStatus, navigate, currentUser, location.state]);
  useEffect(() => {
    if (registerStatus === 'succeeded') {
      const t = setTimeout(() => { dispatch(clearRegisterStatus()); setActiveTab('login'); setRegisterForm({ fullName: '', email: '', phone: '', password: '', confirmPassword: '', agreeTerms: false }); }, 2000);
      return () => clearTimeout(t);
    }
  }, [registerStatus, dispatch]);

  const handleTabSwitch = (tab) => { dispatch(clearAuthError()); if (tab === 'login') dispatch(clearRegisterStatus()); setActiveTab(tab); };
  const handleLoginSubmit = (e) => { 
    e.preventDefault(); 
    console.log(">>> [StarCine] Attempting login with:", loginForm.email);
    dispatch(loginUser({ email: loginForm.email, password: loginForm.password })); 
  };
  const handleRegisterSubmit = (e) => { e.preventDefault(); if (registerForm.password !== registerForm.confirmPassword) return; dispatch(registerUser({ fullName: registerForm.fullName, email: registerForm.email, password: registerForm.password, phone: registerForm.phone })); };
  const pwMismatch = registerForm.confirmPassword && registerForm.password !== registerForm.confirmPassword;

  // ── GOOGLE LOGIN LOGIC ──
  /* const handleGoogleLogin = useGoogleLogin({
    onSuccess: (tokenResponse) => {
      // Lưu ý: tokenResponse.access_token là Access Token
      // Nhưng Backend của chúng ta cần ID Token. 
      // Tuy nhiên, mốt số thư viện Google login mới trả về Implicit Flow.
      // Nếu dùng @react-oauth/google, onSuccess mặc định trả về code (nếu flow=auth-code) 
      // hoặc access_token (nếu flow=implicit).
      // Để lấy ID Token, ta nên dùng GoogleLogin component hoặc flow 'auth-code'.
      // Nhưng để đơn giản nhất, ta có thể dùng ID Token từ GoogleLogin component hoặc fetch từ access_token.
      // Giải pháp tốt nhất là dùng credential từ GoogleLogin component.
      console.log("Google Login Success:", tokenResponse);
      // Backend cần idToken. Nếu dùng useGoogleLogin, ta nhận được access_token.
      // Ta sẽ sửa backend để chấp nhận access_token hoặc dùng component chính thức.
    },
    onError: () => console.log('Login Failed'),
  }); */

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-slate-50 relative overflow-hidden font-sans">
      
      <style>{`
        @keyframes rotate-blob { 
          0%, 100% { transform: translate(0, 0) scale(1); }
          50% { transform: translate(20px, -20px) scale(1.1); }
        }
        .animate-blob { animation: rotate-blob 10s infinite; }
        @keyframes shimmer { 0% { transform: translateX(-100%) skewX(-15deg); } 100% { transform: translateX(300%) skewX(-15deg); } }
        .btn-shimmer::after { content:''; position:absolute; top:0; left:0; width:50%; height:100%; background:linear-gradient(90deg,transparent,rgba(255,255,255,0.4),transparent); animation:shimmer 2.5s infinite; }
      `}</style>

      {/* Decorative Background Blobs */}
      <div className="absolute top-0 -left-20 w-96 h-96 bg-orange-100 rounded-full blur-[100px] opacity-60 animate-blob" />
      <div className="absolute bottom-0 -right-20 w-[30rem] h-[30rem] bg-red-100 rounded-full blur-[120px] opacity-60 animate-blob" style={{ animationDelay: '2s' }} />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full h-full opacity-[0.02]" style={{ backgroundImage: 'radial-gradient(#f97316 1px, transparent 0)', backgroundSize: '40px 40px' }} />

      <div className="relative z-10 w-full max-w-[480px] px-6">
        
        {/* Logo Section */}
        <div className="text-center mb-10">
          <Link to="/" className="inline-flex items-center gap-4 group">
            <div className="w-16 h-16 rounded-[1.5rem] bg-gradient-to-tr from-yellow-300 via-orange-400 to-red-500 flex items-center justify-center shadow-[0_12px_35px_rgba(249,115,22,0.45)] group-hover:scale-110 transition-all duration-500">
              <span className="material-symbols-outlined text-white text-4xl font-black">stars</span>
            </div>
            <span className="text-4xl font-black tracking-tighter text-slate-800">Star<span className="text-orange-500">Cine</span></span>
          </Link>
        </div>

        {/* Main Card */}
        <div className="bg-white/80 backdrop-blur-3xl rounded-[2.5rem] shadow-[0_30px_100px_-20px_rgba(0,0,0,0.1)] border border-white p-10 lg:p-12">
          
          {/* Tab Bar */}
          <div className="flex mb-10 bg-slate-100/50 rounded-2xl p-1.5 border border-slate-100 shadow-inner">
            {[['login', 'ĐĂNG NHẬP'], ['register', 'ĐĂNG KÝ']].map(([key, label]) => (
              <button key={key} onClick={() => handleTabSwitch(key)}
                className={`flex-1 py-3 text-[10px] font-black tracking-[0.2em] rounded-xl transition-all duration-500 ${activeTab === key ? 'bg-white text-orange-500 shadow-lg shadow-orange-100' : 'text-slate-400 hover:text-slate-600'}`}>
                {label}
              </button>
            ))}
          </div>

          {/* ── LOGIN FORM ── */}
          {activeTab === 'login' && (
            <form onSubmit={handleLoginSubmit} className="space-y-6">
              <div className="text-center mb-8">
                <h1 className="text-3xl font-black text-slate-800 tracking-tighter">Chào mừng bạn!</h1>
                <p className="text-sm font-medium text-slate-400 mt-2">Đăng nhập để nhận ngay ưu đãi điện ảnh</p>
              </div>

              {loginError && (
                <div className="flex items-start gap-3 px-5 py-4 rounded-2xl bg-red-50 border border-red-100 text-red-600 text-sm font-bold animate-pulse">
                  <span className="material-symbols-outlined text-xl shrink-0">error</span>
                  <span>{loginError}</span>
                </div>
              )}

              <FormInput id="login-email" icon="mail" label="Tài khoản Email" type="email" required placeholder="example@starcine.vn" value={loginForm.email} onChange={e => setLoginForm({...loginForm, email: e.target.value})} />
              <FormInput id="login-password" icon="lock" label="Mật khẩu" type={showPassword ? 'text' : 'password'} required placeholder="Mật khẩu của bạn" value={loginForm.password} onChange={e => setLoginForm({...loginForm, password: e.target.value})}>
                <TogglePassword id="toggle-login-password" show={showPassword} onClick={() => setShowPassword(!showPassword)} />
              </FormInput>

              <div className="flex items-center justify-between pt-1">
                <label className="flex items-center gap-3 cursor-pointer group">
                  <div className="relative">
                    <input type="checkbox" id="login-remember" checked={loginForm.remember} onChange={e => setLoginForm({...loginForm, remember: e.target.checked})} className="sr-only" />
                    <div className={`w-6 h-6 rounded-lg border-2 transition-all flex items-center justify-center ${loginForm.remember ? 'bg-orange-500 border-orange-500 shadow-lg shadow-orange-100' : 'border-slate-200 bg-white group-hover:border-slate-300 shadow-sm'}`}>
                      {loginForm.remember && <span className="material-symbols-outlined text-white text-sm font-black">check</span>}
                    </div>
                  </div>
                  <span className="text-sm text-slate-500 font-bold group-hover:text-slate-700 transition-colors">Ghi nhớ tôi</span>
                </label>
                <a href="#" className="text-sm text-orange-500 hover:text-red-500 transition-colors font-black">Quên mật khẩu?</a>
              </div>

              <button type="submit" disabled={isLoginLoading} 
                className="btn-shimmer relative w-full py-5 rounded-[1.5rem] bg-gradient-to-r from-orange-500 to-red-500 text-white font-black text-sm tracking-[0.1em] shadow-[0_15px_35px_-5px_rgba(249,115,22,0.4)] hover:shadow-[0_20px_45px_-5px_rgba(249,115,22,0.5)] hover:-translate-y-1 active:scale-95 transition-all duration-300 overflow-hidden disabled:opacity-60 uppercase">
                {isLoginLoading ? <span className="flex items-center justify-center gap-2"><Spinner /> ĐANG XỬ LÝ...</span> : 'ĐĂNG NHẬP NGAY'}
              </button>

              <div className="relative py-4 flex items-center gap-4">
                <div className="flex-1 h-px bg-slate-100" />
                <span className="text-[10px] font-black text-slate-300 uppercase tracking-widest">Tiếp tục với</span>
                <div className="flex-1 h-px bg-slate-100" />
              </div>
              
              <div className="space-y-4 pt-4">
                <div className="flex justify-center w-full">
                  {/* <GoogleLogin 
                    onSuccess={credentialResponse => {
                      console.log(">>> [StarCine] ID Token received:", credentialResponse.credential);
                      dispatch(googleLogin(credentialResponse.credential));
                    }}
                    onError={() => console.log('Login Failed')}
                    theme="outline"
                    width="360"
                    text="signin_with"
                    shape="pill"
                  /> */}
                </div>
                
                <div className="relative py-2 flex items-center gap-4">
                  <div className="flex-1 h-px bg-slate-100" />
                  <span className="text-[9px] font-black text-slate-300 uppercase tracking-widest italic">Hoặc</span>
                  <div className="flex-1 h-px bg-slate-100" />
                </div>

                <button type="button" className="w-full flex items-center justify-center gap-3 py-4 rounded-[1.25rem] border border-slate-100 bg-[#1877F2] hover:bg-[#166fe5] shadow-sm hover:shadow-md transition-all group">
                  <img src="https://www.svgrepo.com/show/475647/facebook-color.svg" className="w-5 h-5 brightness-0 invert" alt="Facebook" />
                  <span className="text-xs font-black text-white uppercase tracking-wider">Tiếp tục với Facebook</span>
                </button>
              </div>
            </form>
          )}

          {/* ── REGISTER FORM ── */}
          {activeTab === 'register' && (
            <form onSubmit={handleRegisterSubmit} className="space-y-4">
              <div className="text-center mb-8">
                <h1 className="text-3xl font-black text-slate-800 tracking-tighter">Gia nhập StarCine</h1>
                <p className="text-sm font-medium text-slate-400 mt-2">Tích lũy điểm thưởng khi xem phim</p>
              </div>
              {registerError && (<div className="flex items-start gap-3 px-5 py-4 rounded-2xl bg-red-50 border border-red-100 text-red-600 text-sm font-bold"><span className="material-symbols-outlined text-xl shrink-0">error</span><span>{registerError}</span></div>)}
              {registerStatus === 'succeeded' && registerSuccess && (<div className="flex items-start gap-3 px-5 py-4 rounded-2xl bg-green-50 border border-green-100 text-green-600 text-sm font-bold"><span className="material-symbols-outlined text-xl shrink-0">check_circle</span><span>{registerSuccess}</span></div>)}
              
              <FormInput id="reg-fullname" icon="person" label="Họ và tên khách hàng" required placeholder="Nguyễn Văn A" value={registerForm.fullName} onChange={e => setRegisterForm({...registerForm, fullName: e.target.value})} />
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <FormInput id="reg-email" icon="mail" label="Email" type="email" required placeholder="email@gmail.vn" value={registerForm.email} onChange={e => setRegisterForm({...registerForm, email: e.target.value})} />
                <FormInput id="reg-phone" icon="phone" label="Điện thoại" type="tel" placeholder="09x.xxx" value={registerForm.phone} onChange={e => setRegisterForm({...registerForm, phone: e.target.value})} />
              </div>

              <FormInput id="reg-password" icon="lock" label="Mật khẩu" type={showPassword ? 'text' : 'password'} required minLength={6} placeholder="Tối thiểu 6 ký tự" value={registerForm.password} onChange={e => setRegisterForm({...registerForm, password: e.target.value})}>
                <TogglePassword id="toggle-reg-password" show={showPassword} onClick={() => setShowPassword(!showPassword)} />
              </FormInput>

              <FormInput id="reg-confirm-password" icon="lock_reset" label="Xác nhận mật khẩu" type={showConfirmPassword ? 'text' : 'password'} required placeholder="Nhập lại mật khẩu" value={registerForm.confirmPassword} onChange={e => setRegisterForm({...registerForm, confirmPassword: e.target.value})} error={pwMismatch}>
                <TogglePassword id="toggle-reg-confirm-password" show={showConfirmPassword} onClick={() => setShowConfirmPassword(!showConfirmPassword)} />
              </FormInput>

              <label className="flex items-start gap-3 cursor-pointer group pt-2">
                <div className="relative mt-1 flex-shrink-0">
                  <input type="checkbox" id="reg-terms" required checked={registerForm.agreeTerms} onChange={e => setRegisterForm({...registerForm, agreeTerms: e.target.checked})} className="sr-only" />
                  <div className={`w-5 h-5 rounded-md border-2 transition-all flex items-center justify-center ${registerForm.agreeTerms ? 'bg-orange-500 border-orange-500 shadow-md shadow-orange-100' : 'border-slate-200 bg-white group-hover:border-slate-300 shadow-sm'}`}>{registerForm.agreeTerms && <span className="material-symbols-outlined text-white text-xs font-black">check</span>}</div>
                </div>
                <span className="text-[11px] text-slate-500 leading-relaxed font-bold">Tôi đồng ý với <a href="#" className="text-orange-500 underline underline-offset-2">Điều khoản</a> và <a href="#" className="text-orange-500 underline underline-offset-2">Chính sách</a> StarCine.</span>
              </label>

              <button type="submit" disabled={isRegisterLoading || registerStatus === 'succeeded' || pwMismatch} className="btn-shimmer relative w-full py-5 rounded-[1.5rem] bg-gradient-to-r from-orange-500 to-red-500 text-white font-black text-sm tracking-[0.1em] shadow-[0_15px_35px_-5px_rgba(249,115,22,0.4)] hover:shadow-[0_20px_45px_-5px_rgba(249,115,22,0.5)] transform transition-all duration-300 overflow-hidden uppercase">
                {isRegisterLoading ? <span className="flex items-center justify-center gap-2"><Spinner /> ĐANG KHỞI TẠO...</span> : 'ĐĂNG KÝ THÀNH VIÊN'}
              </button>
            </form>
          )}

          <div className="text-center mt-10">
            <Link to="/" className="inline-flex items-center gap-2 text-[11px] font-black text-slate-400 hover:text-orange-500 transition-colors group tracking-widest uppercase">
              <span className="material-symbols-outlined text-sm group-hover:-translate-x-1 transition-transform">arrow_back</span>
              Quay về trang chủ
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
