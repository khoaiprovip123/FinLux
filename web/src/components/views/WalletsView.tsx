'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import BankLogo from '@/components/icons/BankLogo';
import { PERIODS } from '@/lib/constants';
import { formatCurrency, formatShortCurrency, formatShortDate } from '@/lib/formatters';

interface WalletsViewProps {
  onOpenWalletModal?: () => void;
}

export default function WalletsView({ onOpenWalletModal }: WalletsViewProps) {
  const {
    period,
    wallets,
    periodTransactions,
    currentNetWorth,
    setSelectedTxId,
  } = useFinance();

  const currentPeriod = PERIODS[period];

  return (
    <div className="space-y-6">
      {/* Intro */}
      <section className="fx-intro">
        <div>
          <p className="fx-eyebrow">FINLUX / DANH MỤC VÍ &amp; TÀI KHOẢN</p>
          <h1 className="fx-title">Tài khoản</h1>
          <p className="fx-lede">
            Tổng hợp số dư từ cùng sổ giao dịch, tại thời điểm cuối kỳ ({currentPeriod.name}).
          </p>
        </div>
        {onOpenWalletModal && (
          <div className="fx-intro-actions">
            <button
              type="button"
              className="fx-btn"
              onClick={onOpenWalletModal}
            >
              <FinluxIcon name="plus" />
              <span>Thêm tài khoản</span>
            </button>
          </div>
        )}
      </section>

      {/* Account Cards Grid */}
      <div className="fx-acc-summary">
        {wallets.map((w) => {
          const info = w.bankName
            ? `${w.bankName}${w.accountNumber ? ` • ${w.accountNumber.slice(-4)}` : ''}`
            : w.name;

          return (
            <div key={w.id} className="fx-card fx-acc-card fx-lift">
              <BankLogo
                walletName={w.name}
                bankName={w.bankName}
                walletType={w.type}
                customColor={w.color}
                size={44}
              />
              <div className="fx-acc-card-label" style={{ marginTop: '10px' }}>{w.name}</div>
              <div
                className="fx-acc-balance"
                style={{ color: w.balance < 0 ? 'var(--red)' : 'inherit' }}
              >
                {formatCurrency(w.balance)}
              </div>
              <div className="fx-acc-name">{info}</div>
            </div>
          );
        })}
      </div>

      {/* Asset and Debt Summary Table & Recent Activity */}
      <div className="fx-analytics">
        {/* Net Worth & Account Breakdown */}
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Tổng hợp tài sản và nợ</h3>
              <p className="fx-panel-sub">
                Tính đến {currentPeriod.end.split('-').reverse().join('/')}
              </p>
            </div>
          </div>

          <div>
            {wallets.map((w) => (
              <div key={w.id} className="fx-kv" style={{ alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <BankLogo
                    walletName={w.name}
                    bankName={w.bankName}
                    walletType={w.type}
                    customColor={w.color}
                    size={26}
                  />
                  <span>{w.name}</span>
                </div>
                <strong style={{ color: w.balance < 0 ? 'var(--red)' : 'inherit' }}>
                  {formatCurrency(w.balance)}
                </strong>
              </div>
            ))}

            <div className="fx-kv" style={{ paddingTop: '16px', borderTop: '2px solid var(--border)' }}>
              <span style={{ fontWeight: 800, color: 'var(--text)' }}>Tài sản ròng (True Net Worth)</span>
              <strong style={{ fontSize: '15px', color: 'var(--purple)' }}>
                {formatCurrency(currentNetWorth)}
              </strong>
            </div>
          </div>
        </div>

        {/* Recent Transactions in this period */}
        <div className="fx-card fx-panel">
          <div className="fx-panel-header">
            <div>
              <h3 className="fx-panel-title">Giao dịch mới nhất</h3>
              <p className="fx-panel-sub">{periodTransactions.length} giao dịch trong kỳ</p>
            </div>
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
                    <div className="fx-trans-meta">
                      {tx.categoryId || 'Chuyển khoản'} · {tx.walletId}
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
                <p>Chưa có giao dịch trong kỳ này</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
