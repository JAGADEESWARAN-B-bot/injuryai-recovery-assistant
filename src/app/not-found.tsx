import Link from 'next/link';
import { FileQuestion, Home, ArrowLeft } from 'lucide-react';

export default function NotFound() {
  return (
    <div className="max-w-md mx-auto py-16 text-center space-y-4">
      <div className="w-16 h-16 rounded-full bg-slate-100 text-slate-400 mx-auto flex items-center justify-center">
        <FileQuestion className="w-8 h-8 text-sky-600" />
      </div>
      <h1 className="text-3xl font-extrabold text-slate-900">404 — Page Not Found</h1>
      <p className="text-xs text-slate-500 leading-relaxed max-w-xs mx-auto">
        The clinical assessment or page you requested does not exist or has been moved.
      </p>

      <div className="pt-4 flex justify-center gap-3">
        <Link
          href="/"
          className="inline-flex items-center gap-2 bg-sky-600 text-white font-bold text-xs px-4 py-2.5 rounded-lg hover:bg-sky-700 transition"
        >
          <Home className="w-4 h-4" />
          <span>Home</span>
        </Link>
        <Link
          href="/dashboard"
          className="inline-flex items-center gap-2 bg-white border border-slate-200 text-slate-700 font-semibold text-xs px-4 py-2.5 rounded-lg hover:bg-slate-50 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Dashboard</span>
        </Link>
      </div>
    </div>
  );
}
