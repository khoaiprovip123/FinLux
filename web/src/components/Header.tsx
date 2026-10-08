'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency } from '@/lib/formatters';
import { Sparkles, Plus, Wallet, ShieldCheck } from 'lucide-react';

interface HeaderProps {
  onOpenAddTx: () => void;
  onOpenSpin: () => void;
}

export default function Header({ onOpenAddTx, onOpenSpin }: HeaderProps) {
  const { activeTab, summary } = useFinance();

  const titleMap: Record<string, { title: string; subtitle: string }> = {
    dashboard: { title: 'Tổng quan tài chính', subtitle: 'Bức tranh toàn cảnh dòng tiền và tài sản' },
    transactions: { title: 'Sổ ghi chép giao dịch', subtitle: 'Lịch sử thu chi, chuyển ví và hoàn trả nguyên tử' },
    wallets: { title: 'Quản lý ví & Tài khoản', subtitle: 'Kiểm soát dòng tiền theo từng tài khoản ngân hàng, ví điện tử' },
    budgets: { title: 'Hạn mức ngân sách', subtitle: 'Kiểm soát chi tiêu theo danh mục với cảnh báo 80% - 100%' },
    debts: { title: 'Sổ nợ & Kế hoạch tất toán', subtitle: 'Chiến lược Snowball / Avalanche xóa nợ thông minh' },
    goals: { title: 'Mục tiêu tài chính', subtitle: 'Tích lũy định kỳ cho các ước mơ và kế hoạch tương lai' },
    reports: { title: 'Báo cáo & Phân tích', subtitle: 'Trực quan hóa thu nhập, chi phí và tỷ lệ tiết kiệm' },
  };

  const current = titleMap[activeTab] || titleMap.dashboard;

  return (
    <header className="px-8 py-5 border-b border-white/10 bg-[#070e1c]/60 backdrop-blur-xl flex flex-wrap items-center justify-between gap-4 sticky top-0 z-20">
      <div>
        <h1 className="text-xl md:text-2xl font-bold text-white tracking-tight flex items-center gap-2">
          {current.title}
        </h1>
        <p className="text-xs md:text-sm text-slate-400 mt-0.5">{current.subtitle}</p>
      </div>

      <div className="flex items-center gap-3">
        {/* Quick summary badge */}
        <div className="hidden lg:flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-cyan-950/40 border border-cyan-500/20 text-xs">
          <Wallet className="w-3.5 h-3.5 text-cyan-400" />
          <span className="text-slate-300">Tổng tài sản:</span>
          <span className="font-semibold text-cyan-300">{formatCurrency(summary.totalBalance)}</span>
        </div>

        {/* Spin action */}
        <button
          onClick={onOpenSpin}
          className="flex items-center gap-1.5 py-2 px-3.5 rounded-xl text-xs font-semibold text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 transition-all duration-150"
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>Vòng quay</span>
        </button>

        {/* Add Transaction Button */}
        <button
          onClick={onOpenAddTx}
          className="flex items-center gap-1.5 py-2 px-4 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 shadow-lg shadow-cyan-500/20 transition-all duration-150"
        >
          <Plus className="w-4 h-4" />
          <span>Giao dịch</span>
        </button>

        {/* User Avatar */}
        <div className="flex items-center gap-2 pl-2 border-l border-white/10">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-600 to-blue-700 flex items-center justify-center text-xs font-bold text-white border border-white/20">
            UX
          </div>
        </div>
      </div>
    </header>
  );
}
