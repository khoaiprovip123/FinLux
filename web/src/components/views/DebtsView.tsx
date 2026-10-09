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
    <div className="space-y-5 prism-page-enter">
      {/* Top Banner */}
      <div className="prism-glass-card p-5.5 rounded-3xl flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-extrabold text-slate-900 flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-rose-500" />
            <span>Sổ nợ & Kế hoạch tất toán (Debt Payoff)</span>
          </h2>
          <p className="text-xs text-slate-500 font-medium">
            Chiến lược Snowball (xóa nợ nhỏ trước) & Avalanche (xóa nợ lãi cao trước)
          </p>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <span className="text-[11px] text-slate-400 block font-medium">Tổng dư nợ hiện tại</span>
            <span className="text-lg font-black text-rose-600">{formatCurrency(totalDebt)}</span>
          </div>
          <button
            onClick={() => setIsAddOpen(true)}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-white bg-rose-500 hover:bg-rose-600 shadow-md shadow-rose-500/20 transition-all active:scale-[0.98]"
          >
            <Plus className="w-4 h-4" strokeWidth={2.5} />
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
              className="prism-glass-card p-5.5 rounded-3xl flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-3.5">
                  <div>
                    <h3 className="font-bold text-slate-900 text-sm">{d.name}</h3>
                    <span className="text-[11px] text-slate-400">{d.type}</span>
                  </div>
                  {d.isSettled ? (
                    <span className="flex items-center gap-1 text-[11px] font-bold text-emerald-600 bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200">
                      <CheckCircle className="w-3.5 h-3.5" />
                      Đã tất toán
                    </span>
                  ) : (
                    <span className="text-[11px] font-bold text-rose-600 bg-rose-50 px-2.5 py-0.5 rounded-full border border-rose-200">
                      Đang trả nợ
                    </span>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-2.5 my-3.5 p-3 rounded-2xl bg-white/70 border border-white/90 text-xs shadow-2xs">
                  <div>
                    <span className="text-[11px] text-slate-400 block font-medium">Dư nợ còn lại</span>
                    <span className="text-sm font-black text-slate-900">
                      {formatCurrency(d.remainingBalance)}
                    </span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400 block font-medium">Nợ gốc</span>
                    <span className="text-sm font-medium text-slate-600">
                      {formatCurrency(d.initialAmount)}
                    </span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400 block font-medium flex items-center gap-1">
                      <Percent className="w-3 h-3 text-amber-500" />
                      Lãi suất năm (APR)
                    </span>
                    <span className="font-bold text-amber-600">{d.interestRateYearly}%</span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400 block font-medium flex items-center gap-1">
                      <Calendar className="w-3 h-3 text-blue-500" />
                      Hạn trả hàng tháng
                    </span>
                    <span className="font-bold text-slate-700">
                      {d.dueDate ? `Ngày ${d.dueDate}` : 'Linh hoạt'}
                    </span>
                  </div>
                </div>

                {/* Progress */}
                <div className="space-y-1.5 my-3.5">
                  <div className="flex justify-between text-xs text-slate-500">
                    <span>Tiến độ thanh toán</span>
                    <span className="font-black text-emerald-600">{percentPaid}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 overflow-hidden">
                    <div
                      className="h-full bg-emerald-500 rounded-full transition-all duration-300"
                      style={{ width: `${percentPaid}%` }}
                    />
                  </div>
                </div>
              </div>

              {/* Payment Section */}
              {!d.isSettled && (
                <div className="pt-3 border-t border-slate-100">
                  {isPaying ? (
                    <div className="space-y-2.5">
                      <div className="grid grid-cols-2 gap-2">
                        <input
                          type="text"
                          value={new Intl.NumberFormat('vi-VN').format(
                            parseInt(payAmountStr.replace(/\D/g, '') || '0', 10)
                          )}
                          onChange={(e) => setPayAmountStr(e.target.value.replace(/\D/g, ''))}
                          placeholder="Số tiền trả"
                          className="bg-white border border-slate-200 rounded-xl px-2.5 py-1.5 text-xs text-slate-900 outline-none focus:ring-2 focus:ring-blue-400/30"
                        />
                        <select
                          value={payWalletId}
                          onChange={(e) => setPayWalletId(e.target.value)}
                          className="bg-white border border-slate-200 rounded-xl px-2 py-1.5 text-xs text-slate-700 outline-none focus:ring-2 focus:ring-blue-400/30"
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
                          className="px-3 py-1 rounded-xl text-xs bg-slate-100 text-slate-600 hover:bg-slate-200"
                        >
                          Hủy
                        </button>
                        <button
                          onClick={() => handlePay(d.id)}
                          className="px-3.5 py-1 rounded-xl text-xs font-bold bg-emerald-500 text-white shadow-xs hover:bg-emerald-600"
                        >
                          Xác nhận trả nợ
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] text-slate-400">
                        Tối thiểu: {formatCurrency(d.minPaymentMonthly)}
                      </span>
                      <button
                        onClick={() => {
                          setPayingDebtId(d.id);
                          setPayAmountStr(d.minPaymentMonthly.toString());
                        }}
                        className="px-3.5 py-1.5 rounded-xl text-xs font-bold text-white bg-rose-500 hover:bg-rose-600 shadow-xs transition-colors flex items-center gap-1"
                      >
                        <DollarSign className="w-3.5 h-3.5" />
                        <span>Thanh toán</span>
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
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-md">
          <div className="w-full max-w-md bg-white/95 rounded-3xl border border-white p-6 shadow-2xl prism-page-enter">
            <h3 className="font-extrabold text-slate-900 text-base mb-4">Thêm khoản nợ mới</h3>
            <form onSubmit={handleCreateDebt} className="space-y-3.5 text-xs">
              <div>
                <label className="text-slate-600 block mb-1 font-medium">Tên khoản nợ</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Thẻ tín dụng VIB, Vay mua xe..."
                  required
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-slate-900 outline-none focus:ring-2 focus:ring-blue-400/30"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-slate-600 block mb-1 font-medium">Loại nợ</label>
                  <select
                    value={type}
                    onChange={(e) => setType(e.target.value as 'CREDIT_CARD' | 'BANK_LOAN' | 'INSTALLMENT' | 'PERSONAL_LOAN')}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-slate-900 outline-none focus:ring-2 focus:ring-blue-400/30"
                  >
                    <option value="CREDIT_CARD">Thẻ tín dụng</option>
                    <option value="BANK_LOAN">Vay ngân hàng</option>
                    <option value="INSTALLMENT">Trả góp</option>
                    <option value="PERSONAL_LOAN">Vay người thân</option>
                  </select>
                </div>
                <div>
                  <label className="text-slate-600 block mb-1 font-medium">Số nợ gốc (VNĐ)</label>
                  <input
                    type="text"
                    value={new Intl.NumberFormat('vi-VN').format(
                      parseInt(amountStr.replace(/\D/g, '') || '0', 10)
                    )}
                    onChange={(e) => setAmountStr(e.target.value.replace(/\D/g, ''))}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-slate-900 outline-none focus:ring-2 focus:ring-blue-400/30 font-bold"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-slate-600 block mb-1 font-medium">Lãi suất (% APR)</label>
                  <input
                    type="number"
                    step="0.1"
                    value={apr}
                    onChange={(e) => setApr(e.target.value)}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-slate-900 outline-none focus:ring-2 focus:ring-blue-400/30"
                  />
                </div>
                <div>
                  <label className="text-slate-600 block mb-1 font-medium">Trả tối thiểu / tháng</label>
                  <input
                    type="text"
                    value={new Intl.NumberFormat('vi-VN').format(
                      parseInt(minPayment.replace(/\D/g, '') || '0', 10)
                    )}
                    onChange={(e) => setMinPayment(e.target.value.replace(/\D/g, ''))}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-slate-900 outline-none focus:ring-2 focus:ring-blue-400/30"
                  />
                </div>
                <div>
                  <label className="text-slate-600 block mb-1 font-medium">Ngày đến hạn</label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    value={dueDate}
                    onChange={(e) => setDueDate(e.target.value)}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-slate-900 outline-none focus:ring-2 focus:ring-blue-400/30"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2.5 pt-3">
                <button
                  type="button"
                  onClick={() => setIsAddOpen(false)}
                  className="px-4 py-2 rounded-xl bg-slate-100 text-slate-600 font-medium hover:bg-slate-200"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl font-bold bg-rose-500 text-white shadow-md shadow-rose-500/20 hover:bg-rose-600"
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
