-- ==============================================================================
-- InjuryAI: Smart Injury Assessment & Recovery Assistant
-- Supabase PostgreSQL Schema & Security Rules Migration
-- ==============================================================================

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Profiles Table
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    emergency_contact_name TEXT,
    emergency_contact_phone TEXT,
    allergies TEXT,
    underlying_conditions TEXT,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL
);

-- 3. Injury Records Table
CREATE TABLE IF NOT EXISTS public.injury_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    injury_date DATE NOT NULL,
    body_area TEXT NOT NULL,
    injury_description TEXT NOT NULL,
    injury_mechanism TEXT NOT NULL,
    symptoms TEXT[] DEFAULT '{}' NOT NULL,
    pain_level INT CHECK (pain_level >= 0 AND pain_level <= 10) DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Healing', 'Plateaued', 'Worsening', 'Resolved', 'Medical Review Pending')),
    emergency_flag BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL
);

-- 4. Injury Images Table (Linked to Private Supabase Storage Bucket 'injury-images')
CREATE TABLE IF NOT EXISTS public.injury_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    injury_record_id UUID NOT NULL REFERENCES public.injury_records(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    storage_path TEXT NOT NULL,
    image_type TEXT NOT NULL CHECK (image_type IN ('EXTERNAL_PHOTO', 'XRAY')),
    original_filename TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL
);

-- 5. AI Assessments Table
CREATE TABLE IF NOT EXISTS public.ai_assessments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    injury_record_id UUID NOT NULL REFERENCES public.injury_records(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    assessment_status TEXT NOT NULL DEFAULT 'COMPLETED' CHECK (assessment_status IN ('PENDING', 'COMPLETED', 'FAILED', 'EMERGENCY_TRIAGED')),
    image_type TEXT NOT NULL,
    visible_observations TEXT NOT NULL,
    possible_category TEXT NOT NULL,
    uncertainty_notes TEXT NOT NULL,
    urgency_level TEXT NOT NULL CHECK (urgency_level IN ('EMERGENCY', 'URGENT_CARE', 'ROUTINE_EVALUATION', 'HOME_CARE_OBSERVATION')),
    recovery_estimate TEXT NOT NULL,
    recovery_explanation TEXT NOT NULL,
    first_aid_guidance TEXT NOT NULL,
    medical_references TEXT[] DEFAULT '{}' NOT NULL,
    model_identifier TEXT NOT NULL,
    disclaimer_notice TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL
);

-- 6. Recovery Logs Table
CREATE TABLE IF NOT EXISTS public.recovery_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    injury_record_id UUID NOT NULL REFERENCES public.injury_records(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    pain_level INT CHECK (pain_level >= 0 AND pain_level <= 10) NOT NULL,
    swelling_status TEXT NOT NULL,
    mobility_score INT CHECK (mobility_score >= 1 AND mobility_score <= 5) DEFAULT 3,
    progress_notes TEXT NOT NULL,
    symptoms TEXT[] DEFAULT '{}' NOT NULL,
    reported_recovery_status TEXT NOT NULL CHECK (reported_recovery_status IN ('Healing well', 'Plateaued', 'Worsening', 'Fully Recovered')),
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL
);

-- Indexes for high-performance querying and user data isolation
CREATE INDEX IF NOT EXISTS idx_injury_records_user_id ON public.injury_records(user_id);
CREATE INDEX IF NOT EXISTS idx_injury_records_status ON public.injury_records(status);
CREATE INDEX IF NOT EXISTS idx_injury_images_record_id ON public.injury_images(injury_record_id);
CREATE INDEX IF NOT EXISTS idx_ai_assessments_record_id ON public.ai_assessments(injury_record_id);
CREATE INDEX IF NOT EXISTS idx_recovery_logs_record_id ON public.recovery_logs(injury_record_id);

-- ------------------------------------------------------------------------------
-- Row Level Security (RLS) Policies
-- Enforce zero-trust user isolation so users can ONLY access their own records
-- ------------------------------------------------------------------------------

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.injury_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.injury_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recovery_logs ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Users can view own profile" ON public.profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

-- Injury Records Policies
CREATE POLICY "Users can view own injury records" ON public.injury_records
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own injury records" ON public.injury_records
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own injury records" ON public.injury_records
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own injury records" ON public.injury_records
    FOR DELETE USING (auth.uid() = user_id);

-- Injury Images Policies
CREATE POLICY "Users can view own injury images" ON public.injury_images
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own injury images" ON public.injury_images
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own injury images" ON public.injury_images
    FOR DELETE USING (auth.uid() = user_id);

-- AI Assessments Policies
CREATE POLICY "Users can view own assessments" ON public.ai_assessments
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own assessments" ON public.ai_assessments
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own assessments" ON public.ai_assessments
    FOR DELETE USING (auth.uid() = user_id);

-- Recovery Logs Policies
CREATE POLICY "Users can view own recovery logs" ON public.recovery_logs
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own recovery logs" ON public.recovery_logs
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own recovery logs" ON public.recovery_logs
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own recovery logs" ON public.recovery_logs
    FOR DELETE USING (auth.uid() = user_id);

-- ------------------------------------------------------------------------------
-- Private Supabase Storage Bucket Configuration
-- Bucket: 'injury-images' (Private, authenticated access only)
-- ------------------------------------------------------------------------------
INSERT INTO storage.buckets (id, name, public)
VALUES ('injury-images', 'injury-images', false)
ON CONFLICT (id) DO NOTHING;

-- Storage RLS: Restrict object storage access to owner
CREATE POLICY "Private Medical Images Read" ON storage.objects
    FOR SELECT USING (bucket_id = 'injury-images' AND auth.uid()::text = (storage.foldername(name))[1]);

CREATE POLICY "Private Medical Images Insert" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'injury-images' AND auth.uid()::text = (storage.foldername(name))[1]);

CREATE POLICY "Private Medical Images Delete" ON storage.objects
    FOR DELETE USING (bucket_id = 'injury-images' AND auth.uid()::text = (storage.foldername(name))[1]);
