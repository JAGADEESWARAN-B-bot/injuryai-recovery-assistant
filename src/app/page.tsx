import Link from 'next/link';
import { 
  ShieldAlert, 
  Activity, 
  Camera, 
  Clock, 
  CheckCircle, 
  AlertTriangle, 
  ArrowRight, 
  FileText, 
  HeartHandshake 
} from 'lucide-react';

export default function HomePage() {
  return (
    <div className="space-y-12">
      {/* Hero Section */}
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-sky-900 via-sky-800 to-slate-900 text-white p-8 sm:p-12 shadow-xl">
        <div className="relative z-10 max-w-3xl space-y-6">
          <div className="inline-flex items-center gap-2 bg-sky-500/20 border border-sky-400/30 rounded-full px-3 py-1 text-xs font-semibold text-sky-200">
            <Activity className="w-3.5 h-3.5" />
            Decision-Support & Recovery Intelligence
          </div>
          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight leading-tight">
            Smart Injury Assessment & Recovery Assistant
          </h1>
          <p className="text-sky-100 text-base sm:text-lg leading-relaxed">
            Upload external injury photos or X-rays for preliminary image assessment, automated safety triage, approximate recovery timelines, and structured healing progress tracking.
          </p>

          <div className="flex flex-wrap items-center gap-4 pt-2">
            <Link
              href="/assessment/new"
              className="bg-white text-sky-900 hover:bg-sky-50 font-bold px-6 py-3 rounded-xl flex items-center gap-2 shadow-md transition"
            >
              <Camera className="w-5 h-5 text-sky-600" />
              <span>Start Injury Assessment</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              href="/dashboard"
              className="bg-sky-700/60 hover:bg-sky-700 border border-sky-400/40 text-white font-semibold px-6 py-3 rounded-xl transition"
            >
              View Case Dashboard
            </Link>
          </div>
        </div>

        {/* Background decorative glow */}
        <div className="absolute -right-20 -bottom-20 w-96 h-96 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />
      </section>

      {/* Emergency Red-Flag Warning Banner */}
      <section className="rounded-xl border border-red-200 bg-red-50 p-6 shadow-sm">
        <div className="flex items-start gap-4">
          <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0 text-red-600">
            <ShieldAlert className="w-5 h-5" />
          </div>
          <div className="space-y-1">
            <h2 className="text-base font-bold text-red-900">
              Emergency Safety-Triage Notice
            </h2>
            <p className="text-sm text-red-700 leading-relaxed">
              If you or the patient have uncontrolled bleeding, a visibly deformed or exposed bone, loss of consciousness, cold/numb extremities, or difficulty breathing, <strong>do not wait for image processing</strong>. Call emergency medical services immediately (<strong>911 / 112 / 999</strong>).
            </p>
            <div className="pt-2">
              <Link
                href="/emergency"
                className="text-xs font-bold text-red-800 underline hover:text-red-900"
              >
                Review Verified Emergency Hotlines & Immediate Trauma Protocols →
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Pillars */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
          <div className="w-10 h-10 rounded-lg bg-sky-100 text-sky-600 flex items-center justify-center">
            <Camera className="w-5 h-5" />
          </div>
          <h2 className="font-bold text-slate-900 text-lg">Cautious AI Vision Analysis</h2>
          <p className="text-sm text-slate-600 leading-relaxed">
            Evaluates visible soft-tissue characteristics (swelling, erythema, abrasions). For X-rays, the system explicitly requires radiologist review and notes that automated fracture detection is clinically unavailable.
          </p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
          <div className="w-10 h-10 rounded-lg bg-teal-100 text-teal-700 flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
          <h2 className="font-bold text-slate-900 text-lg">Evidence-Based Recovery Ranges</h2>
          <p className="text-sm text-slate-600 leading-relaxed">
            Provides estimated healing durations grounded in peer-reviewed clinical guidelines, or returns the required safety fallback when information is insufficient.
          </p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
          <div className="w-10 h-10 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center">
            <FileText className="w-5 h-5" />
          </div>
          <h2 className="font-bold text-slate-900 text-lg">Persistent Recovery Tracking</h2>
          <p className="text-sm text-slate-600 leading-relaxed">
            Log daily pain ratings (0-10), swelling reduction, functional mobility scores (1-5), and recovery milestones to monitor your healing trajectory over time.
          </p>
        </div>
      </section>

      {/* Mandatory Medical Disclaimer Card */}
      <section className="bg-slate-100 rounded-xl border border-slate-200 p-6 text-xs text-slate-600 space-y-2">
        <div className="flex items-center gap-2 font-bold text-slate-800 text-sm">
          <HeartHandshake className="w-4 h-4 text-sky-600" />
          Medical Decision-Support Disclaimer
        </div>
        <p className="leading-relaxed">
          InjuryAI is an educational decision-support tool. It does not provide formal clinical diagnoses, radiology reports, or medical prescriptions. Two-dimensional surface photographs cannot assess bone fractures, deep ligament disruptions, or internal hemorrhage. Always consult a qualified physician or urgent care professional for in-person examination.
        </p>
      </section>
    </div>
  );
}
