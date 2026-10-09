'use client';

import React, { useEffect } from 'react';
import { useFinance } from '@/context/FinanceContext';

export default function Toast() {
  const { toastMessage, showToast } = useFinance();

  useEffect(() => {
    if (!toastMessage) return;
    const timer = setTimeout(() => {
      showToast('');
    }, 3200);
    return () => clearTimeout(timer);
  }, [toastMessage, showToast]);

  if (!toastMessage) return null;

  return (
    <div id="fx-toast" className="fx-toast" role="status">
      {toastMessage}
    </div>
  );
}
