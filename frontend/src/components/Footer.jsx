import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="w-full border-t border-slate-100 dark:border-slate-800 bg-slate-50 dark:bg-slate-950">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-12 max-w-7xl mx-auto px-8 py-16">
        <div className="space-y-6">
          <div className="text-xl font-black text-slate-900 dark:text-slate-100">StarCine</div>
          <p className="text-slate-500 dark:text-slate-400 text-sm leading-relaxed">
            Hệ thống rạp chiếu phim hiện đại hàng đầu Việt Nam với trải nghiệm IMAX Laser đỉnh cao.
          </p>
          <div className="flex gap-4">
            <span className="material-symbols-outlined text-slate-400 hover:text-orange-500 cursor-pointer">language</span>
            <span className="material-symbols-outlined text-slate-400 hover:text-orange-500 cursor-pointer">mail</span>
            <span className="material-symbols-outlined text-slate-400 hover:text-orange-500 cursor-pointer">phone</span>
          </div>
        </div>

        <div className="space-y-6">
          <h5 className="font-bold text-slate-900 dark:text-slate-100 uppercase tracking-widest text-xs">Về chúng tôi</h5>
          <ul className="space-y-4 text-sm font-['Space_Grotesk'] text-slate-500 dark:text-slate-400">
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Hệ thống rạp</a></li>
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Tuyển dụng</a></li>
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Liên hệ quảng cáo</a></li>
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Giới thiệu</a></li>
          </ul>
        </div>

        <div className="space-y-6">
          <h5 className="font-bold text-slate-900 dark:text-slate-100 uppercase tracking-widest text-xs">Hỗ trợ khách hàng</h5>
          <ul className="space-y-4 text-sm font-['Space_Grotesk'] text-slate-500 dark:text-slate-400">
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Trung tâm trợ giúp</a></li>
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Câu hỏi thường gặp</a></li>
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Chính sách bảo mật</a></li>
            <li><a className="hover:text-orange-500 underline-offset-4 hover:underline" href="#">Điều khoản sử dụng</a></li>
          </ul>
        </div>

        <div className="space-y-6">
          <h5 className="font-bold text-slate-900 dark:text-slate-100 uppercase tracking-widest text-xs">Tải ứng dụng</h5>
          <div className="flex flex-col gap-4">
            <div className="flex items-center gap-3 p-3 bg-white rounded-xl border border-slate-200 cursor-pointer hover:border-orange-500 transition-colors">
              <span className="material-symbols-outlined text-3xl">phone_iphone</span>
              <div>
                <div className="text-[10px] text-slate-400 uppercase font-bold leading-none">Download on</div>
                <div className="text-sm font-bold">App Store</div>
              </div>
            </div>
            <div className="flex items-center gap-3 p-3 bg-white rounded-xl border border-slate-200 cursor-pointer hover:border-orange-500 transition-colors">
              <span className="material-symbols-outlined text-3xl">shop</span>
              <div>
                <div className="text-[10px] text-slate-400 uppercase font-bold leading-none">Get it on</div>
                <div className="text-sm font-bold">Google Play</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-8 py-8 border-t border-slate-100 dark:border-slate-900 flex flex-col md:flex-row justify-between items-center gap-4">
        <p className="text-slate-500 dark:text-slate-400 text-xs">© 2026 StarCine. All rights reserved.</p>
        <div className="flex gap-8">
          <a className="text-xs text-slate-500 hover:text-orange-500 transition-colors" href="#">Về chúng tôi</a>
          <a className="text-xs text-slate-500 hover:text-orange-500 transition-colors" href="#">Trung tâm trợ giúp</a>
          <a className="text-xs text-slate-500 hover:text-orange-500 transition-colors" href="#">Chính sách bảo mật</a>
          <a className="text-xs text-slate-500 hover:text-orange-500 transition-colors" href="#">Điều khoản sử dụng</a>
        </div>
      </div>
    </footer>
  );
}
