'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import {
  LayoutDashboard,
  Receipt,
  WalletCards,
  PieChart,
  ShieldAlert,
  Target,
  BarChart3,
  Sparkles,
  PlusCircle,
  CloudCheck,
} from 'lucide-react';

interface SidebarProps {
  onOpenAddTx: () => void;
  onOpenSpin: () => void;
}

export default function Sidebar({ onOpenAddTx, onOpenSpin }: SidebarProps) {
  const { activeTab, setActiveTab } = useFinance();

  const navItems = [
    { id: 'dashboard', label: 'Tổng quan', icon: LayoutDashboard },
    { id: 'transactions', label: 'Sổ giao dịch', icon: Receipt },
    { id: 'wallets', label: 'Ví & Tài khoản', icon: WalletCards },
    { id: 'budgets', label: 'Ngân sách', icon: PieChart },
    { id: 'debts', label: 'Sổ nợ & Trả nợ', icon: ShieldAlert },
    { id: 'goals', label: 'Mục tiêu tài chính', icon: Target },
    { id: 'reports', label: 'Báo cáo & Phân tích', icon: BarChart3 },
  ];

  return (
    <aside className="w-64 min-h-screen bg-[#070e1c]/80 backdrop-blur-2xl border-r border-white/10 flex flex-col justify-between p-4 sticky top-0 z-30">
      <div>
        {/* Brand */}
        <div className="flex items-center gap-3 px-2 py-3 mb-6">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 via-blue-600 to-indigo-600 flex items-center justify-center font-black text-xl text-white shadow-lg shadow-cyan-500/20 border border-white/20">
            F
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-extrabold text-lg tracking-tight text-white">FinLux</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-cyan-500/10 text-cyan-400 font-semibold border border-cyan-500/20">
                WEB
              </span>
            </div>
            <p className="text-xs text-slate-400">Personal Finance Pro</p>
          </div>
        </div>

        {/* Quick Add Button */}
        <div className="px-1 mb-5">
          <button
            onClick={onOpenAddTx}
            className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl font-semibold text-sm text-white bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 shadow-lg shadow-cyan-500/25 transition-all duration-200 active:scale-[0.98]"
          >
            <PlusCircle className="w-4 h-4" />
            <span>+ Ghi chép mới</span>
          </button>
        </div>

        {/* Nav Items */}
        <nav className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 text-left ${
                  isActive
                    ? 'bg-gradient-to-r from-cyan-500/15 to-blue-500/10 text-cyan-300 border border-cyan-500/30 shadow-inner'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-cyan-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}

          {/* Saving Spin Action Button */}
          <button
            onClick={onOpenSpin}
            className="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm text-amber-300 hover:text-amber-200 hover:bg-amber-500/10 border border-amber-500/20 mt-2 transition-all duration-200"
          >
            <Sparkles className="w-4 h-4 text-amber-400 animate-spin" style={{ animationDuration: '6s' }} />
            <span>Saving Spin 🎰</span>
          </button>
        </nav>
      </div>

      {/* Footer Info / Render Badge */}
      <div className="pt-4 border-t border-white/5 text-xs text-slate-400 space-y-2">
        <div className="flex items-center justify-between px-2">
          <span className="flex items-center gap-1.5 text-emerald-400">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            Render Ready
          </span>
          <span className="text-[11px] text-slate-500 font-mono">v1.0.0</span>
        </div>
        <div className="px-2 py-1.5 rounded-lg bg-white/5 text-[11px] text-slate-400 flex items-center gap-2">
          <CloudCheck className="w-3.5 h-3.5 text-cyan-400" />
          <span>Synced with FinLux Engine</span>
        </div>
      </div>
    </aside>
  );
}
