import { Outlet } from 'react-router-dom';
import AdminSidebar from './AdminSidebar';
import AdminHeader from './AdminHeader';

const AdminLayout = () => {
  return (
    <div className="antialiased selection:bg-orange-500/30 bg-surface min-h-screen font-['Space_Grotesk']">
      <AdminSidebar />
      <AdminHeader />
      
      {/* Main Content Canvas */}
      <main className="ml-64 pt-24 pb-12 px-10 min-h-screen">
        <Outlet />
      </main>

      {/* Contextual FAB (Only for main screens) */}
      <button className="fixed bottom-8 right-8 w-14 h-14 bg-on-surface text-on-primary rounded-full flex items-center justify-center shadow-2xl hover:scale-110 active:scale-90 transition-all z-50">
        <span className="material-symbols-outlined">support_agent</span>
      </button>
    </div>
  );
};

export default AdminLayout;
