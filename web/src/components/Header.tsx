'use client';

import React, { useState, useRef, useEffect } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import { PERIODS, PeriodKey } from '@/lib/constants';

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
    setPeriod,
    theme,
    toggleTheme,
    setIsSidebarOpen,
    user,
    isCloudSynced,
    setIsAuthModalOpen,
    logout,
  } = useFinance();

  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const currentTabInfo = TAB_TITLES[activeTab] || TAB_TITLES.overview;

  // Close profile menu when clicking outside
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setIsProfileMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const userInitial = user?.displayName
    ? user.displayName.slice(0, 2).toUpperCase()
    : user?.email
    ? user.email.slice(0, 2).toUpperCase()
    : 'VK';

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
        {/* Cloud Sync / Auth Status Badge */}
        {user ? (
          <div ref={menuRef} style={{ position: 'relative' }}>
            <button
              type="button"
              onClick={() => setIsProfileMenuOpen((prev) => !prev)}
              className="fx-demo-chip"
              style={{
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                borderColor: 'rgba(7, 154, 134, 0.35)',
                background: 'var(--green-soft)',
                color: 'var(--green)',
              }}
              title="Đã đồng bộ với tài khoản Mobile App"
            >
              <span
                style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  backgroundColor: '#079a86',
                  boxShadow: '0 0 8px #079a86',
                  display: 'inline-block',
                }}
              />
              <span style={{ fontWeight: 800 }}>{user.displayName || user.email}</span>
            </button>

            {/* Profile Dropdown */}
            {isProfileMenuOpen && (
              <div
                style={{
                  position: 'absolute',
                  right: 0,
                  top: '120%',
                  background: 'var(--surface)',
                  border: '1px solid var(--border)',
                  borderRadius: '16px',
                  boxShadow: 'var(--shadow)',
                  padding: '16px',
                  minWidth: '240px',
                  zIndex: 50,
                  animation: 'fx-modal-in 0.2s ease',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                  <div
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '11px',
                      background: 'var(--purple-soft)',
                      color: 'var(--purple)',
                      display: 'grid',
                      placeItems: 'center',
                      fontWeight: 800,
                      fontSize: '12px',
                    }}
                  >
                    {userInitial}
                  </div>
                  <div style={{ minWidth: 0 }}>
                    <strong style={{ display: 'block', fontSize: '13px', lineHeight: 1.2 }}>
                      {user.displayName || 'Người dùng'}
                    </strong>
                    <small style={{ color: 'var(--muted)', fontSize: '11px' }}>{user.email}</small>
                  </div>
                </div>

                <div
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    background: 'var(--surface-soft)',
                    fontSize: '11px',
                    color: 'var(--muted)',
                    marginBottom: '12px',
                  }}
                >
                  🟢 {isCloudSynced ? 'Đã kết nối Firestore Cloud' : 'Chưa kết nối'}
                </div>

                <button
                  type="button"
                  className="fx-btn fx-btn-subtle"
                  style={{ width: '100%', color: 'var(--red)' }}
                  onClick={() => {
                    setIsProfileMenuOpen(false);
                    logout();
                  }}
                >
                  <span>Đăng xuất</span>
                </button>
              </div>
            )}
          </div>
        ) : (
          <button
            type="button"
            className="fx-demo-chip"
            style={{
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              color: 'var(--purple)',
              borderColor: 'rgba(101, 91, 220, 0.3)',
              background: 'var(--purple-soft)',
            }}
            onClick={() => setIsAuthModalOpen(true)}
            title="Đăng nhập để đồng bộ với Mobile App"
          >
            <FinluxIcon name="shield" />
            <span>Đăng nhập / Đồng bộ App</span>
          </button>
        )}

        {/* Period Selector */}
        <select
          className="fx-select"
          value={period}
          onChange={(e) => setPeriod(e.target.value as PeriodKey)}
          aria-label="Kỳ báo cáo"
        >
          {Object.entries(PERIODS).map(([k, p]) => (
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
