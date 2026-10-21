'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { 
  AlertCircle, 
  Clock, 
  ArrowLeft, 
  ShieldCheck, 
  Activity, 
  CheckCircle, 
  Eye, 
  BookOpen, 
  PlusCircle, 
  PhoneCall 
} from 'lucide-react';

export default function AssessmentResultPage() {
  const params = useParams();
  const id = params?.id as string;
  const [record, setRecord] = useState<any>(null);
  const [loading, setLoading] = useState(true);

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
    } catch (e) {
      console.warn('Error reading record:', e);
    }
    setLoading(false);
  }, [id]);

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto py-12 text-center text-sm text-slate-500">
        Loading clinical assessment details...
      </div>
    );
  }

  if (!record) {
    return (
      <div className="max-w-3xl mx-auto py-12 text-center space-y-4">
        <h2 className="text-xl font-bold text-slate-800">Record Not Found</h2>
        <p className="text-xs text-slate-500">The requested injury case could not be located.</p>
        <Link href="/dashboard" className="inline-block text-xs font-semibold text-sky-600 underline">
          Return to Dashboard
        </Link>
      </div>
    );
  }

  const assessment = record.assessment || {};
  const isEmergency = assessment.urgencyLevel === 'EMERGENCY';

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div className="flex items-center gap-3">
        <Link href="/dashboard" className="p-2 rounded-lg hover:bg-slate-200 transition text-slate-600">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">
            Clinical Assessment Report
          </h1>
          <p className="text-xs text-slate-500">
            {record.bodyArea} Case • Assessed on {record.injury_date}
          </p>
        </div>
      </div>

      {/* Urgency Level Banner */}
      <div className={`p-6 rounded-xl border ${
        isEmergency 
          ? 'bg-red-50 border-red-300 text-red-900' 
          : 'bg-white border-slate-200 text-slate-900 shadow-sm'
      }`}>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500 block mb-1">
              Assessed Category
            </span>
            <h2 className="text-xl font-bold text-slate-900">
              {assessment.possibleCategory || record.injury_mechanism}
            </h2>
          </div>

          <span className={`px-3 py-1.5 rounded-lg text-xs font-bold self-start sm:self-auto ${
            isEmergency 
              ? 'bg-red-600 text-white' 
              : assessment.urgencyLevel === 'URGENT_CARE'
              ? 'bg-amber-100 text-amber-800 border border-amber-300'
              : 'bg-sky-100 text-sky-800 border border-sky-300'
          }`}>
            {assessment.urgencyLevel?.replace('_', ' ') || 'CLINICAL REVIEW'}
          </span>
        </div>

        {isEmergency && (
          <div className="mt-4 pt-4 border-t border-red-200 flex items-center justify-between">
            <span className="text-xs font-bold text-red-800">
              Immediate medical evaluation required.
            </span>
            <Link
              href="/emergency"
              className="text-xs font-bold bg-red-600 text-white px-3 py-1.5 rounded-lg flex items-center gap-1.5"
            >
              <PhoneCall className="w-3.5 h-3.5" /> Call Hotlines
            </Link>
          </div>
        )}
      </div>

      {/* Submitted Image Preview */}
      {record.image_preview && (
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
          <span className="text-xs font-bold text-slate-800 block">
            Submitted {record.image_type === 'XRAY' ? 'X-Ray Radiograph' : 'External Photograph'}
          </span>
          <div className="rounded-lg overflow-hidden bg-slate-950 flex items-center justify-center max-h-72">
            <img src={record.image_preview} alt="Injury" className="max-h-72 object-contain" />
          </div>
        </div>
      )}

      {/* 1. Visible Observations */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
        <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
          <Eye className="w-4 h-4 text-sky-600" />
          Visible Observations & Characteristics
        </h3>
        <p className="text-xs text-slate-700 leading-relaxed">
          {assessment.visibleObservations || 'No visible observations recorded.'}
        </p>
      </div>

      {/* 2. Assessment Limitations & Uncertainty Notes */}
      <div className="bg-slate-100 p-6 rounded-xl border border-slate-200 space-y-3">
        <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-slate-500" />
          Assessment Limitations & Uncertainty
        </h3>
        <p className="text-xs text-slate-600 leading-relaxed">
          {assessment.uncertaintyNotes || 'Photographs cannot assess subcutaneous or structural integrity.'}
        </p>
      </div>

      {/* 3. Approximate Recovery Range */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
        <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
          <Clock className="w-4 h-4 text-teal-600" />
          Approximate Recovery Timeline
        </h3>
        <div className="p-3 bg-teal-50 border border-teal-200 rounded-lg text-xs font-bold text-teal-800">
          {assessment.recoveryEstimate || 'Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional.'}
        </div>
        <p className="text-xs text-slate-600 leading-relaxed">
          <strong>Factors Influencing Recovery:</strong> {assessment.recoveryExplanation}
        </p>
      </div>

      {/* 4. First-Aid Guidance */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
        <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-sky-600" />
          General First-Aid & Care Principles
        </h3>
        <p className="text-xs text-slate-700 leading-relaxed whitespace-pre-line">
          {assessment.firstAidGuidance || 'Rest, ice intermittently, elevate limb, and protect affected area.'}
        </p>
      </div>

      {/* 5. Medical References */}
      {assessment.medicalReferences && assessment.medicalReferences.length > 0 && (
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-2">
          <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-indigo-600" />
            Clinical Practice Guidelines & References
          </h3>
          <ul className="text-xs text-slate-600 space-y-1 list-disc pl-5">
            {assessment.medicalReferences.map((ref: string, idx: number) => (
              <li key={idx}>{ref}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Actions */}
      <div className="flex flex-col sm:flex-row gap-3 pt-2">
        <Link
          href={`/recovery/${record.id}`}
          className="flex-1 bg-sky-600 hover:bg-sky-700 text-white font-bold text-sm py-3 px-4 rounded-xl flex items-center justify-center gap-2 transition"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Log Daily Recovery Update</span>
        </Link>
        <Link
          href="/dashboard"
          className="bg-slate-100 hover:bg-slate-200 text-slate-800 font-semibold text-sm py-3 px-6 rounded-xl text-center transition"
        >
          Return to Dashboard
        </Link>
      </div>
    </div>
  );
}
