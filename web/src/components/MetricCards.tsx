'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency } from '@/lib/formatters';
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  Sparkles,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-react';

export default function MetricCards() {
  const { summary } = useFinance();

  const cards = [
    {
      title: 'Tổng số dư ví',
      amount: summary.totalBalance,
      icon: Wallet,
      gradient: 'from-cyan-500/10 via-cyan-500/5 to-transparent',
      borderColor: 'border-cyan-500/30',
      iconColor: 'text-cyan-400',
      iconBg: 'bg-cyan-500/15',
      badge: 'Thực tế',
      badgeColor: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20',
    },
    {
      title: 'Thu nhập tháng này',
      amount: summary.monthlyIncome,
      icon: TrendingUp,
      gradient: 'from-emerald-500/10 via-emerald-500/5 to-transparent',
      borderColor: 'border-emerald-500/30',
      iconColor: 'text-emerald-400',
      iconBg: 'bg-emerald-500/15',
      badge: '+ Tích cực',
      badgeColor: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
      trendIcon: ArrowUpRight,
    },
    {
      title: 'Chi tiêu tháng này',
      amount: summary.monthlyExpense,
      icon: TrendingDown,
      gradient: 'from-rose-500/10 via-rose-500/5 to-transparent',
      borderColor: 'border-rose-500/30',
      iconColor: 'text-rose-400',
      iconBg: 'bg-rose-500/15',
      badge: 'Kiểm soát',
      badgeColor: 'text-rose-400 bg-rose-500/10 border-rose-500/20',
      trendIcon: ArrowDownRight,
    },
    {
      title: 'Dòng tiền tự do (FCF)',
      amount: summary.freeCashFlow,
      icon: Sparkles,
      gradient: 'from-purple-500/10 via-purple-500/5 to-transparent',
      borderColor: 'border-purple-500/30',
      iconColor: 'text-purple-400',
      iconBg: 'bg-purple-500/15',
      badge: 'Thặng dư ròng',
      badgeColor: 'text-purple-400 bg-purple-500/10 border-purple-500/20',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {cards.map((c, i) => {
        const Icon = c.icon;
        return (
          <div
            key={i}
            className={`liquid-glass rounded-2xl p-5 border ${c.borderColor} relative overflow-hidden group`}
          >
            {/* Ambient background glow */}
            <div
              className={`absolute -right-8 -top-8 w-28 h-28 bg-gradient-to-br ${c.gradient} rounded-full blur-2xl group-hover:scale-125 transition-transform duration-500`}
            />

            <div className="flex items-center justify-between mb-3 relative z-10">
              <span className="text-xs font-medium text-slate-400">{c.title}</span>
              <div className={`w-9 h-9 rounded-xl ${c.iconBg} flex items-center justify-center`}>
                <Icon className={`w-4 h-4 ${c.iconColor}`} />
              </div>
            </div>

            <div className="relative z-10">
              <div className="text-xl md:text-2xl font-extrabold tracking-tight text-white mb-2">
                {formatCurrency(c.amount)}
              </div>
              <div className="flex items-center justify-between">
                <span
                  className={`text-[11px] font-medium px-2 py-0.5 rounded-full border ${c.badgeColor} flex items-center gap-1`}
                >
                  {c.trendIcon && <c.trendIcon className="w-3 h-3" />}
                  {c.badge}
                </span>
                <span className="text-[11px] text-slate-500">Chu kỳ hiện tại</span>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
