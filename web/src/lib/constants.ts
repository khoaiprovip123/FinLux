import { Category, Wallet, Transaction, Budget, Debt, Goal } from '@/types/finance';

export const PERIODS = {
  six: { start: '2026-05-01', end: '2026-10-09', name: '6 tháng gần đây' },
  three: { start: '2026-08-01', end: '2026-10-09', name: '3 tháng gần đây' },
  oct: { start: '2026-10-01', end: '2026-10-09', name: 'Tháng 10/2026' },
  sep: { start: '2026-09-01', end: '2026-09-30', name: 'Tháng 09/2026' },
  all: { start: '2026-01-01', end: '2026-10-09', name: 'Tất cả' },
} as const;

export type PeriodKey = keyof typeof PERIODS;

export const SYSTEM_CATEGORIES: Category[] = [
  { id: 'salary', name: 'Lương', type: 'income', icon: 'Briefcase', color: '#168A62', isDefault: true },
  { id: 'bonus', name: 'Thưởng', type: 'income', icon: 'Award', color: '#F59E0B', isDefault: true },
  { id: 'freelance', name: 'Thu nhập khác', type: 'income', icon: 'TrendingUp', color: '#56b7cc', isDefault: true },
  { id: 'interest', name: 'Tiền lãi', type: 'income', icon: 'TrendingUp', color: '#10B981', isDefault: true },
  { id: 'food', name: 'Ăn uống', type: 'expense', icon: 'Utensils', color: '#D94B5B', isDefault: true },
  { id: 'transport', name: 'Di chuyển', type: 'expense', icon: 'Car', color: '#E6A23C', isDefault: true },
  { id: 'housing', name: 'Nhà ở', type: 'expense', icon: 'Home', color: '#7566e8', isDefault: true },
  { id: 'home', name: 'Nhà ở', type: 'expense', icon: 'Home', color: '#7566e8', isDefault: true },
  { id: 'bills', name: 'Hóa đơn', type: 'expense', icon: 'Receipt', color: '#e7ad62', isDefault: true },
  { id: 'shopping', name: 'Mua sắm', type: 'expense', icon: 'ShoppingBag', color: '#ed8caf', isDefault: true },
  { id: 'lifestyle', name: 'Giải trí', type: 'expense', icon: 'Film', color: '#a493eb', isDefault: true },
  { id: 'health', name: 'Sức khỏe', type: 'expense', icon: 'HeartPulse', color: '#67aa8e', isDefault: true },
  { id: 'debt_payment', name: 'Trả nợ & Tín dụng', type: 'expense', icon: 'CreditCard', color: '#E11D48', isDefault: true },
  { id: 'savings', name: 'Tích lũy & Mục tiêu', type: 'expense', icon: 'PiggyBank', color: '#8B5CF6', isDefault: true },
];

export const INITIAL_WALLETS: (Wallet & { mark: string; info: string; opening: number })[] = [
  {
    id: 'bank',
    name: 'Vietcombank',
    type: 'bank',
    balance: 48500000,
    opening: 48500000,
    color: '#005C2B',
    mark: 'VCB',
    info: 'Tài khoản thanh toán',
    bankName: 'Vietcombank',
    accountNumber: '9988776655',
    isDefault: true,
    createdAt: '2026-04-01T00:00:00.000Z',
  },
  {
    id: 'cash',
    name: 'Ví tiền mặt',
    type: 'cash',
    balance: 5400000,
    opening: 5400000,
    color: '#20B982',
    mark: 'TM',
    info: 'Tiền mặt hàng ngày',
    createdAt: '2026-04-01T00:00:00.000Z',
  },
  {
    id: 'savings',
    name: 'Techcombank',
    type: 'bank',
    balance: 32000000,
    opening: 32000000,
    color: '#E51A2E',
    mark: 'TCB',
    info: 'Quỹ tiết kiệm SuperSave',
    bankName: 'Techcombank',
    createdAt: '2026-04-01T00:00:00.000Z',
  },
  {
    id: 'momo',
    name: 'Ví MoMo',
    type: 'ewallet',
    balance: 2850000,
    opening: 2850000,
    color: '#A50064',
    mark: 'MM',
    info: 'Ví điện tử',
    bankName: 'MoMo',
    createdAt: '2026-04-01T00:00:00.000Z',
  },
  {
    id: 'credit',
    name: 'MBBank JCB Credit',
    type: 'card',
    balance: -4500000,
    opening: -4500000,
    color: '#183883',
    mark: 'MB',
    info: 'Dư nợ sao kê thẻ',
    bankName: 'MBBank',
    createdAt: '2026-04-01T00:00:00.000Z',
  },
];

export const INITIAL_BUDGETS: Budget[] = [
  { id: 'b_housing', categoryId: 'housing', month: '2026-10', limitAmount: 8500000, spentAmount: 7500000 },
  { id: 'b_food', categoryId: 'food', month: '2026-10', limitAmount: 5200000, spentAmount: 2660000 },
  { id: 'b_shopping', categoryId: 'shopping', month: '2026-10', limitAmount: 2500000, spentAmount: 1080000 },
  { id: 'b_bills', categoryId: 'bills', month: '2026-10', limitAmount: 2200000, spentAmount: 1300000 },
  { id: 'b_transport', categoryId: 'transport', month: '2026-10', limitAmount: 1600000, spentAmount: 605000 },
  { id: 'b_lifestyle', categoryId: 'lifestyle', month: '2026-10', limitAmount: 2000000, spentAmount: 830000 },
];

