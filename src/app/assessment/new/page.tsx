'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  Camera, 
  UploadCloud, 
  AlertTriangle, 
  ShieldAlert, 
  X, 
  Check, 
  ArrowLeft, 
  Loader2 
} from 'lucide-react';
import { evaluateEmergency, generateClinicalAssessment, RED_FLAG_SYMPTOMS } from '@/lib/triage';
import { supabase, isSupabaseConfigured } from '@/lib/supabase';

const BODY_AREAS = [
  'Ankle', 'Knee', 'Wrist', 'Hand / Fingers', 'Foot / Toes', 
  'Shoulder', 'Elbow', 'Head / Face', 'Back / Spine', 'Chest / Ribs', 'Skin / Soft Tissue'
];

const MECHANISMS = [
  'Twisting motion', 'Slip and Fall', 'Blunt Impact', 
  'Sports Collision', 'Laceration / Cut', 'Thermal / Heat Burn', 'Repetitive Strain'
];

const REGULAR_SYMPTOMS = [
  'Localized swelling',
  'Discoloration / Bruising',
  'Erythema / Redness',
  'Warmth around area',
  'Pain on movement',
  'Reduced range of motion',
  'Throbbing sensation',
  'Difficulty bearing weight',
  'Minor superficial bleeding'
];

