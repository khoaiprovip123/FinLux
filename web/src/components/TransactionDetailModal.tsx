'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import BankLogo from '@/components/icons/BankLogo';
import { formatCurrency, formatDate } from '@/lib/formatters';

export default function TransactionDetailModal() {
  const {
    selectedTxId,
    setSelectedTxId,
    transactions,
    categories,
    wallets,
    deleteTransaction,
  } = useFinance();

  if (!selectedTxId) return null;

  const tx = transactions.find((t) => t.id === selectedTxId);
  if (!tx) return null;

  const cat = categories.find((c) => c.id === tx.categoryId);
  const srcWallet = wallets.find((w) => w.id === tx.walletId);
  const targetWallet = wallets.find((w) => w.id === tx.relatedWalletId);

  const isInc = tx.type === 'income';
  const isExp = tx.type === 'expense';
  const sign = isInc ? '+' : isExp ? '-' : '';
  const typeLabel = isInc ? 'Thu nhập' : isExp ? 'Chi tiêu' : 'Chuyển khoản';

  const handleDelete = () => {
    if (confirm('Bạn có chắc chắn muốn xóa giao dịch này? Số dư ví sẽ được hoàn trả nguyên tử.')) {
      deleteTransaction(tx.id);
      setSelectedTxId(null);
    }
  };

  return (
    <div
      className="fx-modal-overlay"
      onClick={(e) => {
        if (e.target === e.currentTarget) setSelectedTxId(null);
      }}
      role="dialog"
      aria-modal="true"
    >
      <div className="fx-modal">
        {/* Top */}
        <div className="fx-modal-top">
          <h2>Chi tiết giao dịch</h2>
          <button
            type="button"
            className="fx-icon-btn"
            onClick={() => setSelectedTxId(null)}
            aria-label="Đóng"
          >
            <FinluxIcon name="close" />
          </button>
        </div>

        <p className="fx-eyebrow">{typeLabel}</p>

        {/* Big Amount Value */}
        <div
          style={{
            fontSize: '29px',
            fontWeight: 830,
            margin: '13px 0',
            color: isInc ? 'var(--green)' : 'var(--text)',
          }}
        >
          {sign}
          {formatCurrency(tx.amount)}
        </div>

        {/* Details list */}
        <div>
          <div className="fx-kv">
            <span>Mô tả</span>
            <strong>{tx.note}</strong>
          </div>
          <div className="fx-kv">
            <span>Ngày</span>
            <strong>{formatDate(tx.date)}</strong>
          </div>
          <div className="fx-kv">
            <span>Danh mục</span>
            <strong>{cat?.name || typeLabel}</strong>
          </div>
          <div className="fx-kv" style={{ alignItems: 'center' }}>
            <span>Tài khoản</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <BankLogo
                walletName={srcWallet?.name}
                bankName={srcWallet?.bankName}
                walletType={srcWallet?.type}
                customColor={srcWallet?.color}
                size={22}
              />
              <strong>{srcWallet?.name || tx.walletId}</strong>
            </div>
          </div>
          {tx.type === 'transfer_out' && targetWallet && (
            <div className="fx-kv" style={{ alignItems: 'center' }}>
              <span>Tài khoản nhận</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <BankLogo
                  walletName={targetWallet.name}
                  bankName={targetWallet.bankName}
                  walletType={targetWallet.type}
                  customColor={targetWallet.color}
                  size={22}
                />
                <strong>{targetWallet.name}</strong>
              </div>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="fx-modal-actions" style={{ marginTop: '24px' }}>
          <button
            type="button"
            className="fx-btn fx-btn-subtle"
            style={{ color: 'var(--red)', borderColor: 'rgba(221, 105, 126, 0.4)' }}
            onClick={handleDelete}
          >
            <FinluxIcon name="trash" />
            <span>Xóa giao dịch</span>
          </button>
          <button
            type="button"
            className="fx-btn"
            onClick={() => setSelectedTxId(null)}
          >
            <span>Đóng</span>
          </button>
        </div>
      </div>
    </div>
  );
}
