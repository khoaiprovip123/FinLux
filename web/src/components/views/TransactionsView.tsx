'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import BankLogo from '@/components/icons/BankLogo';
import { PERIODS } from '@/lib/constants';
import { formatCurrency, formatDate } from '@/lib/formatters';

interface TransactionsViewProps {
  onOpenAddTx: () => void;
}

export default function TransactionsView({ onOpenAddTx }: TransactionsViewProps) {
  const {
    period,
    periodTransactions,
    categories,
    wallets,
    search,
    setSearch,
    kind,
    setKind,
    setSelectedTxId,
  } = useFinance();

  const currentPeriodName = PERIODS[period].name;

  // Filter transactions by kind and search query
  const filteredList = periodTransactions.filter((tx) => {
    // Kind filter
    if (kind === 'income' && tx.type !== 'income') return false;
    if (kind === 'expense' && tx.type !== 'expense') return false;
    if (kind === 'transfer' && tx.type !== 'transfer_out') return false;

    // Search query filter
    if (search.trim()) {
      const q = search.toLowerCase();
      const cat = categories.find((c) => c.id === tx.categoryId);
      const srcWallet = wallets.find((w) => w.id === tx.walletId);
      const textToMatch = [
        tx.note,
        tx.date,
        cat?.name || '',
        srcWallet?.name || '',
        String(tx.amount),
      ].join(' ').toLowerCase();

      return textToMatch.includes(q);
    }
    return true;
  });

  const getCategoryName = (tx: (typeof periodTransactions)[0]) => {
    if (tx.type === 'transfer_out') return 'Chuyển khoản';
    const cat = categories.find((c) => c.id === tx.categoryId);
    return cat?.name || 'Khác';
  };

  const getWalletName = (tx: (typeof periodTransactions)[0]) => {
    const w = wallets.find((item) => item.id === tx.walletId);
    return w?.name || tx.walletId;
  };

  return (
    <div className="space-y-6">
      {/* Intro */}
      <section className="fx-intro">
        <div>
          <p className="fx-eyebrow">FINLUX / SỔ GIAO DỊCH</p>
          <h1 className="fx-title">Giao dịch</h1>
          <p className="fx-lede">
            Mọi giao dịch cùng nguồn với Tổng quan, Tài khoản và Báo cáo.
          </p>
        </div>
      </section>

      {/* Main Table Card */}
      <div className="fx-card fx-panel">
        <div className="fx-panel-header">
          <div>
            <h3 className="fx-panel-title">Sổ giao dịch chi tiết</h3>
            <p className="fx-panel-sub">{currentPeriodName}</p>
          </div>
          <button
            type="button"
            className="fx-btn"
            onClick={onOpenAddTx}
          >
            <FinluxIcon name="plus" />
            <span>Thêm giao dịch</span>
          </button>
        </div>

        {/* Filter Controls: Search & Kind Tabs */}
        <div className="fx-filter-row">
          <label className="fx-search" aria-label="Tìm kiếm">
            <FinluxIcon name="search" />
            <input
              type="text"
              placeholder="Tìm kiếm theo ghi chú, danh mục, ví..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </label>

          <button
            type="button"
            className={`fx-filter ${kind === 'all' ? 'active' : ''}`}
            onClick={() => setKind('all')}
          >
            Tất cả
          </button>
          <button
            type="button"
            className={`fx-filter ${kind === 'income' ? 'active' : ''}`}
            onClick={() => setKind('income')}
          >
            Thu nhập
          </button>
          <button
            type="button"
            className={`fx-filter ${kind === 'expense' ? 'active' : ''}`}
            onClick={() => setKind('expense')}
          >
            Chi tiêu
          </button>
          <button
            type="button"
            className={`fx-filter ${kind === 'transfer' ? 'active' : ''}`}
            onClick={() => setKind('transfer')}
          >
            Chuyển khoản
          </button>
        </div>

        {/* Data Table */}
        <div className="fx-table-wrap">
          <table className="fx-table">
            <thead>
              <tr>
                <th>Mô tả</th>
                <th>Danh mục</th>
                <th>Ngày</th>
                <th>Tài khoản</th>
                <th style={{ textAlign: 'right' }}>Số tiền</th>
              </tr>
            </thead>
            <tbody>
              {filteredList.map((tx) => {
                const isInc = tx.type === 'income';
                const isExp = tx.type === 'expense';
                const sign = isInc ? '+' : isExp ? '-' : '';
                const color = isInc ? 'var(--green)' : 'var(--text)';

                return (
                  <tr
                    key={tx.id}
                    onClick={() => setSelectedTxId(tx.id)}
                    tabIndex={0}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        setSelectedTxId(tx.id);
                      }
                    }}
                  >
                    <td>
                      <strong>{tx.note}</strong>
                    </td>
                    <td>{getCategoryName(tx)}</td>
                    <td>{formatDate(tx.date)}</td>
                    <td>
                      {(() => {
                        const w = wallets.find((item) => item.id === tx.walletId);
                        return (
                          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                            <BankLogo
                              walletName={w?.name}
                              bankName={w?.bankName}
                              walletType={w?.type}
                              customColor={w?.color}
                              size={20}
                            />
                            <span>{w?.name || tx.walletId}</span>
                          </div>
                        );
                      })()}
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 800, color }}>
                      {sign}
                      {formatCurrency(tx.amount)}
                    </td>
                  </tr>
                );
              })}

              {filteredList.length === 0 && (
                <tr>
                  <td colSpan={5}>
                    <div className="fx-empty">
                      <FinluxIcon name="search" />
                      <p>Không tìm thấy giao dịch nào trong kỳ</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <p className="fx-small-note" style={{ marginTop: '18px' }}>
          {filteredList.length} / {periodTransactions.length} giao dịch trong kỳ (bao gồm chuyển khoản). Nhấn vào bất kỳ giao dịch nào để xem chi tiết.
        </p>
      </div>
    </div>
  );
}
