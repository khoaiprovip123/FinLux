'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import { formatCurrency, formatPercent } from '@/lib/formatters';

export default function GoalsView() {
  const { wallets, goals } = useFinance();

  // Find savings account
  const savingsWallet = wallets.find((w) => w.id === 'savings') || wallets[0];
  const savingsBalance = savingsWallet ? savingsWallet.balance : 0;

  // Primary goal: Quỹ tự do tài chính
  const mainGoal = goals.find((g) => g.id === 'goal_financial_freedom') || goals[0] || {
    id: 'goal_financial_freedom',
    name: 'Quỹ tự do tài chính',
    targetAmount: 180000000,
    currentAmount: savingsBalance,
  };

  const currentAmount = Math.max(savingsBalance, mainGoal.currentAmount || 0);
  const targetAmount = mainGoal.targetAmount || 180000000;
  const ratio = targetAmount > 0 ? currentAmount / targetAmount : 0;
  const remaining = Math.max(0, targetAmount - currentAmount);

  return (
    <div className="space-y-6">
      {/* Intro */}
      <section className="fx-intro">
        <div>
          <p className="fx-eyebrow">FINLUX / KẾ HOẠCH TƯƠNG LAI</p>
          <h1 className="fx-title">Mục tiêu tài chính</h1>
          <p className="fx-lede">
            Theo dõi mục tiêu dựa trên số dư thực tế trong tài khoản Quỹ tiết kiệm và sổ chung.
          </p>
        </div>
      </section>

      {/* Goal Progress Banner & Goal Details Grid */}
      <div className="fx-analytics">
        {/* Main Goal Progress Card */}
        <div className="fx-goal-progress">
          <span style={{ fontSize: '11px', letterSpacing: '1.4px', fontWeight: 800 }}>
            MỤC TIÊU CHÍNH
          </span>
          <h3 style={{ fontSize: '21px', margin: '12px 0 0', fontWeight: 800 }}>
            {mainGoal.name}
          </h3>
          <strong>{formatCurrency(currentAmount)}</strong>
          <p>
            Đích đến: {formatCurrency(targetAmount)}
            <br />
            Đồng bộ trực tiếp số dư từ tài khoản Quỹ tiết kiệm.
          </p>
          <div className="fx-progress">
            <div
              className="fx-progress-fill"
              style={{ width: `${Math.min(100, ratio * 100)}%` }}
            />
          </div>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              fontSize: '11px',
              marginTop: '9px',
            }}
          >
            <span>{formatPercent(ratio)} hoàn thành</span>
            <span>{formatCurrency(remaining)} còn lại</span>
          </div>
        </div>

        {/* Goal Detail Breakdown */}
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Chi tiết mục tiêu</h3>
              <p className="fx-panel-sub">Thông tin được tính toán từ số dư ví thực</p>
            </div>
            <FinluxIcon name="target" />
          </div>

          <div>
            <div className="fx-kv">
              <span>Tài khoản liên kết</span>
              <strong>{savingsWallet?.name || 'Quỹ tiết kiệm'}</strong>
            </div>
            <div className="fx-kv">
              <span>Số dư hiện có</span>
              <strong style={{ color: 'var(--green)' }}>{formatCurrency(currentAmount)}</strong>
            </div>
            <div className="fx-kv">
              <span>Đích đến mục tiêu</span>
              <strong>{formatCurrency(targetAmount)}</strong>
            </div>
            <div className="fx-kv">
              <span>Tỷ lệ hoàn thành</span>
              <strong>{formatPercent(ratio)}</strong>
            </div>
            <div className="fx-kv">
              <span>Khoản còn thiếu</span>
              <strong>{formatCurrency(remaining)}</strong>
            </div>
          </div>

          <p className="fx-small-note" style={{ marginTop: '16px' }}>
            Khi thêm giao dịch chuyển tiền vào Quỹ tiết kiệm hoặc thu nhập, tiến độ sẽ tự động nhảy số theo thời gian thực.
          </p>
        </div>
      </div>

      {/* Other Goals Grid if any */}
      {goals.length > 1 && (
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Các mục tiêu khác</h3>
              <p className="fx-panel-sub">{goals.length - 1} mục tiêu phụ đang thực hiện</p>
            </div>
          </div>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
              gap: '16px',
            }}
          >
            {goals.slice(1).map((g) => {
              const gRatio = g.targetAmount > 0 ? g.currentAmount / g.targetAmount : 0;
              return (
                <div key={g.id} className="fx-muted-box">
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800 }}>
                    <span>{g.name}</span>
                    <span>{formatPercent(gRatio)}</span>
                  </div>
                  <div style={{ margin: '8px 0', fontSize: '13px', fontWeight: 700 }}>
                    {formatCurrency(g.currentAmount)} / {formatCurrency(g.targetAmount)}
                  </div>
                  <div className="fx-progress">
                    <div
                      className="fx-progress-fill"
                      style={{ width: `${Math.min(100, gRatio * 100)}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
