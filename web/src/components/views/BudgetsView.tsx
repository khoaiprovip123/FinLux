'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency } from '@/lib/formatters';
import { AlertTriangle, CheckCircle, Edit3, PieChart, ShieldAlert } from 'lucide-react';

export default function BudgetsView() {
  const { budgets, categories, updateBudgetLimit } = useFinance();
  const [editingCatId, setEditingCatId] = useState<string | null>(null);
  const [newLimitStr, setNewLimitStr] = useState<string>('');
  const [currentMonth, setCurrentMonth] = useState('2026-10');

  React.useEffect(() => {
    setCurrentMonth(new Date().toISOString().slice(0, 7));
  }, []);

  const expenseCategories = categories.filter((c) => c.type === 'expense');

  const handleStartEdit = (catId: string, currentLimit: number) => {
    setEditingCatId(catId);
    setNewLimitStr(currentLimit.toString());
  };

  const handleSaveEdit = (catId: string) => {
    const amount = parseInt(newLimitStr.replace(/\D/g, '') || '0', 10);
    if (amount > 0) {
      updateBudgetLimit(catId, amount);
    }
    setEditingCatId(null);
  };

  const totalBudgetLimit = budgets.reduce((s, b) => s + b.limitAmount, 0);
  const totalBudgetSpent = budgets.reduce((s, b) => s + b.spentAmount, 0);
  const totalPercent =
    totalBudgetLimit > 0 ? Math.min(100, Math.round((totalBudgetSpent / totalBudgetLimit) * 100)) : 0;

  return (
    <div className="space-y-6">
      {/* Top Overview */}
      <div className="liquid-glass rounded-2xl p-6 border border-white/10">
        <div className="flex flex-wrap items-center justify-between gap-4 mb-4">
          <div>
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <PieChart className="w-5 h-5 text-purple-400" />
              <span>Ngân sách tháng {currentMonth}</span>
            </h2>
            <p className="text-xs text-slate-400">
              Kiểm soát hạn mức theo BR-09: Cảnh báo tự động khi chạm mốc 80% và 100%
            </p>
          </div>

          <div className="flex items-center gap-6 text-right">
            <div>
              <span className="text-xs text-slate-400 block">Tổng đã chi</span>
              <span className="text-lg font-extrabold text-white">
                {formatCurrency(totalBudgetSpent)}
              </span>
            </div>
            <div>
              <span className="text-xs text-slate-400 block">Tổng hạn mức</span>
              <span className="text-lg font-extrabold text-purple-400">
                {formatCurrency(totalBudgetLimit)}
              </span>
            </div>
          </div>
        </div>

        {/* Global Progress */}
        <div className="space-y-2">
          <div className="flex justify-between text-xs text-slate-400">
            <span>Tiến độ tiêu dùng tổng thể</span>
            <span
              className={`font-bold ${
                totalPercent >= 100
                  ? 'text-rose-400'
                  : totalPercent >= 80
                  ? 'text-amber-400'
                  : 'text-emerald-400'
              }`}
            >
              {totalPercent}%
            </span>
          </div>
          <div className="w-full h-3 rounded-full bg-slate-800 overflow-hidden">
            <div
              className={`h-full rounded-full transition-all duration-500 ${
                totalPercent >= 100
                  ? 'bg-rose-500'
                  : totalPercent >= 80
                  ? 'bg-amber-400'
                  : 'bg-emerald-500'
              }`}
              style={{ width: `${totalPercent}%` }}
            />
          </div>
        </div>
      </div>

      {/* Category Budget Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {expenseCategories.map((cat) => {
          const budget = budgets.find(
            (b) => b.categoryId === cat.id && b.month === currentMonth
          );
          const limit = budget?.limitAmount || 3000000;
          const spent = budget?.spentAmount || 0;
          const percent = Math.min(100, Math.round((spent / limit) * 100));
          const isOver = spent > limit;
          const isWarning = percent >= 80 && !isOver;

          const isEditing = editingCatId === cat.id;

          return (
            <div
              key={cat.id}
              className="liquid-glass rounded-2xl p-5 border border-white/10 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2.5">
                    <div
                      className="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-xs"
                      style={{
                        backgroundColor: `${cat.color}20`,
                        color: cat.color,
                        border: `1px solid ${cat.color}40`,
                      }}
                    >
                      {cat.name.slice(0, 1)}
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-sm">{cat.name}</h3>
                      <span className="text-[11px] text-slate-400">Danh mục định kỳ</span>
                    </div>
                  </div>

                  {isOver ? (
                    <span className="flex items-center gap-1 text-[11px] font-bold text-rose-400 bg-rose-500/10 px-2.5 py-0.5 rounded-full border border-rose-500/20">
                      <ShieldAlert className="w-3 h-3" />
                      Vượt hạn mức
                    </span>
                  ) : isWarning ? (
                    <span className="flex items-center gap-1 text-[11px] font-bold text-amber-400 bg-amber-500/10 px-2.5 py-0.5 rounded-full border border-amber-500/20">
                      <AlertTriangle className="w-3 h-3" />
                      Cảnh báo 80%
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 text-[11px] font-medium text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20">
                      <CheckCircle className="w-3 h-3" />
                      An toàn
                    </span>
                  )}
                </div>

                <div className="space-y-1.5 my-3">
                  <div className="flex justify-between text-xs text-slate-300">
                    <span>Đã chi: {formatCurrency(spent)}</span>
                    <span className="font-bold">{percent}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-300 ${
                        isOver
                          ? 'bg-rose-500'
                          : isWarning
                          ? 'bg-amber-400'
                          : 'bg-emerald-500'
                      }`}
                      style={{ width: `${percent}%` }}
                    />
                  </div>
                </div>
              </div>

              {/* Edit Limit Section */}
              <div className="pt-3 border-t border-white/10 flex items-center justify-between text-xs">
                {isEditing ? (
                  <div className="flex items-center gap-2 w-full">
                    <input
                      type="text"
                      value={newLimitStr}
                      onChange={(e) => setNewLimitStr(e.target.value.replace(/\D/g, ''))}
                      className="bg-[#0d1b33] border border-white/20 rounded-lg px-2 py-1 text-xs text-white flex-1 outline-none"
                      autoFocus
                    />
                    <button
                      onClick={() => handleSaveEdit(cat.id)}
                      className="px-2.5 py-1 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white font-semibold text-[11px]"
                    >
                      Lưu
                    </button>
                    <button
                      onClick={() => setEditingCatId(null)}
                      className="px-2 py-1 rounded-lg bg-white/10 text-slate-300 text-[11px]"
                    >
                      Hủy
                    </button>
                  </div>
                ) : (
                  <>
                    <span className="text-slate-400">
                      Hạn mức: <strong className="text-white">{formatCurrency(limit)}</strong>
                    </span>
                    <button
                      onClick={() => handleStartEdit(cat.id, limit)}
                      className="text-cyan-400 hover:text-cyan-300 flex items-center gap-1 text-[11px]"
                    >
                      <Edit3 className="w-3 h-3" />
                      <span>Đổi hạn mức</span>
                    </button>
                  </>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
