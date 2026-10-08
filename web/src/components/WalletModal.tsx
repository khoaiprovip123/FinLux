'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { WalletType } from '@/types/finance';
import { X, Check } from 'lucide-react';

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
  const [color, setColor] = useState('#06B6D4');

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

  const colors = ['#06B6D4', '#10B981', '#F43F5E', '#8B5CF6', '#F59E0B', '#EC4899', '#3B82F6'];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-md liquid-glass rounded-2xl border border-white/15 p-6 shadow-2xl">
        <div className="flex items-center justify-between pb-4 border-b border-white/10">
          <h2 className="text-lg font-bold text-white">Thêm ví / Tài khoản mới</h2>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-lg bg-white/5 hover:bg-white/10 flex items-center justify-center text-slate-400 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="text-xs text-slate-400 block mb-1">Tên ví / Tài khoản</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Ví dụ: MB Bank, Tiền tiết kiệm..."
              required
              className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-slate-400 block mb-1">Loại tài khoản</label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value as WalletType)}
                className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
              >
                <option value="bank">Ngân hàng</option>
                <option value="cash">Tiền mặt</option>
                <option value="ewallet">Ví điện tử</option>
                <option value="card">Thẻ tín dụng</option>
                <option value="investment">Tài khoản đầu tư</option>
                <option value="other">Khác</option>
              </select>
            </div>

            <div>
              <label className="text-xs text-slate-400 block mb-1">Số dư khởi tạo (VNĐ)</label>
              <input
                type="text"
                value={numericBalance > 0 ? new Intl.NumberFormat('vi-VN').format(numericBalance) : ''}
                onChange={(e) => setBalanceStr(e.target.value.replace(/\D/g, ''))}
                placeholder="0"
                className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
              />
            </div>
          </div>

          {type === 'bank' && (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Tên ngân hàng</label>
                <input
                  type="text"
                  value={bankName}
                  onChange={(e) => setBankName(e.target.value)}
                  placeholder="MB, VCB, ACB..."
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400 block mb-1">Số tài khoản</label>
                <input
                  type="text"
                  value={accountNumber}
                  onChange={(e) => setAccountNumber(e.target.value)}
                  placeholder="xxxx xxxx"
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
                />
              </div>
            </div>
          )}

          {/* Color palette */}
          <div>
            <label className="text-xs text-slate-400 block mb-1.5">Màu đại diện</label>
            <div className="flex items-center gap-2">
              {colors.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setColor(c)}
                  style={{ backgroundColor: c }}
                  className={`w-7 h-7 rounded-full transition-transform ${
                    color === c ? 'scale-125 ring-2 ring-white' : 'opacity-80 hover:opacity-100'
                  }`}
                />
              ))}
            </div>
          </div>

          <div className="pt-2 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-medium text-slate-400 hover:text-white bg-white/5 hover:bg-white/10"
            >
              Hủy
            </button>
            <button
              type="submit"
              className="flex items-center gap-1.5 px-5 py-2 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 transition-all shadow-lg shadow-cyan-500/20"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Tạo ví</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
