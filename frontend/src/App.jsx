import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { BookingProvider } from './contexts/BookingContext';
import Layout from './components/Layout';
import Home from './pages/Home';
import MovieList from './pages/MovieList';
import CinemaDetails from './pages/CinemaDetails';
import SeatSelection from './pages/SeatSelection';
import SnackSelection from './pages/SnackSelection';
import Payment from './pages/Payment';
import Login from './pages/Login';
import TransactionHistory from './pages/TransactionHistory';
import TransactionDetail from './pages/TransactionDetail';

// Admin Pages
import AdminLayout from './components/admin/AdminLayout';
import Dashboard from './pages/admin/Dashboard';
import MovieManagement from './pages/admin/MovieManagement';
import ShowtimeManagement from './pages/admin/ShowtimeManagement';
import FacilitiesManagement from './pages/admin/FacilitiesManagement';
import ArtistManagement from './pages/admin/ArtistManagement';
import FnbManagement from './pages/admin/FnbManagement';
import VoucherManagement from './pages/admin/VoucherManagement';

// Staff Pages
import StaffLayout from './components/staff/StaffLayout';
import BoxOfficePOS from './pages/staff/BoxOfficePOS';
import FnbConcession from './pages/staff/FnbConcession';
import OrderLookup from './pages/staff/OrderLookup';

function RequireAuth({ allowedRoles }) {
  const { user, isAuthenticated } = useSelector((state) => state.auth);

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  const roles = user?.roles || [];
  const hasRole = allowedRoles.some((role) => roles.includes(role));
  if (!hasRole) return <Navigate to="/" replace />;

  return <Outlet />;
}

function CustomerRoutes() {
  const { user, isAuthenticated } = useSelector(state => state.auth);
  
  // Dynamic redirection based on role
  if (isAuthenticated) {
    if (user?.roles?.includes('ROLE_ADMIN')) return <Navigate to="/admin" replace />;
    if (user?.roles?.includes('ROLE_STAFF')) return <Navigate to="/staff" replace />;
  }

  return (
    <Layout>
      <Outlet />
    </Layout>
  );
}

function App() {
  return (
    <BookingProvider>
      <BrowserRouter>
        <Routes>
          {/* Customer Routes */}
          <Route element={<CustomerRoutes />}>
            <Route path="/" element={<Home />} />
            <Route path="/movies" element={<MovieList />} />
            <Route path="/cinema/:id" element={<CinemaDetails />} />
            <Route path="/booking/seats" element={<SeatSelection />} />
            <Route path="/booking/snacks" element={<SnackSelection />} />
            <Route path="/booking/payment" element={<Payment />} />
            <Route path="/profile/transactions" element={<TransactionHistory />} />
            <Route path="/profile/transaction/:id" element={<TransactionDetail />} />
          </Route>

          {/* Auth Routes (no Layout) */}
          <Route path="/login" element={<Login />} />

          {/* Admin Routes */}
          <Route element={<RequireAuth allowedRoles={['ROLE_ADMIN', 'ROLE_STAFF']} />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="management" element={<MovieManagement />} />
              <Route path="showtimes" element={<ShowtimeManagement />} />
              <Route path="facilities" element={<FacilitiesManagement />} />
              <Route path="artists" element={<ArtistManagement />} />
              <Route path="fnb" element={<FnbManagement />} />
              <Route path="vouchers" element={<VoucherManagement />} />
            </Route>
          </Route>

          {/* Staff POS Routes */}
          <Route element={<RequireAuth allowedRoles={['ROLE_STAFF']} />}>
            <Route path="/staff" element={<StaffLayout />}>
              <Route index element={<Navigate to="pos" replace />} />
              <Route path="pos" element={<BoxOfficePOS />} />
              <Route path="fnb" element={<FnbConcession />} />
              <Route path="lookup" element={<OrderLookup />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </BookingProvider>
  );
}

export default App;
