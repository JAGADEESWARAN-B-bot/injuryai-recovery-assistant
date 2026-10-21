# InjuryAI — Smart Injury Assessment & Recovery Assistant

**InjuryAI** is an AI-assisted healthcare decision-support and educational platform that evaluates external injury photographs and X-ray images, provides clinical emergency triage, estimates recovery timelines based on reliable medical literature, and tracks healing progress through structured daily recovery logs.

> **CRITICAL MEDICAL DISCLAIMER:**
> InjuryAI is an educational decision-support tool, **NOT** a replacement for a doctor, radiologist, or emergency trauma service. An image alone cannot establish a definitive diagnosis or an exact healing date. In an emergency, immediately call local emergency services (911 / 112 / 999).

---

## 1. Key Capabilities & Features

### A. Pre-Screening Safety Triage Layer
- Intercepts user clinical intake **before** image analysis for immediate life-threatening warning signs:
  - Active uncontrolled or pulsating hemorrhage
  - Gross joint deformity or exposed bone (open fracture)
  - Neurovascular compromise (cold, pale, or numb limb)
  - Loss of consciousness / severe head trauma
  - Respiratory distress or acute thoracic pain
- Immediately surfaces high-priority emergency protocols and direct-dial emergency hotlines (911 / 112 / 999 / 000).

### B. AI Image Assessment & Uncertainty Modeling
- **External Injury Photos:** Evaluates observable characteristics (edema, ecchymosis, erythema, superficial laceration) without claiming a definitive clinical diagnosis.
- **X-Ray Radiographs:** Adheres strictly to medical safety standards. Explicitly communicates that automated computer vision models are **not** FDA/CE certified diagnostic radiology systems. Recommends professional radiologist review on calibrated DICOM workstations.
- Cites legitimate medical practice references (AAOS, ACEP, ACR, BJSM, CDC).

### C. Clinically Grounded Recovery-Time Estimation
- Formulates recovery ranges based on established tissue healing timelines (Grade I sprains, superficial lacerations, contusions).
- Mandates the exact safety fallback when injury presentation is severe or uncertain:
  > *"Recovery time cannot be reliably estimated from the available information. Please consult a qualified healthcare professional."*

### D. Recovery Progress Tracking
- Interactive daily recovery logs with:
  - Numeric Pain Rating Scale (0 to 10)
  - Observable swelling status (None, Mild, Moderate, Reduced, Severe)
  - Functional mobility score (1 to 5)
  - Clinical recovery state (Healing well, Plateaued, Worsening, Fully Recovered)
  - Progress notes and timestamped recovery trajectory.

---

## 2. Technology Stack & Database Architecture

- **Mobile Client:** Android Native (Kotlin, Jetpack Compose, Material Design 3, Coroutines, StateFlow).
- **Local Persistence & Offline Cache:** Android Room Database with full schema entities (`profiles`, `injury_records`, `injury_images`, `ai_assessments`, `recovery_logs`).
- **Cloud Backend & Database:** Supabase PostgreSQL with Row Level Security (RLS) on all user-owned tables.
- **Private Image Storage:** Supabase Storage private bucket `injury-images` with signed authenticated access.
- **AI Engine:** Multimodal Vision API (Gemini Vision) paired with a deterministic Clinical Triage Rules Engine.
- **Web / Serverless Deployment:** Next.js API routes deployable to Vercel.

---

## 3. Database Schema & Security Rules

The SQL migration script is located at `/supabase/migrations/20241021_initial_schema.sql` and includes:
- `profiles`: User account details, emergency contacts, allergy history.
- `injury_records`: Case date, body area, mechanism, pain score, status.
- `injury_images`: Private storage paths, image types (`EXTERNAL_PHOTO` / `XRAY`), MIME types, file sizes.
- `ai_assessments`: Visible observations, possible category, uncertainty notes, urgency levels, recovery estimates, medical references.
- `recovery_logs`: Longitudinal healing logs with pain ratings, swelling, mobility scores, and progress notes.
- **Row Level Security (RLS):** Strict user-isolation policies (`auth.uid() = user_id`) on `SELECT`, `INSERT`, `UPDATE`, and `DELETE`.

---

## 4. Production Deployment to Vercel & Supabase

Refer to `VERCEL_SUPABASE_DEPLOYMENT.md` for full step-by-step instructions:
1. Create a Supabase project and apply `/supabase/migrations/20241021_initial_schema.sql`.
2. Ensure the `injury-images` storage bucket is set to **Private**.
3. Configure environment variables in Vercel (`NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `GEMINI_API_KEY`).
4. Deploy the repository to Vercel.

---

## 5. Automated Verification & Testing

Run unit tests locally with Gradle:
```bash
gradle :app:testDebugUnitTest
```
Tests verify emergency triage detection, X-ray radiologist disclaimers, recovery estimate safety fallbacks, and database persistence.
