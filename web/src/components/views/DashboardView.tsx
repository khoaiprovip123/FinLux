'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import MetricCards from '@/components/MetricCards';
import { formatCurrency, formatShortDate } from '@/lib/formatters';
import {
  Wallet,
  ArrowUpRight,
  ArrowDownRight,
  RefreshCw,
  Trash2,
  PieChart,
  Target,
  ShieldAlert,
  Plus,
} from 'lucide-react';

interface DashboardViewProps {
  onOpenAddTx: () => void;
  onOpenWalletModal: () => void;
}

export default function DashboardView({
  onOpenAddTx,
  onOpenWalletModal,
}: DashboardViewProps) {
  const {
    wallets,
    transactions,
    budgets,
    categories,
    goals,
    debts,
    deleteTransaction,
    setActiveTab,
  } = useFinance();

  const recentTransactions = transactions.slice(0, 6);

  const getCategory = (catId?: string | null) => {
    return categories.find((c) => c.id === catId);
  };

  const getWallet = (wId: string) => {
    return wallets.find((w) => w.id === wId);
  };

  return (
    <div className="space-y-6">
      {/* 4 Metric Cards */}
      <MetricCards />

      {/* Grid: Wallets & Quick Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Wallets Card Carousel */}
        <div className="lg:col-span-2 liquid-glass rounded-2xl p-5 border border-white/10">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-bold text-white text-base flex items-center gap-2">
                <Wallet className="w-4 h-4 text-cyan-400" />
                <span>Ví & Tài khoản thanh toán</span>
              </h3>
              <p className="text-xs text-slate-400">Danh sách tài khoản trực thuộc</p>
            </div>
            <button
              onClick={onOpenWalletModal}
              className="flex items-center gap-1.5 text-xs text-cyan-400 hover:text-cyan-300 font-semibold px-3 py-1.5 rounded-xl bg-cyan-500/10 border border-cyan-500/20"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Thêm ví</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            {wallets.map((w) => (
              <div
                key={w.id}
                className="liquid-glass-interactive rounded-xl p-4 border border-white/10 relative overflow-hidden group cursor-pointer"
                onClick={() => setActiveTab('wallets')}
              >
                <div
                  className="absolute top-0 right-0 w-24 h-24 rounded-full blur-2xl opacity-20 pointer-events-none"
                  style={{ backgroundColor: w.color }}
                />
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: w.color }}
                    />
                    {w.name}
                  </span>
                  {w.isDefault && (
                    <span className="text-[10px] text-cyan-400 bg-cyan-500/10 px-2 py-0.5 rounded-full font-medium">
                      Mặc định
                    </span>
                  )}
                </div>

                <div className="text-lg font-black text-white">
                  {formatCurrency(w.balance)}
                </div>
                <div className="text-[11px] text-slate-400 mt-1 capitalize flex items-center justify-between">
                  <span>{w.bankName || w.type}</span>
                  {w.accountNumber && (
                    <span className="font-mono text-slate-500">****{w.accountNumber.slice(-4)}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Budget Progress Spotlight */}
        <div className="liquid-glass rounded-2xl p-5 border border-white/10 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-white text-base flex items-center gap-2">
                <PieChart className="w-4 h-4 text-purple-400" />
                <span>Ngân sách tháng</span>
              </h3>
              <button
                onClick={() => setActiveTab('budgets')}
                className="text-xs text-purple-400 hover:text-purple-300 font-semibold"
              >
                Chi tiết →
              </button>
            </div>

            <div className="space-y-3.5">
              {budgets.slice(0, 3).map((b) => {
                const cat = getCategory(b.categoryId);
                const percent = Math.min(100, Math.round((b.spentAmount / b.limitAmount) * 100));
                const isOver = b.spentAmount > b.limitAmount;
                const isWarning = percent >= 80;

                return (
                  <div key={b.id} className="text-xs space-y-1">
                    <div className="flex items-center justify-between text-slate-300">
                      <span className="font-medium">{cat?.name || 'Hạn mức'}</span>
                      <span
                        className={`font-semibold ${
                          isOver ? 'text-rose-400' : isWarning ? 'text-amber-400' : 'text-emerald-400'
                        }`}
                      >
                        {percent}%
                      </span>
                    </div>

                    <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${
                          isOver
                            ? 'bg-rose-500'
                            : isWarning
                            ? 'bg-amber-400'
                            : 'bg-emerald-500'
                        }`}
                        style={{ width: `${percent}%` }}
                      />
                    </div>

                    <div className="flex justify-between text-[10px] text-slate-500">
                      <span>Đã chi: {formatCurrency(b.spentAmount)}</span>
                      <span>Hạn mức: {formatCurrency(b.limitAmount)}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Quick Snapshot */}
          <div className="mt-4 pt-4 border-t border-white/10 grid grid-cols-2 gap-2 text-center text-xs">
            <div
              className="p-2 rounded-xl bg-white/5 cursor-pointer hover:bg-white/10 transition-colors"
              onClick={() => setActiveTab('goals')}
            >
              <div className="text-[11px] text-slate-400 flex items-center justify-center gap-1">
                <Target className="w-3 h-3 text-cyan-400" />
                <span>Mục tiêu ({goals.length})</span>
              </div>
              <div className="font-bold text-white mt-0.5">
                {formatCurrency(goals.reduce((s, g) => s + g.currentAmount, 0))}
              </div>
            </div>

            <div
              className="p-2 rounded-xl bg-white/5 cursor-pointer hover:bg-white/10 transition-colors"
              onClick={() => setActiveTab('debts')}
            >
              <div className="text-[11px] text-slate-400 flex items-center justify-center gap-1">
                <ShieldAlert className="w-3 h-3 text-rose-400" />
                <span>Tổng nợ ({debts.filter((d) => !d.isSettled).length})</span>
              </div>
              <div className="font-bold text-rose-400 mt-0.5">
                {formatCurrency(
                  debts.filter((d) => !d.isSettled).reduce((s, d) => s + d.remainingBalance, 0)
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Transactions List */}
      <div className="liquid-glass rounded-2xl p-5 border border-white/10">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="font-bold text-white text-base">Giao dịch gần đây</h3>
            <p className="text-xs text-slate-400">Các phát sinh thu chi mới nhất trong sổ</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={onOpenAddTx}
              className="px-3 py-1.5 rounded-xl text-xs font-semibold text-white bg-cyan-600 hover:bg-cyan-500 transition-colors"
            >
              + Thêm
            </button>
            <button
              onClick={() => setActiveTab('transactions')}
              className="text-xs text-slate-400 hover:text-white px-2 py-1"
            >
              Xem tất cả →
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="text-[11px] uppercase tracking-wider text-slate-500 border-b border-white/10">
              <tr>
                <th className="pb-3 font-semibold">Loại & Danh mục</th>
                <th className="pb-3 font-semibold">Ghi chú</th>
                <th className="pb-3 font-semibold">Ví tài khoản</th>
                <th className="pb-3 font-semibold">Thời gian</th>
                <th className="pb-3 font-semibold text-right">Số tiền</th>
                <th className="pb-3 font-semibold text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {recentTransactions.map((tx) => {
                const cat = getCategory(tx.categoryId);
                const w = getWallet(tx.walletId);
                const relW = tx.relatedWalletId ? getWallet(tx.relatedWalletId) : null;

                return (
                  <tr key={tx.id} className="hover:bg-white/5 transition-colors">
                    <td className="py-3">
                      <div className="flex items-center gap-2.5">
                        <div
                          className={`w-7 h-7 rounded-lg flex items-center justify-center ${
                            tx.type === 'income'
                              ? 'bg-emerald-500/15 text-emerald-400'
                              : tx.type === 'expense'
                              ? 'bg-rose-500/15 text-rose-400'
                              : 'bg-cyan-500/15 text-cyan-400'
                          }`}
                        >
                          {tx.type === 'income' ? (
                            <ArrowUpRight className="w-4 h-4" />
                          ) : tx.type === 'expense' ? (
                            <ArrowDownRight className="w-4 h-4" />
                          ) : (
                            <RefreshCw className="w-4 h-4" />
                          )}
                        </div>
                        <div>
                          <span className="font-semibold text-white block">
                            {tx.type === 'transfer_out' ? 'Chuyển tiền' : cat?.name || 'Khác'}
                          </span>
                        </div>
                      </div>
                    </td>

                    <td className="py-3 text-slate-300 max-w-[200px] truncate">{tx.note}</td>

                    <td className="py-3 text-slate-400">
                      {tx.type === 'transfer_out' ? (
                        <span>
                          {w?.name} → {relW?.name}
                        </span>
                      ) : (
                        <span>{w?.name}</span>
                      )}
                    </td>

                    <td className="py-3 text-slate-400">{formatShortDate(tx.date)}</td>

                    <td className="py-3 text-right font-bold text-sm">
                      <span
                        className={
                          tx.type === 'income'
                            ? 'text-emerald-400'
                            : tx.type === 'expense'
                            ? 'text-rose-400'
                            : 'text-cyan-400'
                        }
                      >
                        {tx.type === 'income' ? '+' : tx.type === 'expense' ? '-' : ''}
                        {formatCurrency(tx.amount)}
                      </span>
                    </td>

                    <td className="py-3 text-right">
                      <button
                        onClick={() => deleteTransaction(tx.id)}
                        title="Xóa giao dịch và hoàn trả số dư"
                        className="p-1.5 rounded-lg text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