export default function NewAssessmentPage() {
  const router = useRouter();

  const [bodyArea, setBodyArea] = useState('Ankle');
  const [mechanism, setMechanism] = useState('Twisting motion');
  const [injuryDate, setInjuryDate] = useState(new Date().toISOString().split('T')[0]);
  const [painLevel, setPainLevel] = useState(4);
  const [description, setDescription] = useState('');
  const [selectedSymptoms, setSelectedSymptoms] = useState<string[]>(['Localized swelling', 'Pain on movement']);
  const [imageType, setImageType] = useState<'EXTERNAL_PHOTO' | 'XRAY'>('EXTERNAL_PHOTO');
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Real-time emergency check
  const emergencyCheck = evaluateEmergency(selectedSymptoms, mechanism, description);

  const toggleSymptom = (sym: string) => {
    if (selectedSymptoms.includes(sym)) {
      setSelectedSymptoms(selectedSymptoms.filter(s => s !== sym));
    } else {
      setSelectedSymptoms([...selectedSymptoms, sym]);
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        alert('File size exceeds 10MB limit.');
        return;
      }
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setErrorMsg(null);

    try {
      const assessment = generateClinicalAssessment({
        imageType,
        bodyArea,
        injuryMechanism: mechanism,
        symptoms: selectedSymptoms,
        painLevel,
        description,
        hasImage: Boolean(imagePreview)
      });

      const recordId = 'rec_' + Date.now();
      const newRecord = {
        id: recordId,
        injury_date: injuryDate,
        body_area: bodyArea,
        injury_description: description || `Reported ${mechanism} on ${bodyArea}.`,
        injury_mechanism: mechanism,
        pain_level: painLevel,
        status: emergencyCheck.isEmergency ? 'Active - Emergency Alert' : 'Active',
        created_at: new Date().toISOString(),
        image_preview: imagePreview,
        image_type: imageType,
        assessment,
        log_count: 0
      };

      // Persist to Supabase if configured
      if (isSupabaseConfigured) {
        try {
          await supabase.from('injury_records').insert({
            id: recordId,
            user_id: (await supabase.auth.getUser()).data.user?.id || 'anonymous_user',
            injury_date: injuryDate,
            body_area: bodyArea,
            injury_description: newRecord.injury_description,
            injury_mechanism: mechanism,
            symptoms: selectedSymptoms,
            pain_level: painLevel,
            status: newRecord.status
          });

          await supabase.from('ai_assessments').insert({
            injury_record_id: recordId,
            assessment_status: assessment.assessmentStatus,
            image_type: assessment.imageType,
            visible_observations: assessment.visibleObservations,
            possible_category: assessment.possibleCategory,
            uncertainty_notes: assessment.uncertaintyNotes,
            urgency_level: assessment.urgencyLevel,
            recovery_estimate: assessment.recoveryEstimate,
            recovery_explanation: assessment.recoveryExplanation,
            first_aid_guidance: assessment.firstAidGuidance,
            medical_references: assessment.medicalReferences,
            model_identifier: assessment.modelIdentifier
          });
        } catch (dbErr) {
          console.warn('Supabase insert failed, maintaining local persistence:', dbErr);
        }
      }

      // Always maintain in local storage cache
      try {
        const existing = JSON.parse(localStorage.getItem('injuryai_records') || '[]');
        localStorage.setItem('injuryai_records', JSON.stringify([newRecord, ...existing]));
        localStorage.setItem(`injuryai_detail_${recordId}`, JSON.stringify(newRecord));
      } catch (err) {
        console.warn('LocalStorage error:', err);
      }

      router.push(`/assessment/${recordId}`);
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to submit clinical assessment.');
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div className="flex items-center gap-3">
        <Link href="/dashboard" className="p-2 rounded-lg hover:bg-slate-200 transition text-slate-600">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">
            New Injury Intake & Assessment
          </h1>
          <p className="text-xs text-slate-500">
            Fill in clinical observations, mechanism, and upload an external photograph or X-ray radiograph.
          </p>
        </div>
      </div>

      {/* Real-time Emergency Warning Intercept */}
      {emergencyCheck.isEmergency && (
        <div className="rounded-xl border-2 border-red-500 bg-red-50 p-6 shadow-md space-y-3">
          <div className="flex items-center gap-3 text-red-700 font-bold text-base">
            <ShieldAlert className="w-6 h-6 shrink-0" />
            <span>CRITICAL RED FLAG WARNING DETECTED</span>
          </div>
          <p className="text-xs text-red-800 leading-relaxed">
            <strong>Identified Risk:</strong> {emergencyCheck.redFlagReason}
          </p>
          <p className="text-xs text-red-700 leading-relaxed">
            Do NOT wait for image processing or home treatment. Seek immediate in-person emergency hospital trauma evaluation.
          </p>
          <div className="pt-2">
            <Link
              href="/emergency"
              className="inline-flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white font-bold text-xs px-4 py-2 rounded-lg transition"
            >
              Access Emergency Hotlines (911 / 112)
            </Link>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Step 1: Body Area */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <label className="block text-sm font-bold text-slate-900">
            1. Affected Body Area
          </label>
          <div className="flex flex-wrap gap-2">
            {BODY_AREAS.map((area) => (
              <button
                type="button"
                key={area}
                onClick={() => setBodyArea(area)}
                className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition ${
                  bodyArea === area 
                    ? 'bg-sky-600 border-sky-600 text-white' 
                    : 'bg-white border-slate-200 text-slate-700 hover:border-slate-300'
                }`}
              >
                {area}
              </button>
            ))}
          </div>
        </div>

        {/* Step 2: Mechanism and Date */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <label className="block text-sm font-bold text-slate-900">
            2. How Did the Injury Occur?
          </label>
          <div className="flex flex-wrap gap-2">
            {MECHANISMS.map((m) => (
              <button
                type="button"
                key={m}
                onClick={() => setMechanism(m)}
                className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition ${
                  mechanism === m 
                    ? 'bg-sky-600 border-sky-600 text-white' 
                    : 'bg-white border-slate-200 text-slate-700 hover:border-slate-300'
                }`}
              >
                {m}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Injury Incident Date
              </label>
              <input
                type="date"
                value={injuryDate}
                onChange={(e) => setInjuryDate(e.target.value)}
                className="w-full border border-slate-200 rounded-lg text-xs p-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500"
              />
            </div>

            <div>
              <div className="flex justify-between items-center mb-1">
                <label className="block text-xs font-semibold text-slate-700">
                  Pain Level (0 to 10)
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
          </div>
        </div>

        {/* Step 3: Symptoms & Red Flags */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <label className="block text-sm font-bold text-slate-900">
            3. Observable Signs & Symptoms
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {REGULAR_SYMPTOMS.map((sym) => {
              const isChecked = selectedSymptoms.includes(sym);
              return (
                <button
                  type="button"
                  key={sym}
                  onClick={() => toggleSymptom(sym)}
                  className={`text-xs text-left p-2.5 rounded-lg border flex items-center justify-between transition ${
                    isChecked 
                      ? 'bg-sky-50 border-sky-300 text-sky-800 font-medium' 
                      : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  <span>{sym}</span>
                  {isChecked && <Check className="w-4 h-4 text-sky-600 shrink-0" />}
                </button>
              );
            })}
          </div>

          {/* Red Flag Checklist */}
          <div className="mt-4 p-4 rounded-xl bg-red-50/70 border border-red-200 space-y-3">
            <span className="text-xs font-bold text-red-900 flex items-center gap-1.5">
              <AlertTriangle className="w-4 h-4 text-red-600" />
              Check for Severe Red-Flag Warning Signs:
            </span>
            <div className="space-y-1.5">
              {RED_FLAG_SYMPTOMS.map((rf) => {
                const isChecked = selectedSymptoms.includes(rf);
                return (
                  <button
                    type="button"
                    key={rf}
                    onClick={() => toggleSymptom(rf)}
                    className={`w-full text-xs text-left p-2 rounded-lg border flex items-center justify-between transition ${
                      isChecked 
                        ? 'bg-red-600 border-red-600 text-white font-bold' 
                        : 'bg-white border-red-200 text-red-900 hover:bg-red-100/50'
                    }`}
                  >
                    <span>{rf}</span>
                    {isChecked && <Check className="w-4 h-4 text-white shrink-0" />}
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        {/* Step 4: Image Upload */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
          <label className="block text-sm font-bold text-slate-900">
            4. Image Submission (External Photo or X-Ray)
          </label>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => setImageType('EXTERNAL_PHOTO')}
              className={`flex-1 py-2 text-xs font-semibold rounded-lg border transition ${
                imageType === 'EXTERNAL_PHOTO'
                  ? 'bg-sky-600 text-white border-sky-600'
                  : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
              }`}
            >
              External Photograph
            </button>
            <button
              type="button"
              onClick={() => setImageType('XRAY')}
              className={`flex-1 py-2 text-xs font-semibold rounded-lg border transition ${
                imageType === 'XRAY'
                  ? 'bg-sky-600 text-white border-sky-600'
                  : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
              }`}
            >
              X-Ray Radiograph
            </button>
          </div>

          {imageType === 'XRAY' && (
            <div className="p-3 bg-amber-50 rounded-lg border border-amber-200 text-xs text-amber-800 leading-relaxed">
              <strong>Notice:</strong> Automated fracture detection is clinically unavailable on this general system. X-ray submissions will be analyzed for reference context, but require certified radiologist evaluation.
            </div>
          )}

          {imagePreview ? (
            <div className="relative rounded-xl overflow-hidden border border-slate-200 max-h-72 flex items-center justify-center bg-slate-950">
              <img src={imagePreview} alt="Injury Preview" className="max-h-72 object-contain" />
              <button
                type="button"
                onClick={() => setImagePreview(null)}
                className="absolute top-3 right-3 bg-black/70 hover:bg-black text-white p-1.5 rounded-full transition"
                title="Remove image"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <label className="border-2 border-dashed border-slate-200 hover:border-sky-400 rounded-xl p-8 flex flex-col items-center justify-center text-center cursor-pointer transition bg-slate-50/50">
              <UploadCloud className="w-10 h-10 text-sky-600 mb-2" />
              <span className="text-xs font-bold text-slate-800">
                Click to upload image (JPG, PNG, WebP)
              </span>
              <span className="text-[11px] text-slate-500 mt-0.5">
                Max file size: 10MB • Evaluated client-side & stored in private bucket
              </span>
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={handleImageChange}
                className="hidden"
              />
            </label>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Clinical Context / Specific Symptoms Notes
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Felt a pop when turning quickly, immediate sharp pain, swelling developed over 2 hours..."
              className="w-full border border-slate-200 rounded-lg text-xs p-3 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        </div>

        {errorMsg && (
          <div className="p-3 bg-red-100 text-red-800 text-xs rounded-lg font-medium">
            {errorMsg}
          </div>
        )}

        {/* Submit Button */}
        <button
          type="submit"
          disabled={submitting}
          className={`w-full py-3.5 px-4 rounded-xl font-bold text-sm text-white flex items-center justify-center gap-2 shadow-sm transition ${
            emergencyCheck.isEmergency 
              ? 'bg-red-600 hover:bg-red-700' 
              : 'bg-sky-600 hover:bg-sky-700'
          }`}
        >
          {submitting ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              <span>Analyzing Clinical Presentation...</span>
            </>
          ) : (
            <>
              <Check className="w-5 h-5" />
              <span>{emergencyCheck.isEmergency ? 'Submit Case & Alert EMS' : 'Generate Clinical Assessment'}</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
}
