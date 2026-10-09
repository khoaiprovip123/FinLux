'use client';

import React from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon } from '@/components/icons/FinluxIcons';

export default function SettingsView() {
  const {
    theme,
    toggleTheme,
    resetDemoData,
    user,
    isCloudSynced,
    setIsAuthModalOpen,
    logout,
  } = useFinance();

  return (
    <div className="space-y-6">
      {/* Intro */}
      <section className="fx-intro">
        <div>
          <p className="fx-eyebrow">FINLUX / CẤU HÌNH HỆ THỐNG</p>
          <h1 className="fx-title">Cài đặt</h1>
          <p className="fx-lede">
            Quản lý tài khoản, đồng bộ đám mây và tùy biến giao diện hiển thị.
          </p>
        </div>
      </section>

      {/* 1. Account & Mobile App Cloud Sync Panel */}
      <div className="fx-card fx-panel">
        <div className="fx-panel-header">
          <div>
            <h3 className="fx-panel-title">Tài khoản &amp; Đồng bộ Cloud (Mobile App)</h3>
            <p className="fx-panel-sub">Kết nối chung cơ sở dữ liệu Firebase với ứng dụng Android</p>
          </div>
          <FinluxIcon name="shield" />
        </div>

        <div>
          <div className="fx-kv" style={{ alignItems: 'center' }}>
            <span>Trạng thái đồng bộ</span>
            <strong style={{ color: user && isCloudSynced ? 'var(--green)' : 'var(--muted)' }}>
              {user && isCloudSynced
                ? '🟢 Đã kết nối Firestore Cloud (Real-time)'
                : '🟡 Ngoại tuyến — Chế độ mẫu cục bộ'}
            </strong>
          </div>

          {user ? (
            <>
              <div className="fx-kv">
                <span>Tên hiển thị</span>
                <strong>{user.displayName || 'Chưa đặt'}</strong>
              </div>
              <div className="fx-kv">
                <span>Email đăng ký</span>
                <strong>{user.email}</strong>
              </div>
              <div className="fx-kv">
                <span>Mã định danh User UID</span>
                <strong style={{ fontFamily: 'monospace', fontSize: '11px' }}>{user.uid}</strong>
              </div>
              <div className="fx-kv" style={{ alignItems: 'center' }}>
                <span>Thao tác tài khoản</span>
                <button
                  type="button"
                  className="fx-btn fx-btn-subtle"
                  style={{ color: 'var(--red)', borderColor: 'rgba(221, 105, 126, 0.4)' }}
                  onClick={logout}
                >
                  <span>Đăng xuất khỏi thiết bị này</span>
                </button>
              </div>
            </>
          ) : (
            <div className="fx-kv" style={{ alignItems: 'center' }}>
              <span>Đăng nhập hoặc đăng ký</span>
              <button
                type="button"
                className="fx-btn"
                onClick={() => setIsAuthModalOpen(true)}
              >
                <FinluxIcon name="shield" />
                <span>Đăng nhập để đồng bộ với Mobile App</span>
              </button>
            </div>
          )}
        </div>

        <div className="fx-muted-box" style={{ marginTop: '16px' }}>
          Khi đăng nhập bằng tài khoản FinLux của bạn, mọi giao dịch, số dư ví, ngân sách và mục tiêu trên ứng dụng di động Android sẽ được tự động đồng bộ hai chiều theo thời gian thực (Real-time Listener).
        </div>
      </div>

      {/* 2. Main Interface Settings Panel */}
      <div className="fx-card fx-panel">
        <div className="fx-panel-header">
          <div>
            <h3 className="fx-panel-title">Giao diện &amp; Trải nghiệm</h3>
            <p className="fx-panel-sub">Prism Liquid Glass Design System</p>
          </div>
          <FinluxIcon name="gear" />
        </div>

        <div>
          {/* Theme switcher */}
          <div className="fx-kv" style={{ alignItems: 'center' }}>
            <span>Chế độ hiển thị</span>
            <button
              type="button"
              className="fx-btn fx-btn-subtle"
              onClick={toggleTheme}
            >
              <FinluxIcon name={theme === 'light' ? 'moon' : 'sun'} />
              <span>{theme === 'light' ? 'Prism Dark Mode' : 'Prism Light Mode'}</span>
            </button>
          </div>

          <div className="fx-kv">
            <span>Đơn vị tiền tệ</span>
            <strong>VND (Việt Nam Đồng ₫)</strong>
          </div>

          <div className="fx-kv">
            <span>Ngôn ngữ hiển thị</span>
            <strong>Tiếng Việt (vi-VN)</strong>
          </div>

          <div className="fx-kv" style={{ alignItems: 'center' }}>
            <span>Dữ liệu mẫu demo</span>
            <button
              type="button"
              className="fx-btn fx-btn-subtle"
              onClick={resetDemoData}
            >
              <FinluxIcon name="swap" />
              <span>Khôi phục dữ liệu mẫu gốc</span>
            </button>
          </div>
        </div>

        <div className="fx-muted-box" style={{ marginTop: '24px' }}>
          Hệ thống FinLux Web hoạt động với kiến trúc Clean Architecture &amp; Prism Design System. Dữ liệu được tính toán đồng bộ theo thời gian thực từ sổ cái chung, chống tính trùng chi phí và bảo toàn số dư nguyên tử.
        </div>
      </div>
    </div>
  );
}
