import { useState, useEffect } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import { Clock, CheckCircle, AlertTriangle, LayoutDashboard, BarChart3, Users } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface Ticket {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  categoryName: string;
  assignedAdminName?: string;
  employeeId?: number;
  employeeEmpId?: string;
  employeeName?: string;
  employeeEmail?: string;
  employeeContactNumber?: string;
  businessUnit?: string;
  workLocation?: string;
  assetTag?: string;
  createdAt: string;
  latestResolution?: {
    solutionDescription: string;
    resolutionCode: string;
    resolvedAt: string;
  };
  latestRejectionReason?: string;
}

export default function ManagerDashboard() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  
  const navigate = useNavigate();

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    try {
      const token = localStorage.getItem('token');
      const res = await axios.get('/api/manager/tickets', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setTickets(res.data);
    } catch (err: any) {
      if (err.response?.status === 401) {
        localStorage.removeItem('token');
        navigate('/login');
      }
    } finally {
      setLoading(false);
    }
  };



  const getStatusColor = (status: string) => {
    switch(status) {
      case 'OPEN': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'ASSIGNED': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'IN_PROGRESS': return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'RESOLVED': return 'bg-green-100 text-green-800 border-green-200';
      case 'CLOSED': return 'bg-gray-100 text-gray-600 border-gray-200';
      case 'REOPENED': return 'bg-red-100 text-brand-red border-brand-red';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getPriorityIcon = (prio: string) => {
    if (prio === 'CRITICAL' || prio === 'HIGH') return <AlertTriangle className="w-4 h-4 text-brand-red mr-1" />;
    return <Clock className="w-4 h-4 text-brand-navy opacity-50 mr-1" />;
  };

  const statOpen = tickets.filter(t => t.status === 'OPEN' || t.status === 'ASSIGNED').length;
  const statProgress = tickets.filter(t => t.status === 'IN_PROGRESS').length;
  const statResolved = tickets.filter(t => t.status === 'RESOLVED' || t.status === 'CLOSED').length;

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-7xl mx-auto space-y-6 pb-20"
    >
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 bg-white p-6 rounded-2xl shadow-sm border border-orange-50 gap-4">
        <div>
          <h2 className="text-3xl font-black text-brand-navy flex items-center gap-3">
            <LayoutDashboard className="text-brand-red w-8 h-8" />
            Manager Command Center
          </h2>
          <p className="text-gray-500 mt-1 font-medium">Global overview of all organizational tickets</p>
        </div>
      </div>

      {/* Analytics Summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <motion.div initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.1 }} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex items-center justify-between">
          <div>
            <p className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-1">Open / Assigned</p>
            <h3 className="text-4xl font-black text-brand-navy">{statOpen}</h3>
          </div>
          <div className="w-14 h-14 rounded-full bg-blue-50 flex items-center justify-center">
            <AlertTriangle className="w-7 h-7 text-blue-500" />
          </div>
        </motion.div>
        
        <motion.div initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.2 }} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex items-center justify-between">
          <div>
            <p className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-1">In Progress</p>
            <h3 className="text-4xl font-black text-brand-navy">{statProgress}</h3>
          </div>
          <div className="w-14 h-14 rounded-full bg-purple-50 flex items-center justify-center">
            <BarChart3 className="w-7 h-7 text-purple-500" />
          </div>
        </motion.div>

        <motion.div initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.3 }} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex items-center justify-between">
          <div>
            <p className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-1">Resolved</p>
            <h3 className="text-4xl font-black text-brand-navy">{statResolved}</h3>
          </div>
          <div className="w-14 h-14 rounded-full bg-green-50 flex items-center justify-center">
            <CheckCircle className="w-7 h-7 text-green-500" />
          </div>
        </motion.div>
      </div>

      {/* Ticket Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {loading ? (
          [1,2,3,4].map(i => (
            <div key={i} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 h-64 animate-pulse">
              <div className="h-4 bg-gray-200 w-1/3 rounded mb-4"></div>
              <div className="h-6 bg-gray-200 w-3/4 rounded mb-8"></div>
              <div className="h-20 bg-gray-100 rounded mb-4"></div>
            </div>
          ))
        ) : tickets.length === 0 ? (
          <div className="col-span-full bg-white p-12 text-center rounded-2xl shadow-sm border border-gray-100">
            <div className="bg-gray-50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4">
              <CheckCircle className="w-10 h-10 text-gray-300" />
            </div>
            <h3 className="text-xl font-bold text-brand-navy">No Tickets in System</h3>
            <p className="text-gray-500 mt-2">The organization currently has no active tickets.</p>
          </div>
        ) : (
          tickets.map((ticket, idx) => (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.05 }}
              key={ticket.id} 
              className="bg-white rounded-2xl shadow-[0_4px_20px_rgb(0,0,0,0.05)] border border-gray-100 overflow-hidden flex flex-col hover:shadow-[0_8px_30px_rgb(0,0,0,0.08)] transition-all duration-300"
            >
              <div className="p-6 flex-1 flex flex-col">
                <div className="flex justify-between items-start mb-4">
                  <span className="font-mono text-xs font-bold text-gray-400">{ticket.ticketNumber}</span>
                  <span className={`px-2 py-1 rounded-md text-[10px] font-black border uppercase tracking-wider ${getStatusColor(ticket.status)}`}>
                    {ticket.status.replace('_', ' ')}
                  </span>
                </div>
                
                <h3 className="text-lg font-bold text-brand-navy mb-2 line-clamp-2">{ticket.title}</h3>
                <p className="text-gray-500 text-xs line-clamp-2 mb-4">{ticket.description}</p>
                
                <div className="mt-auto space-y-2 pt-4 border-t border-gray-50">
                  <div className="flex justify-between items-center text-xs font-bold">
                    <span className="flex items-center text-gray-600 bg-gray-50 px-2 py-1 rounded">
                       {ticket.categoryName}
                    </span>
                    <span className="flex items-center">
                      {getPriorityIcon(ticket.priority)} {ticket.priority}
                    </span>
                  </div>
                  
                  {(ticket.employeeEmpId || ticket.employeeName) && (
                    <div className="flex flex-col gap-1 text-[10px] text-gray-500 font-mono mt-1 pt-1 border-t border-gray-100">
                      <div className="font-bold text-gray-700">{ticket.employeeEmpId} - {ticket.employeeName}</div>
                      {(ticket.employeeEmail || ticket.employeeContactNumber) && (
                        <div className="flex gap-2">
                          {ticket.employeeEmail && <span>📧 {ticket.employeeEmail}</span>}
                          {ticket.employeeContactNumber && <span>📞 {ticket.employeeContactNumber}</span>}
                        </div>
                      )}
                      {(ticket.businessUnit || ticket.workLocation || ticket.assetTag) && (
                        <div className="flex flex-wrap gap-x-2 mt-1">
                          {ticket.businessUnit && <span>BU: {ticket.businessUnit}</span>}
                          {ticket.workLocation && <span>Loc: {ticket.workLocation}</span>}
                          {ticket.assetTag && <span>Asset: {ticket.assetTag}</span>}
                        </div>
                      )}
                    </div>
                  )}

                  <div className="flex items-center text-xs text-gray-500 bg-brand-cream/30 p-2 rounded-lg border border-orange-50">
                    <Users className="w-3 h-3 mr-1 text-brand-red" /> 
                    <span className="truncate">
                      {ticket.assignedAdminName ? `Admin: ${ticket.assignedAdminName}` : 'Unassigned'}
                    </span>
                  </div>
                  
                  {ticket.latestResolution?.resolutionCode && (
                    <div className="text-[10px] bg-green-50 text-green-700 px-2 py-1 rounded font-mono border border-green-200 mt-2">
                      Resolution Code: <span className="font-bold">{ticket.latestResolution.resolutionCode}</span>
                    </div>
                  )}

                  {ticket.status === 'REOPENED' && ticket.latestRejectionReason && (
                    <div className="bg-red-50 text-red-700 px-2 py-1.5 rounded border border-red-200 mt-2 text-[10px]">
                      <div className="font-bold flex items-center mb-0.5"><AlertTriangle className="w-3 h-3 mr-1"/> Rejection Reason:</div>
                      <span className="italic">"{ticket.latestRejectionReason}"</span>
                    </div>
                  )}
                </div>
              </div>
            </motion.div>
          ))
        )}
      </div>
    </motion.div>
  );
}
