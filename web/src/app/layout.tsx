import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'FINLUX - Prism Personal Finance',
  description:
    'Nền tảng quản lý tài chính cá nhân toàn diện, trực quan và bảo mật. Bức tranh tài chính của bạn — Mọi con số, một góc nhìn rõ ràng.',
  keywords: ['FinLux', 'Prism Finance', 'Personal Finance', 'Quản lý tài chính', 'Quản lý thu chi', 'Next.js'],
  openGraph: {
    title: 'FINLUX - Prism Personal Finance',
    description: 'Bức tranh tài chính của bạn — Mọi con số, một góc nhìn rõ ràng.',
    siteName: 'FinLux',
  },
  icons: {
    icon: '/finlux_logo.png',
    shortcut: '/finlux_logo.png',
    apple: '/finlux_logo.png',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="vi" suppressHydrationWarning>
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=Manrope:wght@400;500;600;700;800&family=Space+Mono:wght@400;700&display=swap"
          rel="stylesheet"
        />
      </head>
      <body suppressHydrationWarning>
        <div id="finlux-root">{children}</div>
      </body>
    </html>
  );
}
