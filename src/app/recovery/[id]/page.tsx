'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  ArrowLeft, 
  Activity, 
  Save, 
  CheckCircle, 
  Clock, 
  TrendingUp, 
  AlertTriangle 
} from 'lucide-react';
import { supabase, isSupabaseConfigured } from '@/lib/supabase';

interface LogEntry {
  id: string;
  created_at: string;
  pain_level: number;
  swelling_status: string;
  mobility_score: number;
  progress_notes: string;
  reported_recovery_status: string;
}

export default function RecoveryTrackingPage() {
  const params = useParams();
  const router = useRouter();
  const id = params?.id as string;

  const [record, setRecord] = useState<any>(null);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [painLevel, setPainLevel] = useState(3);
  const [swellingStatus, setSwellingStatus] = useState('Mild');
  const [mobilityScore, setMobilityScore] = useState(3);
  const [progressNotes, setProgressNotes] = useState('');
  const [reportedStatus, setReportedStatus] = useState('Healing well');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    try {
      const stored = localStorage.getItem(`injuryai_detail_${id}`);
      if (stored) {
        setRecord(JSON.parse(stored));
      } else {
        const records = JSON.parse(localStorage.getItem('injuryai_records') || '[]');
        const found = records.find((r: any) => r.id === id);
        if (found) setRecord(found);
      }

      const storedLogs = localStorage.getItem(`injuryai_logs_${id}`);
      if (storedLogs) {
        setLogs(JSON.parse(storedLogs));
      }
    } catch (e) {
      console.warn(e);
    }
  }, [id]);

  const handleSaveLog = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);

    const newLog: LogEntry = {
      id: 'log_' + Date.now(),
      created_at: new Date().toISOString(),
      pain_level: painLevel,
      swelling_status: swellingStatus,
      mobility_score: mobilityScore,
      progress_notes: progressNotes,
      reported_recovery_status: reportedStatus
    };

    if (isSupabaseConfigured) {
      try {
        await supabase.from('recovery_logs').insert({
          id: newLog.id,
          injury_record_id: id,
          user_id: (await supabase.auth.getUser()).data.user?.id || 'anonymous_user',
          pain_level: painLevel,
          swelling_status: swellingStatus,
          mobility_score: mobilityScore,
          progress_notes: progressNotes,
          reported_recovery_status: reportedStatus
        });
      } catch (err) {
        console.warn('Supabase log insert error:', err);
      }
    }

    const updated = [newLog, ...logs];
    setLogs(updated);
    try {
      localStorage.setItem(`injuryai_logs_${id}`, JSON.stringify(updated));

      // Also update record status
      if (record) {
        const updatedRecord = {
          ...record,
          status: reportedStatus === 'Fully Recovered' ? 'Resolved' : reportedStatus === 'Worsening' ? 'Worsening' : 'Healing',
          log_count: updated.length
        };
        setRecord(updatedRecord);
        localStorage.setItem(`injuryai_detail_${id}`, JSON.stringify(updatedRecord));

        const records = JSON.parse(localStorage.getItem('injuryai_records') || '[]');
        const updatedList = records.map((r: any) => r.id === id ? updatedRecord : r);
        localStorage.setItem('injuryai_records', JSON.stringify(updatedList));
      }
    } catch {}

    setProgressNotes('');
    setSaving(false);
  };

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div className="flex items-center gap-3">
        <Link href={`/assessment/${id}`} className="p-2 rounded-lg hover:bg-slate-200 transition text-slate-600">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">
            Recovery Progress Log
          </h1>
          <p className="text-xs text-slate-500">
            Case: {record?.body_area || 'Injury'} • Incident Date: {record?.injury_date || 'N/A'}
          </p>
        </div>
      </div>

      {/* New Log Entry Form */}
      <form onSubmit={handleSaveLog} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-6">
        <h2 className="text-base font-bold text-slate-900">
          Record Daily Progress Update
        </h2>

        {/* Pain Level */}
        <div>
          <div className="flex justify-between items-center mb-1">
            <label className="text-xs font-semibold text-slate-700">
              Current Pain Level (0 to 10)
            </label>
            <span className={`text-xs font-bold ${painLevel >= 7 ? 'text-red-600' : 'text-sky-600'}`}>
              {painLevel} / 10
            </span>
          </div>
          <input
            type="range"
            min="0"
            max="10"
            value={painLevel}
            onChange={(e) => setPainLevel(parseInt(e.target.value))}
            className="w-full accent-sky-600"
          />
        </div>

        {/* Swelling */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-2">
            Swelling & Edema Status
          </label>
          <div className="flex flex-wrap gap-2">
            {['None', 'Mild', 'Moderate', 'Noticeably Reduced', 'Severe'].map((st) => (
              <button
                type="button"
                key={st}
                onClick={() => setSwellingStatus(st)}
                className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition ${
                  swellingStatus === st 
                    ? 'bg-sky-600 text-white border-sky-600' 
                    : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
                }`}
              >
                {st}
              </button>
            ))}
          </div>
        </div>

        {/* Mobility */}
        <div>
          <div className="flex justify-between items-center mb-1">
            <label className="text-xs font-semibold text-slate-700">
              Functional Mobility Score (1 to 5)
            </label>
            <span className="text-xs font-bold text-teal-700">{mobilityScore} / 5</span>
          </div>
          <p className="text-[11px] text-slate-500 mb-2">
            1: Immobile • 3: Partial range with mild discomfort • 5: Full normal pain-free motion
          </p>
          <div className="grid grid-cols-5 gap-2">
            {[1, 2, 3, 4, 5].map((score) => (
              <button
                type="button"
                key={score}
                onClick={() => setMobilityScore(score)}
                className={`py-2 text-xs font-bold rounded-lg border transition ${
                  mobilityScore === score 
                    ? 'bg-teal-600 text-white border-teal-600' 
                    : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
                }`}
              >
                {score}
              </button>
            ))}
          </div>
        </div>

        {/* Overall Recovery State */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-2">
            Overall Recovery Impression
          </label>
          <div className="flex flex-wrap gap-2">
            {['Healing well', 'Plateaued', 'Worsening', 'Fully Recovered'].map((st) => (
              <button
                type="button"
                key={st}
                onClick={() => setReportedStatus(st)}
                className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition ${
                  reportedStatus === st 
                    ? st === 'Fully Recovered' ? 'bg-emerald-600 text-white border-emerald-600' : 'bg-sky-600 text-white border-sky-600'
                    : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
                }`}
              >
                {st}
              </button>
            ))}
          </div>
        </div>

        {/* Progress Notes */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Daily Progress Notes
          </label>
          <textarea
            rows={3}
            value={progressNotes}
            onChange={(e) => setProgressNotes(e.target.value)}
            placeholder="e.g. Swelling noticeably diminished after 20 mins elevation and compression..."
            className="w-full border border-slate-200 rounded-lg text-xs p-3 focus:outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <button
          type="submit"
          disabled={saving}
          className="w-full py-3 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl flex items-center justify-center gap-2 transition"
        >
          <Save className="w-4 h-4" />
          <span>Save Recovery Entry</span>
        </button>
      </form>

      {/* Historical Recovery Timeline */}
      <div className="space-y-4">
        <h2 className="text-base font-bold text-slate-900">
          Recovery Timeline Entries ({logs.length})
        </h2>

        {logs.length === 0 ? (
          <div className="bg-white rounded-xl border border-slate-200 p-8 text-center text-xs text-slate-500">
            No recovery logs recorded yet. Submit your first daily update above.
          </div>
        ) : (
          <div className="space-y-3">
            {logs.map((log) => (
              <div
                key={log.id}
                className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-2"
              >
                <div className="flex items-center justify-between text-xs">
                  <span className="font-bold text-slate-800">
                    {new Date(log.created_at).toLocaleDateString()}
                  </span>
                  <span className={`px-2 py-0.5 rounded-full text-[11px] font-semibold ${
                    log.reported_recovery_status === 'Fully Recovered'
                      ? 'bg-emerald-100 text-emerald-800'
                      : log.reported_recovery_status === 'Worsening'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-sky-100 text-sky-800'
                  }`}>
                    {log.reported_recovery_status}
                  </span>
                </div>

                <div className="flex items-center gap-4 text-xs text-slate-600">
                  <span>Pain: <strong>{log.pain_level}/10</strong></span>
                  <span>Swelling: <strong>{log.swelling_status}</strong></span>
                  <span>Mobility: <strong>{log.mobility_score}/5</strong></span>
                </div>

                {log.progress_notes && (
                  <p className="text-xs text-slate-700 pt-1 border-t border-slate-100">
                    {log.progress_notes}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
