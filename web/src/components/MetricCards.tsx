'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency } from '@/lib/formatters';
import {
  Wallet,
  ArrowUpRight,
  ArrowDownRight,
  Sparkles,
} from 'lucide-react';

export default function MetricCards() {
  const { summary } = useFinance();

  const cards = [
    {
      title: 'Tổng số dư ví',
      amount: summary.totalBalance,
      icon: Wallet,
      accentColor: '#23C7E8',
      badge: 'Thực tế',
      badgeColor: 'text-[#23C7E8] bg-[#23C7E8]/10 border-[#23C7E8]/20',
      subtitle: 'Khả dụng trên tất cả ví',
    },
    {
      title: 'Thu nhập tháng này',
      amount: summary.monthlyIncome,
      icon: ArrowUpRight,
      accentColor: '#20B486',
      badge: 'Dương',
      badgeColor: 'text-[#20B486] bg-[#20B486]/10 border-[#20B486]/20',
      subtitle: 'Lương & nguồn thu phụ',
    },
    {
      title: 'Chi tiêu tháng này',
      amount: summary.monthlyExpense,
      icon: ArrowDownRight,
      accentColor: '#EB5C6E',
      badge: 'Kiểm soát',
      badgeColor: 'text-[#EB5C6E] bg-[#EB5C6E]/10 border-[#EB5C6E]/20',
      subtitle: 'Sinh hoạt & định kỳ',
    },
    {
      title: 'Dòng tiền tự do (FCF)',
      amount: summary.freeCashFlow,
      icon: Sparkles,
      accentColor: '#6F52F5',
      badge: 'Thặng dư ròng',
      badgeColor: 'text-[#6F52F5] bg-[#6F52F5]/10 border-[#6F52F5]/20',
      subtitle: 'Thu nhập - Chi tiêu',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {cards.map((c, i) => {
        const Icon = c.icon;
        return (
          <div
            key={i}
            className="prism-card p-5 flex flex-col justify-between group"
          >
            <div>
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-medium text-[#A8B0C0]">{c.title}</span>
                <div
                  className="w-8 h-8 rounded-lg flex items-center justify-center transition-transform group-hover:scale-105"
                  style={{
                    backgroundColor: `${c.accentColor}15`,
                    color: c.accentColor,
                    border: `1px solid ${c.accentColor}25`,
                  }}
                >
                  <Icon className="w-4 h-4" />
                </div>
              </div>

              <div className="text-2xl font-black tracking-tight text-[#F7F9FC] mb-1">
                {formatCurrency(c.amount)}
              </div>
            </div>

            <div className="mt-3 pt-3 border-t border-white/5 flex items-center justify-between text-[11px]">
              <span
                className={`px-2 py-0.5 rounded-md font-semibold border ${c.badgeColor}`}
              >
                {c.badge}
              </span>
              <span className="text-[#647087]">{c.subtitle}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
