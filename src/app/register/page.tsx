'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Activity, Lock, Mail, ArrowRight, User } from 'lucide-react';
import { supabase, isSupabaseConfigured } from '@/lib/supabase';

export default function RegisterPage() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg(null);

    if (isSupabaseConfigured) {
      const { data, error } = await supabase.auth.signUp({
        email,
        password,
        options: {
          data: { display_name: name }
        }
      });
      if (error) {
        setErrorMsg(error.message);
        setLoading(false);
        return;
      }
    }
    router.push('/dashboard');
  };

  return (
    <div className="max-w-md mx-auto py-12 space-y-6">
      <div className="text-center space-y-2">
        <div className="w-12 h-12 rounded-xl bg-sky-600 text-white mx-auto flex items-center justify-center shadow-md">
          <Activity className="w-6 h-6" />
        </div>
        <h1 className="text-2xl font-extrabold text-slate-900">Create an Account</h1>
        <p className="text-xs text-slate-500">Sign up for InjuryAI to track your recovery history</p>
      </div>

      <form onSubmit={handleRegister} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
        {errorMsg && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700">
            {errorMsg}
          </div>
        )}

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name</label>
          <div className="relative">
            <User className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Alex Doe"
              className="w-full pl-9 pr-3 py-2 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
          <div className="relative">
            <Mail className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              className="w-full pl-9 pr-3 py-2 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Password</label>
          <div className="relative">
            <Lock className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
            <input
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full pl-9 pr-3 py-2 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-lg flex items-center justify-center gap-2 transition"
        >
          <span>{loading ? 'Creating account...' : 'Create Account'}</span>
          <ArrowRight className="w-4 h-4" />
        </button>

        <p className="text-center text-xs text-slate-500 pt-2">
          Already have an account?{' '}
          <Link href="/login" className="text-sky-600 font-bold hover:underline">
            Sign In
          </Link>
        </p>
      </form>
    </div>
  );
}
