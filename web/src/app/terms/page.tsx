'use client';

import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useFinance } from '@/context/FinanceContext';

export default function TermsPage() {
  const { theme, toggleTheme } = useFinance();

  return (
    <div
      className="fx-scene-atelier"
      data-theme={theme}
      suppressHydrationWarning
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        position: 'relative',
        color: 'var(--text)',
      }}
    >
      {/* Background Decorative Halos */}
      <div className="fx-halo fx-halo-a" />
      <div className="fx-halo fx-halo-b" />
      <div className="fx-halo fx-halo-c" />
      <div className="fx-grain" />

      {/* Top Navigation Bar */}
      <header
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '20px 5vw',
          zIndex: 20,
          position: 'relative',
        }}
      >
        <Link
          href="/"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            textDecoration: 'none',
            color: 'inherit',
          }}
        >
          <Image
            src="/finlux_logo.png"
            alt="FinLux Logo"
            width={38}
            height={38}
            priority
            style={{ width: '38px', height: '38px', objectFit: 'contain' }}
          />
          <div>
            <div
              style={{
                fontSize: '18px',
                fontWeight: 800,
                letterSpacing: '-0.02em',
                color: 'var(--text)',
                lineHeight: 1,
              }}
            >
              FINLUX
            </div>
            <div
              style={{
                fontSize: '9.5px',
                fontWeight: 700,
                color: 'var(--muted)',
                letterSpacing: '0.12em',
                textTransform: 'uppercase',
                marginTop: '3px',
              }}
            >
              Financial Atelier
            </div>
          </div>
        </Link>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <button
            type="button"
            onClick={toggleTheme}
            aria-label="Đổi giao diện"
            className="fx-round-btn"
            style={{ width: '40px', height: '40px' }}
            suppressHydrationWarning
          >
            {theme === 'dark' ? (
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="5" />
                <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
              </svg>
            ) : (
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            )}
          </button>

          <Link
            href="/login"
            style={{
              padding: '9px 18px',
              borderRadius: '999px',
              fontSize: '13px',
              fontWeight: 650,
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              color: 'var(--text)',
              textDecoration: 'none',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              boxShadow: '0 2px 10px rgba(0, 0, 0, 0.05)',
              transition: 'all 0.2s ease',
            }}
          >
            <span>← Đăng nhập</span>
          </Link>

          <Link
            href="/"
            style={{
              padding: '9px 18px',
              borderRadius: '999px',
              fontSize: '13px',
              fontWeight: 650,
              background: 'var(--purple)',
              border: 'none',
              color: '#ffffff',
              textDecoration: 'none',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              boxShadow: '0 4px 16px rgba(101, 91, 220, 0.35)',
              transition: 'all 0.2s ease',
            }}
          >
            <span>Trang chủ</span>
          </Link>
        </div>
      </header>

      {/* Main Content Container */}
      <main
        style={{
          flex: 1,
          maxWidth: '860px',
          margin: '0 auto',
          padding: '24px 20px 80px',
          zIndex: 10,
          position: 'relative',
          width: '100%',
        }}
      >
        {/* Hero Title Card */}
        <div
          style={{
            textAlign: 'center',
            marginBottom: '36px',
          }}
        >
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              padding: '6px 14px',
              borderRadius: '999px',
              background: 'rgba(101, 91, 220, 0.1)',
              border: '1px solid rgba(101, 91, 220, 0.25)',
              fontSize: '11px',
              fontWeight: 750,
              color: 'var(--purple)',
              letterSpacing: '0.08em',
              textTransform: 'uppercase',
              marginBottom: '14px',
            }}
          >
            <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'currentColor', display: 'inline-block' }} />
            <span>CAM KẾT BẢO MẬT &amp; MINH BẠCH</span>
          </div>

          <h1
            style={{
              fontSize: 'clamp(26px, 4vw, 36px)',
              fontWeight: 800,
              letterSpacing: '-0.03em',
              lineHeight: 1.25,
              margin: '0 0 14px',
              color: 'var(--text)',
            }}
          >
            Điều khoản Dịch vụ &amp; Chính sách Bảo mật
          </h1>

          <p
            style={{
              fontSize: '15px',
              lineHeight: 1.6,
              color: 'var(--muted)',
              maxWidth: '680px',
              margin: '0 auto',
            }}
          >
            FinLux cam kết cung cấp giải pháp quản lý tài chính cá nhân <strong>hoàn toàn miễn phí</strong>, 
            bảo vệ quyền riêng tư tuyệt đối và <strong>không bao giờ thu thập hay khai thác dữ liệu của bạn cho bất kỳ mục đích nào khác</strong>.
          </p>
        </div>

        {/* 3 Golden Commitments Highlight Grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
            gap: '16px',
            marginBottom: '36px',
          }}
        >
          <div
            style={{
              padding: '22px 20px',
              borderRadius: '20px',
              background:
                theme === 'light'
                  ? 'linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(240, 245, 255, 0.85))'
                  : 'linear-gradient(135deg, rgba(28, 35, 60, 0.85), rgba(18, 24, 45, 0.85))',
              border: '1.5px solid rgba(101, 91, 220, 0.2)',
              boxShadow: '0 10px 30px rgba(0, 0, 0, 0.04)',
              backdropFilter: 'blur(20px)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '10px',
                  background: 'rgba(101, 91, 220, 0.12)',
                  border: '1px solid rgba(101, 91, 220, 0.25)',
                  display: 'grid',
                  placeItems: 'center',
                  color: 'var(--purple)',
                }}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18.178 8c5.096 0 5.096 8 0 8-5.095 0-7.26-8-12.356-8-5.096 0-5.096 8 0 8 5.096 0 7.26-8 12.356-8z" />
                </svg>
              </div>
              <span style={{ fontSize: '10.5px', fontWeight: 800, letterSpacing: '0.08em', color: 'var(--purple)', textTransform: 'uppercase', background: 'rgba(101, 91, 220, 0.1)', padding: '3px 8px', borderRadius: '6px' }}>
                01 / MIỄN PHÍ
              </span>
            </div>
            <div style={{ fontSize: '15.5px', fontWeight: 800, marginBottom: '6px', color: 'var(--text)' }}>
              100% Hoàn Toàn Miễn Phí
            </div>
            <div style={{ fontSize: '13px', lineHeight: 1.55, color: 'var(--muted)' }}>
              Không phí kích hoạt, không gói trả phí hàng tháng, không giới hạn tính năng. FinLux phục vụ bạn trọn đời.
            </div>
          </div>

          <div
            style={{
              padding: '22px 20px',
              borderRadius: '20px',
              background:
                theme === 'light'
                  ? 'linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(235, 255, 248, 0.85))'
                  : 'linear-gradient(135deg, rgba(16, 45, 40, 0.85), rgba(12, 32, 30, 0.85))',
              border: '1.5px solid rgba(7, 154, 134, 0.25)',
              boxShadow: '0 10px 30px rgba(0, 0, 0, 0.04)',
              backdropFilter: 'blur(20px)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '10px',
                  background: 'rgba(7, 154, 134, 0.12)',
                  border: '1px solid rgba(7, 154, 134, 0.25)',
                  display: 'grid',
                  placeItems: 'center',
                  color: 'var(--green)',
                }}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
                  <path d="M14.12 14.12a3 3 0 1 1-4.24-4.24" />
                  <path d="M1 1l22 22" />
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
                </svg>
              </div>
              <span style={{ fontSize: '10.5px', fontWeight: 800, letterSpacing: '0.08em', color: 'var(--green)', textTransform: 'uppercase', background: 'rgba(7, 154, 134, 0.1)', padding: '3px 8px', borderRadius: '6px' }}>
                02 / RIÊNG TƯ
              </span>
            </div>
            <div style={{ fontSize: '15.5px', fontWeight: 800, marginBottom: '6px', color: 'var(--text)' }}>
              Không Khai Thác Dữ Liệu
            </div>
            <div style={{ fontSize: '13px', lineHeight: 1.55, color: 'var(--muted)' }}>
              Tuyệt đối <strong>KHÔNG</strong> bán, không chia sẻ cho bên thứ ba, không quảng cáo và không dùng dữ liệu để trục lợi thương mại.
            </div>
          </div>

          <div
            style={{
              padding: '22px 20px',
              borderRadius: '20px',
              background:
                theme === 'light'
                  ? 'linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(248, 240, 255, 0.85))'
                  : 'linear-gradient(135deg, rgba(38, 25, 58, 0.85), rgba(24, 18, 40, 0.85))',
              border: '1.5px solid rgba(165, 154, 255, 0.25)',
              boxShadow: '0 10px 30px rgba(0, 0, 0, 0.04)',
              backdropFilter: 'blur(20px)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '10px',
                  background: 'rgba(165, 154, 255, 0.12)',
                  border: '1px solid rgba(165, 154, 255, 0.25)',
                  display: 'grid',
                  placeItems: 'center',
                  color: '#7d7cff',
                }}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <ellipse cx="12" cy="5" rx="9" ry="3" />
                  <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3" />
                  <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5" />
                </svg>
              </div>
              <span style={{ fontSize: '10.5px', fontWeight: 800, letterSpacing: '0.08em', color: '#7d7cff', textTransform: 'uppercase', background: 'rgba(165, 154, 255, 0.1)', padding: '3px 8px', borderRadius: '6px' }}>
                03 / HẠ TẦNG
              </span>
            </div>
            <div style={{ fontSize: '15.5px', fontWeight: 800, marginBottom: '6px', color: 'var(--text)' }}>
              Bảo Mật Google Cloud
            </div>
            <div style={{ fontSize: '13px', lineHeight: 1.55, color: 'var(--muted)' }}>
              Mã hóa TLS đầu-cuối. Quy tắc phân quyền Firestore Security Rules đảm bảo chỉ duy nhất bạn truy cập được dữ liệu của mình.
            </div>
          </div>
        </div>

        {/* Detailed Sections Glass Container */}
        <div
          style={{
            background:
              theme === 'light'
                ? 'rgba(255, 255, 255, 0.92)'
                : 'rgba(22, 28, 48, 0.92)',
            border: '1.5px solid rgba(255, 255, 255, 0.8)',
            boxShadow:
              theme === 'light'
                ? '0 20px 60px rgba(25, 45, 95, 0.08)'
                : '0 25px 70px rgba(0, 0, 0, 0.5)',
            borderRadius: '28px',
            padding: '36px 32px',
            backdropFilter: 'blur(30px)',
            WebkitBackdropFilter: 'blur(30px)',
          }}
        >
          {/* Section 1 */}
          <section style={{ marginBottom: '32px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                marginBottom: '12px',
              }}
            >
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '28px',
                  height: '28px',
                  borderRadius: '8px',
                  background: 'rgba(101, 91, 220, 0.15)',
                  color: 'var(--purple)',
                  fontWeight: 800,
                  fontSize: '13px',
                }}
              >
                1
              </span>
              <h2 style={{ fontSize: '18px', fontWeight: 800, margin: 0, color: 'var(--text)' }}>
                Tôn chỉ Sản phẩm &amp; Chính sách Miễn phí (100% Free)
              </h2>
            </div>
            <p style={{ fontSize: '14px', lineHeight: 1.65, color: 'var(--muted)', margin: '0 0 10px' }}>
              FinLux được xây dựng với sứ mệnh đem lại một công cụ quản trị dòng tiền và tự do tài chính cá nhân
              chuẩn mực, trực quan và không rào cản. FinLux được phát hành <strong>hoàn toàn miễn phí</strong>.
            </p>
            <ul style={{ fontSize: '13.5px', lineHeight: 1.65, color: 'var(--muted)', margin: 0, paddingLeft: '20px' }}>
              <li>Không thu phí đăng ký, duy trì hoặc gia hạn tài khoản.</li>
              <li>Không cắt giảm tính năng để ép người dùng nâng cấp gói cao cấp (không Paywall).</li>
              <li>Tất cả các tính năng: Ví, Giao dịch, Ngân sách, Báo cáo chuyên sâu, Mục tiêu tích lũy, Lương chu kỳ đều khả dụng trọn vẹn.</li>
            </ul>
          </section>

          {/* Section 2 */}
          <section style={{ marginBottom: '32px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                marginBottom: '12px',
              }}
            >
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '28px',
                  height: '28px',
                  borderRadius: '8px',
                  background: 'rgba(7, 154, 134, 0.15)',
                  color: 'var(--green)',
                  fontWeight: 800,
                  fontSize: '13px',
                }}
              >
                2
              </span>
              <h2 style={{ fontSize: '18px', fontWeight: 800, margin: 0, color: 'var(--text)' }}>
                Cam kết Không Thu Thập Dữ Liệu Ngoài Mục Đích Cá Nhân
              </h2>
            </div>
            <p style={{ fontSize: '14px', lineHeight: 1.65, color: 'var(--muted)', margin: '0 0 10px' }}>
              Chúng tôi hiểu rằng tài chính là dữ liệu riêng tư và nhạy cảm nhất của mỗi cá nhân. Do đó, FinLux thực thi
              chính sách bảo mật nghiêm ngặt:
            </p>
            <div
              style={{
                padding: '16px 18px',
                borderRadius: '16px',
                background: 'rgba(7, 154, 134, 0.08)',
                border: '1px solid rgba(7, 154, 134, 0.2)',
                marginBottom: '14px',
              }}
            >
              <div style={{ fontWeight: 700, fontSize: '13.5px', color: 'var(--text)', marginBottom: '6px' }}>
                ✓ Mục đích duy nhất của dữ liệu:
              </div>
              <div style={{ fontSize: '13px', lineHeight: 1.6, color: 'var(--muted)' }}>
                Dữ liệu bạn nhập (thu nhập, chi tiêu, số dư các ví, hạn mức ngân sách, ghi chú giao dịch) chỉ phục vụ một mục đích duy nhất:
                <strong> Lưu trữ và hiển thị đồ thị, số liệu cho chính bạn theo dõi trên thiết bị di động và máy tính.</strong>
              </div>
            </div>
            <ul style={{ fontSize: '13.5px', lineHeight: 1.65, color: 'var(--muted)', margin: 0, paddingLeft: '20px' }}>
              <li>
                <strong>KHÔNG bán hoặc cho thuê dữ liệu:</strong> FinLux không bao giờ bán, thương mại hóa hay chia sẻ cơ sở dữ liệu người dùng cho bất kỳ bên thứ ba nào (ngân hàng, bảo hiểm, công ty quảng cáo).
              </li>
              <li>
                <strong>KHÔNG theo dõi quảng cáo (Zero Ad-Trackers):</strong> Ứng dụng không nhúng mã theo dõi tiếp thị, không hiển thị quảng cáo phiền toái và không tạo hồ sơ người dùng để quảng cáo.
              </li>
              <li>
                <strong>KHÔNG yêu cầu thông tin ngân hàng nhạy cảm:</strong> FinLux không yêu cầu mật khẩu ngân hàng trực tuyến, mã OTP hay mã thẻ tín dụng CVC/CVV. Người dùng hoàn toàn tự chủ nhập liệu số tiền theo mong muốn.
              </li>
            </ul>
          </section>

          {/* Section 3 */}
          <section style={{ marginBottom: '32px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                marginBottom: '12px',
              }}
            >
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '28px',
                  height: '28px',
                  borderRadius: '8px',
                  background: 'rgba(101, 91, 220, 0.15)',
                  color: 'var(--purple)',
                  fontWeight: 800,
                  fontSize: '13px',
                }}
              >
                3
              </span>
              <h2 style={{ fontSize: '18px', fontWeight: 800, margin: 0, color: 'var(--text)' }}>
                Hạ tầng Lưu trữ &amp; Mã hóa Bảo mật Cao
              </h2>
            </div>
            <p style={{ fontSize: '14px', lineHeight: 1.65, color: 'var(--muted)', margin: '0 0 10px' }}>
              FinLux vận hành trên nền tảng đám mây Google Cloud / Firebase tiêu chuẩn toàn cầu:
            </p>
            <ul style={{ fontSize: '13.5px', lineHeight: 1.65, color: 'var(--muted)', margin: 0, paddingLeft: '20px' }}>
              <li>
                <strong>Xác thực an toàn (Firebase Authentication):</strong> Hỗ trợ Google Sign-In và Email/Mật khẩu. Mật khẩu được mã hóa băm một chiều an toàn, hệ thống không bao giờ lưu trữ mật khẩu dưới dạng văn bản thô.
              </li>
              <li>
                <strong>Phân quyền cấp Document (Firestore Security Rules):</strong> Hệ thống thực thi quy tắc nghiêm ngặt: chỉ tài khoản đã đăng nhập có User ID khớp với tài liệu mới có quyền đọc và ghi dữ liệu của mình. Bất kỳ ai khác đều không thể xem hay chỉnh sửa thông tin của bạn.
              </li>
              <li>
                <strong>Mã hóa trên đường truyền:</strong> 100% dữ liệu truyền tải giữa thiết bị của bạn và máy chủ Google Cloud đều được bảo vệ bởi giao thức HTTPS/TLS mã hóa cao.
              </li>
            </ul>
          </section>

          {/* Section 4 */}
          <section style={{ marginBottom: '32px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                marginBottom: '12px',
              }}
            >
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '28px',
                  height: '28px',
                  borderRadius: '8px',
                  background: 'rgba(221, 105, 126, 0.15)',
                  color: 'var(--red)',
                  fontWeight: 800,
                  fontSize: '13px',
                }}
              >
                4
              </span>
              <h2 style={{ fontSize: '18px', fontWeight: 800, margin: 0, color: 'var(--text)' }}>
                Quyền Làm Chủ Dữ Liệu &amp; Quyền Được Quên (Data Ownership)
              </h2>
            </div>
            <p style={{ fontSize: '14px', lineHeight: 1.65, color: 'var(--muted)', margin: '0 0 10px' }}>
              Bạn là chủ sở hữu tối cao đối với toàn bộ dữ liệu tài chính của mình:
            </p>
            <ul style={{ fontSize: '13.5px', lineHeight: 1.65, color: 'var(--muted)', margin: 0, paddingLeft: '20px' }}>
              <li>
                <strong>Quyền xem &amp; chỉnh sửa:</strong> Bạn có thể tra cứu, chỉnh sửa hoặc xóa bất kỳ ví, giao dịch hay hạn mức ngân sách nào tức thời.
              </li>
              <li>
                <strong>Quyền xóa vĩnh viễn (Right to be Forgotten):</strong> Bạn có quyền xóa tài khoản và toàn bộ dữ liệu liên quan khỏi hệ thống Firestore bất cứ lúc nào. Khi thực hiện, hệ thống sẽ xóa vĩnh viễn không thể khôi phục.
              </li>
            </ul>
          </section>

          {/* Section 5 */}
          <section style={{ marginBottom: '32px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                marginBottom: '12px',
              }}
            >
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '28px',
                  height: '28px',
                  borderRadius: '8px',
                  background: 'rgba(101, 91, 220, 0.15)',
                  color: 'var(--purple)',
                  fontWeight: 800,
                  fontSize: '13px',
                }}
              >
                5
              </span>
              <h2 style={{ fontSize: '18px', fontWeight: 800, margin: 0, color: 'var(--text)' }}>
                Đồng Bộ Hai Chiều Liền Mạch (App Android &amp; Web)
              </h2>
            </div>
            <p style={{ fontSize: '14px', lineHeight: 1.65, color: 'var(--muted)', margin: 0 }}>
              Một tài khoản FinLux duy nhất dùng chung 100% giữa ứng dụng di động FinLux Android và nền tảng Web.
              Mọi biến động giao dịch hoặc thiết lập ngân sách được cập nhật thời gian thực hai chiều, giúp bạn dễ dàng theo dõi
              thu chi mọi lúc mọi nơi.
            </p>
          </section>

          {/* Section 6 */}
          <section style={{ marginBottom: '16px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                marginBottom: '12px',
              }}
            >
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '28px',
                  height: '28px',
                  borderRadius: '8px',
                  background: 'rgba(101, 91, 220, 0.15)',
                  color: 'var(--purple)',
                  fontWeight: 800,
                  fontSize: '13px',
                }}
              >
                6
              </span>
              <h2 style={{ fontSize: '18px', fontWeight: 800, margin: 0, color: 'var(--text)' }}>
                Trách nhiệm Người Dùng &amp; Liên Hệ
              </h2>
            </div>
            <p style={{ fontSize: '14px', lineHeight: 1.65, color: 'var(--muted)', margin: '0 0 10px' }}>
              Người dùng có trách nhiệm bảo mật thiết bị và mật khẩu tài khoản của mình. FinLux là công cụ hỗ trợ theo dõi
              và lập kế hoạch tài chính cá nhân, không cung cấp các dịch vụ tư vấn tài chính ủy thác hay đầu tư cam kết sinh lời.
            </p>
            <p style={{ fontSize: '13.5px', lineHeight: 1.65, color: 'var(--muted)', margin: 0 }}>
              Nếu có bất kỳ thắc mắc hoặc cần hỗ trợ về quyền riêng tư và dữ liệu, bạn có thể liên hệ với đội ngũ phát triển qua email:{' '}
              <strong style={{ color: 'var(--purple)' }}>support@finlux.app</strong> hoặc thông qua dự án mã nguồn mở FinLux.
            </p>
          </section>

          {/* Bottom Actions CTA */}
          <div
            style={{
              marginTop: '36px',
              paddingTop: '28px',
              borderTop: '1px solid var(--border)',
              display: 'flex',
              flexWrap: 'wrap',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: '14px',
            }}
          >
            <div style={{ fontSize: '12px', color: 'var(--muted)' }}>
              Có hiệu lực từ tháng 10/2026 • Phiên bản 1.0.0
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <Link
                href="/register"
                style={{
                  padding: '11px 22px',
                  borderRadius: '14px',
                  fontSize: '13.5px',
                  fontWeight: 750,
                  background: 'var(--purple)',
                  color: '#ffffff',
                  textDecoration: 'none',
                  boxShadow: '0 4px 16px rgba(101, 91, 220, 0.35)',
                  transition: 'all 0.2s ease',
                }}
              >
                Đăng ký tài khoản miễn phí →
              </Link>
              <Link
                href="/login"
                style={{
                  padding: '11px 20px',
                  borderRadius: '14px',
                  fontSize: '13.5px',
                  fontWeight: 700,
                  background: 'var(--surface)',
                  border: '1px solid var(--border)',
                  color: 'var(--text)',
                  textDecoration: 'none',
                  transition: 'all 0.2s ease',
                }}
              >
                Đăng nhập
              </Link>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer
        style={{
          textAlign: 'center',
          padding: '24px 20px',
          fontSize: '12px',
          color: 'var(--muted)',
          zIndex: 10,
          borderTop: '1px solid var(--border)',
        }}
      >
        <span>© FinLux Project • Nền tảng Quản trị Tài chính Cá nhân • 100% Free &amp; Bảo mật Dữ liệu</span>
      </footer>
    </div>
  );
}
