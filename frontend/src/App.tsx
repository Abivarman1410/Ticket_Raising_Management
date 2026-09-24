import { Routes, Route, Navigate, useLocation, useNavigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import EmployeeDashboard from './pages/EmployeeDashboard';
import AdminDashboard from './pages/AdminDashboard';
import ManagerDashboard from './pages/ManagerDashboard';
import { LogOut } from 'lucide-react';

function AppContent() {
  const location = useLocation();
  const navigate = useNavigate();
  const isLoginPage = location.pathname === '/login' || location.pathname === '/';

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-brand-cream text-brand-navy flex flex-col font-sans">
      <header className="bg-brand-navy text-brand-cream p-5 shadow-lg border-b-4 border-brand-red">
        <div className="container mx-auto flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold tracking-wide">Enterprisix</h1>
            <p className="text-sm opacity-80 mt-1">Ticket Management System</p>
          </div>
          {!isLoginPage && (
            <button 
              onClick={handleLogout}
              className="bg-brand-red hover:bg-brand-dark text-white px-4 py-2 rounded-xl font-bold flex items-center transition-colors shadow-md"
            >
              <LogOut className="w-4 h-4 mr-2" /> Logout
            </button>
          )}
        </div>
      </header>
      <main className="flex-1 container mx-auto p-4 md:p-8">
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/employee/dashboard" element={<EmployeeDashboard />} />
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/manager/dashboard" element={<ManagerDashboard />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return <AppContent />;
}

