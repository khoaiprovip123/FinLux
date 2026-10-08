'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency } from '@/lib/formatters';
import { ShieldAlert, DollarSign, Plus, CheckCircle, Percent, Calendar } from 'lucide-react';

export default function DebtsView() {
  const { debts, wallets, addDebt, makeDebtPayment } = useFinance();

  const [payingDebtId, setPayingDebtId] = useState<string | null>(null);
  const [payAmountStr, setPayAmountStr] = useState<string>('1000000');
  const [payWalletId, setPayWalletId] = useState<string>(wallets[0]?.id || '');

  // Add modal state
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [name, setName] = useState('');
  const [type, setType] = useState<'CREDIT_CARD' | 'BANK_LOAN' | 'INSTALLMENT' | 'PERSONAL_LOAN'>('CREDIT_CARD');
  const [amountStr, setAmountStr] = useState('10000000');
  const [apr, setApr] = useState('18.5');
  const [minPayment, setMinPayment] = useState('1000000');
  const [dueDate, setDueDate] = useState('20');

  const handlePay = (debtId: string) => {
    const amount = parseInt(payAmountStr.replace(/\D/g, '') || '0', 10);
    if (amount > 0 && payWalletId) {
      makeDebtPayment(debtId, payWalletId, amount);
      setPayingDebtId(null);
    }
  };

  const handleCreateDebt = (e: React.FormEvent) => {
    e.preventDefault();
    const amount = parseInt(amountStr.replace(/\D/g, '') || '0', 10);
    const minPay = parseInt(minPayment.replace(/\D/g, '') || '0', 10);
    const aprNum = parseFloat(apr) || 0;
    const due = parseInt(dueDate, 10) || null;

    if (!name.trim() || amount <= 0) return;

    addDebt({
      name: name.trim(),
      type,
      initialAmount: amount,
      remainingBalance: amount,
      interestRateYearly: aprNum,
      minPaymentMonthly: minPay,
      dueDate: due,
    });

    setIsAddOpen(false);
    setName('');
  };

  const totalDebt = debts
    .filter((d) => !d.isSettled)
    .reduce((s, d) => s + d.remainingBalance, 0);

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="liquid-glass rounded-2xl p-6 border border-white/10 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-rose-400" />
            <span>Sổ nợ & Kế hoạch tất toán (Debt Payoff)</span>
          </h2>
          <p className="text-xs text-slate-400">
            Hỗ trợ chiến lược Snowball (tất toán nợ nhỏ trước) & Avalanche (tất toán lãi cao trước)
          </p>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <span className="text-xs text-slate-400 block">Tổng dư nợ hiện tại</span>
            <span className="text-xl font-black text-rose-400">{formatCurrency(totalDebt)}</span>
          </div>
          <button
            onClick={() => setIsAddOpen(true)}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 shadow-lg shadow-rose-600/20"
          >
            <Plus className="w-4 h-4" />
            <span>Thêm khoản nợ</span>
          </button>
        </div>
      </div>

      {/* Debt List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {debts.map((d) => {
          const percentPaid = Math.min(
            100,
            Math.round(((d.initialAmount - d.remainingBalance) / d.initialAmount) * 100)
          );
          const isPaying = payingDebtId === d.id;

          return (
            <div
              key={d.id}
              className={`liquid-glass rounded-2xl p-5 border ${
                d.isSettled ? 'border-emerald-500/30' : 'border-rose-500/20'
              } flex flex-col justify-between`}
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <h3 className="font-bold text-white text-base">{d.name}</h3>
                    <span className="text-[11px] text-slate-400">{d.type}</span>
                  </div>
                  {d.isSettled ? (
                    <span className="flex items-center gap-1 text-xs font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20">
                      <CheckCircle className="w-3.5 h-3.5" />
                      Đã tất toán
                    </span>
                  ) : (
                    <span className="text-xs font-bold text-rose-400 bg-rose-500/10 px-2.5 py-0.5 rounded-full border border-rose-500/20">
                      Đang trả nợ
                    </span>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-3 my-4 p-3 rounded-xl bg-black/30 border border-white/5 text-xs">
                  <div>
                    <span className="text-[11px] text-slate-400 block">Dư nợ còn lại</span>
                    <span className="text-base font-extrabold text-white">
                      {formatCurrency(d.remainingBalance)}
                    </span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400 block">Nợ gốc ban đầu</span>
                    <span className="text-base font-medium text-slate-400">
                      {formatCurrency(d.initialAmount)}
                    </span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400 block flex items-center gap-1">
                      <Percent className="w-3 h-3 text-amber-400" />
                      Lãi suất năm (APR)
                    </span>
                    <span className="font-bold text-amber-400">{d.interestRateYearly}%</span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400 block flex items-center gap-1">
                      <Calendar className="w-3 h-3 text-cyan-400" />
                      Hạn trả hàng tháng
                    </span>
                    <span className="font-bold text-slate-300">
                      {d.dueDate ? `Ngày ${d.dueDate}` : 'Linh hoạt'}
                    </span>
                  </div>
                </div>

                {/* Progress */}
                <div className="space-y-1 my-3">
                  <div className="flex justify-between text-xs text-slate-400">
                    <span>Tiến độ thanh toán</span>
                    <span className="font-bold text-emerald-400">{percentPaid}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                    <div
                      className="h-full bg-emerald-500 rounded-full transition-all duration-300"
                      style={{ width: `${percentPaid}%` }}
                    />
                  </div>
                </div>
              </div>

              {/* Action / Payment Form */}
              {!d.isSettled && (
                <div className="pt-3 border-t border-white/10">
                  {isPaying ? (
                    <div className="space-y-2">
                      <div className="grid grid-cols-2 gap-2">
                        <input
                          type="text"
                          value={new Intl.NumberFormat('vi-VN').format(
                            parseInt(payAmountStr.replace(/\D/g, '') || '0', 10)
                          )}
                          onChange={(e) => setPayAmountStr(e.target.value.replace(/\D/g, ''))}
                          placeholder="Số tiền trả"
                          className="bg-[#0d1b33] border border-white/20 rounded-lg px-2.5 py-1.5 text-xs text-white outline-none"
                        />
                        <select
                          value={payWalletId}
                          onChange={(e) => setPayWalletId(e.target.value)}
                          className="bg-[#0d1b33] border border-white/20 rounded-lg px-2 py-1.5 text-xs text-slate-200 outline-none"
                        >
                          {wallets.map((w) => (
                            <option key={w.id} value={w.id}>
                              {w.name}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => setPayingDebtId(null)}
                          className="px-3 py-1 rounded-lg text-xs bg-white/10 text-slate-300"
                        >
                          Hủy
                        </button>
                        <button
                          onClick={() => handlePay(d.id)}
                          className="px-3 py-1 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white"
                        >
                          Xác nhận trả nợ
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] text-slate-400">
                        Trả tối thiểu: {formatCurrency(d.minPaymentMonthly)}
                      </span>
                      <button
                        onClick={() => {
                          setPayingDebtId(d.id);
                          setPayAmountStr(d.minPaymentMonthly.toString());
                        }}
                        className="px-3 py-1.5 rounded-xl text-xs font-semibold text-white bg-rose-600 hover:bg-rose-500 transition-colors flex items-center gap-1"
                      >
                        <DollarSign className="w-3.5 h-3.5" />
                        <span>Thanh toán nợ</span>
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Add Debt Modal */}
      {isAddOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md">
          <div className="w-full max-w-md liquid-glass rounded-2xl border border-white/15 p-6 shadow-2xl">
            <h3 className="font-bold text-white text-base mb-4">Thêm khoản nợ mới</h3>
            <form onSubmit={handleCreateDebt} className="space-y-3 text-xs">
              <div>
                <label className="text-slate-400 block mb-1">Tên khoản nợ</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Thẻ tín dụng VIB, Vay mua xe..."
                  required
                  className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="text-slate-400 block mb-1">Loại nợ</label>
                  <select
                    value={type}
                    onChange={(e) => setType(e.target.value as any)}
                    className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                  >
                    <option value="CREDIT_CARD">Thẻ tín dụng</option>
                    <option value="BANK_LOAN">Vay ngân hàng</option>
                    <option value="INSTALLMENT">Trả góp</option>
                    <option value="PERSONAL_LOAN">Vay người thân</option>
                  </select>
                </div>
                <div>
                  <label className="text-slate-400 block mb-1">Số nợ ban đầu (VNĐ)</label>
                  <input
                    type="text"
                    value={new Intl.NumberFormat('vi-VN').format(
                      parseInt(amountStr.replace(/\D/g, '') || '0', 10)
                    )}
                    onChange={(e) => setAmountStr(e.target.value.replace(/\D/g, ''))}
                    className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="text-slate-400 block mb-1">Lãi suất năm (% APR)</label>
                  <input
                    type="number"
                    step="0.1"
                    value={apr}
                    onChange={(e) => setApr(e.target.value)}
                    className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                  />
                </div>
                <div>
                  <label className="text-slate-400 block mb-1">Trả tối thiểu / tháng</label>
                  <input
                    type="text"
                    value={new Intl.NumberFormat('vi-VN').format(
                      parseInt(minPayment.replace(/\D/g, '') || '0', 10)
                    )}
                    onChange={(e) => setMinPayment(e.target.value.replace(/\D/g, ''))}
                    className="w-full bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-white outline-none"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setIsAddOpen(false)}
                  className="px-4 py-2 rounded-xl bg-white/10 text-slate-300"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl font-semibold bg-rose-600 hover:bg-rose-500 text-white"
                >
                  Lưu khoản nợ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
