import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Clock, CheckCircle, AlertTriangle, FileText, Send } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface Ticket {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  categoryName: string;
  businessUnit?: string;
  workLocation?: string;
  assetTag?: string;
  createdAt: string;
  latestResolution?: {
    solutionDescription: string;
    resolvedAt: string;
  };
  latestRejectionReason?: string;
}

export default function EmployeeDashboard() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState<number | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');
  
  // Form state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('HARDWARE');
  const [priority, setPriority] = useState('LOW');
  const [businessUnit, setBusinessUnit] = useState('');
  const [workLocation, setWorkLocation] = useState('');
  const [assetTag, setAssetTag] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    fetchProfileAndTickets();
  }, []);

  const fetchProfileAndTickets = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      
      // Fetch profile to get default values
      const profileRes = await axios.get('/api/auth/me', {
        headers: { Authorization: `Bearer ${token}` }
      });
      const user = profileRes.data;
      if (user.businessUnit) setBusinessUnit(user.businessUnit);
      if (user.workLocation) setWorkLocation(user.workLocation);
      if (user.assetTag) setAssetTag(user.assetTag);

      // Fetch tickets
      const res = await axios.get('/api/employee/tickets', {
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

  const fetchTickets = async () => {
    try {
      const token = localStorage.getItem('token');
      const res = await axios.get('/api/employee/tickets', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setTickets(res.data);
    } catch (err) {
      console.error(err);
    }
  };



  const submitTicket = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormLoading(true);
    try {
      const token = localStorage.getItem('token');
      await axios.post('/api/employee/tickets', 
        { title, description, category, priority, businessUnit, workLocation, assetTag },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setShowCreateForm(false);
      setTitle('');
      setDescription('');
      fetchTickets();
    } catch (err) {
      alert("Error creating ticket");
    } finally {
      setFormLoading(false);
    }
  };

  const confirmResolution = async (ticketId: number, decision: 'CONFIRMED' | 'REJECTED') => {
    if (decision === 'REJECTED' && !rejectionReason.trim()) {
      alert("Please provide a reason for rejecting the resolution.");
      return;
    }
    try {
      const token = localStorage.getItem('token');
      await axios.post(`/api/employee/tickets/${ticketId}/confirmation`, 
        { decision, reason: decision === 'REJECTED' ? rejectionReason : undefined },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setShowConfirmation(null);
      setRejectionReason('');
      fetchTickets();
    } catch (err) {
      alert("Error processing confirmation");
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

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="max-w-6xl mx-auto space-y-6 pb-20"
    >
      <div className="flex justify-between items-center mb-8 bg-white p-6 rounded-2xl shadow-sm border border-orange-50">
        <div>
          <h2 className="text-3xl font-black text-brand-navy">My Workspace</h2>
          <p className="text-gray-500 mt-1 font-medium">Manage and track your support requests</p>
        </div>
        <div className="flex gap-4">
          <motion.button 
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={() => setShowCreateForm(true)}
            className="bg-brand-red hover:bg-brand-dark text-white px-5 py-2.5 rounded-xl font-bold flex items-center shadow-md transition-colors"
          >
            <Plus className="w-5 h-5 mr-2" /> New Ticket
          </motion.button>
        </div>
      </div>

      <AnimatePresence>
        {showCreateForm && (
          <motion.div 
            initial={{ opacity: 0, height: 0, y: -20 }}
            animate={{ opacity: 1, height: 'auto', y: 0 }}
            exit={{ opacity: 0, height: 0, overflow: 'hidden' }}
            className="bg-white rounded-2xl shadow-lg border-t-8 border-brand-red p-8 mb-8"
          >
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-2xl font-bold text-brand-navy">Submit a Request</h3>
              <button onClick={() => setShowCreateForm(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            
            <form onSubmit={submitTicket} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-bold text-brand-navy mb-2">Category</label>
                  <select 
                    value={category} onChange={e => setCategory(e.target.value)}
                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none"
                  >
                    <option value="HARDWARE">Hardware</option>
                    <option value="SOFTWARE">Software</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-bold text-brand-navy mb-2">Priority</label>
                  <select 
                    value={priority} onChange={e => setPriority(e.target.value)}
                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none"
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="CRITICAL">Critical</option>
                  </select>
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div>
                  <label className="block text-sm font-bold text-brand-navy mb-2">Business Unit</label>
                  <input 
                    type="text" value={businessUnit} onChange={e => setBusinessUnit(e.target.value)}
                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none"
                    placeholder="e.g. Sales, IT"
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-brand-navy mb-2">Work Location</label>
                  <input 
                    type="text" value={workLocation} onChange={e => setWorkLocation(e.target.value)}
                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none"
                    placeholder="e.g. Office, Remote"
                  />
                </div>
                <div>
                  <label className="block text-sm font-bold text-brand-navy mb-2">Asset Tag (Optional)</label>
                  <input 
                    type="text" value={assetTag} onChange={e => setAssetTag(e.target.value)}
                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none"
                    placeholder="e.g. PC-1234"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-bold text-brand-navy mb-2">Title</label>
                <input 
                  type="text" required value={title} onChange={e => setTitle(e.target.value)}
                  className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none"
                  placeholder="Brief summary of the issue"
                />
              </div>

              <div>
                <label className="block text-sm font-bold text-brand-navy mb-2">Description</label>
                <textarea 
                  required value={description} onChange={e => setDescription(e.target.value)} rows={4}
                  className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-brand-red outline-none resize-none"
                  placeholder="Detailed description of what is happening..."
                ></textarea>
              </div>

              <div className="flex justify-end gap-4">
                <button type="button" onClick={() => setShowCreateForm(false)} className="px-6 py-3 font-bold text-gray-500 hover:text-gray-700">Cancel</button>
                <motion.button 
                  whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}
                  type="submit" disabled={formLoading}
                  className="bg-brand-red hover:bg-brand-dark text-white px-8 py-3 rounded-xl font-bold flex items-center shadow-md transition-colors"
                >
                  {formLoading ? 'Submitting...' : <><Send className="w-4 h-4 mr-2" /> Submit Ticket</>}
                </motion.button>
              </div>
            </form>
          </motion.div>
        )}
      </AnimatePresence>

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
              <FileText className="w-10 h-10 text-gray-300" />
            </div>
            <h3 className="text-xl font-bold text-brand-navy">No tickets found</h3>
            <p className="text-gray-500 mt-2">You haven't submitted any support requests yet.</p>
          </div>
        ) : (
          tickets.map((ticket, idx) => (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.1 }}
              key={ticket.id} 
              className="bg-white rounded-2xl shadow-[0_4px_20px_rgb(0,0,0,0.05)] border border-gray-100 overflow-hidden flex flex-col hover:shadow-[0_8px_30px_rgb(0,0,0,0.08)] transition-all duration-300"
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
                
                <div className="flex items-center gap-4 text-xs font-bold mt-auto pt-4 border-t border-gray-50 mb-2">
                  <span className="flex items-center bg-gray-50 px-2 py-1 rounded text-gray-600">
                    <span className="w-2 h-2 rounded-full bg-brand-navy mr-2"></span> {ticket.categoryName}
                  </span>
                  <span className="flex items-center">
                    {getPriorityIcon(ticket.priority)} {ticket.priority}
                  </span>
                </div>
                
                {(ticket.businessUnit || ticket.assetTag) && (
                  <div className="text-[10px] text-gray-400 font-mono mt-2">
                    {ticket.businessUnit && <span>BU: {ticket.businessUnit} </span>}
                    {ticket.assetTag && <span>| Asset: {ticket.assetTag}</span>}
                  </div>
                )}
                
                {ticket.status === 'REOPENED' && ticket.latestRejectionReason && (
                  <div className="bg-red-50 border border-red-200 p-3 rounded-lg text-sm text-gray-700 mt-4">
                    <div className="font-bold text-brand-red mb-1 text-xs flex items-center">
                      <AlertTriangle className="w-3 h-3 mr-1"/> Your Rejection Reason
                    </div>
                    <span className="italic">"{ticket.latestRejectionReason}"</span>
                  </div>
                )}
              </div>

              {ticket.status === 'RESOLVED' && (
                <div className="bg-brand-cream border-t border-orange-100 p-5">
                  <h4 className="text-sm font-black text-brand-navy flex items-center mb-2">
                    <CheckCircle className="w-4 h-4 text-green-600 mr-2" /> Resolution Provided
                  </h4>
                  <p className="text-sm text-gray-700 mb-4 bg-white p-3 rounded-lg border border-orange-50 italic">
                    "{ticket.latestResolution?.solutionDescription}"
                  </p>
                  
                  {showConfirmation === ticket.id ? (
                    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex flex-col gap-3">
                      <input 
                        type="text"
                        placeholder="Reason for rejection (required if rejecting)"
                        value={rejectionReason}
                        onChange={(e) => setRejectionReason(e.target.value)}
                        className="w-full p-2 text-sm border border-gray-200 rounded-lg outline-none focus:border-brand-red focus:ring-1 focus:ring-brand-red"
                      />
                      <div className="flex gap-2">
                        <button onClick={() => confirmResolution(ticket.id, 'CONFIRMED')} className="flex-1 bg-green-600 hover:bg-green-700 text-white font-bold py-2 rounded-lg text-sm transition-colors">Accept</button>
                        <button onClick={() => confirmResolution(ticket.id, 'REJECTED')} className="flex-1 bg-brand-red hover:bg-brand-dark text-white font-bold py-2 rounded-lg text-sm transition-colors">Reject</button>
                      </div>
                    </motion.div>
                  ) : (
                    <button 
                      onClick={() => setShowConfirmation(ticket.id)}
                      className="w-full bg-brand-navy hover:bg-brand-dark text-brand-cream font-bold py-2.5 rounded-lg text-sm transition-colors shadow-sm"
                    >
                      Review Solution
                    </button>
                  )}
                </div>
              )}
            </motion.div>
          ))
        )}
      </div>
    </motion.div>
  );
}
