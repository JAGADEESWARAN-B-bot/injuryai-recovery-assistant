'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  History, 
  Calendar, 
  ChevronRight, 
  ArrowLeft, 
  Search,
  Activity,
  Trash2
} from 'lucide-react';
import { supabase, isSupabaseConfigured } from '@/lib/supabase';

export default function HistoryPage() {
  const [records, setRecords] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    try {
      const stored = localStorage.getItem('injuryai_records');
      if (stored) {
        setRecords(JSON.parse(stored));
      }
    } catch {}
    setLoading(false);
  }, []);

  const filtered = records.filter(r => 
    !search || 
    r.body_area?.toLowerCase().includes(search.toLowerCase()) ||
    r.injury_description?.toLowerCase().includes(search.toLowerCase()) ||
    r.injury_mechanism?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div className="flex items-center gap-3">
        <Link href="/dashboard" className="p-2 rounded-lg hover:bg-slate-200 transition text-slate-600">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">
            Case History & Archived Evaluations
          </h1>
          <p className="text-xs text-slate-500">
            Chronological log of past injury intakes, clinical assessments, and outcomes.
          </p>
        </div>
      </div>

      <div className="relative">
        <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
        <input
          type="text"
          placeholder="Filter case history by body region or description..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-sky-500 bg-white"
        />
      </div>

      <div className="space-y-3">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading case history...</div>
        ) : filtered.length === 0 ? (
          <div className="bg-white rounded-xl border border-slate-200 p-12 text-center text-xs text-slate-500">
            No past injury records found.
          </div>
        ) : (
          filtered.map((item) => (
            <div
              key={item.id}
              className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-sky-300 transition"
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-sm text-slate-900">{item.body_area}</span>
                  <span className="text-xs text-slate-400">• {item.injury_date}</span>
                  <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">
                    {item.status}
                  </span>
                </div>
                <p className="text-xs text-slate-600 line-clamp-1">
                  {item.injury_description}
                </p>
              </div>

              <div className="flex items-center gap-2">
                <Link
                  href={`/assessment/${item.id}`}
                  className="text-xs font-semibold text-sky-600 hover:text-sky-700 border border-sky-200 hover:bg-sky-50 px-3 py-1.5 rounded-lg flex items-center gap-1 transition"
                >
                  View Details
                  <ChevronRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
