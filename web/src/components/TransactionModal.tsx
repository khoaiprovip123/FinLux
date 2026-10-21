'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import { TransactionType } from '@/types/finance';
import { FINANCE_CONFIG, financeDate } from '@/lib/financeCalculations';
import { SYSTEM_CATEGORIES } from '@/lib/semantics';

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
  const { wallets, categories, addTransaction, showToast, financeTimeZone } = useFinance();

  const [type, setType] = useState<TransactionType>(initialType);
  const [amount, setAmount] = useState<string>('');
  const [note, setNote] = useState<string>('');
  const [categoryId, setCategoryId] = useState<string>(SYSTEM_CATEGORIES.FOOD);
  const [walletId, setWalletId] = useState<string>('');
  const [relatedWalletId, setRelatedWalletId] = useState<string>('');
  const [date, setDate] = useState<string>('');
  const [saving, setSaving] = useState(false);

  if (!isOpen) return null;

  // Filter categories by type
  const availableCategories = categories.filter((c) => {
    if (type === 'income') return c.type === 'income';
    if (type === 'expense') return c.type === 'expense';
    return false;
  });
  const sourceId = wallets.some((wallet) => wallet.id === walletId) ? walletId : wallets[0]?.id || '';
  const destinationId = wallets.some((wallet) => wallet.id === relatedWalletId && wallet.id !== sourceId)
    ? relatedWalletId : wallets.find((wallet) => wallet.id !== sourceId)?.id || '';
  const selectedCategoryId = availableCategories.some((category) => category.id === categoryId)
    ? categoryId : availableCategories[0]?.id || '';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (saving) return;
    const num = Number(amount);
    if (!Number.isSafeInteger(num) || num <= 0 || num > FINANCE_CONFIG.maxVndAmount) {
      showToast('Vui lòng nhập số tiền hợp lệ (> 0)');
      return;
    }
    if (!note.trim()) {
      showToast('Vui lòng nhập mô tả giao dịch');
      return;
    }
    if (!sourceId || (type !== 'transfer_out' && !selectedCategoryId)) {
      showToast('Cần có tài khoản và danh mục phù hợp để ghi giao dịch.');
      return;
    }
    if (type === 'transfer_out' && (!destinationId || sourceId === destinationId)) {
      showToast('Tài khoản nguồn và tài khoản nhận phải khác nhau');
      return;
    }

    setSaving(true);
    const saved = await addTransaction({
      type,
      amount: num,
      categoryId: type === 'transfer_out' ? null : selectedCategoryId,
      walletId: sourceId,
      relatedWalletId: type === 'transfer_out' ? destinationId : null,
      note: note.trim(),
      date: date || financeDate(new Date(), financeTimeZone),
    });
    setSaving(false);
    if (!saved) return;

    // Reset & close
    setAmount('');
    setNote('');
    setDate('');
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
          Giao dịch được lưu vào tài khoản FinLux và cập nhật số dư ví.
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
                if (newType === 'income') setCategoryId(SYSTEM_CATEGORIES.SALARY);
                else if (newType === 'expense') setCategoryId(SYSTEM_CATEGORIES.FOOD);
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
              value={date || financeDate(new Date(), financeTimeZone)}
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
              max={FINANCE_CONFIG.maxVndAmount}
              step="1"
              required
            />
          </div>

          {/* Source Account */}
          <div className="fx-form-field">
            <label htmlFor="fx-modal-wallet">Tài khoản</label>
            <select
              id="fx-modal-wallet"
              value={sourceId}
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
                value={selectedCategoryId}
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
                value={destinationId}
                onChange={(e) => setRelatedWalletId(e.target.value)}
              >
                {wallets
                  .filter((w) => w.id !== sourceId)
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
            <button type="submit" className="fx-btn" disabled={saving || !sourceId}>
              <FinluxIcon name="check" />
              <span>{saving ? 'Đang lưu...' : 'Lưu giao dịch'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
