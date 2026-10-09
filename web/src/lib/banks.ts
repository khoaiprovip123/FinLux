/**
 * Đồng bộ danh mục Ngân hàng & Định chế tài chính Việt Nam (VietQR + E-Wallets)
 * Thừa hưởng 100% từ FinancialInstitutions.kt và VietQrBankCatalog.kt của Android App.
 */

export interface FinancialInstitution {
  id: string;
  code: string;
  shortName: string;
  fullName: string;
  type: 'bank' | 'ewallet' | 'cash' | 'investment' | 'card' | 'other';
  category: 'bank' | 'ewallet' | 'cash_savings' | 'investment';
  color: string;
  logoUrl?: string;
  keywords: string[];
}

export const FINANCIAL_INSTITUTIONS: FinancialInstitution[] = [
  // ----------------------------------------------------
  // 1. TIỀN MẶT & TIẾT KIỆM
  // ----------------------------------------------------
  {
    id: 'cash',
    shortName: 'Tiền mặt',
    fullName: 'Ví tiền mặt chi tiêu hàng ngày',
    code: 'CASH',
    type: 'cash',
    category: 'cash_savings',
    color: '#20B982',
    keywords: ['tiền mặt', 'cash', 'tien mat', 'ví tiền', 'vi tien', 'bóp'],
  },
  {
    id: 'savings',
    shortName: 'Sổ tiết kiệm',
    fullName: 'Tiền gửi tiết kiệm / Heo đất',
    code: 'SAVE',
    type: 'investment',
    category: 'cash_savings',
    color: '#F2A63B',
    keywords: ['tiết kiệm', 'tiet kiem', 'heo đất', 'saving', 'savings', 'heo dat', 'tích lũy'],
  },

  // ----------------------------------------------------
  // 2. VÍ ĐIỆN TỬ (E-WALLETS)
  // ----------------------------------------------------
  {
    id: 'vietqr_971025',
    shortName: 'MoMo',
    fullName: 'Ví điện tử MoMo',
    code: 'MOMO',
    type: 'ewallet',
    category: 'ewallet',
    color: '#A50064',
    logoUrl: '/banks/ic_vietqr_971025.png',
    keywords: ['momo', 'ví momo', 'vi momo'],
  },
  {
    id: 'zalopay',
    shortName: 'ZaloPay',
    fullName: 'Ví điện tử ZaloPay',
    code: 'ZALOPAY',
    type: 'ewallet',
    category: 'ewallet',
    color: '#0068FF',
    keywords: ['zalopay', 'zalo pay', 'zalo', 'ví zalopay', 'vi zalo'],
  },
  {
    id: 'vietqr_971005',
    shortName: 'Viettel Money',
    fullName: 'Viettel Money (ViettelPay)',
    code: 'VTLMONEY',
    type: 'ewallet',
    category: 'ewallet',
    color: '#EE0033',
    logoUrl: '/banks/ic_vietqr_971005.png',
    keywords: ['viettel money', 'viettel pay', 'viettelpay', 'viettel', 'vtmoney'],
  },
  {
    id: 'vietqr_971011',
    shortName: 'VNPT Money',
    fullName: 'VNPT Money',
    code: 'VNPTMONEY',
    type: 'ewallet',
    category: 'ewallet',
    color: '#0085EC',
    logoUrl: '/banks/ic_vietqr_971011.png',
    keywords: ['vnpt money', 'vnptmoney', 'vnpt pay', 'vnpt'],
  },
  {
    id: 'shopeepay',
    shortName: 'ShopeePay',
    fullName: 'Ví điện tử ShopeePay (AirPay)',
    code: 'SHOPEEPAY',
    type: 'ewallet',
    category: 'ewallet',
    color: '#EE4D2D',
    keywords: ['shopeepay', 'shopee pay', 'shopee', 'airpay'],
  },
  {
    id: 'ewallet_payoo',
    shortName: 'Payoo',
    fullName: 'Ví điện tử Payoo',
    code: 'PAYOO',
    type: 'ewallet',
    category: 'ewallet',
    color: '#1757A6',
    logoUrl: '/banks/ic_ewallet_payoo.png',
    keywords: ['payoo', 'ví payoo', 'vietunion'],
  },
  {
    id: 'ewallet_9pay',
    shortName: '9Pay',
    fullName: 'Ví điện tử 9Pay',
    code: '9PAY',
    type: 'ewallet',
    category: 'ewallet',
    color: '#E31D2D',
    logoUrl: '/banks/ic_ewallet_9pay.png',
    keywords: ['9pay', 'ninepay', 'ví 9pay'],
  },
  {
    id: 'ewallet_foxpay',
    shortName: 'Foxpay',
    fullName: 'Ví điện tử Foxpay',
    code: 'FOXPAY',
    type: 'ewallet',
    category: 'ewallet',
    color: '#ED1C24',
    logoUrl: '/banks/ic_ewallet_foxpay.png',
    keywords: ['foxpay', 'fox pay', 'fpt'],
  },
  {
    id: 'ewallet_vtcpay',
    shortName: 'VTC Pay',
    fullName: 'Ví điện tử VTC Pay',
    code: 'VTCPAY',
    type: 'ewallet',
    category: 'ewallet',
    color: '#00ADEF',
    logoUrl: '/banks/ic_ewallet_vtcpay.png',
    keywords: ['vtcpay', 'vtc pay', 'ví vtc'],
  },

  // ----------------------------------------------------
  // 3. TOP NGÂN HÀNG VIỆT NAM (VIETNAMESE BANKS)
  // ----------------------------------------------------
  {
    id: 'vietqr_970436',
    shortName: 'Vietcombank',
    fullName: 'Ngân hàng TMCP Ngoại Thương Việt Nam',
    code: 'VCB',
    type: 'bank',
    category: 'bank',
    color: '#005C2B',
    logoUrl: '/banks/ic_vietqr_970436.png',
    keywords: ['vietcombank', 'vcb', 'ngoại thương', 'ngoai thuong', '970436'],
  },
  {
    id: 'vietqr_970407',
    shortName: 'Techcombank',
    fullName: 'Ngân hàng TMCP Kỹ Thương Việt Nam',
    code: 'TCB',
    type: 'bank',
    category: 'bank',
    color: '#E51A2E',
    logoUrl: '/banks/ic_vietqr_970407.png',
    keywords: ['techcombank', 'techcom', 'tcb', 'kỹ thương', 'ky thuong', '970407'],
  },
  {
    id: 'vietqr_970422',
    shortName: 'MBBank',
    fullName: 'Ngân hàng TMCP Quân Đội',
    code: 'MB',
    type: 'bank',
    category: 'bank',
    color: '#183883',
    logoUrl: '/banks/ic_vietqr_970422.png',
    keywords: ['mbbank', 'mb bank', 'mb', 'quân đội', 'quan doi', '970422'],
  },
  {
    id: 'vietqr_970415',
    shortName: 'VietinBank',
    fullName: 'Ngân hàng TMCP Công Thương Việt Nam',
    code: 'ICB',
    type: 'bank',
    category: 'bank',
    color: '#005596',
    logoUrl: '/banks/ic_vietqr_970415.png',
    keywords: ['vietinbank', 'vietin', 'ctg', 'icb', 'công thương', '970415'],
  },
  {
    id: 'vietqr_970418',
    shortName: 'BIDV',
    fullName: 'Ngân hàng TMCP Đầu tư và Phát triển Việt Nam',
    code: 'BIDV',
    type: 'bank',
    category: 'bank',
    color: '#0B6B38',
    logoUrl: '/banks/ic_vietqr_970418.png',
    keywords: ['bidv', 'đầu tư phát triển', 'dau tu phat trien', '970418'],
  },
  {
    id: 'vietqr_970405',
    shortName: 'Agribank',
    fullName: 'Ngân hàng Nông nghiệp và Phát triển Nông thôn Việt Nam',
    code: 'VBA',
    type: 'bank',
    category: 'bank',
    color: '#8A1538',
    logoUrl: '/banks/ic_vietqr_970405.png',
    keywords: ['agribank', 'vba', 'nông nghiệp', 'nong nghiep', '970405'],
  },
  {
    id: 'vietqr_970416',
    shortName: 'ACB',
    fullName: 'Ngân hàng TMCP Á Châu',
    code: 'ACB',
    type: 'bank',
    category: 'bank',
    color: '#005696',
    logoUrl: '/banks/ic_vietqr_970416.png',
    keywords: ['acb', 'á châu', 'a chau', '970416'],
  },
  {
    id: 'vietqr_970432',
    shortName: 'VPBank',
    fullName: 'Ngân hàng TMCP Việt Nam Thịnh Vượng',
    code: 'VPB',
    type: 'bank',
    category: 'bank',
    color: '#00B14F',
    logoUrl: '/banks/ic_vietqr_970432.png',
    keywords: ['vpbank', 'vpb', 'thịnh vượng', 'thinh vuong', '970432'],
  },
  {
    id: 'vietqr_970423',
    shortName: 'TPBank',
    fullName: 'Ngân hàng TMCP Tiên Phong',
    code: 'TPB',
    type: 'bank',
    category: 'bank',
    color: '#7B2C82',
    logoUrl: '/banks/ic_vietqr_970423.png',
    keywords: ['tpbank', 'tpb', 'tiên phong', 'tien phong', '970423'],
  },
  {
    id: 'vietqr_970403',
    shortName: 'Sacombank',
    fullName: 'Ngân hàng TMCP Sài Gòn Thương Tín',
    code: 'STB',
    type: 'bank',
    category: 'bank',
    color: '#004B8D',
    logoUrl: '/banks/ic_vietqr_970403.png',
    keywords: ['sacombank', 'stb', 'sài gòn thương tín', 'sai gon thuong tin', '970403'],
  },
  {
    id: 'vietqr_970437',
    shortName: 'HDBank',
    fullName: 'Ngân hàng TMCP Phát triển TP.HCM',
    code: 'HDB',
    type: 'bank',
    category: 'bank',
    color: '#E31B23',
    logoUrl: '/banks/ic_vietqr_970437.png',
    keywords: ['hdbank', 'hdb', '970437'],
  },
  {
    id: 'vietqr_970441',
    shortName: 'VIB',
    fullName: 'Ngân hàng TMCP Quốc tế Việt Nam',
    code: 'VIB',
    type: 'bank',
    category: 'bank',
    color: '#0054A6',
    logoUrl: '/banks/ic_vietqr_970441.png',
    keywords: ['vib', 'quốc tế', 'quoc te', '970441'],
  },
  {
    id: 'vietqr_970448',
    shortName: 'OCB',
    fullName: 'Ngân hàng TMCP Phương Đông',
    code: 'OCB',
    type: 'bank',
    category: 'bank',
    color: '#007A3E',
    logoUrl: '/banks/ic_vietqr_970448.png',
    keywords: ['ocb', 'phương đông', 'phuong dong', '970448'],
  },
  {
    id: 'vietqr_970443',
    shortName: 'SHB',
    fullName: 'Ngân hàng TMCP Sài Gòn - Hà Nội',
    code: 'SHB',
    type: 'bank',
    category: 'bank',
    color: '#F47920',
    logoUrl: '/banks/ic_vietqr_970443.png',
    keywords: ['shb', 'sài gòn hà nội', '970443'],
  },
  {
    id: 'vietqr_970431',
    shortName: 'Eximbank',
    fullName: 'Ngân hàng TMCP Xuất Nhập khẩu Việt Nam',
    code: 'EIB',
    type: 'bank',
    category: 'bank',
    color: '#005BAA',
    logoUrl: '/banks/ic_vietqr_970431.png',
    keywords: ['eximbank', 'eib', 'xuất nhập khẩu', '970431'],
  },
  {
    id: 'vietqr_970426',
    shortName: 'MSB',
    fullName: 'Ngân hàng TMCP Hàng Hải Việt Nam',
    code: 'MSB',
    type: 'bank',
    category: 'bank',
    color: '#EE2E24',
    logoUrl: '/banks/ic_vietqr_970426.png',
    keywords: ['msb', 'hàng hải', 'maritime bank', '970426'],
  },
  {
    id: 'vietqr_963388',
    shortName: 'Timo',
    fullName: 'Ngân hàng số Timo by BVBank',
    code: 'TIMO',
    type: 'bank',
    category: 'bank',
    color: '#655BDC',
    logoUrl: '/banks/ic_vietqr_963388.png',
    keywords: ['timo', 'timo digital bank', '963388'],
  },
  {
    id: 'vietqr_546034',
    shortName: 'CAKE',
    fullName: 'Ngân hàng số CAKE by VPBank',
    code: 'CAKE',
    type: 'bank',
    category: 'bank',
    color: '#EE005E',
    logoUrl: '/banks/ic_vietqr_546034.png',
    keywords: ['cake', 'cake by vpbank', '546034'],
  },
  {
    id: 'vietqr_970454',
    shortName: 'BVBank',
    fullName: 'Ngân hàng TMCP Bản Việt',
    code: 'VCCB',
    type: 'bank',
    category: 'bank',
    color: '#0054A6',
    logoUrl: '/banks/ic_vietqr_970454.png',
    keywords: ['bvbank', 'vietcapitalbank', 'bản việt', 'vccb', '970454'],
  },
  {
    id: 'vietqr_970449',
    shortName: 'LPBank',
    fullName: 'Ngân hàng TMCP Lộc Phát Việt Nam',
    code: 'LPB',
    type: 'bank',
    category: 'bank',
    color: '#DE6800',
    logoUrl: '/banks/ic_vietqr_970449.png',
    keywords: ['lpbank', 'lộc phát', 'lienvietpostbank', 'lpb', '970449'],
  },
  {
    id: 'vietqr_970440',
    shortName: 'SeABank',
    fullName: 'Ngân hàng TMCP Đông Nam Á',
    code: 'SEAB',
    type: 'bank',
    category: 'bank',
    color: '#E31B23',
    logoUrl: '/banks/ic_vietqr_970440.png',
    keywords: ['seabank', 'seab', 'đông nam á', '970440'],
  },
  {
    id: 'vietqr_970424',
    shortName: 'ShinhanBank',
    fullName: 'Ngân hàng TNHH MTV Shinhan Việt Nam',
    code: 'SHBVN',
    type: 'bank',
    category: 'bank',
    color: '#00468C',
    logoUrl: '/banks/ic_vietqr_970424.png',
    keywords: ['shinhan', 'shinhanbank', 'shbvn', '970424'],
  },
  {
    id: 'vietqr_458761',
    shortName: 'HSBC',
    fullName: 'Ngân hàng TNHH MTV HSBC Việt Nam',
    code: 'HSBC',
    type: 'bank',
    category: 'bank',
    color: '#DB0011',
    logoUrl: '/banks/ic_vietqr_458761.png',
    keywords: ['hsbc', 'hsbc bank', '458761'],
  },
  {
    id: 'vietqr_970409',
    shortName: 'BacABank',
    fullName: 'Ngân hàng TMCP Bắc Á',
    code: 'BAB',
    type: 'bank',
    category: 'bank',
    color: '#BA8D31',
    logoUrl: '/banks/ic_vietqr_970409.png',
    keywords: ['bacabank', 'bắc á', 'bab', '970409'],
  },
  {
    id: 'vietqr_970428',
    shortName: 'NamABank',
    fullName: 'Ngân hàng TMCP Nam Á',
    code: 'NAB',
    type: 'bank',
    category: 'bank',
    color: '#EAA900',
    logoUrl: '/banks/ic_vietqr_970428.png',
    keywords: ['namabank', 'nam á', 'nab', '970428'],
  },
  {
    id: 'vietqr_970419',
    shortName: 'NCB',
    fullName: 'Ngân hàng TMCP Quốc Dân',
    code: 'NCB',
    type: 'bank',
    category: 'bank',
    color: '#0054A6',
    logoUrl: '/banks/ic_vietqr_970419.png',
    keywords: ['ncb', 'quốc dân', '970419'],
  },
  {
    id: 'vietqr_970412',
    shortName: 'PVcomBank',
    fullName: 'Ngân hàng TMCP Đại Chúng Việt Nam',
    code: 'PVCB',
    type: 'bank',
    category: 'bank',
    color: '#F48220',
    logoUrl: '/banks/ic_vietqr_970412.png',
    keywords: ['pvcombank', 'pvcb', 'đại chúng', '970412'],
  },
  {
    id: 'vietqr_970438',
    shortName: 'BaoVietBank',
    fullName: 'Ngân hàng TMCP Bảo Việt',
    code: 'BVB',
    type: 'bank',
    category: 'bank',
    color: '#005CA9',
    logoUrl: '/banks/ic_vietqr_970438.png',
    keywords: ['baovietbank', 'bảo việt', 'bvb', '970438'],
  },
  {
    id: 'vietqr_970457',
    shortName: 'Woori',
    fullName: 'Ngân hàng TNHH MTV Woori Việt Nam',
    code: 'WVN',
    type: 'bank',
    category: 'bank',
    color: '#0067AC',
    logoUrl: '/banks/ic_vietqr_970457.png',
    keywords: ['woori', 'woori bank', 'wvn', '970457'],
  },
  {
    id: 'vietqr_970406',
    shortName: 'Vikki',
    fullName: 'Ngân hàng Số Vikki',
    code: 'Vikki',
    type: 'bank',
    category: 'bank',
    color: '#FF4D6D',
    logoUrl: '/banks/ic_vietqr_970406.png',
    keywords: ['vikki', 'đông á', 'dongabank', '970406'],
  },
  {
    id: 'vietqr_970452',
    shortName: 'KienLongBank',
    fullName: 'Ngân hàng TMCP Kiên Long',
    code: 'KLB',
    type: 'bank',
    category: 'bank',
    color: '#0054A6',
    logoUrl: '/banks/ic_vietqr_970452.png',
    keywords: ['kienlongbank', 'kiên long', 'klb', '970452'],
  },
];