export const INITIAL_DEBTS: Debt[] = [
  {
    id: 'debt_credit',
    name: 'Thẻ Tín Dụng Techcombank Visa',
    type: 'CREDIT_CARD',
    initialAmount: 15000000,
    remainingBalance: 4500000,
    interestRateYearly: 24.5,
    minPaymentMonthly: 1200000,
    dueDate: 25,
    isSettled: false,
    createdAt: '2026-04-01T00:00:00.000Z',
  },
];

export const INITIAL_GOALS: Goal[] = [
  {
    id: 'goal_financial_freedom',
    name: 'Quỹ tự do tài chính',
    targetAmount: 180000000,
    currentAmount: 32000000,
    deadline: '2027-12-31',
    color: '#6258d8',
    createdAt: '2026-04-01T00:00:00.000Z',
  },
];

export function generateSeedTransactions(): Transaction[] {
  let counter = 0;
  const list: Transaction[] = [];
  const pad = (n: number) => String(n).padStart(2, '0');

  const put = (
    date: string,
    type: 'income' | 'expense' | 'transfer_out',
    amount: number,
    categoryId: string | null,
    walletId: string,
    note: string,
    relatedWalletId: string | null = null
  ) => {
    if (date > '2026-10-09') return;
    counter++;
    list.push({
      id: `s${counter}`,
      date,
      type,
      amount,
      categoryId,
      walletId,
      relatedWalletId,
      note,
      createdAt: `${date}T08:00:00.000Z`,
    });
  };

  for (let m = 4; m <= 10; m++) {
    const ym = `2026-${pad(m)}`;
    put(`${ym}-02`, 'expense', 7500000, 'housing', 'bank', `Tiền nhà tháng ${m}`);
    put(`${ym}-03`, 'expense', 1200000 + (m % 3) * 100000, 'bills', 'bank', 'Điện, nước & Internet');
    put(`${ym}-04`, 'expense', 1900000 + (m % 2) * 200000, 'food', 'cash', 'Mua thực phẩm');
    put(`${ym}-05`, 'income', 28500000 + (m % 3) * 500000, 'salary', 'bank', `Lương tháng ${m}`);
    put(`${ym}-06`, 'expense', 520000 + (m % 3) * 85000, 'transport', 'bank', 'Di chuyển & xăng xe');
    put(`${ym}-06`, 'transfer_out', 2200000, null, 'bank', 'Rút tiền mặt', 'cash');
    put(`${ym}-07`, 'expense', 850000 + (m % 3) * 230000, 'shopping', 'bank', 'Mua sắm cá nhân');
    put(`${ym}-08`, 'income', 3100000 + (m % 3) * 500000, 'freelance', 'bank', 'Dự án bên ngoài');
    put(`${ym}-08`, 'transfer_out', 2000000, null, 'bank', 'Chuyển vào quỹ tiết kiệm', 'savings');
    put(`${ym}-09`, 'expense', 760000 + (m % 2) * 125000, 'food', 'credit', 'Ăn uống cuối tuần');
    if (m < 10) {
      put(`${ym}-12`, 'expense', 1780000 + (m % 2) * 270000, 'food', 'bank', 'Mua sắm siêu thị');
      put(`${ym}-15`, 'expense', 730000 + (m % 3) * 100000, 'lifestyle', 'bank', 'Giải trí & thư giãn');
      put(`${ym}-20`, 'expense', 420000, 'health', 'cash', 'Chăm sóc sức khỏe');
      put(`${ym}-25`, 'transfer_out', 1550000, null, 'bank', 'Thanh toán thẻ tín dụng', 'credit');
    }
  }

  return list.sort((a, b) => b.date.localeCompare(a.date));
}

export const INITIAL_TRANSACTIONS: Transaction[] = generateSeedTransactions();

export const SAVING_SPIN_PRIZES = [
  { label: 'Tiết kiệm 50.000 ₫', amount: 50000, color: '#10B981', message: 'Tích tiểu thành đại! Gửi vào quỹ ngay nào!' },
  { label: 'Tiết kiệm 100.000 ₫', amount: 100000, color: '#06B6D4', message: 'Xuất sắc! Bớt 1 bữa ăn vặt để có 100k tiết kiệm.' },
  { label: 'Nhịn 1 ly Trà Sữa (40k)', amount: 40000, color: '#F59E0B', message: 'Vừa giữ eo vừa có thêm tiền bỏ heo!' },
  { label: 'Tiết kiệm 200.000 ₫', amount: 200000, color: '#8B5CF6', message: 'Quá đỉnh! Khoản tiết kiệm ấn tượng trong ngày.' },
  { label: 'Bỏ ống heo 20.000 ₫', amount: 20000, color: '#3B82F6', message: 'Một bước nhỏ mỗi ngày cho mục tiêu tự do tài chính.' },
  { label: 'Thử thách Không Tiêu Xài', amount: 0, color: '#EC4899', message: 'Thử thách: Hôm nay không phát sinh chi tiêu tùy hứng!' },
];
