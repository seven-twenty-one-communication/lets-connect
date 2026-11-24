import React from 'react';

export default function AdminHomePage() {
  return (
    <main className="p-8 max-w-6xl mx-auto">
      <header className="mb-8 border-b border-slate-800 pb-4 flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">LetsConnect Admin</h1>
          <p className="text-sm text-slate-400 mt-1">Management, moderation & financial ledger monitoring</p>
        </div>
        <div className="px-3 py-1 bg-violet-950/60 border border-violet-700/60 text-violet-300 rounded-full text-xs font-semibold">
          Phase 1 Foundation
        </div>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60">
          <h2 className="text-lg font-semibold text-slate-200">Users & Presence</h2>
          <p className="text-xs text-slate-400 mt-1">Callers, active listeners, availability and reports.</p>
        </div>
        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60">
          <h2 className="text-lg font-semibold text-slate-200">Financial Ledger</h2>
          <p className="text-xs text-slate-400 mt-1">Double-entry ledger transactions, balances, and payouts.</p>
        </div>
        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60">
          <h2 className="text-lg font-semibold text-slate-200">System Configuration</h2>
          <p className="text-xs text-slate-400 mt-1">Platform commission rules, categories and rate bounds.</p>
        </div>
      </div>
    </main>
  );
}
