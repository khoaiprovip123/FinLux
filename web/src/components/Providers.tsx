'use client';

import React from 'react';
import { FinanceProvider } from '@/context/FinanceContext';
import Toast from '@/components/Toast';

export default function Providers({ children }: { children: React.ReactNode }) {
  return (
    <FinanceProvider>
      {children}
      <Toast />
    </FinanceProvider>
  );
}
