'use client';

import React, { useState } from 'react';
import { FinanceProvider, useFinance } from '@/context/FinanceContext';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import DashboardView from '@/components/views/DashboardView';
import TransactionsView from '@/components/views/TransactionsView';
import WalletsView from '@/components/views/WalletsView';
import BudgetsView from '@/components/views/BudgetsView';
import DebtsView from '@/components/views/DebtsView';
import GoalsView from '@/components/views/GoalsView';
import ReportsView from '@/components/views/ReportsView';
import TransactionModal from '@/components/TransactionModal';
import WalletModal from '@/components/WalletModal';
import SavingSpinModal from '@/components/SavingSpinModal';

function MainApp() {
  const { activeTab } = useFinance();
  const [isTxModalOpen, setIsTxModalOpen] = useState(false);
  const [isWalletModalOpen, setIsWalletModalOpen] = useState(false);
  const [isSpinOpen, setIsSpinOpen] = useState(false);

  const renderActiveView = () => {
    switch (activeTab) {
      case 'dashboard':
        return (
          <DashboardView
            onOpenAddTx={() => setIsTxModalOpen(true)}
            onOpenWalletModal={() => setIsWalletModalOpen(true)}
          />
        );
      case 'transactions':
        return <TransactionsView onOpenAddTx={() => setIsTxModalOpen(true)} />;
      case 'wallets':
        return <WalletsView onOpenWalletModal={() => setIsWalletModalOpen(true)} />;
      case 'budgets':
        return <BudgetsView />;
      case 'debts':
        return <DebtsView />;
      case 'goals':
        return <GoalsView />;
      case 'reports':
        return <ReportsView />;
      default:
        return (
          <DashboardView
            onOpenAddTx={() => setIsTxModalOpen(true)}
            onOpenWalletModal={() => setIsWalletModalOpen(true)}
          />
        );
    }
  };

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <Sidebar
        onOpenAddTx={() => setIsTxModalOpen(true)}
        onOpenSpin={() => setIsSpinOpen(true)}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        <Header
          onOpenAddTx={() => setIsTxModalOpen(true)}
          onOpenSpin={() => setIsSpinOpen(true)}
        />

        <main className="flex-1 p-6 md:p-8 max-w-7xl w-full mx-auto animate-in fade-in duration-300">
          {renderActiveView()}
        </main>
      </div>

      {/* Modals */}
      <TransactionModal
        isOpen={isTxModalOpen}
        onClose={() => setIsTxModalOpen(false)}
      />
      <WalletModal
        isOpen={isWalletModalOpen}
        onClose={() => setIsWalletModalOpen(false)}
      />
      <SavingSpinModal
        isOpen={isSpinOpen}
        onClose={() => setIsSpinOpen(false)}
      />
    </div>
  );
}

export default function Home() {
  return (
    <FinanceProvider>
      <MainApp />
    </FinanceProvider>
  );
}
