'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import DashboardView from '@/components/views/DashboardView';
import TransactionsView from '@/components/views/TransactionsView';
import WalletsView from '@/components/views/WalletsView';
import BudgetsView from '@/components/views/BudgetsView';
import ReportsView from '@/components/views/ReportsView';
import GoalsView from '@/components/views/GoalsView';
import SettingsView from '@/components/views/SettingsView';
import TransactionModal from '@/components/TransactionModal';
import TransactionDetailModal from '@/components/TransactionDetailModal';
import WalletModal from '@/components/WalletModal';
import AuthModal from '@/components/AuthModal';
import Toast from '@/components/Toast';

import GuestFinanceView from '@/components/views/GuestFinanceView';

function MainApp() {
  const {
    activeTab,
    theme,
    isSidebarOpen,
    isAuthModalOpen,
    setIsAuthModalOpen,
    user,
    authLoading,
  } = useFinance();

  const [isTxModalOpen, setIsTxModalOpen] = useState(false);
  const [isWalletModalOpen, setIsWalletModalOpen] = useState(false);

  // Màn hình tải trạng thái phiên đăng nhập
  if (authLoading) {
    return (
      <div
        id="fx-app"
        className="fx-app"
        data-theme={theme}
        style={{
          minHeight: '100vh',
          display: 'grid',
          placeItems: 'center',
          background: 'var(--bg)',
        }}
      >
        <div style={{ textAlign: 'center' }}>
          <div
            style={{
              width: '44px',
              height: '44px',
              borderRadius: '50%',
              border: '3px solid var(--border)',
              borderTopColor: 'var(--purple)',
              animation: 'spin 0.8s linear infinite',
              margin: '0 auto 16px',
            }}
          />
          <div style={{ fontSize: '13px', color: 'var(--muted)', fontWeight: 600 }}>
            Đang khởi động FinLux...
          </div>
        </div>
      </div>
    );
  }

  // Khi chưa đăng nhập: Hiển thị Màn hình tài chính Landing View (chuyển sang /login khi nhấn đăng nhập)
  if (!user) {
    return (
      <div id="fx-app" className="fx-app" data-theme={theme} style={{ display: 'block' }}>
        <GuestFinanceView />
      </div>
    );
  }

  const renderActiveView = () => {
    switch (activeTab) {
      case 'overview':
      case 'dashboard':
        return <DashboardView />;
      case 'transactions':
        return <TransactionsView onOpenAddTx={() => setIsTxModalOpen(true)} />;
      case 'accounts':
      case 'wallets':
        return <WalletsView onOpenWalletModal={() => setIsWalletModalOpen(true)} />;
      case 'budgets':
        return <BudgetsView />;
      case 'reports':
        return <ReportsView />;
      case 'goals':
        return <GoalsView />;
      case 'settings':
        return <SettingsView />;
      default:
        return <DashboardView />;
    }
  };

  return (
    <div
      id="fx-app"
      className={`fx-app ${isSidebarOpen ? 'fx-side-open' : ''}`}
      data-theme={theme}
    >
      {/* Sidebar with mobile drawer support */}
      <Sidebar />

      {/* Main Workspace Area */}
      <div className="fx-workspace">
        <Header onOpenAddTx={() => setIsTxModalOpen(true)} />

        <main className="fx-main" id="fx-view">
          {renderActiveView()}
        </main>
      </div>

      {/* Modals & Overlays */}
      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
      />
      <TransactionModal
        isOpen={isTxModalOpen}
        onClose={() => setIsTxModalOpen(false)}
      />
      <TransactionDetailModal />
      <WalletModal
        isOpen={isWalletModalOpen}
        onClose={() => setIsWalletModalOpen(false)}
      />
      <Toast />
    </div>
  );
}

export default function Home() {
  return <MainApp />;
}
