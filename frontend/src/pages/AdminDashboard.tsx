import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { motion, AnimatePresence } from 'framer-motion';
import { Clock, CheckCircle, AlertTriangle, Send, Wrench } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface Ticket {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  categoryName: string;
  employeeId: number;
  employeeEmpId: string;
  employeeName: string;
  employeeEmail?: string;
  employeeContactNumber?: string;
  businessUnit?: string;
  workLocation?: string;
  assetTag?: string;
  createdAt: string;
  latestResolution?: {
    solutionDescription: string;
    resolutionCode: string;
  };
  latestRejectionReason?: string;
}

export default function AdminDashboard() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [resolutionText, setResolutionText] = useState('');
  const [resolvingTicketId, setResolvingTicketId] = useState<number | null>(null);
  
  const navigate = useNavigate();

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    try {
      const token = localStorage.getItem('token');
      const res = await axios.get('/api/admin/tickets', {
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



  const updateStatus = async (ticketId: number, newStatus: string) => {
    try {
      const token = localStorage.getItem('token');
      await axios.patch(`/api/admin/tickets/${ticketId}/status`, null, {
        params: { newStatus },
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchTickets();
    } catch (err) {
      alert("Error updating status");
    }
  };

  const submitResolution = async (e: React.FormEvent, ticketId: number) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      await axios.post(`/api/admin/tickets/${ticketId}/resolution`, 
        { solutionDescription: resolutionText },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setResolvingTicketId(null);
      setResolutionText('');
      fetchTickets();
    } catch (err) {
      alert("Error submitting resolution");
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

  // Determine if the admin is likely Hardware or Software based on their tickets
  const inferredCategory = tickets.length > 0 ? tickets[0].categoryName : 'System';

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-6xl mx-auto space-y-6 pb-20"
    >
      <div className="flex justify-between items-center mb-8 bg-white p-6 rounded-2xl shadow-sm border border-orange-50">
        <div>
          <h2 className="text-3xl font-black text-brand-navy flex items-center gap-3">
            <Wrench className="text-brand-red w-8 h-8" />
            {inferredCategory} Admin Workspace
          </h2>
          <p className="text-gray-500 mt-1 font-medium">Resolve tickets assigned to your queue</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {loading ? (
          [1,2,3].map(i => (
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
            <h3 className="text-xl font-bold text-brand-navy">All clear!</h3>
            <p className="text-gray-500 mt-2">You don't have any pending tickets assigned to you.</p>
          </div>
        ) : (
          tickets.map((ticket, idx) => (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.1 }}
              key={ticket.id} 
              className={`bg-white rounded-2xl shadow-[0_4px_20px_rgb(0,0,0,0.05)] border border-gray-100 overflow-hidden flex flex-col hover:shadow-[0_8px_30px_rgb(0,0,0,0.08)] transition-all duration-300 ${resolvingTicketId === ticket.id ? 'ring-2 ring-brand-red' : ''}`}
            >
              <div className="p-6 flex-1">
                <div className="flex justify-between items-start mb-4">
                  <span className="font-mono text-xs font-bold text-gray-400">{ticket.ticketNumber}</span>
                  <span className={`px-3 py-1 rounded-full text-xs font-black border ${getStatusColor(ticket.status)}`}>
                    {ticket.status.replace('_', ' ')}
                  </span>
                </div>
                
                <h3 className="text-xl font-bold text-brand-navy mb-2 line-clamp-2">{ticket.title}</h3>
                <p className="text-gray-500 text-sm line-clamp-3 mb-4">{ticket.description}</p>
                
                <div className="flex flex-col gap-2 text-xs text-gray-500 mt-2 mb-4 bg-gray-50 p-2 rounded-lg border border-gray-100">
                  <div className="font-bold text-brand-navy flex justify-between">
                    <span>{ticket.employeeEmpId} - {ticket.employeeName}</span>
                  </div>
                  <div className="text-[10px] space-y-1">
                    {ticket.employeeEmail && <div>📧 {ticket.employeeEmail}</div>}
                    {ticket.employeeContactNumber && <div>📞 {ticket.employeeContactNumber}</div>}
                  </div>
                  {(ticket.businessUnit || ticket.workLocation || ticket.assetTag) && (
                    <div className="font-mono text-[10px] mt-1 pt-1 border-t border-gray-200">
                      {ticket.businessUnit && <span>BU: {ticket.businessUnit} </span>}
                      {ticket.workLocation && <span>| Loc: {ticket.workLocation} </span>}
                      {ticket.assetTag && <span>| Asset: {ticket.assetTag}</span>}
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-4 text-xs font-bold mt-auto pt-4 border-t border-gray-50 mb-4">
                  <span className="flex items-center">
                    {getPriorityIcon(ticket.priority)} {ticket.priority}
                  </span>
                </div>

                {/* Actions */}
                <div className="space-y-3">
                  {ticket.status === 'RESOLVED' && (
                    <div className="bg-brand-cream border border-orange-100 p-3 rounded-lg text-sm italic text-gray-700 mb-2">
                      <div className="font-bold text-brand-navy mb-1 not-italic text-xs flex items-center">
                        <CheckCircle className="w-3 h-3 text-green-600 mr-1"/> Your Resolution
                      </div>
                      "{ticket.latestResolution?.solutionDescription}"
                      {ticket.latestResolution?.resolutionCode && (
                        <div className="text-[10px] text-green-700 font-mono mt-1 not-italic">Code: {ticket.latestResolution.resolutionCode}</div>
                      )}
                    </div>
                  )}

                  {ticket.status === 'REOPENED' && ticket.latestRejectionReason && (
                    <div className="bg-red-50 border border-red-200 p-3 rounded-lg text-sm text-gray-700 mb-2">
                      <div className="font-bold text-brand-red mb-1 text-xs flex items-center">
                        <AlertTriangle className="w-3 h-3 mr-1"/> Reason for Rejection
                      </div>
                      <span className="italic">"{ticket.latestRejectionReason}"</span>
                    </div>
                  )}

                  {(ticket.status === 'ASSIGNED' || ticket.status === 'REOPENED') && (
                    <button 
                      onClick={() => updateStatus(ticket.id, 'IN_PROGRESS')}
                      className="w-full bg-brand-navy hover:bg-brand-dark text-brand-cream font-bold py-2.5 rounded-lg text-sm transition-colors shadow-sm"
                    >
                      Start Working
                    </button>
                  )}
                  
                  {ticket.status === 'IN_PROGRESS' && resolvingTicketId !== ticket.id && (
                    <button 
                      onClick={() => setResolvingTicketId(ticket.id)}
                      className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2.5 rounded-lg text-sm transition-colors shadow-sm"
                    >
                      Provide Resolution
                    </button>
                  )}
                </div>
              </div>

              <AnimatePresence>
                {resolvingTicketId === ticket.id && (
                  <motion.div 
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    className="bg-brand-cream border-t border-orange-100 p-5 overflow-hidden"
                  >
                    <form onSubmit={(e) => submitResolution(e, ticket.id)}>
                      <label className="block text-sm font-bold text-brand-navy mb-2">Solution Details</label>
                      <textarea 
                        required
                        value={resolutionText}
                        onChange={(e) => setResolutionText(e.target.value)}
                        className="w-full p-3 bg-white border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none mb-3 text-sm resize-none"
                        rows={3}
                        placeholder="Explain how the issue was fixed..."
                      />
                      <div className="flex gap-2">
                        <button type="button" onClick={() => setResolvingTicketId(null)} className="flex-1 bg-white border-2 border-gray-200 hover:border-brand-navy text-brand-navy font-bold py-2 rounded-lg text-sm transition-colors">Cancel</button>
                        <button type="submit" className="flex-1 bg-brand-red hover:bg-brand-dark text-white font-bold py-2 rounded-lg text-sm transition-colors flex justify-center items-center">
                          <Send className="w-4 h-4 mr-2" /> Submit
                        </button>
                      </div>
                    </form>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          ))
        )}
      </div>
    </motion.div>
  );
}
