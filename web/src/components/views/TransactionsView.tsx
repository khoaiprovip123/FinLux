'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { formatCurrency, formatDate } from '@/lib/formatters';
import {
  Search,
  Filter,
  Plus,
  Trash2,
  ArrowUpRight,
  ArrowDownRight,
  RefreshCw,
} from 'lucide-react';

interface TransactionsViewProps {
  onOpenAddTx: () => void;
}

export default function TransactionsView({ onOpenAddTx }: TransactionsViewProps) {
  const { transactions, wallets, categories, deleteTransaction } = useFinance();

  const [search, setSearch] = useState('');
  const [filterType, setFilterType] = useState<string>('all');
  const [filterWallet, setFilterWallet] = useState<string>('all');

  const filtered = transactions.filter((tx) => {
    if (filterType !== 'all' && tx.type !== filterType) return false;
    if (filterWallet !== 'all' && tx.walletId !== filterWallet && tx.relatedWalletId !== filterWallet)
      return false;
    if (search.trim()) {
      const q = search.toLowerCase();
      const matchNote = tx.note.toLowerCase().includes(q);
      const cat = categories.find((c) => c.id === tx.categoryId);
      const matchCat = cat?.name.toLowerCase().includes(q);
      if (!matchNote && !matchCat) return false;
    }
    return true;
  });

  const getCategory = (catId?: string | null) => categories.find((c) => c.id === catId);
  const getWallet = (wId: string) => wallets.find((w) => w.id === wId);

  return (
    <div className="space-y-6">
      {/* Controls Bar */}
      <div className="liquid-glass rounded-2xl p-4 border border-white/10 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3 flex-1 min-w-[240px]">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Tìm theo ghi chú, danh mục..."
              className="w-full bg-[#0d1b33] border border-white/10 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-200 placeholder:text-slate-500 outline-none"
            />
          </div>

          {/* Filter Type */}
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
          >
            <option value="all">Tất cả loại</option>
            <option value="expense">Chi tiêu</option>
            <option value="income">Thu nhập</option>
            <option value="transfer_out">Chuyển ví</option>
          </select>

          {/* Filter Wallet */}
          <select
            value={filterWallet}
            onChange={(e) => setFilterWallet(e.target.value)}
            className="bg-[#0d1b33] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 outline-none"
          >
            <option value="all">Tất cả ví</option>
            {wallets.map((w) => (
              <option key={w.id} value={w.id}>
                {w.name}
              </option>
            ))}
          </select>
        </div>

        <button
          onClick={onOpenAddTx}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 shadow-md shadow-cyan-500/20"
        >
          <Plus className="w-4 h-4" />
          <span>Thêm giao dịch</span>
        </button>
      </div>

      {/* Transaction Table */}
      <div className="liquid-glass rounded-2xl p-5 border border-white/10">
        <div className="flex items-center justify-between mb-4">
          <span className="text-xs font-semibold text-slate-400">
            Tổng cộng: <strong className="text-white">{filtered.length}</strong> giao dịch
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="text-[11px] uppercase tracking-wider text-slate-500 border-b border-white/10">
              <tr>
                <th className="pb-3 font-semibold">Loại</th>
                <th className="pb-3 font-semibold">Ghi chú & Danh mục</th>
                <th className="pb-3 font-semibold">Ví tài khoản</th>
                <th className="pb-3 font-semibold">Thời gian</th>
                <th className="pb-3 font-semibold text-right">Số tiền</th>
                <th className="pb-3 font-semibold text-right">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filtered.map((tx) => {
                const cat = getCategory(tx.categoryId);
                const w = getWallet(tx.walletId);
                const relW = tx.relatedWalletId ? getWallet(tx.relatedWalletId) : null;

                return (
                  <tr key={tx.id} className="hover:bg-white/5 transition-colors">
                    <td className="py-3.5">
                      <div
                        className={`w-7 h-7 rounded-lg flex items-center justify-center ${
                          tx.type === 'income'
                            ? 'bg-emerald-500/15 text-emerald-400'
                            : tx.type === 'expense'
                            ? 'bg-rose-500/15 text-rose-400'
                            : 'bg-cyan-500/15 text-cyan-400'
                        }`}
                      >
                        {tx.type === 'income' ? (
                          <ArrowUpRight className="w-4 h-4" />
                        ) : tx.type === 'expense' ? (
                          <ArrowDownRight className="w-4 h-4" />
                        ) : (
                          <RefreshCw className="w-4 h-4" />
                        )}
                      </div>
                    </td>

                    <td className="py-3.5">
                      <div className="font-semibold text-white">{tx.note}</div>
                      <div className="text-[11px] text-slate-400">
                        {tx.type === 'transfer_out' ? 'Chuyển tiền giữa ví' : cat?.name || 'Khác'}
                      </div>
                    </td>

                    <td className="py-3.5 text-slate-300">
                      {tx.type === 'transfer_out' ? (
                        <span>
                          {w?.name} → {relW?.name}
                        </span>
                      ) : (
                        <span>{w?.name}</span>
                      )}
                    </td>

                    <td className="py-3.5 text-slate-400 font-mono text-[11px]">
                      {formatDate(tx.date)}
                    </td>

                    <td className="py-3.5 text-right font-black text-sm">
                      <span
                        className={
                          tx.type === 'income'
                            ? 'text-emerald-400'
                            : tx.type === 'expense'
                            ? 'text-rose-400'
                            : 'text-cyan-400'
                        }
                      >
                        {tx.type === 'income' ? '+' : tx.type === 'expense' ? '-' : ''}
                        {formatCurrency(tx.amount)}
                      </span>
                    </td>

                    <td className="py-3.5 text-right">
                      <button
                        onClick={() => deleteTransaction(tx.id)}
                        title="Xóa và hoàn trả số dư"
                        className="p-1.5 rounded-lg text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
