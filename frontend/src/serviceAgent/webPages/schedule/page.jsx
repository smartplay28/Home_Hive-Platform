import React, { useState, useEffect, useCallback } from 'react';
import Layout from '../../layout.jsx';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import api from '../../../lib/api.js';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DAY_SHORT = { MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu',
                    FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun' };
const HOURS = Array.from({ length: 24 }, (_, i) => i);

const formatHour = (h) => {
  if (h === 0) return '12 AM';
  if (h === 12) return '12 PM';
  return h < 12 ? `${h} AM` : `${h - 12} PM`;
};

/* ─── Day Toggle Card ──────────────────────────────────────────────────────── */
const DaySlotCard = ({ day, slot, onToggle, onTimeChange }) => {
  const isEnabled = !!slot;

  return (
    <div className={`rounded-xl border p-4 transition-all duration-200
      ${isEnabled ? 'bg-[#eef4fb] border-[#1c4e80]' : 'bg-white border-gray-200'}`}>

      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-3">
          <span className="font-bold text-gray-700">{DAY_SHORT[day]}</span>
          <span className="text-xs text-gray-400">{day}</span>
        </div>

        {/* Toggle switch */}
        <button
          onClick={() => onToggle(day)}
          className={`relative w-11 h-6 rounded-full transition-colors duration-200
            ${isEnabled ? 'bg-[#1c4e80]' : 'bg-gray-300'}`}
        >
          <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow
            transition-transform duration-200
            ${isEnabled ? 'translate-x-5' : 'translate-x-0'}`} />
        </button>
      </div>

      {isEnabled && (
        <div className="flex items-center gap-3 mt-2">
          <div className="flex-1">
            <label className="text-xs text-gray-500 mb-1 block">From</label>
            <select
              value={slot.startHour}
              onChange={(e) => onTimeChange(day, 'startHour', e.target.value)}
              className="w-full text-sm border border-gray-300 rounded-lg px-2 py-1.5
                focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none"
            >
              {HOURS.slice(0, 23).map(h => (
                <option key={h} value={h}>{formatHour(h)}</option>
              ))}
            </select>
          </div>
          <span className="text-gray-400 mt-4">→</span>
          <div className="flex-1">
            <label className="text-xs text-gray-500 mb-1 block">To</label>
            <select
              value={slot.endHour}
              onChange={(e) => onTimeChange(day, 'endHour', e.target.value)}
              className="w-full text-sm border border-gray-300 rounded-lg px-2 py-1.5
                focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none"
            >
              {HOURS.slice(1).map(h => (
                <option key={h} value={h}>{formatHour(h)}</option>
              ))}
            </select>
          </div>
        </div>
      )}
    </div>
  );
};

/* ─── Blocked Date Picker ───────────────────────────────────────────────────── */
const BlockedDateSection = ({ blockedDates, onAdd, onRemove }) => {
  const [newDate, setNewDate] = useState('');

  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5">
      <h3 className="font-bold text-gray-700 mb-4">📅 Days Off / Blocked Dates</h3>

      <div className="flex gap-2 mb-4">
        <input
          type="date"
          min={today}
          value={newDate}
          onChange={(e) => setNewDate(e.target.value)}
          className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm
            focus:ring-2 focus:ring-[#1c4e80] outline-none"
        />
        <button
          onClick={() => { if (newDate) { onAdd(newDate); setNewDate(''); } }}
          className="px-4 py-2 bg-[#1c4e80] text-white rounded-lg text-sm hover:bg-[#153a61] transition"
        >
          Block Date
        </button>
      </div>

      <div className="flex flex-wrap gap-2">
        {blockedDates.length === 0 && (
          <p className="text-sm text-gray-400">No blocked dates</p>
        )}
        {blockedDates.map(date => (
          <div key={date}
            className="flex items-center gap-1.5 bg-red-50 border border-red-200 rounded-full px-3 py-1">
            <span className="text-sm text-red-600">{date}</span>
            <button onClick={() => onRemove(date)}
              className="text-red-400 hover:text-red-600 font-bold text-xs ml-0.5">✕</button>
          </div>
        ))}
      </div>
    </div>
  );
};

/* ─── Schedule Page ─────────────────────────────────────────────────────────── */
const AgentSchedule = () => {
  const agentId = localStorage.getItem('agentId');

  // Map of day → slot config (null = disabled)
  const [slots, setSlots] = useState(
    Object.fromEntries(DAYS.map(d => [d, null]))
  );
  const [blockedDates, setBlockedDates] = useState([]);
  const [maxOrders, setMaxOrders] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const loadSchedule = useCallback(async () => {
    try {
      const res = await api.get(`/schedules/${agentId}`);
      const schedule = res.data.data;

      // Rebuild slots map from API response
      const slotMap = Object.fromEntries(DAYS.map(d => [d, null]));
      schedule.weeklySlots?.forEach(s => {
        slotMap[s.dayOfWeek] = { startHour: s.startHour, endHour: s.endHour };
      });

      setSlots(slotMap);
      setBlockedDates(schedule.blockedDates || []);
      setMaxOrders(schedule.maxOrdersPerDay || 0);
    } catch (err) {
      toast.error('Failed to load schedule.');
    } finally {
      setLoading(false);
    }
  }, [agentId]);

  useEffect(() => { loadSchedule(); }, [loadSchedule]);

  const toggleDay = (day) => {
    setSlots(prev => ({
      ...prev,
      [day]: prev[day] ? null : { startHour: 9, endHour: 18 },
    }));
  };

  const handleTimeChange = (day, field, value) => {
    setSlots(prev => ({
      ...prev,
      [day]: { ...prev[day], [field]: value },
    }));
  };

  const saveSchedule = async () => {
    setSaving(true);
    try {
      const weeklySlots = DAYS
        .filter(d => slots[d] !== null)
        .map(d => ({ dayOfWeek: d, ...slots[d] }));

      await api.put(`/schedules/${agentId}/slots`, weeklySlots);
      await api.put(`/schedules/${agentId}/max-orders`, { maxOrdersPerDay: maxOrders });
      toast.success('Schedule saved! ✅');
    } catch (err) {
      toast.error('Failed to save schedule.');
    } finally {
      setSaving(false);
    }
  };

  const addBlockedDate = async (date) => {
    try {
      await api.post(`/schedules/${agentId}/blocked`, [date]);
      setBlockedDates(prev => [...prev, date].sort());
      toast.success(`${date} blocked.`);
    } catch (err) {
      toast.error('Failed to block date.');
    }
  };

  const removeBlockedDate = async (date) => {
    try {
      await api.delete(`/schedules/${agentId}/blocked`, { params: { date } });
      setBlockedDates(prev => prev.filter(d => d !== date));
      toast.info(`${date} unblocked.`);
    } catch (err) {
      toast.error('Failed to unblock date.');
    }
  };

  const enabledDays = DAYS.filter(d => slots[d] !== null).length;

  return (
    <Layout>
      <ToastContainer position="top-right" limit={3} />
      <div className="bg-[#eaf0f7] min-h-screen p-6">

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-[#1c4e80]">My Availability</h1>
          <p className="text-gray-500 mt-1 text-sm">
            Set your working hours so you only get orders when you're free.
          </p>
        </div>

        {loading ? (
          <div className="grid gap-3">
            {[...Array(7)].map((_, i) => (
              <div key={i} className="h-16 bg-white rounded-xl animate-pulse" />
            ))}
          </div>
        ) : (
          <div className="max-w-xl space-y-6">

            {/* Summary badge */}
            <div className="flex items-center gap-3 p-4 bg-[#1c4e80] text-white rounded-xl">
              <span className="text-2xl">📆</span>
              <div>
                <p className="font-semibold">{enabledDays} days active</p>
                <p className="text-xs text-white/70">You'll receive orders on these days only</p>
              </div>
            </div>

            {/* Weekly slots */}
            <div className="space-y-3">
              <h2 className="font-bold text-gray-700">Weekly Schedule</h2>
              {DAYS.map(day => (
                <DaySlotCard
                  key={day}
                  day={day}
                  slot={slots[day]}
                  onToggle={toggleDay}
                  onTimeChange={handleTimeChange}
                />
              ))}
            </div>

            {/* Max orders per day */}
            <div className="bg-white rounded-xl border border-gray-200 p-5">
              <h3 className="font-bold text-gray-700 mb-1">Max Orders Per Day</h3>
              <p className="text-xs text-gray-400 mb-3">Set to 0 for unlimited</p>
              <div className="flex items-center gap-4">
                <button onClick={() => setMaxOrders(p => Math.max(0, p - 1))}
                  className="w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 font-bold text-lg">−</button>
                <span className="text-2xl font-bold text-[#1c4e80] w-8 text-center">
                  {maxOrders === 0 ? '∞' : maxOrders}
                </span>
                <button onClick={() => setMaxOrders(p => p + 1)}
                  className="w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 font-bold text-lg">+</button>
              </div>
            </div>

            {/* Blocked Dates */}
            <BlockedDateSection
              blockedDates={blockedDates}
              onAdd={addBlockedDate}
              onRemove={removeBlockedDate}
            />

            {/* Save button */}
            <button
              onClick={saveSchedule}
              disabled={saving}
              className="w-full py-4 bg-[#1c4e80] text-white rounded-xl font-bold text-base
                hover:bg-[#153a61] transition-all duration-200
                disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {saving ? 'Saving...' : 'Save Schedule'}
            </button>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default AgentSchedule;
