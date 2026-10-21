# InjuryAI — Supabase & Vercel Production Deployment Guide

This guide documents the end-to-end setup and production deployment of **InjuryAI — Smart Injury Assessment & Recovery Assistant**.

---

## 1. Supabase Project Setup

1. **Create Supabase Project**:
   - Go to [Supabase Dashboard](https://supabase.com/dashboard) and click **New Project**.
   - Set project name to `injuryai-production` and configure a strong database password.
   - Choose your preferred cloud region closest to your users.

2. **Apply Database Migrations**:
   - Navigate to **SQL Editor** in the Supabase Dashboard.
   - Paste and run the full contents of `/supabase/migrations/20241021_initial_schema.sql`.
   - This creates all 5 tables (`profiles`, `injury_records`, `injury_images`, `ai_assessments`, `recovery_logs`), foreign keys, cascade delete rules, performance indexes, and strict **Row Level Security (RLS)** policies.

3. **Configure Storage Bucket**:
   - Navigate to **Storage** -> **Buckets**.
   - Verify the `injury-images` bucket is set to **Private** (public access disabled).
   - The migration automatically sets RLS policies restricting file read/write to the authenticated user's folder (`/injury-images/<user_id>/...`).

4. **Authentication Settings**:
   - In **Authentication** -> **URL Configuration**, set:
     - **Site URL**: `https://<your-vercel-domain>.vercel.app`
     - **Redirect URLs**:
       - `https://<your-vercel-domain>.vercel.app/**`
       - `http://localhost:3000/**` (for local development)

---

## 2. Environment Variables Configuration

Create `.env.local` for local development and add the following keys to your **Vercel Project Settings -> Environment Variables**:

```env
# Supabase Configuration
NEXT_PUBLIC_SUPABASE_URL=https://<your-project-ref>.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOi...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOi...

# AI Provider Configuration (Server-Side Only)
AI_PROVIDER_API_KEY=AIzaSy...
GEMINI_API_KEY=AIzaSy...
```

*Note: Never prefix sensitive keys (`SUPABASE_SERVICE_ROLE_KEY`, `AI_PROVIDER_API_KEY`) with `NEXT_PUBLIC_`.*

---

## 3. Vercel Deployment

1. Push your repository to GitHub:
   ```bash
   git add .
   git commit -m "feat: InjuryAI complete implementation with Supabase & Android client"
   git push origin main
   ```
2. In [Vercel Dashboard](https://vercel.com):
   - Click **Add New Project** -> **Import Git Repository**.
   - Framework Preset: **Next.js**.
   - Root Directory: `./`.
   - Add the Environment Variables configured in Step 2.
   - Click **Deploy**.

---

## 4. Android Client Configuration

The Android application included in this repository connects directly to InjuryAI's clinical decision support and AI vision engine:
- Room Database on-device provides resilient offline caching and fast querying.
- `GEMINI_API_KEY` can be entered via AI Studio Secrets Panel or `.env`.
- Full emergency red-flag triage intercept operates instantly without network latency.
