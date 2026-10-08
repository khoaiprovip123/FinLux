'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency } from '@/lib/formatters';
import { Plus, Trash2, CheckCircle2, Shield, CreditCard, Landmark, Banknote } from 'lucide-react';

interface WalletsViewProps {
  onOpenWalletModal: () => void;
}

export default function WalletsView({ onOpenWalletModal }: WalletsViewProps) {
  const { wallets, updateWallet, deleteWallet } = useFinance();

  const getWalletIcon = (type: string) => {
    switch (type) {
      case 'bank':
        return Landmark;
      case 'card':
        return CreditCard;
      case 'cash':
        return Banknote;
      default:
        return Shield;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-white">Danh sách Ví & Tài khoản</h2>
          <p className="text-xs text-slate-400">
            Quản lý số dư tiền mặt, tài khoản ngân hàng và thẻ tín dụng
          </p>
        </div>
        <button
          onClick={onOpenWalletModal}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 shadow-md shadow-cyan-500/20"
        >
          <Plus className="w-4 h-4" />
          <span>Thêm ví mới</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {wallets.map((w) => {
          const Icon = getWalletIcon(w.type);
          return (
            <div
              key={w.id}
              className="liquid-glass rounded-2xl p-5 border border-white/10 relative overflow-hidden flex flex-col justify-between"
            >
              <div
                className="absolute top-0 right-0 w-32 h-32 rounded-full blur-3xl opacity-20 pointer-events-none"
                style={{ backgroundColor: w.color }}
              />

              <div>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2.5">
                    <div
                      className="w-9 h-9 rounded-xl flex items-center justify-center text-white"
                      style={{ backgroundColor: `${w.color}25`, border: `1px solid ${w.color}40` }}
                    >
                      <Icon className="w-4 h-4" style={{ color: w.color }} />
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-sm">{w.name}</h3>
                      <span className="text-[11px] text-slate-400 capitalize">
                        {w.bankName || w.type}
                      </span>
                    </div>
                  </div>

                  {w.isDefault ? (
                    <span className="flex items-center gap-1 text-[11px] text-cyan-400 bg-cyan-500/10 px-2.5 py-0.5 rounded-full font-medium border border-cyan-500/20">
                      <CheckCircle2 className="w-3 h-3" />
                      Mặc định
                    </span>
                  ) : (
                    <button
                      onClick={() => {
                        wallets.forEach((item) => {
                          updateWallet(item.id, { isDefault: item.id === w.id });
                        });
                      }}
                      className="text-[11px] text-slate-400 hover:text-cyan-400 px-2 py-0.5 rounded hover:bg-white/5 transition-colors"
                    >
                      Đặt mặc định
                    </button>
                  )}
                </div>

                <div className="mt-4 mb-3">
                  <span className="text-[11px] text-slate-400 block mb-0.5">Số dư khả dụng</span>
                  <div
                    className={`text-2xl font-black ${
                      w.balance < 0 ? 'text-rose-400' : 'text-white'
                    }`}
                  >
                    {formatCurrency(w.balance)}
                  </div>
                </div>

                {w.accountNumber && (
                  <div className="text-[11px] text-slate-400 font-mono bg-white/5 px-2.5 py-1.5 rounded-lg inline-block">
                    STK: ****{w.accountNumber.slice(-4)}
                  </div>
                )}
              </div>

              <div className="mt-5 pt-3 border-t border-white/10 flex items-center justify-between text-xs">
                <span className="text-[11px] text-slate-500">Mã: {w.id}</span>
                {!w.isDefault && (
                  <button
                    onClick={() => deleteWallet(w.id)}
                    className="text-slate-500 hover:text-rose-400 p-1.5 rounded-lg hover:bg-rose-500/10 transition-colors"
                    title="Xóa ví"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
