'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon, FinluxLogo } from '@/components/icons/FinluxIcons';
import UserAvatar from '@/components/UserAvatar';

const NAV_ITEMS = [
  { id: 'overview', label: 'Tổng quan', icon: 'home' },
  { id: 'transactions', label: 'Giao dịch', icon: 'swap' },
  { id: 'accounts', label: 'Tài khoản', icon: 'wallet' },
  { id: 'budgets', label: 'Ngân sách', icon: 'budget' },
  { id: 'reports', label: 'Phân tích', icon: 'chart' },
  { id: 'goals', label: 'Mục tiêu', icon: 'target' },
  { id: 'settings', label: 'Cài đặt', icon: 'gear' },
];

export default function Sidebar() {
  const {
    activeTab,
    setActiveTab,
    isSidebarOpen,
    setIsSidebarOpen,
    user,
    isCloudSynced,
    openAccountDetails,
  } = useFinance();

  const handleNavClick = (id: string) => {
    setActiveTab(id);
    setIsSidebarOpen(false);
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  return (
    <>
      <aside className="fx-side">
        {/* Brand Header */}
        <div className="fx-brand">
          <FinluxLogo />
          <span className="fx-brand-copy">
            <b>FINLUX</b>
            <small>PERSONAL FINANCE</small>
          </span>
        </div>

        {/* Section Label */}
        <div className="fx-side-label">MENU CHÍNH</div>

        {/* Primary Navigation */}
        <nav className="fx-nav" aria-label="Menu chính">
          {NAV_ITEMS.map((item) => {
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => handleNavClick(item.id)}
                className={`fx-nav-btn ${isActive ? 'active' : ''}`}
              >
                <FinluxIcon name={item.icon} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Sidebar End Footer */}
        <div className="fx-side-end">
          <button
            type="button"
            className="fx-side-note"
            onClick={openAccountDetails}
            aria-label={user ? 'Xem thông tin tài khoản' : 'Đăng nhập tài khoản'}
            title={user ? 'Xem thông tin tài khoản' : 'Đăng nhập tài khoản'}
          >
            <UserAvatar user={user} />
            <strong>{user ? (user.displayName || user.email) : 'PRISM FINANCE SPACE'}</strong>
            <p>
              {user
                ? (isCloudSynced ? 'Đã đồng bộ · Xem thông tin tài khoản' : 'Xem thông tin tài khoản')
                : 'Nhấn để đăng nhập đồng bộ dữ liệu với Mobile App.'}
            </p>
          </button>
          <div className="fx-side-mini">FINLUX DESIGN CONCEPT · 2026</div>
        </div>
      </aside>

      {/* Mobile Drawer Backdrop */}
      {isSidebarOpen && (
        <div
          className="fx-backdrop"
          onClick={() => setIsSidebarOpen(false)}
          role="presentation"
        />
      )}
    </>
  );
}
