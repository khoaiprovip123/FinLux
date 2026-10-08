import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'FinLux Web — Quản Lý Tài Chính & Hoạch Định Dòng Tiền Cá Nhân',
  description:
    'Nền tảng quản lý tài chính cá nhân toàn diện: Sổ giao dịch, Đa ví, Chu kỳ lương, Ngân sách, Sổ nợ Snowball/Avalanche, Mục tiêu và Saving Spin.',
  keywords: ['FinLux', 'Personal Finance', 'Quản lý tài chính', 'Quản lý thu chi', 'Next.js', 'Firebase'],
  openGraph: {
    title: 'FinLux Web — Nền Tảng Tài Chính Cá Nhân Hiện Đại',
    description: 'Quản lý dòng tiền thông minh, kiểm soát ngân sách và mục tiêu tự do tài chính.',
    siteName: 'FinLux',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="vi" className="h-full dark">
      <body className="min-h-full flex flex-col bg-[#070d19] text-slate-100 antialiased selection:bg-cyan-500 selection:text-white">
        {children}
      </body>
    </html>
  );
}
