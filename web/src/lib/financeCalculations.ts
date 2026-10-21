import type { Budget, Transaction, Wallet } from '@/types/finance';

export const FINANCE_CONFIG = {
  defaultTimeZone: 'Asia/Ho_Chi_Minh',
  clockRefreshMs: 60_000,
  budgetWarningRatio: 0.8,
  budgetLimitRatio: 1,
  maxVndAmount: 999_999_999_999_999,
} as const;

export type PeriodKey = 'six' | 'three' | 'month' | 'previous' | 'all';
export interface ReportPeriod { start: string; end: string; name: string }

export function financeDate(value: Date, timeZone: string = FINANCE_CONFIG.defaultTimeZone): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone, year: 'numeric', month: '2-digit', day: '2-digit',
  }).formatToParts(value);
  const part = (name: string) => parts.find((p) => p.type === name)?.value;
  return `${part('year')}-${part('month')}-${part('day')}`;
}

export function validFinanceTimeZone(value: unknown): string {
  if (typeof value === 'string') {
    try { financeDate(new Date(), value); return value; } catch { /* Use the configured default. */ }
  }
  return FINANCE_CONFIG.defaultTimeZone;
}

export function shiftDate(value: string, days: number): string {
  if (!value) return '';
  const date = new Date(`${value}T12:00:00Z`);
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

function monthStart(today: string, offset: number): string {
  const date = new Date(`${today.slice(0, 7)}-01T12:00:00Z`);
  date.setUTCMonth(date.getUTCMonth() + offset);
  return date.toISOString().slice(0, 10);
}

function nextAnchoredMonth(start: string): string {
  const first = monthStart(start, 1);
  const last = shiftDate(monthStart(start, 2), -1);
  return `${first.slice(0, 8)}${String(Math.min(Number(start.slice(8)), Number(last.slice(8)))).padStart(2, '0')}`;
}

function validDateKey(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const date = new Date(`${value}T12:00:00Z`);
  return Number.isFinite(date.getTime()) && date.toISOString().slice(0, 10) === value;
}

/** The web form is date-only: persist local noon in the finance timezone, never the browser timezone. */
export function financeDayInstant(day: string, timeZone: string): Date {
  if (!validDateKey(day)) throw new Error('Ngày giao dịch không hợp lệ.');
  const desired = Date.parse(`${day}T12:00:00Z`);
  let candidate = desired;
  const formatter = new Intl.DateTimeFormat('en-CA', {
    timeZone, year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23',
  });
  // The second pass accounts for an offset change across a daylight-saving boundary.
  for (let pass = 0; pass < 2; pass++) {
    const parts = formatter.formatToParts(new Date(candidate));
    const value = (name: string) => Number(parts.find((part) => part.type === name)?.value);
    const represented = Date.UTC(value('year'), value('month') - 1, value('day'), value('hour'), value('minute'), value('second'));
    candidate += desired - represented;
  }
  const result = new Date(candidate);
  if (financeDate(result, timeZone) !== day) throw new Error('Ngày không tồn tại trong múi giờ tài chính.');
  return result;
}

export function reportPeriods(today: string, recordDates: string[] = []): Record<PeriodKey, ReportPeriod> {
  if (!today) return {
    six: { start: '', end: '', name: '6 tháng gần đây' },
    three: { start: '', end: '', name: '3 tháng gần đây' },
    month: { start: '', end: '', name: 'Tháng này' },
    previous: { start: '', end: '', name: 'Tháng trước' },
    all: { start: '', end: '', name: 'Tất cả' },
  };
  const dates = recordDates.filter(validDateKey);
  const first = dates.reduce((min, date) => date < min ? date : min, today);
  const last = dates.reduce((max, date) => date > max ? date : max, today);
  return {
    six: { start: monthStart(today, -5), end: today, name: '6 tháng gần đây' },
    three: { start: monthStart(today, -2), end: today, name: '3 tháng gần đây' },
    month: { start: monthStart(today, 0), end: today, name: 'Tháng này' },
    previous: { start: monthStart(today, -1), end: shiftDate(monthStart(today, 0), -1), name: 'Tháng trước' },
    all: { start: first, end: last, name: 'Tất cả' },
  };
}

export function monthsInPeriod(period: ReportPeriod): string[] {
  if (!period.start || !period.end) return [];
  const months: string[] = [];
  for (let date = monthStart(period.start, 0); date <= period.end; date = monthStart(date, 1)) {
    months.push(date.slice(0, 7));
  }
  return months;
}

/** Start from the persisted balance and reverse only entries after the requested date. */
export function walletBalanceAt(wallet: Wallet, transactions: Transaction[], asOfDate: string): number {
  const ids = new Set(transactions.map((tx) => tx.id));
  return transactions.reduce((balance, tx) => {
    if (tx.date.slice(0, 10) <= asOfDate) return balance;
    if (tx.walletId === wallet.id) {
      const incoming = tx.type === 'income' || tx.type === 'transfer_in';
      return balance - (incoming ? tx.amount : -tx.amount);
    }
    // Legacy records stored only the outgoing half; paired transfers use their own incoming entry.
    if (tx.type === 'transfer_out' && tx.relatedWalletId === wallet.id &&
      !ids.has(tx.id.replace(/_out$/, '') + '_in')) return balance - tx.amount;
    return balance;
  }, wallet.balance);
}

export function netWalletAssets(wallets: Wallet[]): number {
  return wallets.reduce((sum, wallet) => sum + (wallet.type === 'card' ? Math.min(0, wallet.balance) : wallet.balance), 0);
}

export interface BudgetWindow { start: string; end: string }

export function budgetWindow(budget: Budget, timeZone: string): BudgetWindow | null {
  if (budget.periodStart != null && budget.periodEndExclusive != null && budget.periodEndExclusive > budget.periodStart &&
    Number.isFinite(new Date(budget.periodStart).getTime()) && Number.isFinite(new Date(budget.periodEndExclusive).getTime())) {
    return { start: financeDate(new Date(budget.periodStart), timeZone), end: shiftDate(financeDate(new Date(budget.periodEndExclusive), timeZone), -1) };
  }
  const key = budget.periodKey || budget.id.slice(budget.categoryId.length + 1);
  if (key.startsWith('salary:') && validDateKey(key.slice(7))) {
    const start = key.slice(7);
    return { start, end: shiftDate(nextAnchoredMonth(start), -1) };
  }
  const month = /^month:\d{4}-\d{2}$/.test(key) ? key.slice(6) : budget.month;
  if (validDateKey(`${month}-01`)) {
    const start = `${month}-01`;
    return { start, end: shiftDate(monthStart(start, 1), -1) };
  }
  return null;
}

/** One row per actual budget window, even when a report spans several months. */
export function budgetUsage(budgets: Budget[], transactions: Transaction[], period: ReportPeriod, timeZone: string) {
  const unique = new Map<string, { budget: Budget; window: BudgetWindow }>();
  for (const budget of budgets) {
    const window = budgetWindow(budget, timeZone);
    if (!window || window.start > period.end || window.end < period.start) continue;
    const key = `${budget.categoryId}:${window.start}:${window.end}`;
    const existing = unique.get(key);
    if (!existing || budget.id === `${budget.categoryId}_${budget.periodKey}`) unique.set(key, { budget, window });
  }
  return [...unique.values()].map(({ budget, window }) => {
    const start = window.start > period.start ? window.start : period.start;
    const end = window.end < period.end ? window.end : period.end;
    const spent = transactions.filter((tx) => tx.type === 'expense' && tx.categoryId === budget.categoryId &&
      tx.date.slice(0, 10) >= start && tx.date.slice(0, 10) <= end).reduce((sum, tx) => sum + tx.amount, 0);
    return { id: budget.id, categoryId: budget.categoryId, start: window.start, end: window.end, spent, limit: budget.limitAmount };
  });
}
