import { Category, Wallet, Transaction, Budget, Debt, Goal } from '@/types/finance';

export const SYSTEM_CATEGORIES: Category[] = [
  { id: 'cat_food', name: 'Ăn uống', type: 'expense', icon: 'Utensils', color: '#F97316', isDefault: true },
  { id: 'cat_transport', name: 'Đi lại & Xăng xe', type: 'expense', icon: 'Car', color: '#3B82F6', isDefault: true },
  { id: 'cat_housing', name: 'Nhà cửa & Điện nước', type: 'expense', icon: 'Home', color: '#8B5CF6', isDefault: true },
  { id: 'cat_shopping', name: 'Mua sắm', type: 'expense', icon: 'ShoppingBag', color: '#EC4899', isDefault: true },
  { id: 'cat_entertainment', name: 'Giải trí & Hẹn hò', type: 'expense', icon: 'Film', color: '#F43F5E', isDefault: true },
  { id: 'cat_health', name: 'Y tế & Sức khỏe', type: 'expense', icon: 'HeartPulse', color: '#10B981', isDefault: true },
  { id: 'cat_education', name: 'Giáo dục & Học tập', type: 'expense', icon: 'GraduationCap', color: '#06B6D4', isDefault: true },
  { id: 'cat_other_expense', name: 'Chi tiêu khác', type: 'expense', icon: 'MoreHorizontal', color: '#64748B', isDefault: true },
  
  { id: 'cat_salary', name: 'Lương & Thưởng', type: 'income', icon: 'Briefcase', color: '#10B981', isDefault: true },
  { id: 'cat_investment', name: 'Đầu tư & Lãi suất', type: 'income', icon: 'TrendingUp', color: '#06B6D4', isDefault: true },
  { id: 'cat_side_hustle', name: 'Kinh doanh phụ', type: 'income', icon: 'Zap', color: '#F59E0B', isDefault: true },
  { id: 'cat_gift', name: 'Quà tặng & Thưởng', type: 'income', icon: 'Gift', color: '#8B5CF6', isDefault: true },
  { id: 'cat_other_income', name: 'Thu nhập khác', type: 'income', icon: 'Coins', color: '#14B8A6', isDefault: true },
];

export const INITIAL_WALLETS: Wallet[] = [
  {
    id: 'w_cash',
    name: 'Tiền mặt',
    type: 'cash',
    balance: 3500000,
    color: '#10B981',
    isDefault: true,
    createdAt: '2026-10-01T00:00:00.000Z',
  },
  {
    id: 'w_vcb',
    name: 'Vietcombank',
    type: 'bank',
    balance: 45800000,
    color: '#06B6D4',
    accountNumber: '9988776655',
    bankName: 'VCB - DigiBank',
    createdAt: '2026-10-01T00:00:00.000Z',
  },
  {
    id: 'w_momo',
    name: 'MoMo E-Wallet',
    type: 'ewallet',
    balance: 1850000,
    color: '#EC4899',
    createdAt: '2026-10-01T00:00:00.000Z',
  },
  {
    id: 'w_credit',
    name: 'Thẻ tín dụng Techcombank',
    type: 'card',
    balance: -8200000, // Dư nợ thẻ
    color: '#F43F5E',
    createdAt: '2026-10-01T00:00:00.000Z',
  },
];

export const INITIAL_TRANSACTIONS: Transaction[] = [
  {
    id: 't_1',
    type: 'income',
    amount: 32000000,
    categoryId: 'cat_salary',
    walletId: 'w_vcb',
    note: 'Lương tháng hiện tại công ty ABC',
    date: '2026-10-05T08:30:00.000Z',
    createdAt: '2026-10-05T08:30:00.000Z',
  },
  {
    id: 't_2',
    type: 'expense',
    amount: 4500000,
    categoryId: 'cat_housing',
    walletId: 'w_vcb',
    note: 'Tiền thuê căn hộ & phí dịch vụ',
    date: '2026-10-06T10:00:00.000Z',
    createdAt: '2026-10-06T10:00:00.000Z',
  },
  {
    id: 't_3',
    type: 'expense',
    amount: 285000,
    categoryId: 'cat_food',
    walletId: 'w_momo',
    note: 'Bữa trưa sushi cùng team',
    date: '2026-10-07T12:15:00.000Z',
    createdAt: '2026-10-07T12:15:00.000Z',
  },
  {
    id: 't_4',
    type: 'expense',
    amount: 500000,
    categoryId: 'cat_transport',
    walletId: 'w_vcb',
    note: 'Đổ xăng ô tô & phí cao tốc',
    date: '2026-10-08T09:00:00.000Z',
    createdAt: '2026-10-08T09:00:00.000Z',
  },
  {
    id: 't_5',
    type: 'income',
    amount: 2500000,
    categoryId: 'cat_side_hustle',
    walletId: 'w_vcb',
    note: 'Thanh toán dự án thiết kế UI/UX',
    date: '2026-10-08T14:20:00.000Z',
    createdAt: '2026-10-08T14:20:00.000Z',
  },
];

