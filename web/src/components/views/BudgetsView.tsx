'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import { formatCurrency, formatPercent, formatDate } from '@/lib/formatters';

export default function BudgetsView() {
  const { period, periods, budgetProgressList } = useFinance();

  const currentPeriodName = periods[period].name;

  // Calculate totals across budgets
  const totalBudgetLimit = budgetProgressList.reduce((sum, b) => sum + b.limit, 0);
  const totalBudgetSpent = budgetProgressList.reduce((sum, b) => sum + b.spent, 0);
  const totalBudgetRemaining = totalBudgetLimit - totalBudgetSpent;

  return (
    <div className="space-y-6">
      {/* Intro */}
      <section className="fx-intro">
        <div>
          <p className="fx-eyebrow">FINLUX / QUẢN LÝ HẠN MỨC</p>
          <h1 className="fx-title">Ngân sách</h1>
          <p className="fx-lede">
            Theo dõi ngân sách theo danh mục và theo bộ lọc kỳ đang chọn ({currentPeriodName}).
          </p>
        </div>
      </section>

      {/* 3 Metric Cards */}
      <section className="fx-metrics">
        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Tổng hạn mức</span>
            <span className="fx-metric-icon purple">
              <FinluxIcon name="budget" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(totalBudgetLimit)}</strong>
          <div className="fx-metric-foot">{currentPeriodName}</div>
        </div>

        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Chi tiêu thuộc ngân sách</span>
            <span className="fx-metric-icon red">
              <FinluxIcon name="up" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(totalBudgetSpent)}</strong>
          <div className="fx-metric-foot">Từ giao dịch thực tế cùng kỳ</div>
        </div>

        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Ngân sách còn</span>
            <span className="fx-metric-icon green">
              <FinluxIcon name="target" />
            </span>
          </div>
          <strong
            className="fx-metric-number"
            style={{ color: totalBudgetRemaining < 0 ? 'var(--red)' : 'var(--text)' }}
          >
            {formatCurrency(totalBudgetRemaining)}
          </strong>
          <div className="fx-metric-foot">Có thể âm nếu vượt quá hạn mức</div>
        </div>
      </section>

      {/* Category Budgets Grid Card */}
      <div className="fx-card fx-panel">
        <div className="fx-panel-header">
          <div>
            <h3 className="fx-panel-title">Chi tiết ngân sách danh mục</h3>
            <p className="fx-panel-sub">
              Mỗi hạn mức thuộc một kỳ gốc. Chi tiêu chỉ tính trong phần giao nhau với bộ lọc ({currentPeriodName}).
            </p>
          </div>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '16px 28px',
          }}
        >
          {budgetProgressList.map((b) => (
            <div key={b.id} className="fx-budget-item">
              <div className="fx-budget-text">
                <span className="fx-budget-name">{b.name}</span>
                <span className="fx-budget-money">
                  {formatCurrency(b.spent)} / {formatCurrency(b.limit)}
                </span>
              </div>
              <div className="fx-progress" title={formatPercent(b.ratio)}>
                <div
                  className={`fx-progress-fill ${b.status}`}
                  style={{ width: `${Math.max(0, Math.min(100, b.ratio * 100))}%` }}
                />
              </div>
              <div className="fx-small-note" style={{ marginTop: '6px' }}>
                {formatDate(b.start)} – {formatDate(b.end)} · {' '}
                {formatPercent(b.ratio)} đã sử dụng
                {b.ratio > 1 && ' (Vượt hạn mức)'}
              </div>
            </div>
          ))}

          {budgetProgressList.length === 0 && (
            <div className="fx-empty" style={{ gridColumn: '1 / -1' }}>
              <FinluxIcon name="budget" />
              <p>Chưa có danh mục ngân sách nào được thiết lập</p>
            </div>
          )}
        </div>

        <div className="fx-muted-box" style={{ marginTop: '24px' }}>
          Chi tiêu được đối soát từ giao dịch thực tế của từng danh mục và kỳ ngân sách; hạn mức không nhân theo số tháng của bộ lọc.
        </div>
      </div>
    </div>
  );
}
