'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { WalletType } from '@/types/finance';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import BankLogo from '@/components/icons/BankLogo';
import {
  FINANCIAL_INSTITUTIONS,
  FinancialInstitution,
  findInstitutionForWallet,
} from '@/lib/banks';

interface WalletModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function WalletModal({ isOpen, onClose }: WalletModalProps) {
  const { addWallet } = useFinance();

  const [name, setName] = useState('');
  const [type, setType] = useState<WalletType>('bank');
  const [balanceStr, setBalanceStr] = useState('0');
  const [bankName, setBankName] = useState('');
  const [accountNumber, setAccountNumber] = useState('');
  const [color, setColor] = useState('#655bdc');

  if (!isOpen) return null;

  const numericBalance = parseInt(balanceStr.replace(/\D/g, '') || '0', 10);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    addWallet({
      name: name.trim(),
      type,
      balance: numericBalance,
      color,
      bankName: bankName.trim() || undefined,
      accountNumber: accountNumber.trim() || undefined,
      isDefault: false,
    });

    onClose();
  };

  const colors = ['#655bdc', '#079a86', '#dd697e', '#e0ab65', '#4bbfdd', '#7566e8'];

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
        <div className="fx-modal-top">
          <h2>Thêm tài khoản mới</h2>
          <button
            type="button"
            onClick={onClose}
            className="fx-icon-btn"
            aria-label="Đóng"
          >
            <FinluxIcon name="close" />
          </button>
        </div>

        {/* Popular Banks / Wallets Quick Selector */}
        <div style={{ marginBottom: '14px' }}>
          <label style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Chọn nhanh ngân hàng / ví phổ biến
          </label>
          <div
            style={{
              display: 'flex',
              gap: '8px',
              overflowX: 'auto',
              padding: '6px 2px 8px',
              scrollbarWidth: 'none',
            }}
          >
            {FINANCIAL_INSTITUTIONS.slice(0, 10).map((inst) => {
              const isSelected = name.toLowerCase() === inst.shortName.toLowerCase();
              return (
                <button
                  key={inst.id}
                  type="button"
                  onClick={() => {
                    setName(inst.shortName);
                    setBankName(inst.category === 'bank' ? inst.shortName : '');
                    setType(inst.type);
                    setColor(inst.color);
                  }}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '5px 10px',
                    borderRadius: '10px',
                    border: isSelected ? '2px solid var(--purple)' : '1px solid var(--border)',
                    background: isSelected ? 'var(--purple-soft)' : 'var(--surface-soft)',
                    cursor: 'pointer',
                    whiteSpace: 'nowrap',
                    fontSize: '11px',
                    fontWeight: 700,
                    color: isSelected ? 'var(--purple)' : 'var(--text)',
                    flexShrink: 0,
                    transition: 'all 0.15s ease',
                  }}
                >
                  <BankLogo institution={inst} size={18} />
                  <span>{inst.shortName}</span>
                </button>
              );
            })}
          </div>
        </div>

        <form onSubmit={handleSubmit} className="fx-form-grid">
          <div className="fx-form-field wide">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label htmlFor="fx-wallet-name">Tên ví / Tài khoản</label>
              {name && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <BankLogo walletName={name} bankName={bankName} walletType={type} customColor={color} size={22} />
                  <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)' }}>Xem trước logo</span>
                </div>
              )}
            </div>
            <input
              id="fx-wallet-name"
              type="text"
              value={name}
              onChange={(e) => {
                const val = e.target.value;
                setName(val);
                const matched = findInstitutionForWallet(val);
                if (matched) {
                  if (matched.category === 'bank') setBankName(matched.shortName);
                  setType(matched.type);
                  setColor(matched.color);
                }
              }}
              placeholder="Ví dụ: Vietcombank, MB Bank, Tiền mặt..."
              required
              autoFocus
            />
          </div>

          <div className="fx-form-field">
            <label htmlFor="fx-wallet-type">Loại tài khoản</label>
            <select
              id="fx-wallet-type"
              value={type}
              onChange={(e) => setType(e.target.value as WalletType)}
            >
              <option value="bank">Ngân hàng</option>
              <option value="cash">Tiền mặt</option>
              <option value="ewallet">Ví điện tử</option>
              <option value="card">Thẻ tín dụng</option>
              <option value="investment">Đầu tư & Tiết kiệm</option>
              <option value="other">Khác</option>
            </select>
          </div>

          <div className="fx-form-field">
            <label htmlFor="fx-wallet-bal">Số dư khởi tạo (VND)</label>
            <input
              id="fx-wallet-bal"
              type="text"
              value={numericBalance > 0 ? new Intl.NumberFormat('vi-VN').format(numericBalance) : ''}
              onChange={(e) => setBalanceStr(e.target.value.replace(/\D/g, ''))}
              placeholder="0"
            />
          </div>

          {type === 'bank' && (
            <>
              <div className="fx-form-field">
                <label htmlFor="fx-bank-name">Ngân hàng liên kết</label>
                <input
                  id="fx-bank-name"
                  type="text"
                  list="vietnam-banks-list"
                  value={bankName}
                  onChange={(e) => {
                    const val = e.target.value;
                    setBankName(val);
                    const matched = findInstitutionForWallet(name, val);
                    if (matched) {
                      setColor(matched.color);
                    }
                  }}
                  placeholder="Chọn hoặc nhập tên ngân hàng..."
                />
                <datalist id="vietnam-banks-list">
                  {FINANCIAL_INSTITUTIONS.filter((i) => i.category === 'bank').map((b) => (
                    <option key={b.id} value={b.shortName}>
                      {b.fullName} ({b.code})
                    </option>
                  ))}
                </datalist>
              </div>
              <div className="fx-form-field">
                <label htmlFor="fx-acc-number">Số tài khoản / Số thẻ</label>
                <input
                  id="fx-acc-number"
                  type="text"
                  value={accountNumber}
                  onChange={(e) => setAccountNumber(e.target.value)}
                  placeholder="Ví dụ: 1029384..."
                />
              </div>
            </>
          )}

          <div className="fx-form-field wide">
            <label>Màu đại diện</label>
            <div style={{ display: 'flex', gap: '10px', marginTop: '4px' }}>
              {colors.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setColor(c)}
                  style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '10px',
                    backgroundColor: c,
                    border: color === c ? '3px solid #fff' : 'none',
                    boxShadow: color === c ? '0 0 0 2px var(--purple)' : 'none',
                    transform: color === c ? 'scale(1.1)' : 'scale(1)',
                    transition: 'transform 0.15s ease',
                  }}
                  aria-label={`Chọn màu ${c}`}
                />
              ))}
            </div>
          </div>

          <div className="fx-form-field wide fx-modal-actions">
            <button
              type="button"
              onClick={onClose}
              className="fx-btn fx-btn-subtle"
            >
              Hủy
            </button>
            <button type="submit" className="fx-btn">
              <FinluxIcon name="check" />
              <span>Tạo tài khoản</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
