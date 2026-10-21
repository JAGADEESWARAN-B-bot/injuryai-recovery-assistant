'use client';

import Link from 'next/link';
import { 
  PhoneCall, 
  ShieldAlert, 
  AlertTriangle, 
  ArrowLeft, 
  HeartHandshake 
} from 'lucide-react';

const EMERGENCY_NUMBERS = [
  { country: 'United States & Canada', number: '911' },
  { country: 'European Union & India', number: '112' },
  { country: 'United Kingdom', number: '999' },
  { country: 'Australia', number: '000' }
];

export default function EmergencyPage() {
  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div className="flex items-center gap-3">
        <Link href="/dashboard" className="p-2 rounded-lg hover:bg-slate-200 transition text-slate-600">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-extrabold text-red-700 flex items-center gap-2">
            <ShieldAlert className="w-6 h-6" />
            Emergency Medical & Trauma Protocols
          </h1>
          <p className="text-xs text-slate-500">
            Immediate high-acuity guidelines and verified emergency telephone hotlines.
          </p>
        </div>
      </div>

      {/* Critical Red-Flag Banner */}
      <div className="bg-red-600 text-white p-6 rounded-2xl shadow-lg space-y-4">
        <h2 className="text-lg font-bold flex items-center gap-2">
          <AlertTriangle className="w-5 h-5 text-amber-300" />
          When to Seek Immediate Emergency Department Care
        </h2>
        <ul className="text-xs space-y-2 list-disc pl-5 leading-relaxed text-red-50">
          <li><strong>Uncontrolled bleeding</strong> that does not slow after 10-15 minutes of firm, direct pressure.</li>
          <li><strong>Open fracture or gross anatomic deformity</strong> (bone protruding through skin, severely angled limb).</li>
          <li><strong>Loss of consciousness, dizziness, or head trauma</strong> with vomiting, confusion, or pupil asymmetry.</li>
          <li><strong>Difficulty breathing or severe chest/abdominal pain</strong> following blunt trauma.</li>
          <li><strong>Cold, pale, blue, or numb extremity</strong> distal to the injury indicating neurovascular compromise.</li>
        </ul>
      </div>

      {/* Verified Emergency Hotlines */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <PhoneCall className="w-5 h-5 text-red-600" />
          Direct Emergency Service Numbers
        </h3>
        <p className="text-xs text-slate-500">
          Locally verified emergency dispatch services. Tap to dial directly on mobile devices:
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
          {EMERGENCY_NUMBERS.map((item) => (
            <a
              key={item.number}
              href={`tel:${item.number}`}
              className="p-4 rounded-xl border border-red-200 bg-red-50/50 hover:bg-red-100 flex items-center justify-between transition group"
            >
              <div>
                <span className="block text-xl font-extrabold text-red-700 group-hover:text-red-800">
                  {item.number}
                </span>
                <span className="text-xs font-medium text-slate-600">
                  {item.country}
                </span>
              </div>
              <PhoneCall className="w-5 h-5 text-red-600 group-hover:scale-110 transition" />
            </a>
          ))}
        </div>
      </div>

      {/* Immediate Trauma First-Aid Steps */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <h3 className="text-base font-bold text-slate-900">
          Immediate First-Aid Actions (While Dispatching EMS)
        </h3>

        <div className="space-y-3 text-xs text-slate-700 leading-relaxed">
          <div className="p-3 bg-slate-50 rounded-lg">
            <strong>1. Direct Pressure:</strong> For active bleeding, apply continuous firm pressure with a sterile gauze or clean cloth. Do not lift dressing to check; layer additional cloths on top if blood soaks through.
          </div>
          <div className="p-3 bg-slate-50 rounded-lg">
            <strong>2. Do Not Manipulate Fractures:</strong> If an extremity is deformed or bone is visible, do NOT attempt to straighten, push back, or test range of motion. Support limb loosely in its found position.
          </div>
          <div className="p-3 bg-slate-50 rounded-lg">
            <strong>3. Cervical Spine Immobilization:</strong> If head, neck, or high-impact fall is involved, keep patient lying still. Do not move the head or neck unless there is immediate environmental peril.
          </div>
          <div className="p-3 bg-slate-50 rounded-lg">
            <strong>4. Thermal Burns:</strong> Cool immediately with room-temperature or gently cool running potable water for 10-20 minutes. Do not use ice, butter, or puncture blisters. Cover with clean cling film or sterile non-adherent dressing.
          </div>
        </div>
      </div>

      <div className="text-center">
        <Link
          href="/dashboard"
          className="inline-block text-xs font-semibold text-slate-600 hover:text-slate-900 underline"
        >
          Return to Dashboard
        </Link>
      </div>
    </div>
  );
}
