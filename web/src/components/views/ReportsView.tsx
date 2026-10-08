'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency } from '@/lib/formatters';
import { BarChart3, TrendingUp, TrendingDown, PieChart, Sparkles } from 'lucide-react';

export default function ReportsView() {
  const { transactions, categories, summary } = useFinance();

  const expenseTxs = transactions.filter((t) => t.type === 'expense');

  // Breakdown by category
  const categoryBreakdown = categories
    .filter((c) => c.type === 'expense')
    .map((c) => {
      const spent = expenseTxs
        .filter((t) => t.categoryId === c.id)
        .reduce((sum, t) => sum + t.amount, 0);
      return {
        ...c,
        spent,
        percent: summary.monthlyExpense > 0 ? Math.round((spent / summary.monthlyExpense) * 100) : 0,
      };
    })
    .filter((c) => c.spent > 0)
    .sort((a, b) => b.spent - a.spent);

  const savingsRate =
    summary.monthlyIncome > 0
      ? Math.max(0, Math.round(((summary.monthlyIncome - summary.monthlyExpense) / summary.monthlyIncome) * 100))
      : 0;

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="liquid-glass rounded-2xl p-6 border border-white/10">
        <h2 className="text-lg font-bold text-white flex items-center gap-2 mb-1">
          <BarChart3 className="w-5 h-5 text-cyan-400" />
          <span>Báo cáo & Phân tích Dòng tiền</span>
        </h2>
        <p className="text-xs text-slate-400">
          Tổng quan cơ cấu thu chi, tỷ lệ tiết kiệm và các khoản chi tiêu chiếm tỷ trọng lớn
        </p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-5">
          <div className="p-4 rounded-xl bg-black/30 border border-white/5">
            <span className="text-xs text-slate-400 block mb-1">Tổng thu nhập</span>
            <div className="text-xl font-black text-emerald-400">
              {formatCurrency(summary.monthlyIncome)}
            </div>
            <span className="text-[11px] text-slate-500 mt-1 block">Tất cả nguồn thu tháng này</span>
          </div>

          <div className="p-4 rounded-xl bg-black/30 border border-white/5">
            <span className="text-xs text-slate-400 block mb-1">Tổng chi tiêu</span>
            <div className="text-xl font-black text-rose-400">
              {formatCurrency(summary.monthlyExpense)}
            </div>
            <span className="text-[11px] text-slate-500 mt-1 block">Sinh hoạt và nghĩa vụ định kỳ</span>
          </div>

          <div className="p-4 rounded-xl bg-black/30 border border-white/5">
            <span className="text-xs text-slate-400 block mb-1">Tỷ lệ tiết kiệm (Savings Rate)</span>
            <div className="text-xl font-black text-cyan-400 flex items-center gap-2">
              <span>{savingsRate}%</span>
              <Sparkles className="w-4 h-4 text-cyan-400" />
            </div>
            <span className="text-[11px] text-slate-500 mt-1 block">
              {savingsRate >= 20 ? 'Khỏe mạnh (chuẩn tài chính > 20%)' : 'Cần tối ưu thêm chi phí'}
            </span>
          </div>
        </div>
      </div>

      {/* Category Breakdown Breakdown List */}
      <div className="liquid-glass rounded-2xl p-6 border border-white/10">
        <h3 className="font-bold text-white text-base mb-4 flex items-center gap-2">
          <PieChart className="w-4 h-4 text-purple-400" />
          <span>Phân bổ chi tiêu theo danh mục</span>
        </h3>

        {categoryBreakdown.length === 0 ? (
          <div className="text-center py-8 text-xs text-slate-400">Chưa phát sinh chi tiêu</div>
        ) : (
          <div className="space-y-4">
            {categoryBreakdown.map((item) => (
              <div key={item.id} className="space-y-1.5 text-xs">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-white flex items-center gap-2">
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: item.color }}
                    />
                    {item.name}
                  </span>
                  <div className="text-right">
                    <span className="font-bold text-slate-200">{formatCurrency(item.spent)}</span>
                    <span className="text-slate-400 text-[11px] ml-2">({item.percent}%)</span>
                  </div>
                </div>

                <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${item.percent}%`,
                      backgroundColor: item.color || '#F43F5E',
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
