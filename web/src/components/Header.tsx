'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import UserAvatar from '@/components/UserAvatar';
import type { PeriodKey } from '@/lib/financeCalculations';

const TAB_TITLES: Record<string, { title: string; icon: string }> = {
  overview: { title: 'Tổng quan', icon: 'home' },
  transactions: { title: 'Giao dịch', icon: 'swap' },
  accounts: { title: 'Tài khoản', icon: 'wallet' },
  budgets: { title: 'Ngân sách', icon: 'budget' },
  reports: { title: 'Phân tích', icon: 'chart' },
  goals: { title: 'Mục tiêu', icon: 'target' },
  settings: { title: 'Cài đặt', icon: 'gear' },
};

interface HeaderProps {
  onOpenAddTx: () => void;
}

export default function Header({ onOpenAddTx }: HeaderProps) {
  const {
    activeTab,
    period,
    periods,
    setPeriod,
    theme,
    toggleTheme,
    setIsSidebarOpen,
    user,
    openAccountDetails,
  } = useFinance();

  const currentTabInfo = TAB_TITLES[activeTab] || TAB_TITLES.overview;

  return (
    <header className="fx-top">
      {/* Left: Mobile trigger, icon badge & breadcrumb title */}
      <div className="fx-top-left">
        <button
          className="fx-icon-btn fx-menu-trigger"
          type="button"
          onClick={() => setIsSidebarOpen(true)}
          aria-label="Mở menu"
        >
          <FinluxIcon name="menu" />
        </button>

        <span className="fx-page-symbol">
          <FinluxIcon name={currentTabInfo.icon} />
        </span>

        <div>
          <p className="fx-top-crumb">FINLUX / PERSONAL SPACE</p>
          <h2 className="fx-top-title">{currentTabInfo.title}</h2>
        </div>
      </div>

      {/* Right: Auth status, Period selector, Theme toggle, Add transaction */}
      <div className="fx-top-right">
        <button
          type="button"
          className="fx-account-trigger"
          onClick={openAccountDetails}
          aria-label={user ? 'Xem thông tin tài khoản' : 'Đăng nhập tài khoản'}
          title={user ? 'Xem thông tin tài khoản' : 'Đăng nhập tài khoản'}
        >
          <UserAvatar user={user} />
        </button>

        {/* Period Selector */}
        <select
          className="fx-select"
          value={period}
          onChange={(e) => setPeriod(e.target.value as PeriodKey)}
          aria-label="Kỳ báo cáo"
        >
          {Object.entries(periods).map(([k, p]) => (
            <option key={k} value={k}>
              {p.name}
            </option>
          ))}
        </select>

        {/* Theme Toggle Button */}
        <button
          className="fx-icon-btn"
          type="button"
          onClick={toggleTheme}
          aria-label="Đổi giao diện"
          title={theme === 'light' ? 'Chuyển sang Prism Dark' : 'Chuyển sang Prism Light'}
        >
          <FinluxIcon name={theme === 'light' ? 'moon' : 'sun'} />
        </button>

        {/* Add Transaction Button */}
        <button
          className="fx-btn fx-top-add"
          type="button"
          onClick={onOpenAddTx}
        >
          <FinluxIcon name="plus" />
          <span>Thêm giao dịch</span>
        </button>
      </div>
    </header>
  );
}
