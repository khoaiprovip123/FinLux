'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency, formatShortDate } from '@/lib/formatters';
import { Target, Plus, CheckCircle, Calendar, Coins } from 'lucide-react';

export default function GoalsView() {
  const { goals, wallets, addGoal, depositToGoal } = useFinance();

  const [isAddOpen, setIsAddOpen] = useState(false);
  const [name, setName] = useState('');
  const [targetStr, setTargetStr] = useState('20000000');
  const [deadline, setDeadline] = useState('2026-12-31');
  const [color, setColor] = useState('#10B981');

  // Deposit state
  const [depositingGoalId, setDepositingGoalId] = useState<string | null>(null);
  const [depositAmountStr, setDepositAmountStr] = useState('1000000');
  const [depositWalletId, setDepositWalletId] = useState(wallets[0]?.id || '');

  const handleDeposit = (goalId: string) => {
    const amount = parseInt(depositAmountStr.replace(/\D/g, '') || '0', 10);
    if (amount > 0 && depositWalletId) {
      depositToGoal(goalId, depositWalletId, amount);
      setDepositingGoalId(null);
    }
  };

  const handleCreateGoal = (e: React.FormEvent) => {
    e.preventDefault();
    const target = parseInt(targetStr.replace(/\D/g, '') || '0', 10);
    if (!name.trim() || target <= 0) return;

    addGoal({
      name: name.trim(),
      targetAmount: target,
      currentAmount: 0,
      deadline,
      color,
    });

    setIsAddOpen(false);
    setName('');
  };

  const totalTarget = goals.reduce((s, g) => s + g.targetAmount, 0);
  const totalSaved = goals.reduce((s, g) => s + g.currentAmount, 0);

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="liquid-glass rounded-2xl p-6 border border-white/10 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Target className="w-5 h-5 text-cyan-400" />
            <span>Mục tiêu tài chính (Financial Goals)</span>
          </h2>
          <p className="text-xs text-slate-400">
            Kế hoạch tiết kiệm cho các mục đích lớn: Mua nhà, xe, quỹ khẩn cấp, du lịch
          </p>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <span className="text-xs text-slate-400 block">Đã tích lũy</span>
            <span className="text-xl font-black text-cyan-400">{formatCurrency(totalSaved)}</span>
            <span className="text-[11px] text-slate-500 block">
              Mục tiêu: {formatCurrency(totalTarget)}
            </span>
          </div>
          <button
            onClick={() => setIsAddOpen(true)}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 shadow-lg shadow-cyan-500/20"
          >
            <Plus className="w-4 h-4" />
            <span>Thêm mục tiêu</span>
          </button>
        </div>
      </div>

      {/* Goal Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {goals.map((g) => {
          const percent = Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100));
          const isCompleted = g.currentAmount >= g.targetAmount;
          const isDepositing = depositingGoalId === g.id;

          return (
            <div
              key={g.id}
              className="liquid-glass rounded-2xl p-5 border border-white/10 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2.5">
                    <span
                      className="w-3.5 h-3.5 rounded-full"
                      style={{ backgroundColor: g.color }}
                    />
                    <h3 className="font-bold text-white text-base">{g.name}</h3>
                  </div>

                  {isCompleted ? (
                    <span className="flex items-center gap-1 text-xs font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20">
                      <CheckCircle className="w-3.5 h-3.5" />
                      Đạt mục tiêu 🎉
                    </span>
                  ) : (
                    <span className="text-xs font-bold text-cyan-400 bg-cyan-500/10 px-2.5 py-0.5 rounded-full border border-cyan-500/20">
                      {percent}%
                    </span>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-3 my-4 p-3 rounded-xl bg-black/30 border border-white/5 text-xs">
                  <div>
                    <span className="text-[11px] text-slate-400 block">Đã tích lũy</span>
                    <span className="text-base font-extrabold text-white">
                      {formatCurrency(g.currentAmount)}
                    </span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400 block">Cần đạt</span>
                    <span className="text-base font-medium text-slate-300">
                      {formatCurrency(g.targetAmount)}
                    </span>
                  </div>
                  {g.deadline && (
                    <div className="col-span-2 flex items-center gap-1 text-[11px] text-slate-400 pt-1 border-t border-white/5">
                      <Calendar className="w-3.5 h-3.5 text-cyan-400" />
                      <span>Hạn chót: {g.deadline}</span>
                    </div>
                  )}
                </div>

                <div className="space-y-1 my-3">
                  <div className="w-full h-2.5 rounded-full bg-slate-800 overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-300"
                      style={{
                        width: `${percent}%`,
                        backgroundColor: g.color || '#10B981',
                      }}
                    />
                  </div>
                </div>
              </div>

              {/* Deposit Form */}
              <div className="pt-3 border-t border-white/10">
                {isDepositing ? (
                  <div className="space-y-2">
                    <div className="grid grid-cols-2 gap-2">
                      <input
                        type="text"
                        value={new Intl.NumberFormat('vi-VN').format(
                          parseInt(depositAmountStr.replace(/\D/g, '') || '0', 10)
                        )}
                        onChange={(e) => setDepositAmountStr(e.target.value.replace(/\D/g, ''))}
                        className="bg-[#0d1b33] border border-white/20 rounded-lg px-2.5 py-1.5 text-xs text-white outline-none"
                      />
                      <select
                        value={depositWalletId}
                        onChange={(e) => setDepositWalletId(e.target.value)}
                        className="bg-[#0d1b33] border border-white/20 rounded-lg px-2 py-1.5 text-xs text-slate-200 outline-none"
                      >
                        {wallets.map((w) => (
                          <option key={w.id} value={w.id}>
                            {w.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="flex justify-end gap-2">
                      <button
                        onClick={() => setDepositingGoalId(null)}
                        className="px-3 py-1 rounded-lg text-xs bg-white/10 text-slate-300"
                      >
                        Hủy
                      </button>
                      <button
                        onClick={() => handleDeposit(g.id)}
                        className="px-3 py-1 rounded-lg text-xs font-semibold bg-cyan-600 hover:bg-cyan-500 text-white"
                      >
                        Xác nhận gửi tiền
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between">
                    <span className="text-[11px] text-slate-400">
                      Còn thiếu: {formatCurrency(Math.max(0, g.targetAmount - g.currentAmount))}
                    </span>
                    <button
                      onClick={() => setDepositingGoalId(g.id)}
                      className="px-3 py-1.5 rounded-xl text-xs font-semibold text-white bg-cyan-600 hover:bg-cyan-500 transition-colors flex items-center gap-1"
                    >
                      <Coins className="w-3.5 h-3.5" />
                      <span>+ Nạp tiền tiết kiệm</span>
                    </button>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Add Goal Modal */}
      {isAddOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md">
          <div className="w-full max-w-md liquid-glass rounded-2xl border border-white/15 p-6 shadow-2xl">
            <h3 className="font-bold text-white text-base mb-4">Tạo mục tiêu tài chính mới</h3>
            <form onSubmit={handleCreateGoal} className="space-y-3 text-xs">
              <div>
                <label className="text-slate-400 block mb-1">Tên mục tiêu</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Quỹ mua nhà, Quỹ du lịch Châu Âu..."
                  required
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                />
              </div>

              <div>
                <label className="text-slate-400 block mb-1">Số tiền mục tiêu (VNĐ)</label>
                <input
                  type="text"
                  value={new Intl.NumberFormat('vi-VN').format(
                    parseInt(targetStr.replace(/\D/g, '') || '0', 10)
                  )}
                  onChange={(e) => setTargetStr(e.target.value.replace(/\D/g, ''))}
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                />
              </div>

              <div>
                <label className="text-slate-400 block mb-1">Hạn chót dự kiến</label>
                <input
                  type="date"
                  value={deadline}
                  onChange={(e) => setDeadline(e.target.value)}
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setIsAddOpen(false)}
                  className="px-4 py-2 rounded-xl bg-white/10 text-slate-300"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl font-semibold bg-cyan-600 hover:bg-cyan-500 text-white"
                >
                  Tạo mục tiêu
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
