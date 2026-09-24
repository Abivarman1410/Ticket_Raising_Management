import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Lock, Mail, LogIn, AlertCircle, User, Briefcase, MapPin, Tag, Phone } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function LoginPage() {
  const [isLogin, setIsLogin] = useState(true);
  
  // Login State
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  
  // Registration State
  const [fullName, setFullName] = useState('');
  const [employeeId, setEmployeeId] = useState('');
  const [contactNumber, setContactNumber] = useState('');
  const [businessUnit, setBusinessUnit] = useState('');
  const [workLocation, setWorkLocation] = useState('');
  const [assetTag, setAssetTag] = useState('');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      let response;
      if (isLogin) {
        response = await axios.post('/api/auth/login', { email, password });
      } else {
        response = await axios.post('/api/auth/register', {
          email,
          password,
          fullName,
          employeeId,
          contactNumber,
          businessUnit,
          workLocation,
          assetTag
        });
      }

      const { token } = response.data;
      localStorage.setItem('token', token);
      
      const profileRes = await axios.get('/api/auth/me', {
        headers: { Authorization: `Bearer ${token}` }
      });
      const user = profileRes.data;
      localStorage.setItem('userRole', user.role);
      
      if (user.role === 'MANAGER') navigate('/manager/dashboard');
      else if (user.role === 'ADMIN') navigate('/admin/dashboard');
      else navigate('/employee/dashboard');
    } catch (err: any) {
      console.error('Auth error:', err);
      if (err.response?.data?.errors) {
         setError(Object.values(err.response.data.errors).join(', '));
      } else {
         setError(err.response?.data?.message || 'Invalid credentials or request');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="flex justify-center items-center min-h-[80vh] py-10"
    >
      <div className="bg-white p-8 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.12)] w-full max-w-md border-t-8 border-brand-red overflow-hidden relative">
        
        <div className="flex justify-center mb-6">
          <div className="flex bg-gray-100 rounded-xl p-1 w-full max-w-xs">
            <button
              onClick={() => { setIsLogin(true); setError(''); }}
              className={`flex-1 py-2 text-sm font-bold rounded-lg transition-colors ${isLogin ? 'bg-white shadow text-brand-navy' : 'text-gray-500 hover:text-brand-navy'}`}
            >
              Login
            </button>
            <button
              onClick={() => { setIsLogin(false); setError(''); }}
              className={`flex-1 py-2 text-sm font-bold rounded-lg transition-colors ${!isLogin ? 'bg-white shadow text-brand-navy' : 'text-gray-500 hover:text-brand-navy'}`}
            >
              New Employee
            </button>
          </div>
        </div>

        <div className="text-center mb-8">
          <div className="bg-brand-cream w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm border border-orange-100">
            {isLogin ? <LogIn className="w-8 h-8 text-brand-red" /> : <User className="w-8 h-8 text-brand-red" />}
          </div>
          <h2 className="text-3xl font-extrabold text-brand-navy">
            {isLogin ? 'Welcome Back' : 'Create Account'}
          </h2>
          <p className="text-gray-500 mt-2 font-medium">
            {isLogin ? 'Sign in to your account' : 'Register as a new employee'}
          </p>
        </div>

        {error && (
          <motion.div 
            initial={{ opacity: 0, x: -10 }} animate={{ opacity: 1, x: 0 }}
            className="bg-red-50 border-l-4 border-brand-red p-4 mb-6 rounded-r-md flex items-start"
          >
            <AlertCircle className="w-5 h-5 text-brand-red mr-2 flex-shrink-0 mt-0.5" />
            <p className="text-brand-dark text-sm font-bold">{error}</p>
          </motion.div>
        )}

        <form onSubmit={handleAuth} className="space-y-4">
          
          <AnimatePresence mode="wait">
            {!isLogin && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="space-y-4 overflow-hidden"
              >
                <div className="flex gap-4">
                  <div className="flex-1">
                    <label className="block text-xs font-bold text-brand-navy mb-1">Full Name *</label>
                    <div className="relative group">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red">
                        <User className="h-4 w-4" />
                      </div>
                      <input
                        type="text" required={!isLogin} value={fullName} onChange={(e) => setFullName(e.target.value)}
                        className="pl-9 w-full p-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                        placeholder="John Doe"
                      />
                    </div>
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs font-bold text-brand-navy mb-1">Employee ID *</label>
                    <div className="relative group">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red">
                        <Briefcase className="h-4 w-4" />
                      </div>
                      <input
                        type="text" required={!isLogin} value={employeeId} onChange={(e) => setEmployeeId(e.target.value)}
                        className="pl-9 w-full p-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                        placeholder="EMP123"
                      />
                    </div>
                  </div>
                </div>

                <div className="flex gap-4">
                  <div className="flex-1">
                    <label className="block text-xs font-bold text-brand-navy mb-1">Contact Number *</label>
                    <div className="relative group">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red">
                        <Phone className="h-4 w-4" />
                      </div>
                      <input
                        type="text" required={!isLogin} value={contactNumber} onChange={(e) => setContactNumber(e.target.value)}
                        className="pl-9 w-full p-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                        placeholder="555-0100"
                      />
                    </div>
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs font-bold text-brand-navy mb-1">Asset Tag *</label>
                    <div className="relative group">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red">
                        <Tag className="h-4 w-4" />
                      </div>
                      <input
                        type="text" required={!isLogin} value={assetTag} onChange={(e) => setAssetTag(e.target.value)}
                        className="pl-9 w-full p-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                        placeholder="PC-9999"
                      />
                    </div>
                  </div>
                </div>

                <div className="flex gap-4">
                  <div className="flex-1">
                    <label className="block text-xs font-bold text-brand-navy mb-1">Business Unit</label>
                    <div className="relative group">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red">
                        <Briefcase className="h-4 w-4" />
                      </div>
                      <input
                        type="text" value={businessUnit} onChange={(e) => setBusinessUnit(e.target.value)}
                        className="pl-9 w-full p-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                        placeholder="Sales"
                      />
                    </div>
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs font-bold text-brand-navy mb-1">Work Location</label>
                    <div className="relative group">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red">
                        <MapPin className="h-4 w-4" />
                      </div>
                      <input
                        type="text" value={workLocation} onChange={(e) => setWorkLocation(e.target.value)}
                        className="pl-9 w-full p-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                        placeholder="Office"
                      />
                    </div>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          <div>
            <label className="block text-sm font-bold text-brand-navy mb-1.5">Email Address *</label>
            <div className="relative group">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red transition-colors">
                <Mail className="h-5 w-5" />
              </div>
              <input
                type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
                className="pl-10 w-full p-3 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                placeholder="you@company.com"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-bold text-brand-navy mb-1.5">Password *</label>
            <div className="relative group">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-red transition-colors">
                <Lock className="h-5 w-5" />
              </div>
              <input
                type="password" required value={password} onChange={(e) => setPassword(e.target.value)}
                className="pl-10 w-full p-3 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red focus:border-brand-red outline-none transition-all"
                placeholder="••••••••"
              />
            </div>
          </div>

          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            type="submit" disabled={loading}
            className={`w-full py-3.5 px-4 mt-4 bg-brand-red hover:bg-brand-dark text-white font-bold rounded-xl shadow-lg transition-colors flex justify-center items-center ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
          >
            {loading ? (
              <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            ) : (
              isLogin ? 'Sign In' : 'Create Account'
            )}
          </motion.button>
        </form>
        
        {isLogin && (
          <div className="mt-8 text-center text-sm text-gray-500">
            <p className="font-semibold text-brand-navy">Demo Accounts:</p>
            <div className="mt-2 space-y-2 flex flex-col items-center">
              <span className="font-mono text-xs bg-gray-100 px-3 py-1.5 rounded-full text-brand-navy border border-gray-200">manager@company.com</span>
              <span className="font-mono text-xs bg-gray-100 px-3 py-1.5 rounded-full text-brand-navy border border-gray-200">hardware_admin@company.com</span>
              <span className="font-mono text-xs bg-gray-100 px-3 py-1.5 rounded-full text-brand-navy border border-gray-200">software_admin@company.com</span>
              <span className="font-mono text-xs bg-gray-100 px-3 py-1.5 rounded-full text-brand-navy border border-gray-200">employee@company.com</span>
            </div>
          </div>
        )}
      </div>
    </motion.div>
  );
}
