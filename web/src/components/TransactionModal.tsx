'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { TransactionType } from '@/types/finance';
import { X, ArrowRight, Calendar, FileText, Check } from 'lucide-react';

interface TransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function TransactionModal({ isOpen, onClose }: TransactionModalProps) {
  const { wallets, categories, addTransaction } = useFinance();

  const [type, setType] = useState<TransactionType>('expense');
  const [amountStr, setAmountStr] = useState<string>('50000');
  const [walletId, setWalletId] = useState<string>(wallets[0]?.id || '');
  const [relatedWalletId, setRelatedWalletId] = useState<string>(wallets[1]?.id || '');
  const [categoryId, setCategoryId] = useState<string>('cat_food');
  const [date, setDate] = useState<string>('2026-10-08T12:00');
  const [note, setNote] = useState<string>('');

  React.useEffect(() => {
    setDate(new Date().toISOString().slice(0, 16));
  }, []);

  if (!isOpen) return null;

  const filteredCategories = categories.filter((c) =>
    type === 'expense' ? c.type === 'expense' : c.type === 'income'
  );

  const numericAmount = parseInt(amountStr.replace(/\D/g, '') || '0', 10);

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value.replace(/\D/g, '');
    setAmountStr(raw);
  };

  const handleQuickAdd = (added: number) => {
    setAmountStr((prev) => (parseInt(prev.replace(/\D/g, '') || '0', 10) + added).toString());
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (numericAmount <= 0 || !walletId) return;

    addTransaction({
      type,
      amount: numericAmount,
      walletId,
      relatedWalletId: type === 'transfer_out' ? relatedWalletId : null,
      categoryId: type === 'transfer_out' ? null : categoryId,
      date: new Date(date).toISOString(),
      note: note.trim() || (type === 'transfer_out' ? 'Chuyển tiền giữa các ví' : 'Giao dịch'),
    });

    onClose();
  };

  const formatDisplayAmount = (num: number) => {
    return new Intl.NumberFormat('vi-VN').format(num);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-lg liquid-glass rounded-2xl border border-white/15 p-6 shadow-2xl relative overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-white/10">
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <span>Ghi chép giao dịch</span>
          </h2>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-lg bg-white/5 hover:bg-white/10 flex items-center justify-center text-slate-400 hover:text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {/* Type Selector (Tabs) */}
          <div className="grid grid-cols-3 gap-1 p-1 bg-black/40 rounded-xl border border-white/5">
            <button
              type="button"
              onClick={() => setType('expense')}
              className={`py-2 text-xs font-semibold rounded-lg transition-all ${
                type === 'expense'
                  ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Chi tiêu
            </button>
            <button
              type="button"
              onClick={() => setType('income')}
              className={`py-2 text-xs font-semibold rounded-lg transition-all ${
                type === 'income'
                  ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Thu nhập
            </button>
            <button
              type="button"
              onClick={() => setType('transfer_out')}
              className={`py-2 text-xs font-semibold rounded-lg transition-all ${
                type === 'transfer_out'
                  ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/30'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Chuyển ví
            </button>
          </div>

          {/* Amount Input with FinLux Auto-formatting */}
          <div className="p-4 rounded-xl bg-black/30 border border-white/10 text-center">
            <label className="text-[11px] text-slate-400 block mb-1">Số tiền (VNĐ)</label>
            <div className="flex items-center justify-center gap-2">
              <input
                type="text"
                value={numericAmount > 0 ? formatDisplayAmount(numericAmount) : ''}
                onChange={handleAmountChange}
                placeholder="0"
                autoFocus
                className={`w-full text-center text-2xl md:text-3xl font-black bg-transparent outline-none tracking-tight ${
                  type === 'expense'
                    ? 'text-rose-400'
                    : type === 'income'
                    ? 'text-emerald-400'
                    : 'text-cyan-400'
                }`}
              />
              <span className="text-xl font-bold text-slate-400">₫</span>
            </div>

            {/* Quick chips */}
            <div className="flex items-center justify-center gap-1.5 mt-3 flex-wrap">
              {[50000, 100000, 200000, 500000, 1000000].map((chip) => (
                <button
                  key={chip}
                  type="button"
                  onClick={() => handleQuickAdd(chip)}
                  className="text-[11px] px-2.5 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-slate-300 border border-white/5 transition-all"
                >
                  +{formatDisplayAmount(chip)}
                </button>
              ))}
            </div>
          </div>

          {/* Wallet Selector */}
          {type === 'transfer_out' ? (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Từ ví nguồn</label>
                <select
                  value={walletId}
                  onChange={(e) => setWalletId(e.target.value)}
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
                >
                  {wallets.map((w) => (
                    <option key={w.id} value={w.id}>
                      {w.name} ({formatDisplayAmount(w.balance)} ₫)
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-xs text-slate-400 block mb-1">Đến ví đích</label>
                <select
                  value={relatedWalletId}
                  onChange={(e) => setRelatedWalletId(e.target.value)}
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
                >
                  {wallets
                    .filter((w) => w.id !== walletId)
                    .map((w) => (
                      <option key={w.id} value={w.id}>
                        {w.name} ({formatDisplayAmount(w.balance)} ₫)
                      </option>
                    ))}
                </select>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Ví tài khoản</label>
                <select
                  value={walletId}
                  onChange={(e) => setWalletId(e.target.value)}
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
                >
                  {wallets.map((w) => (
                    <option key={w.id} value={w.id}>
                      {w.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Danh mục</label>
                <select
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
                >
                  {filteredCategories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )}

          {/* Date & Time Picker */}
          <div>
            <label className="text-xs text-slate-400 mb-1 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-cyan-400" />
              <span>Thời gian giao dịch</span>
            </label>
            <input
              type="datetime-local"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
            />
          </div>

          {/* Note Input */}
          <div>
            <label className="text-xs text-slate-400 mb-1 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-slate-400" />
              <span>Ghi chú</span>
            </label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Ví dụ: Ăn trưa Highlands, mua sách..."
              className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 placeholder:text-slate-500 outline-none"
            />
          </div>

          {/* Action buttons */}
          <div className="pt-2 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-medium text-slate-400 hover:text-white bg-white/5 hover:bg-white/10 transition-colors"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={numericAmount <= 0}
              className="flex items-center gap-1.5 px-5 py-2 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 disabled:opacity-50 transition-all shadow-lg shadow-cyan-500/20"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Lưu giao dịch</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