export const INITIAL_BUDGETS: Budget[] = [
  {
    id: 'cat_food_2026-10',
    categoryId: 'cat_food',
    month: '2026-10',
    limitAmount: 5000000,
    spentAmount: 285000,
  },
  {
    id: 'cat_transport_2026-10',
    categoryId: 'cat_transport',
    month: '2026-10',
    limitAmount: 2000000,
    spentAmount: 500000,
  },
  {
    id: 'cat_housing_2026-10',
    categoryId: 'cat_housing',
    month: '2026-10',
    limitAmount: 6000000,
    spentAmount: 4500000,
    notified80: false,
  },
  {
    id: 'cat_shopping_2026-10',
    categoryId: 'cat_shopping',
    month: '2026-10',
    limitAmount: 3000000,
    spentAmount: 0,
  },
];

export const INITIAL_DEBTS: Debt[] = [
  {
    id: 'debt_1',
    name: 'Thẻ Tín Dụng Techcombank Visa',
    type: 'CREDIT_CARD',
    initialAmount: 15000000,
    remainingBalance: 8200000,
    interestRateYearly: 24.5,
    minPaymentMonthly: 1200000,
    dueDate: 25,
    isSettled: false,
    createdAt: '2026-10-01T00:00:00.000Z',
  },
  {
    id: 'debt_2',
    name: 'Vay mua Macbook Trả Góp',
    type: 'INSTALLMENT',
    initialAmount: 32000000,
    remainingBalance: 12500000,
    interestRateYearly: 0.0,
    minPaymentMonthly: 2660000,
    dueDate: 15,
    isSettled: false,
    createdAt: '2026-10-01T00:00:00.000Z',
  },
];

export const INITIAL_GOALS: Goal[] = [
  {
    id: 'goal_1',
    name: 'Quỹ Dự Phòng Khẩn Cấp (6 Tháng)',
    targetAmount: 80000000,
    currentAmount: 45000000,
    deadline: '2026-12-31',
    color: '#10B981',
    createdAt: '2026-10-01T00:00:00.000Z',
  },
  {
    id: 'goal_2',
    name: 'Du Lịch Nhật Bản Mùa Thu',
    targetAmount: 35000000,
    currentAmount: 14000000,
    deadline: '2026-11-20',
    color: '#8B5CF6',
    createdAt: '2026-10-01T00:00:00.000Z',
  },
];

export const SAVING_SPIN_PRIZES = [
  { label: 'Tiết kiệm 50.000 ₫', amount: 50000, color: '#10B981', message: 'Tích tiểu thành đại! Gửi vào quỹ ngay nào!' },
  { label: 'Tiết kiệm 100.000 ₫', amount: 100000, color: '#06B6D4', message: 'Xuất sắc! Bớt 1 bữa ăn vặt để có 100k tiết kiệm.' },
  { label: 'Nhịn 1 ly Trà Sữa (40k)', amount: 40000, color: '#F59E0B', message: 'Vừa giữ eo vừa có thêm tiền bỏ heo!' },
  { label: 'Tiết kiệm 200.000 ₫', amount: 200000, color: '#8B5CF6', message: 'Quá đỉnh! Khoản tiết kiệm ấn tượng trong ngày.' },
  { label: 'Bỏ ống heo 20.000 ₫', amount: 20000, color: '#3B82F6', message: 'Một bước nhỏ mỗi ngày cho mục tiêu tự do tài chính.' },
  { label: 'Thử thách Không Tiêu Xài', amount: 0, color: '#EC4899', message: 'Thử thách: Hôm nay không phát sinh chi tiêu tùy hứng!' },
];
