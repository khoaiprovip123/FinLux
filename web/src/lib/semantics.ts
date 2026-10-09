import { Transaction, Wallet } from '@/types/finance';

/**
 * Single Source of Truth cho toàn bộ Category ID chuẩn của hệ thống FinLux
 * Đồng bộ 100% với SystemCategories.kt của Mobile App Android.
 */
export const SYSTEM_CATEGORIES = {
  // --- Chi tiêu (Expense) ---
  FOOD: 'food',
  TRANSPORT: 'transport',
  SHOPPING: 'shopping',
  BILLS: 'bills',
  HOME: 'home',
  HEALTH: 'health',
  TRAVEL: 'travel',
  DEBT_PAYMENT: 'debt_payment',
  SAVINGS: 'savings',

  // --- Thu nhập (Income) ---
  SALARY: 'salary',
  BONUS: 'bonus',
  FREELANCE: 'freelance',
  INTEREST: 'interest',
  REFUND: 'refund',
  INVESTMENT_INCOME: 'investment-income',

  // --- Legacy / Backwards Compatibility ---
  LEGACY_DEBT_PRINCIPAL: 'debt_principal',
  LEGACY_DEBT_INTEREST: 'debt_interest',
} as const;

/**
 * Kiểm tra xem giao dịch có phải là tiền lãi vay (chi phí tài chính thực sự) hay không.
 */
export function isDebtInterest(tx: Pick<Transaction, 'categoryId' | 'note'>): boolean {
  if (tx.categoryId === SYSTEM_CATEGORIES.LEGACY_DEBT_INTEREST) return true;
  if (tx.categoryId === SYSTEM_CATEGORIES.DEBT_PAYMENT) {
    const lower = (tx.note || '').toLowerCase();
    return (
      lower.includes('tiền lãi') ||
      lower.includes('trả lãi') ||
      lower.includes('lãi vay') ||
      lower.includes('lãi khoản') ||
      lower.includes('lãi:')
    );
  }
  return false;
}

/**
 * Kiểm tra xem giao dịch có phải là trả nợ gốc (hoán đổi tài sản, không phải tiêu dùng mất đi) hay không.
 */
export function isDebtPrincipalSettlement(tx: Pick<Transaction, 'categoryId' | 'note'>): boolean {
  if (tx.categoryId === SYSTEM_CATEGORIES.LEGACY_DEBT_PRINCIPAL) return true;
  if (tx.categoryId === SYSTEM_CATEGORIES.DEBT_PAYMENT) {
    return !isDebtInterest(tx);
  }
  return false;
}

/**
 * Kiểm tra xem giao dịch có phải là chi phí sinh hoạt thường nhật (Living Expense) hay không.
 *
 * Loại trừ theo Hiến pháp tài chính FinLux (BR-01, BR-02, BR-14):
 * - Giao dịch không phải 'expense'
 * - Thanh toán nợ gốc (hoán đổi tài sản)
 * - Tích lũy mục tiêu (SAVINGS - tích lũy tài sản)
 *
 * Giữ lại:
 * - Chi tiêu sinh hoạt thông thường
 * - Tiền lãi vay (chi phí tài chính thực sự)
 */
export function isLivingExpense(tx: Pick<Transaction, 'type' | 'categoryId' | 'note'>): boolean {
  if (tx.type !== 'expense') return false;
  if (isDebtPrincipalSettlement(tx)) return false;
  if (tx.categoryId === SYSTEM_CATEGORIES.SAVINGS) return false;
  return true;
}

/**
 * Thu gọn cặp giao dịch chuyển khoản nội bộ (TRANSFER_IN và TRANSFER_OUT) cho hiển thị danh sách,
 * tránh tính trùng hoặc hiển thị 2 dòng cho 1 lần chuyển khoản giữa 2 ví.
 */
export function collapseInternalTransferPairs(transactions: Transaction[]): Transaction[] {
  const hasTransferIn = transactions.some((t) => t.type === 'transfer_in');
  if (!hasTransferIn) return transactions;

  const idSet = new Set(transactions.map((t) => t.id));
  return transactions.filter((tx) => {
    if (tx.type !== 'transfer_in') return true;
    if (tx.id.endsWith('_in')) {
      const outgoingId = tx.id.replace(/_in$/, '_out');
      if (idSet.has(outgoingId)) return false;
    }
    return true;
  });
}

/**
 * Danh sách ví tài sản (loại trừ thẻ tín dụng là nợ/công cụ thanh toán)
 */
export function assetWallets(wallets: Wallet[]): Wallet[] {
  return wallets.filter((w) => w.type !== 'card');
}
