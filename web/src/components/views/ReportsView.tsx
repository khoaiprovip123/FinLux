'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import { PERIODS } from '@/lib/constants';
import { formatCurrency, formatShortCurrency, formatPercent } from '@/lib/formatters';

export default function ReportsView() {
  const {
    period,
    periodIncome,
    periodExpense,
    periodCashFlow,
    monthlyTrend,
    categoryBreakdown,
  } = useFinance();

  const currentPeriodName = PERIODS[period].name;

  const maxBarValue = Math.max(
    1,
    ...monthlyTrend.flatMap((d) => [d.income, d.expense])
  );

  const totalExpense = categoryBreakdown.reduce((sum, c) => sum + c.amount, 0);

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
      {/* Intro */}
      <section className="fx-intro">
        <div>
          <p className="fx-eyebrow">FINLUX / PHÂN TÍCH CHUYÊN SÂU</p>
          <h1 className="fx-title">Phân tích</h1>
          <p className="fx-lede">
            Phân tích tài chính cá nhân với bộ lọc và công thức tính thống nhất ({currentPeriodName}).
          </p>
        </div>
      </section>

      {/* 3 Metric Cards */}
      <section className="fx-metrics">
        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Tổng thu nhập</span>
            <span className="fx-metric-icon green">
              <FinluxIcon name="down" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(periodIncome)}</strong>
          <div className="fx-metric-foot">{currentPeriodName}</div>
        </div>

        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Tổng chi tiêu</span>
            <span className="fx-metric-icon red">
              <FinluxIcon name="up" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(periodExpense)}</strong>
          <div className="fx-metric-foot">{currentPeriodName}</div>
        </div>

        <div className="fx-card fx-metric fx-lift">
          <div className="fx-metric-row">
            <span className="fx-metric-label">Dòng tiền thuần</span>
            <span className="fx-metric-icon purple">
              <FinluxIcon name="chart" />
            </span>
          </div>
          <strong className="fx-metric-number">{formatCurrency(periodCashFlow)}</strong>
          <div className="fx-metric-foot">{currentPeriodName}</div>
        </div>
      </section>

      {/* Analytics Chart & Donut */}
      <section className="fx-analytics">
        {/* Monthly Bar Chart */}
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

        {/* Donut Chart */}
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
                <div className="fx-small-note">Không có dữ liệu trong kỳ này</div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Category Breakdown Table */}
      <div className="fx-card fx-panel">
        <div className="fx-panel-header">
          <div>
            <h3 className="fx-panel-title">Chi tiết chi tiêu theo nhóm</h3>
            <p className="fx-panel-sub">Đối chiếu với sổ giao dịch trong kỳ</p>
          </div>
        </div>

        <div>
          {categoryBreakdown.map((cat) => (
            <div key={cat.categoryId} className="fx-kv">
              <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <i
                  style={{
                    display: 'inline-block',
                    width: '10px',
                    height: '10px',
                    borderRadius: '3px',
                    backgroundColor: cat.color,
                  }}
                />
                {cat.name} ({formatPercent(cat.ratio)})
              </span>
              <strong>{formatCurrency(cat.amount)}</strong>
            </div>
          ))}

          {categoryBreakdown.length === 0 && (
            <div className="fx-empty">
              <FinluxIcon name="chart" />
              <p>Chưa có chi tiêu trong kỳ này</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