/**
 * Tìm ngân hàng hoặc ví điện tử phù hợp dựa theo tên ví / mã ngân hàng
 * Thuật toán so khớp ưu tiên tên khớp chính xác, sau đó ưu tiên alias có độ dài lớn nhất
 * (ngăn chặn tình trạng Techcombank bị nhận nhầm thành MB).
 */
export function findInstitutionForWallet(
  walletName?: string,
  bankName?: string
): FinancialInstitution | null {
  const candidates = [walletName, bankName].filter(Boolean) as string[];
  if (candidates.length === 0) return null;

  for (const text of candidates) {
    const clean = text.trim().toLowerCase();
    if (!clean) continue;

    // 1. Exact match
    const exactMatch = FINANCIAL_INSTITUTIONS.find(
      (inst) =>
        clean === inst.shortName.toLowerCase() ||
        clean === inst.code.toLowerCase() ||
        inst.keywords.some((k) => clean === k.toLowerCase())
    );
    if (exactMatch) return exactMatch;

    // 2. Longest alias substring match
    let bestMatch: FinancialInstitution | null = null;
    let longestLength = 0;

    for (const inst of FINANCIAL_INSTITUTIONS) {
      const allAliases = [inst.shortName, inst.code, ...inst.keywords];
      for (const alias of allAliases) {
        const aliasLower = alias.toLowerCase();
        if (clean.includes(aliasLower) || aliasLower.includes(clean)) {
          if (aliasLower.length > longestLength) {
            longestLength = aliasLower.length;
            bestMatch = inst;
          }
        }
      }
    }

    if (bestMatch) return bestMatch;
  }

  return null;
}
