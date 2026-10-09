'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import { TransactionType } from '@/types/finance';

interface TransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialType?: TransactionType;
}

export default function TransactionModal({
  isOpen,
  onClose,
  initialType = 'expense',
}: TransactionModalProps) {
  const { wallets, categories, addTransaction, showToast } = useFinance();

  const [type, setType] = useState<TransactionType>(initialType);
  const [amount, setAmount] = useState<string>('');
  const [note, setNote] = useState<string>('');
  const [categoryId, setCategoryId] = useState<string>('food');
  const [walletId, setWalletId] = useState<string>('bank');
  const [relatedWalletId, setRelatedWalletId] = useState<string>('cash');
  const [date, setDate] = useState<string>('2026-10-09');

  if (!isOpen) return null;

  // Filter categories by type
  const availableCategories = categories.filter((c) => {
    if (type === 'income') return c.type === 'income';
    if (type === 'expense') return c.type === 'expense';
    return false;
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const num = parseFloat(amount.replace(/[^0-9.-]+/g, ''));
    if (isNaN(num) || num <= 0) {
      showToast('Vui lòng nhập số tiền hợp lệ (> 0)');
      return;
    }
    if (!note.trim()) {
      showToast('Vui lòng nhập mô tả giao dịch');
      return;
    }
    if (type === 'transfer_out' && walletId === relatedWalletId) {
      showToast('Tài khoản nguồn và tài khoản nhận phải khác nhau');
      return;
    }

    addTransaction({
      type,
      amount: num,
      categoryId: type === 'transfer_out' ? null : categoryId,
      walletId,
      relatedWalletId: type === 'transfer_out' ? relatedWalletId : null,
      note: note.trim(),
      date,
    });

    // Reset & close
    setAmount('');
    setNote('');
    onClose();
  };

  return (
    <div
      className="fx-modal-overlay"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
      role="dialog"
      aria-modal="true"
    >
      <div className="fx-modal">
        {/* Modal Header */}
        <div className="fx-modal-top">
          <h2>Thêm giao dịch</h2>
          <button
            type="button"
            className="fx-icon-btn"
            onClick={onClose}
            aria-label="Đóng"
          >
            <FinluxIcon name="close" />
          </button>
        </div>

        <p className="fx-small-note" style={{ marginBottom: '16px' }}>
          Dữ liệu mẫu cập nhật trực tiếp vào số dư ví và ngân sách.
        </p>

        {/* Form Grid */}
        <form onSubmit={handleSubmit} className="fx-form-grid">
          {/* Transaction Type */}
          <div className="fx-form-field">
            <label htmlFor="fx-modal-type">Loại giao dịch</label>
            <select
              id="fx-modal-type"
              value={type}
              onChange={(e) => {
                const newType = e.target.value as TransactionType;
                setType(newType);
                if (newType === 'income') setCategoryId('salary');
                else if (newType === 'expense') setCategoryId('food');
              }}
            >
              <option value="expense">Chi tiêu</option>
              <option value="income">Thu nhập</option>
              <option value="transfer_out">Chuyển khoản</option>
            </select>
          </div>

          {/* Date Picker */}
          <div className="fx-form-field">
            <label htmlFor="fx-modal-date">Ngày giao dịch</label>
            <input
              id="fx-modal-date"
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
            />
          </div>

          {/* Note / Description */}
          <div className="fx-form-field wide">
            <label htmlFor="fx-modal-note">Mô tả / Ghi chú</label>
            <input
              id="fx-modal-note"
              type="text"
              placeholder="Ví dụ: Cơm trưa cùng đồng nghiệp"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              required
              maxLength={120}
              autoFocus
            />
          </div>

          {/* Amount */}
          <div className="fx-form-field">
            <label htmlFor="fx-modal-amount">Số tiền (VND)</label>
            <input
              id="fx-modal-amount"
              type="number"
              placeholder="150000"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              min="1"
              step="1"
              required
            />
          </div>

          {/* Source Account */}
          <div className="fx-form-field">
            <label htmlFor="fx-modal-wallet">Tài khoản</label>
            <select
              id="fx-modal-wallet"
              value={walletId}
              onChange={(e) => setWalletId(e.target.value)}
            >
              {wallets.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
          </div>

          {/* Category Select (hidden for transfers) */}
          {type !== 'transfer_out' ? (
            <div className="fx-form-field wide">
              <label htmlFor="fx-modal-cat">Danh mục</label>
              <select
                id="fx-modal-cat"
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
              >
                {availableCategories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          ) : (
            /* Target Account Select (for transfers) */
            <div className="fx-form-field wide">
              <label htmlFor="fx-modal-related">Tài khoản nhận</label>
              <select
                id="fx-modal-related"
                value={relatedWalletId}
                onChange={(e) => setRelatedWalletId(e.target.value)}
              >
                {wallets
                  .filter((w) => w.id !== walletId)
                  .map((w) => (
                    <option key={w.id} value={w.id}>
                      {w.name}
                    </option>
                  ))}
              </select>
            </div>
          )}

          {/* Action Buttons */}
          <div className="fx-form-field wide fx-modal-actions">
            <button
              type="button"
              className="fx-btn fx-btn-subtle"
              onClick={onClose}
            >
              Hủy
            </button>
            <button type="submit" className="fx-btn">
              <FinluxIcon name="check" />
              <span>Lưu giao dịch</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
