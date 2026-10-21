import type { Metadata } from 'next';
import './globals.css';
import Link from 'next/link';
import { Shield, Activity, PhoneCall, User, Home, PlusCircle, History } from 'lucide-react';

export const metadata: Metadata = {
  title: 'InjuryAI — Smart Injury Assessment & Recovery Assistant',
  description: 'AI-assisted healthcare decision-support and educational platform for preliminary injury assessment and recovery tracking.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        {/* Safety Header Banner */}
        <div className="bg-amber-500/10 border-b border-amber-200 px-4 py-2 text-xs text-amber-900 flex items-center justify-between">
          <div className="max-w-7xl mx-auto w-full flex items-center justify-between">
            <span className="flex items-center gap-1.5 font-medium">
              <Shield className="w-3.5 h-3.5 text-amber-600" />
              <strong>Decision-Support & Educational Tool Only:</strong> Not a replacement for a doctor, radiologist, or emergency services.
            </span>
            <Link
              href="/emergency"
              className="text-red-600 font-bold hover:underline flex items-center gap-1 ml-4 shrink-0"
            >
              <PhoneCall className="w-3 h-3" />
              Emergency Triage
            </Link>
          </div>
        </div>

        {/* Navigation Bar */}
        <header className="sticky top-0 z-50 bg-white/95 backdrop-blur border-b border-slate-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <Link href="/" className="flex items-center gap-2.5 group">
              <div className="w-9 h-9 rounded-lg bg-sky-600 flex items-center justify-center text-white shadow-sm group-hover:bg-sky-700 transition">
                <Activity className="w-5 h-5" />
              </div>
              <div>
                <span className="font-bold text-lg text-slate-900 tracking-tight flex items-center gap-1">
                  Injury<span className="text-sky-600">AI</span>
                </span>
                <span className="block text-[10px] text-slate-500 leading-none">Assessment & Recovery</span>
              </div>
            </Link>

            <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-slate-600">
              <Link href="/" className="hover:text-sky-600 transition flex items-center gap-1.5">
                <Home className="w-4 h-4" /> Home
              </Link>
              <Link href="/dashboard" className="hover:text-sky-600 transition flex items-center gap-1.5">
                <Activity className="w-4 h-4" /> Dashboard
              </Link>
              <Link href="/history" className="hover:text-sky-600 transition flex items-center gap-1.5">
                <History className="w-4 h-4" /> Case History
              </Link>
              <Link href="/profile" className="hover:text-sky-600 transition flex items-center gap-1.5">
                <User className="w-4 h-4" /> Profile
              </Link>
            </nav>

            <div className="flex items-center gap-3">
              <Link
                href="/assessment/new"
                className="bg-sky-600 hover:bg-sky-700 text-white text-sm font-semibold px-4 py-2 rounded-lg flex items-center gap-2 shadow-sm transition"
              >
                <PlusCircle className="w-4 h-4" />
                <span>New Assessment</span>
              </Link>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {children}
        </main>

        {/* Footer */}
        <footer className="bg-white border-t border-slate-200 mt-auto py-8 text-xs text-slate-500">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
            <p>© {new Date().getFullYear()} InjuryAI. Built for educational & clinical decision support.</p>
            <div className="flex items-center gap-6">
              <Link href="/emergency" className="text-red-600 hover:underline font-semibold">
                Emergency Hotlines (911 / 112)
              </Link>
              <Link href="/profile" className="hover:underline">
                Privacy & Data Security
              </Link>
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
