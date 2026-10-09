'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import BankLogo from '@/components/icons/BankLogo';
import { PERIODS } from '@/lib/constants';
import { formatCurrency, formatShortCurrency, formatPercent, formatShortDate } from '@/lib/formatters';

export default function DashboardView() {
  const {
    period,
    setActiveTab,
    periodTransactions,
    periodIncome,
    periodExpense,
    periodCashFlow,
    currentNetWorth,
    netWorthDelta,
    availableLiquid,
    monthlyTrend,
    categoryBreakdown,
    budgetProgressList,
    setSelectedTxId,
    wallets,
    categories,
  } = useFinance();

  const currentPeriodName = PERIODS[period].name;

  // Maximum value for scaling the bar chart
  const maxBarValue = Math.max(
    1,
    ...monthlyTrend.flatMap((d) => [d.income, d.expense])
  );

  // Total spending for donut chart
  const totalExpense = categoryBreakdown.reduce((sum, c) => sum + c.amount, 0);

  // Dynamic conic-gradient for Donut Chart
  let currentAngle = 0;
  const gradientStops: string[] = [];
  categoryBreakdown.forEach((cat) => {
    const nextAngle = currentAngle + (cat.amount / Math.max(1, totalExpense)) * 100;
    gradientStops.push(`${cat.color} ${currentAngle.toFixed(2)}% ${nextAngle.toFixed(2)}%`);
    currentAngle = nextAngle;
  });
  const donutGradient = gradientStops.length > 0
    ? `conic-gradient(${gradientStops.join(', ')})`
    : 'conic-gradient(#dde2ed 0% 100%)';

  return (
    <div className="space-y-6">
      {/* 1. Header Intro */}
      <section className="fx-intro" aria-label="Giới thiệu bức tranh tài chính">
        <div>
          <p className="fx-eyebrow">FINLUX / DỮ LIỆU THỰC TẾ &amp; MINH HOẠ</p>
          <h1 className="fx-title">Bức tranh tài chính của bạn</h1>
          <p className="fx-lede">Mọi con số, một góc nhìn rõ ràng.</p>
        </div>
        <div className="fx-intro-actions">
          <button
            type="button"
            className="fx-link"
            onClick={() => setActiveTab('reports')}
          >
            <span>Xem phân tích</span>
            <FinluxIcon name="arrow" />
          </button>
        </div>
      </section>

      {/* 2. Iconic Prism Wealth Hero Banner */}
      <section className="fx-hero" aria-label="Tổng quan tài sản ròng">
        <div className="fx-hero-left">
          <div className="fx-hero-upper">
            <span className="fx-hero-dot" />
            <span>PRISM WEALTH OVERVIEW</span>
          </div>
          <p className="fx-hero-name">Tài sản ròng</p>
          <div className="fx-hero-value">{formatCurrency(currentNetWorth)}</div>
          <div className="fx-hero-foot">
            <span className="fx-hero-pill">
              {netWorthDelta >= 0 ? '+' : ''}
              {formatCurrency(netWorthDelta)}
            </span>
            <span>Thay đổi trong kỳ</span>
          </div>
        </div>

        <div className="fx-hero-right">
          <div className="fx-prism" aria-hidden="true" />
          <div className="fx-glass-mini">
            <span>Tiền mặt &amp; ngân hàng</span>
            <strong>{formatCurrency(availableLiquid)}</strong>
            <small>{currentPeriodName}</small>
          </div>
        </div>
      </section>

      {/* 3. 3-Column Core Metrics */}
      <section className="fx-metrics" aria-label="Chỉ số tài chính trọng yếu">
        {/* Income Card */}
        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Tổng thu nhập</span>
            <span className="fx-metric-icon green">
              <FinluxIcon name="down" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(periodIncome)}</strong>
          <div className="fx-metric-foot">
            Từ các giao dịch thu nhập trong kỳ
          </div>
        </div>

        {/* Expense Card */}
        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Tổng chi tiêu</span>
            <span className="fx-metric-icon red">
              <FinluxIcon name="up" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(periodExpense)}</strong>
          <div className="fx-metric-foot">
            Không gồm chuyển tiền nội bộ
          </div>
        </div>

        {/* Cash Flow Card */}
        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Dòng tiền thuần</span>
            <span className="fx-metric-icon purple">
              <FinluxIcon name="swap" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(periodCashFlow)}</strong>
          <div className="fx-metric-foot">
            <em>Thu nhập</em> - <em>Chi tiêu</em> trong kỳ
          </div>
        </div>
      </section>

      {/* 4. Analytics: Monthly Trend Bar Chart & Category Spending Donut */}
      <section className="fx-analytics" aria-label="Phân tích xu hướng và cơ cấu chi tiêu">
        {/* Bar Chart Panel */}
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Xu hướng thu và chi</h3>
              <p className="fx-panel-sub">{currentPeriodName} · VND</p>
            </div>
            <div className="fx-legend">
              <span className="fx-legend-item">
                <i className="fx-legend-dot inc" />
                <span>Thu nhập</span>
              </span>
              <span className="fx-legend-item">
                <i className="fx-legend-dot exp" />
                <span>Chi tiêu</span>
              </span>
            </div>
          </div>

          <div className="fx-barplot">
            {monthlyTrend.map((d) => {
              const incH = Math.max(2, (d.income / maxBarValue) * 98);
              const expH = Math.max(2, (d.expense / maxBarValue) * 98);
              return (
                <div
                  key={d.key}
                  className="fx-barcell"
                  title={`Tháng ${d.key.slice(5)}: Thu ${formatCurrency(d.income)} | Chi ${formatCurrency(d.expense)}`}
                >
                  <div
                    className="fx-bar inc"
                    style={{ ['--h' as string]: `${incH}%` }}
                  />
                  <div
                    className="fx-bar exp"
                    style={{ ['--h' as string]: `${expH}%` }}
                  />
                  <span className="fx-barlabel">{d.label}</span>
                </div>
              );
            })}
          </div>
          <div className="fx-chart-note">
            Thu nhập / Chi tiêu theo tháng, không tính chuyển khoản nội bộ.
          </div>
        </div>

        {/* Donut Chart Panel */}
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Cơ cấu chi tiêu</h3>
              <p className="fx-panel-sub">{currentPeriodName}</p>
            </div>
            <FinluxIcon name="chart" />
          </div>

          <div className="fx-donut-layout">
            <div
              className="fx-donut"
              style={{ background: donutGradient }}
            >
              <div className="fx-donut-inner">
                <span>Chi tiêu</span>
                <strong>{formatShortCurrency(totalExpense)}</strong>
              </div>
            </div>

            <div className="fx-cat-list">
              {categoryBreakdown.slice(0, 5).map((cat) => (
                <div key={cat.categoryId} className="fx-cat-row">
                  <i
                    className="fx-cat-swatch"
                    style={{ background: cat.color }}
                  />
                  <span>{cat.name}</span>
                  <b>{formatPercent(cat.ratio)}</b>
                </div>
              ))}
              {categoryBreakdown.length === 0 && (
                <div className="fx-small-note">Không có dữ liệu chi tiêu trong kỳ</div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* 5. Bottom Grid: Recent Transactions & Budget Progress */}
      <section className="fx-bottom" aria-label="Giao dịch gần đây và tiến độ ngân sách">
        {/* Recent Transactions List */}
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Giao dịch gần đây</h3>
              <p className="fx-panel-sub">{periodTransactions.length} giao dịch trong kỳ</p>
            </div>
            <button
              type="button"
              className="fx-link"
              onClick={() => setActiveTab('transactions')}
            >
              <span>Xem tất cả</span>
              <FinluxIcon name="arrow" />
            </button>
          </div>

          <div>
            {periodTransactions.slice(0, 5).map((tx) => {
              const symbolType = tx.type === 'income' ? 'inc' : tx.type === 'expense' ? 'exp' : 'transfer';
              const iconName = tx.type === 'income' ? 'down' : tx.type === 'expense' ? 'up' : 'swap';
              const sign = tx.type === 'income' ? '+' : tx.type === 'expense' ? '-' : '';

              return (
                <div
                  key={tx.id}
                  className="fx-trans-row"
                  onClick={() => setSelectedTxId(tx.id)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      setSelectedTxId(tx.id);
                    }
                  }}
                >
                  <div className={`fx-trans-symbol ${symbolType}`}>
                    <FinluxIcon name={iconName} />
                  </div>
                  <div style={{ minWidth: 0 }}>
                    <div className="fx-trans-name">{tx.note}</div>
                    <div className="fx-trans-meta" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span>
                        {tx.type === 'transfer_out'
                          ? 'Chuyển khoản'
                          : categories.find((c) => c.id === tx.categoryId)?.name || 'Khác'}
                      </span>
                      <span>·</span>
                      {(() => {
                        const w = wallets.find((item) => item.id === tx.walletId);
                        return (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                            <BankLogo
                              walletName={w?.name}
                              bankName={w?.bankName}
                              walletType={w?.type}
                              customColor={w?.color}
                              size={14}
                            />
                            <span>{w?.name || tx.walletId}</span>
                          </span>
                        );
                      })()}
                    </div>
                  </div>
                  <div className="fx-trans-date">{formatShortDate(tx.date)}</div>
                  <div className={`fx-trans-value ${symbolType}`}>
                    {sign}
                    {formatShortCurrency(tx.amount)}
                  </div>
                </div>
              );
            })}

            {periodTransactions.length === 0 && (
              <div className="fx-empty">
                <FinluxIcon name="receipt" />
                <p>Không có giao dịch trong kỳ này</p>
              </div>
            )}
          </div>
        </div>

        {/* Budget Progress Panel */}
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Tiến độ ngân sách</h3>
              <p className="fx-panel-sub">Theo {currentPeriodName.toLowerCase()}</p>
            </div>
            <button
              type="button"
              className="fx-link"
              onClick={() => setActiveTab('budgets')}
            >
              <span>Chi tiết</span>
              <FinluxIcon name="arrow" />
            </button>
          </div>

          <div>
            {budgetProgressList.slice(0, 5).map((b) => (
              <div key={b.categoryId} className="fx-budget-item">
                <div className="fx-budget-text">
                  <span className="fx-budget-name">{b.name}</span>
                  <span className="fx-budget-money">
                    {formatShortCurrency(b.spent)} / {formatShortCurrency(b.limit)}
                  </span>
                </div>
                <div className="fx-progress" title={formatPercent(b.ratio)}>
                  <div
                    className={`fx-progress-fill ${b.status}`}
                    style={{ width: `${Math.max(0, Math.min(100, b.ratio * 100))}%` }}
                  />
                </div>
              </div>
            ))}

            {budgetProgressList.length === 0 && (
              <div className="fx-empty">
                <FinluxIcon name="budget" />
                <p>Chưa thiết lập ngân sách</p>
              </div>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}
