'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  Activity, 
  PlusCircle, 
  Search, 
  Calendar, 
  AlertCircle, 
  CheckCircle, 
  Clock, 
  Trash2, 
  ChevronRight,
  TrendingUp,
  FileQuestion
} from 'lucide-react';
import { supabase, isSupabaseConfigured } from '@/lib/supabase';

interface InjuryItem {
  id: string;
  injury_date: string;
  body_area: string;
  injury_description: string;
  injury_mechanism: string;
  pain_level: number;
  status: string;
  created_at: string;
  urgency_level?: string;
  category?: string;
  recovery_estimate?: string;
  log_count?: number;
}

export default function DashboardPage() {
  const [injuries, setInjuries] = useState<InjuryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedBodyArea, setSelectedBodyArea] = useState('All');
  const [selectedStatus, setSelectedStatus] = useState('All');
  const [deletingId, setDeletingId] = useState<string | null>(null);

  useEffect(() => {
    loadInjuries();
  }, []);

  const loadInjuries = async () => {
    setLoading(true);
    if (isSupabaseConfigured) {
      try {
        const { data, error } = await supabase
          .from('injury_records')
          .select('*, ai_assessments(possible_category, urgency_level, recovery_estimate), recovery_logs(count)')
          .order('created_at', { ascending: false });

        if (!error && data) {
          const formatted = data.map((d: any) => ({
            id: d.id,
            injury_date: d.injury_date,
            body_area: d.body_area,
            injury_description: d.injury_description,
            injury_mechanism: d.injury_mechanism,
            pain_level: d.pain_level || 3,
            status: d.status || 'Active',
            created_at: d.created_at,
            urgency_level: d.ai_assessments?.[0]?.urgency_level || 'ROUTINE_EVALUATION',
            category: d.ai_assessments?.[0]?.possible_category || d.injury_mechanism,
            recovery_estimate: d.ai_assessments?.[0]?.recovery_estimate,
            log_count: d.recovery_logs?.[0]?.count || 0
          }));
          setInjuries(formatted);
          setLoading(false);
          return;
        }
      } catch (err) {
        console.warn('Supabase fetch failed, falling back to local storage cache:', err);
      }
    }

    // LocalStorage fallback cache
    try {
      const cached = localStorage.getItem('injuryai_records');
      if (cached) {
        setInjuries(JSON.parse(cached));
      } else {
        // Initial sample record
        const sample: InjuryItem[] = [
          {
            id: 'demo-rec-1',
            injury_date: new Date().toISOString().split('T')[0],
            body_area: 'Ankle',
            injury_description: 'Inverted right foot stepping off curb during light jog. Mild swelling on lateral malleolus.',
            injury_mechanism: 'Twisting motion',
            pain_level: 4,
            status: 'Active',
            created_at: new Date().toISOString(),
            urgency_level: 'ROUTINE_EVALUATION',
            category: 'Suspected Ligamentous Sprain (Grade I)',
            recovery_estimate: 'Approximately 2 to 4 weeks for mild-to-moderate soft-tissue recovery.',
            log_count: 1
          }
        ];
        setInjuries(sample);
        localStorage.setItem('injuryai_records', JSON.stringify(sample));
      }
    } catch {
      setInjuries([]);
    }
    setLoading(false);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to permanently delete this injury case and all associated recovery logs?')) {
      return;
    }
    setDeletingId(id);

    if (isSupabaseConfigured) {
      await supabase.from('injury_records').delete().eq('id', id);
    }

    const updated = injuries.filter(i => i.id !== id);
    setInjuries(updated);
    try {
      localStorage.setItem('injuryai_records', JSON.stringify(updated));
    } catch {}
    setDeletingId(null);
  };

  const filtered = injuries.filter(item => {
    const matchesQuery = !searchQuery || 
      item.body_area.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.injury_description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.category && item.category.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesArea = selectedBodyArea === 'All' || item.body_area.toLowerCase() === selectedBodyArea.toLowerCase();
    const matchesStatus = selectedStatus === 'All' || item.status.toLowerCase() === selectedStatus.toLowerCase();

    return matchesQuery && matchesArea && matchesStatus;
  });

  const totalCount = injuries.length;
  const activeCount = injuries.filter(i => i.status !== 'Resolved').length;
  const resolvedCount = injuries.filter(i => i.status === 'Resolved').length;
  const totalLogs = injuries.reduce((acc, curr) => acc + (curr.log_count || 0), 0);

  return (
    <div className="space-y-8">
      {/* Header and Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
            Case Dashboard
          </h1>
          <p className="text-sm text-slate-500">
            Monitor injury cases, clinical triage assessments, and daily recovery milestones.
          </p>
        </div>

        <Link
          href="/assessment/new"
          className="bg-sky-600 hover:bg-sky-700 text-white font-semibold px-4 py-2.5 rounded-lg flex items-center justify-center gap-2 shadow-sm transition"
        >
          <PlusCircle className="w-4 h-4" />
          <span>New Assessment</span>
        </Link>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Cases</span>
          <p className="text-2xl font-bold text-slate-900 mt-1">{totalCount}</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <span className="text-xs font-semibold text-amber-600 uppercase tracking-wider">Under Care</span>
          <p className="text-2xl font-bold text-amber-700 mt-1">{activeCount}</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <span className="text-xs font-semibold text-emerald-600 uppercase tracking-wider">Fully Recovered</span>
          <p className="text-2xl font-bold text-emerald-700 mt-1">{resolvedCount}</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <span className="text-xs font-semibold text-sky-600 uppercase tracking-wider">Recovery Logs</span>
          <p className="text-2xl font-bold text-sky-700 mt-1">{totalLogs}</p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <div className="flex flex-col md:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
            <input
              type="text"
              placeholder="Search by body area, symptoms, or category..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            <select
              value={selectedBodyArea}
              onChange={(e) => setSelectedBodyArea(e.target.value)}
              className="border border-slate-200 rounded-lg text-xs font-medium px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
            >
              <option value="All">All Body Areas</option>
              <option value="Ankle">Ankle</option>
              <option value="Knee">Knee</option>
              <option value="Wrist">Wrist</option>
              <option value="Hand">Hand</option>
              <option value="Foot">Foot</option>
              <option value="Shoulder">Shoulder</option>
              <option value="Head">Head</option>
            </select>

            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="border border-slate-200 rounded-lg text-xs font-medium px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
            >
              <option value="All">All Statuses</option>
              <option value="Active">Active</option>
              <option value="Healing">Healing</option>
              <option value="Resolved">Resolved</option>
            </select>
          </div>
        </div>
      </div>

      {/* Cases List */}
      <div className="space-y-4">
        <h2 className="text-lg font-bold text-slate-900">
          Injury Records ({filtered.length})
        </h2>

        {loading ? (
          <div className="bg-white rounded-xl border border-slate-200 p-8 text-center text-sm text-slate-500">
            Loading injury records...
          </div>
        ) : filtered.length === 0 ? (
          <div className="bg-white rounded-xl border border-slate-200 p-12 text-center space-y-3">
            <div className="w-12 h-12 rounded-full bg-slate-100 text-slate-400 mx-auto flex items-center justify-center">
              <FileQuestion className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-800">No Injury Records Found</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              {searchQuery ? "No records match your search criteria." : "Start your first injury assessment to begin tracking your clinical evaluation and recovery progress."}
            </p>
            <div className="pt-2">
              <Link
                href="/assessment/new"
                className="inline-flex items-center gap-2 bg-sky-600 text-white font-semibold text-xs px-4 py-2 rounded-lg hover:bg-sky-700 transition"
              >
                <PlusCircle className="w-4 h-4" />
                <span>Create Assessment</span>
              </Link>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {filtered.map((item) => (
              <div
                key={item.id}
                className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm hover:border-sky-300 transition flex flex-col md:flex-row md:items-center justify-between gap-4"
              >
                <div className="space-y-1.5 flex-1">
                  <div className="flex items-center gap-2.5">
                    <span className="font-bold text-base text-slate-900">
                      {item.body_area}
                    </span>
                    <span className="text-xs font-medium text-slate-400">
                      • {item.injury_date}
                    </span>
                    <span className={`text-[11px] font-semibold px-2 py-0.5 rounded-full ${
                      item.status === 'Resolved' ? 'bg-emerald-100 text-emerald-800' : 'bg-sky-100 text-sky-800'
                    }`}>
                      {item.status}
                    </span>
                  </div>

                  <p className="text-sm font-semibold text-sky-700">
                    {item.category || item.injury_mechanism}
                  </p>

                  <p className="text-xs text-slate-600 line-clamp-2">
                    {item.injury_description}
                  </p>

                  {item.recovery_estimate && (
                    <p className="text-xs text-teal-700 font-medium">
                      ⏱ {item.recovery_estimate}
                    </p>
                  )}
                </div>

                <div className="flex items-center gap-2 shrink-0 border-t md:border-t-0 pt-3 md:pt-0">
                  <Link
                    href={`/assessment/${item.id}`}
                    className="text-xs font-semibold text-slate-700 hover:text-sky-600 border border-slate-200 hover:border-sky-300 px-3 py-1.5 rounded-lg flex items-center gap-1 transition"
                  >
                    View Report
                    <ChevronRight className="w-3.5 h-3.5" />
                  </Link>

                  <Link
                    href={`/recovery/${item.id}`}
                    className="text-xs font-semibold text-white bg-sky-600 hover:bg-sky-700 px-3 py-1.5 rounded-lg flex items-center gap-1 transition"
                  >
                    Log Recovery
                  </Link>

                  <button
                    onClick={() => handleDelete(item.id)}
                    disabled={deletingId === item.id}
                    className="p-1.5 text-slate-400 hover:text-red-600 rounded-lg hover:bg-red-50 transition"
                    title="Delete Record"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
