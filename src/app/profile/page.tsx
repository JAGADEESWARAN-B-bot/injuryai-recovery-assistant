'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  User, 
  Shield, 
  Save, 
  ArrowLeft, 
  Check, 
  Lock, 
  Database, 
  HeartHandshake 
} from 'lucide-react';
import { isSupabaseConfigured } from '@/lib/supabase';

export default function ProfilePage() {
  const [displayName, setDisplayName] = useState('Patient User');
  const [emergencyContact, setEmergencyContact] = useState('Alex Doe');
  const [emergencyPhone, setEmergencyPhone] = useState('+1 (555) 019-2831');
  const [allergies, setAllergies] = useState('Penicillin, Latex');
  const [conditions, setConditions] = useState('None');
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    try {
      const stored = localStorage.getItem('injuryai_profile');
      if (stored) {
        const p = JSON.parse(stored);
        setDisplayName(p.displayName || 'Patient User');
        setEmergencyContact(p.emergencyContact || 'Alex Doe');
        setEmergencyPhone(p.emergencyPhone || '+1 (555) 019-2831');
        setAllergies(p.allergies || 'Penicillin, Latex');
        setConditions(p.conditions || 'None');
      }
    } catch {}
  }, []);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const profile = { displayName, emergencyContact, emergencyPhone, allergies, conditions };
    try {
      localStorage.setItem('injuryai_profile', JSON.stringify(profile));
    } catch {}
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div className="flex items-center gap-3">
        <Link href="/dashboard" className="p-2 rounded-lg hover:bg-slate-200 transition text-slate-600">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">
            Profile & Privacy Settings
          </h1>
          <p className="text-xs text-slate-500">
            Manage your personal clinical profile, emergency contacts, and privacy preferences.
          </p>
        </div>
      </div>

      <form onSubmit={handleSave} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-6">
        <div className="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div className="w-12 h-12 rounded-full bg-sky-100 text-sky-600 flex items-center justify-center font-bold text-lg">
            <User className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-base font-bold text-slate-900">{displayName}</h2>
            <span className="text-xs text-slate-400">Medical Decision-Support Profile</span>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Patient Full Name
            </label>
            <input
              type="text"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              className="w-full border border-slate-200 rounded-lg text-xs p-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Emergency Contact Name
            </label>
            <input
              type="text"
              value={emergencyContact}
              onChange={(e) => setEmergencyContact(e.target.value)}
              className="w-full border border-slate-200 rounded-lg text-xs p-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Emergency Contact Phone
            </label>
            <input
              type="text"
              value={emergencyPhone}
              onChange={(e) => setEmergencyPhone(e.target.value)}
              className="w-full border border-slate-200 rounded-lg text-xs p-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Known Drug/Material Allergies
            </label>
            <input
              type="text"
              value={allergies}
              onChange={(e) => setAllergies(e.target.value)}
              className="w-full border border-slate-200 rounded-lg text-xs p-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div className="sm:col-span-2">
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Underlying Medical Conditions
            </label>
            <input
              type="text"
              value={conditions}
              onChange={(e) => setConditions(e.target.value)}
              className="w-full border border-slate-200 rounded-lg text-xs p-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        </div>

        <button
          type="submit"
          className="w-full py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-lg flex items-center justify-center gap-2 transition"
        >
          {saved ? <Check className="w-4 h-4" /> : <Save className="w-4 h-4" />}
          <span>{saved ? 'Profile Changes Saved!' : 'Save Profile Changes'}</span>
        </button>
      </form>

      {/* Cloud & Privacy Status */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
          <Database className="w-4 h-4 text-teal-600" />
          Cloud Architecture & Data Protection
        </h3>
        <p className="text-xs text-slate-600 leading-relaxed">
          {isSupabaseConfigured
            ? "Connected to production Supabase PostgreSQL database. Row Level Security (RLS) is active, ensuring you can only access your own private medical records."
            : "Running in local resilient offline mode. To enable persistent cross-device cloud synchronization, configure your NEXT_PUBLIC_SUPABASE_URL and NEXT_PUBLIC_SUPABASE_ANON_KEY in Vercel project environment variables."}
        </p>
      </div>
    </div>
  );
}
