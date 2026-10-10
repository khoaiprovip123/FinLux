'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import { useFinance } from '@/context/FinanceContext';

interface GuestFinanceViewProps {
  onOpenAuth?: () => void;
}

// Danh ngôn tài chính tinh hoa
const FINANCIAL_QUOTES = [
  {
    quote: 'Sự giàu có thực sự không nằm ở những gì người khác nhìn thấy, mà nằm ở quyền tự do được đưa ra lựa chọn mỗi ngày.',
    author: 'Morgan Housel',
    work: 'Tâm lý học về tiền',
  },
  {
    quote: 'Đừng tiết kiệm những gì còn lại sau khi chi tiêu. Hãy chi tiêu những gì còn lại sau khi đã tiết kiệm.',
    author: 'Warren Buffett',
    work: 'Thư gửi cổ đông',
  },
  {
    quote: 'Tự do tài chính không phải là sở hữu mọi thứ, mà là làm chủ hoàn toàn thời gian của chính mình.',
    author: 'Naval Ravikant',
    work: 'The Almanack of Naval',
  },
  {
    quote: 'Bản chất của quản trị tài chính không phải là vượt qua người khác, mà là làm chủ kỷ luật của chính bản thân.',
    author: 'Benjamin Graham',
    work: 'Nhà đầu tư thông minh',
  },
];

export default function GuestFinanceView({ onOpenAuth }: GuestFinanceViewProps) {
  const router = useRouter();
  const { theme, toggleTheme } = useFinance();
  const [currentQuoteIndex, setCurrentQuoteIndex] = useState(0);
  const [scrollProgress, setScrollProgress] = useState(0);

  // Theo dõi tiến trình cuộn trang để cập nhật thanh line
  useEffect(() => {
    const handleScroll = () => {
      const scrollY = window.scrollY;
      const totalHeight = document.documentElement.scrollHeight - window.innerHeight;
      if (totalHeight > 0) {
        const percent = Math.min(100, Math.max(0, (scrollY / totalHeight) * 100));
        setScrollProgress(percent);
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll();
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Biến đổi màu sắc line theo từng vị trí cuộn trang (và đa sắc rực rỡ khi full thanh)
  const getProgressColor = (percent: number) => {
    if (percent < 20) return 'linear-gradient(90deg, #8b5cf6, #a78bfa)'; // Vị trí đầu: Tím neon Atelier
    if (percent < 45) return 'linear-gradient(90deg, #8b5cf6, #06b6d4)'; // Vị trí 2: Tím sang Cyan
    if (percent < 70) return 'linear-gradient(90deg, #06b6d4, #10b981)'; // Vị trí 3: Cyan sang Lục bảo
    if (percent < 90) return 'linear-gradient(90deg, #10b981, #f59e0b)'; // Vị trí 4: Lục bảo sang Hổ phách
    return 'linear-gradient(90deg, #8b5cf6, #06b6d4, #10b981, #f59e0b, #ec4899)'; // Kéo hết trang: Đa sắc full thanh rực rỡ
  };

  // Auto rotate quotes every 7 seconds
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentQuoteIndex((prev) => (prev + 1) % FINANCIAL_QUOTES.length);
    }, 7000);
    return () => clearInterval(timer);
  }, []);

  const scrollToSection = (id: string) => {
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="fx-scene-atelier">
      {/* Subtle Micro-Grain Texture Overlay */}
      <div className="fx-grain" aria-hidden="true" />

      {/* Ambient Floating Halos & Astronomical Orbits */}
      <div aria-hidden="true">
        <div className="fx-halo fx-halo-a" />
        <div className="fx-halo fx-halo-b" />
        <div className="fx-halo fx-halo-c" />
        <div className="fx-orbit fx-orbit-a" />
        <div className="fx-orbit fx-orbit-b" />
      </div>

      {/* ============================================================== */}
      {/* 1. SITE HEADER (Fixed Spacing, Navigation, Theme & Login)       */}
      {/* ============================================================== */}
      <header
        className="fx-guest-header"
        style={{
          background: theme === 'light' ? 'rgba(251, 253, 255, 0.92)' : 'rgba(9, 13, 26, 0.92)',
        }}
      >
        {/* Brand: Pure App Icon + Clean Typography */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div
            style={{
              width: '38px',
              height: '38px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <Image
              src="/finlux_logo.png"
              alt="FinLux App Logo"
              width={38}
              height={38}
              priority
              style={{
                width: '100%',
                height: '100%',
                objectFit: 'contain',
              }}
            />
          </div>
          <div>
            <div
              style={{
                fontSize: '17px',
                fontWeight: 850,
                letterSpacing: '-0.4px',
                lineHeight: 1.1,
                color: 'var(--text)',
              }}
            >
              FINLUX
            </div>
            <div
              className="fx-eyebrow"
              style={{
                fontSize: '8.5px',
                letterSpacing: '0.2em',
                color: 'var(--muted)',
                marginTop: '2px',
              }}
            >
              DIGITAL FINANCIAL ATELIER
            </div>
          </div>
        </div>

        {/* Center: Explicitly Spaced Navigation Menu (Hidden on mobile) */}
        <nav aria-label="Điều hướng chính" className="fx-guest-nav">
          <button
            type="button"
            onClick={() => scrollToSection('features')}
            className="fx-nav-link"
            style={{
              background: 'none',
              border: 'none',
              padding: '8px 12px',
              cursor: 'pointer',
              whiteSpace: 'nowrap',
            }}
          >
            Tính năng cốt lõi
          </button>
          <button
            type="button"
            onClick={() => scrollToSection('workflow')}
            className="fx-nav-link"
            style={{
              background: 'none',
              border: 'none',
              padding: '8px 12px',
              cursor: 'pointer',
              whiteSpace: 'nowrap',
            }}
          >
            Quy trình sử dụng
          </button>
          <button
            type="button"
            onClick={() => scrollToSection('ecosystem')}
            className="fx-nav-link"
            style={{
              background: 'none',
              border: 'none',
              padding: '8px 12px',
              cursor: 'pointer',
              whiteSpace: 'nowrap',
            }}
          >
            Đồng bộ Mobile &amp; Web
          </button>
          <button
            type="button"
            onClick={() => scrollToSection('highlights')}
            className="fx-nav-link"
            style={{
              background: 'none',
              border: 'none',
              padding: '8px 12px',
              cursor: 'pointer',
              whiteSpace: 'nowrap',
            }}
          >
            Tính năng nổi bật
          </button>
        </nav>

        {/* Right: Theme Toggle & Login Button */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <button
            className="fx-icon-btn"
            type="button"
            onClick={toggleTheme}
            aria-label="Đổi giao diện"
            title={theme === 'light' ? 'Chế độ tối' : 'Chế độ sáng'}
            style={{
              width: '38px',
              height: '38px',
              borderRadius: '12px',
              border: '1px solid var(--border)',
              background: 'transparent',
            }}
          >
            <FinluxIcon name={theme === 'light' ? 'moon' : 'sun'} />
          </button>

          <button
            type="button"
            onClick={() => (onOpenAuth ? onOpenAuth() : router.push('/login'))}
            className="fx-btn"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: theme === 'light' ? '#0f1423' : '#655bdc',
              color: '#ffffff',
              borderRadius: '999px',
              padding: '8px 18px',
              fontSize: '13px',
              fontWeight: 750,
              letterSpacing: '0.02em',
              border: 'none',
              boxShadow: '0 8px 24px rgba(15, 20, 35, 0.18)',
              cursor: 'pointer',
              transition: 'all 0.25s ease',
              whiteSpace: 'nowrap',
            }}
          >
            <FinluxIcon name="shield" />
            <span>Đăng nhập</span>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: '16px',
                height: '16px',
                borderRadius: '50%',
                background: 'rgba(255, 255, 255, 0.2)',
                fontSize: '10px',
                marginLeft: '1px',
              }}
            >
              →
            </span>
          </button>
        </div>

        {/* Dynamic Scroll Progress Indicator Line (chạy và đổi màu theo từng vị trí cuộn trang) */}
        <div
          aria-hidden="true"
          style={{
            position: 'absolute',
            bottom: '-1px',
            left: 0,
            width: '100%',
            height: '2.8px',
            background: theme === 'light' ? 'rgba(0, 0, 0, 0.04)' : 'rgba(255, 255, 255, 0.05)',
            pointerEvents: 'none',
          }}
        >
          <div
            style={{
              height: '100%',
              width: `${scrollProgress}%`,
              background: getProgressColor(scrollProgress),
              transition: 'width 0.1s linear, background 0.3s ease, box-shadow 0.3s ease',
              boxShadow: scrollProgress > 95
                ? '0 0 12px rgba(236, 72, 153, 0.75), 0 0 4px #ffffff'
                : scrollProgress > 50
                  ? '0 0 10px rgba(6, 182, 212, 0.5)'
                  : '0 0 8px rgba(139, 92, 246, 0.5)',
            }}
          />
        </div>
      </header>

      {/* Spacer for Fixed Header so content is never hidden behind it */}
      <div style={{ height: '76px' }} aria-hidden="true" />

      {/* ============================================================== */}
      {/* 2. HERO: Brand Statement + 3D Layered Phone Mockups (High Depth)*/}
      {/* ============================================================== */}
      <section className="fx-guest-hero-section">
        <div className="fx-guest-hero-grid">
          {/* LEFT: Headline, Quotes & CTAs */}
          <div>
            <div className="fx-eyebrow" style={{ marginBottom: '18px' }}>
              FINANCIAL ARCHITECTURE · LIQUID GLASS · SYSTEM INVARIANTS
            </div>

            <h1
              className="fx-editorial-title"
              style={{
                fontSize: 'clamp(46px, 10vw, 102px)',
                lineHeight: 0.9,
                letterSpacing: '-0.045em',
                margin: '0 0 18px',
              }}
            >
              FINLUX
            </h1>

            <h2
              style={{
                fontSize: 'clamp(22px, 5vw, 40px)',
                lineHeight: 1.25,
                letterSpacing: '-0.03em',
                fontWeight: 500,
                color: 'var(--text)',
                margin: '0 0 14px',
                maxWidth: '560px',
              }}
            >
              Hiểu đúng dòng tiền, làm chủ tài chính.
            </h2>

            <p
              style={{
                fontSize: '15px',
                lineHeight: 1.65,
                color: 'var(--muted)',
                margin: '0 0 26px',
                maxWidth: '480px',
              }}
            >
              Quản trị đa ví, ngân sách chu kỳ lương và dòng tiền tự do — minh bạch theo chuẩn kế toán kép.
            </p>

            {/* Inspiring Financial Quotes Box */}
            <div
              className="fx-quote-highlight fx-guest-quote-box"
              style={{
                background: theme === 'light' ? 'rgba(255, 255, 255, 0.85)' : 'rgba(255, 255, 255, 0.03)',
                border: '1px solid var(--border)',
                borderLeft: '4px solid #655bdc',
                backdropFilter: 'blur(16px)',
              }}
            >
              <p
                style={{
                  fontFamily: '"Instrument Serif", serif',
                  fontSize: 'clamp(19px, 4.2vw, 25px)',
                  fontStyle: 'italic',
                  lineHeight: 1.35,
                  color: 'var(--text)',
                  margin: '0 0 12px',
                  fontWeight: 400,
                }}
              >
                &ldquo;{FINANCIAL_QUOTES[currentQuoteIndex].quote}&rdquo;
              </p>

              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  paddingTop: '8px',
                }}
              >
                <div
                  className="fx-eyebrow"
                  style={{
                    fontSize: '11px',
                    color: 'var(--purple)',
                    letterSpacing: '0.08em',
                  }}
                >
                  — {FINANCIAL_QUOTES[currentQuoteIndex].author}
                  <span style={{ color: 'var(--muted)', fontWeight: 500, marginLeft: '6px' }}>
                    ({FINANCIAL_QUOTES[currentQuoteIndex].work})
                  </span>
                </div>

                {/* Quote Indicator Dots */}
                <div style={{ display: 'flex', gap: '6px' }}>
                  {FINANCIAL_QUOTES.map((_, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => setCurrentQuoteIndex(idx)}
                      aria-label={`Trích dẫn ${idx + 1}`}
                      style={{
                        width: idx === currentQuoteIndex ? '22px' : '7px',
                        height: '7px',
                        borderRadius: '4px',
                        background: idx === currentQuoteIndex ? 'var(--purple)' : 'var(--border)',
                        border: 'none',
                        cursor: 'pointer',
                        transition: 'all 0.25s ease',
                        padding: 0,
                      }}
                    />
                  ))}
                </div>
              </div>
            </div>

            {/* Hero CTAs */}
            <div className="fx-guest-hero-ctas">
              <button
                type="button"
                onClick={() => (onOpenAuth ? onOpenAuth() : router.push('/login'))}
                className="fx-btn fx-neon-btn"
                style={{
                  padding: '14px 28px',
                  borderRadius: '16px',
                  fontSize: '14px',
                  fontWeight: 750,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  cursor: 'pointer',
                }}
              >
                <span>Bắt đầu quản trị tài chính</span>
                <span>→</span>
              </button>

              <button
                type="button"
                onClick={() => scrollToSection('features')}
                className="fx-btn"
                style={{
                  padding: '13px 22px',
                  borderRadius: '16px',
                  fontSize: '14px',
                  fontWeight: 650,
                  background: 'transparent',
                  border: '1px solid var(--border)',
                  color: 'var(--text)',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <span>Khám phá tính năng</span>
                <span>↓</span>
              </button>
            </div>
          </div>

          {/* RIGHT: Centered Digital Atelier Orbit Portal (Desktop: 3D Orbit, Mobile: Liquid Glass Constellation) */}
          <div aria-label="FinLux Planetary Orbit Showcase">
            {/* 1. DESKTOP 3D ORBITAL STAGE (Screen >= 860px) */}
            <div className="fx-orbit-stage fx-orbit-desktop" aria-label="FinLux Planetary Orbit Portal">
              {/* Concentric Circular Orbital Tracks centered on the logo */}
              <div className="fx-orbit-track fx-orbit-track-inner" />
              <div className="fx-orbit-track fx-orbit-track-outer" />

              {/* DEAD CENTER: Pure FinLux Brand Logo (No Surrounding Box/Border) */}
              <div
                style={{
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%)',
                  zIndex: 5,
                  width: '180px',
                  height: '180px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  animation: 'float 7.5s ease-in-out infinite',
                }}
              >
                <Image
                  src="/finlux_logo.png"
                  alt="FinLux Brand Icon"
                  width={180}
                  height={180}
                  priority
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain',
                    filter: 'drop-shadow(0 16px 36px rgba(10, 25, 60, 0.12))',
                  }}
                />
              </div>

              {/* 5 Planetary Orbit Badges */}
              {/* 1. Free Cash Flow (Cyan Star) */}
              <div
                className="fx-orbit-card fx-orbit-card-1"
                style={{ ['--star-glow' as any]: '#06b6d4' }}
              >
                <div className="fx-orbit-card-inner">
                  <span
                    className="fx-star-glint"
                    style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '9px',
                      background: 'linear-gradient(135deg, #06b6d4, #3b82f6)',
                      display: 'grid',
                      placeItems: 'center',
                      boxShadow: '0 4px 14px rgba(6, 182, 212, 0.5)',
                      color: '#ffffff',
                      flexShrink: 0,
                    }}
                  >
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M6 3h12l4 6-10 12L2 9z" />
                      <path d="M11 3 8 9l4 12 4-12-3-6" />
                      <path d="M2 9h20" />
                    </svg>
                  </span>
                  <span>Dòng tiền Tự do (Free Cash Flow)</span>
                </div>
              </div>

              {/* 2. Salary Budget (Purple Nebula Star) */}
              <div
                className="fx-orbit-card fx-orbit-card-2"
                style={{ ['--star-glow' as any]: '#8b5cf6' }}
              >
                <div className="fx-orbit-card-inner">
                  <span
                    className="fx-star-glint"
                    style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '9px',
                      background: 'linear-gradient(135deg, #8b5cf6, #6366f1)',
                      display: 'grid',
                      placeItems: 'center',
                      boxShadow: '0 4px 14px rgba(139, 92, 246, 0.5)',
                      color: '#ffffff',
                      flexShrink: 0,
                    }}
                  >
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10" />
                      <circle cx="12" cy="12" r="5" />
                      <line x1="12" y1="2" x2="12" y2="5" />
                      <line x1="12" y1="19" x2="12" y2="22" />
                    </svg>
                  </span>
                  <span>Ngân sách Chu kỳ Lương</span>
                </div>
              </div>

              {/* 3. Ledger Invariant (Emerald Shield Star) */}
              <div
                className="fx-orbit-card fx-orbit-card-3"
                style={{ ['--star-glow' as any]: '#10b981' }}
              >
                <div className="fx-orbit-card-inner">
                  <span
                    className="fx-star-glint"
                    style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '9px',
                      background: 'linear-gradient(135deg, #10b981, #059669)',
                      display: 'grid',
                      placeItems: 'center',
                      boxShadow: '0 4px 14px rgba(16, 185, 129, 0.5)',
                      color: '#ffffff',
                      flexShrink: 0,
                    }}
                  >
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                      <path d="m9 12 2 2 4-4" />
                    </svg>
                  </span>
                  <span>Bảo toàn Kế toán Kép</span>
                </div>
              </div>

              {/* 4. Real-time Cloud (Amber Lightning Star) */}
              <div
                className="fx-orbit-card fx-orbit-card-4"
                style={{ ['--star-glow' as any]: '#f59e0b' }}
              >
                <div className="fx-orbit-card-inner">
                  <span
                    className="fx-star-glint"
                    style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '9px',
                      background: 'linear-gradient(135deg, #f59e0b, #ea580c)',
                      display: 'grid',
                      placeItems: 'center',
                      boxShadow: '0 4px 14px rgba(245, 158, 11, 0.5)',
                      color: '#ffffff',
                      flexShrink: 0,
                    }}
                  >
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                    </svg>
                  </span>
                  <span>Đồng bộ Real-time Mobile &amp; Web</span>
                </div>
              </div>

              {/* 5. Zero Double-Counting (Sapphire Star) */}
              <div
                className="fx-orbit-card fx-orbit-card-5"
                style={{ ['--star-glow' as any]: '#0284c7' }}
              >
                <div className="fx-orbit-card-inner">
                  <span
                    className="fx-star-glint"
                    style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '9px',
                      background: 'linear-gradient(135deg, #0284c7, #2563eb)',
                      display: 'grid',
                      placeItems: 'center',
                      boxShadow: '0 4px 14px rgba(2, 132, 199, 0.4)',
                      color: '#ffffff',
                      flexShrink: 0,
                    }}
                  >
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M18.178 8c5.096 0 5.096 8 0 8-5.095 0-7.267-8-12.362-8-5.096 0-5.096 8 0 8 5.095 0 7.267-8 12.362-8z" />
                    </svg>
                  </span>
                  <span>Zero Double-Counting</span>
                </div>
              </div>
            </div>

            {/* 2. MOBILE NATIVE CONSTELLATION: Compact Centered Logo + 5 Liquid Glass Invariant Chips */}
            <div className="fx-orbit-mobile" aria-label="FinLux Mobile Feature Showcase">
              <div className="fx-mobile-logo-wrap">
                <div className="fx-mobile-logo-halo" />
                <Image
                  src="/finlux_logo.png"
                  alt="FinLux Brand Icon"
                  width={110}
                  height={110}
                  priority
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain',
                    filter: 'drop-shadow(0 10px 24px rgba(10, 25, 60, 0.16))',
                  }}
                />
              </div>

              <div className="fx-mobile-badges-grid">
                <div className="fx-mobile-badge" style={{ ['--star-glow' as any]: '#06b6d4' }}>
                  <span className="fx-mobile-badge-icon" style={{ background: 'linear-gradient(135deg, #06b6d4, #3b82f6)' }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M6 3h12l4 6-10 12L2 9z" />
                      <path d="M11 3 8 9l4 12 4-12-3-6" />
                      <path d="M2 9h20" />
                    </svg>
                  </span>
                  <span>Dòng tiền Tự do (FCF)</span>
                </div>

                <div className="fx-mobile-badge" style={{ ['--star-glow' as any]: '#8b5cf6' }}>
                  <span className="fx-mobile-badge-icon" style={{ background: 'linear-gradient(135deg, #8b5cf6, #6366f1)' }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10" />
                      <circle cx="12" cy="12" r="5" />
                    </svg>
                  </span>
                  <span>Ngân sách Chu kỳ Lương</span>
                </div>

                <div className="fx-mobile-badge" style={{ ['--star-glow' as any]: '#10b981' }}>
                  <span className="fx-mobile-badge-icon" style={{ background: 'linear-gradient(135deg, #10b981, #059669)' }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                      <path d="m9 12 2 2 4-4" />
                    </svg>
                  </span>
                  <span>Bảo toàn Kế toán Kép</span>
                </div>

                <div className="fx-mobile-badge" style={{ ['--star-glow' as any]: '#f59e0b' }}>
                  <span className="fx-mobile-badge-icon" style={{ background: 'linear-gradient(135deg, #f59e0b, #ea580c)' }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                    </svg>
                  </span>
                  <span>Đồng bộ Real-time</span>
                </div>

                <div className="fx-mobile-badge fx-mobile-badge-span" style={{ ['--star-glow' as any]: '#0284c7' }}>
                  <span className="fx-mobile-badge-icon" style={{ background: 'linear-gradient(135deg, #0284c7, #2563eb)' }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M18.178 8c5.096 0 5.096 8 0 8-5.095 0-7.267-8-12.362-8-5.096 0-5.096 8 0 8 5.095 0 7.267-8 12.362-8z" />
                    </svg>
                  </span>
                  <span>Zero Double-Counting (Chống tính trùng thẻ)</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================================== */}
      {/* 3. SECTION 1: CORE ARCHITECTURE & FEATURES                     */}
      {/* ============================================================== */}
      <section
        id="features"
        className="fx-guest-section"
      >
        <div style={{ marginBottom: '56px' }}>
          <div className="fx-eyebrow" style={{ color: 'var(--purple)', marginBottom: '14px' }}>
            01 / KIẾN TRÚC CỐT LÕI
          </div>
          <h2
            className="fx-editorial-title"
            style={{ fontSize: 'clamp(36px, 4.5vw, 64px)', margin: '0 0 18px' }}
          >
            Bốn trụ cột kiến tạo tự do tài chính.
          </h2>
          <p style={{ fontSize: '15px', color: 'var(--muted)', maxWidth: '640px', margin: 0, lineHeight: 1.75 }}>
            Mỗi module trong FinLux được thiết kế dựa trên quy chuẩn kế toán khắt khe, triệt tiêu sự mập mờ và giúp bạn nắm trọn quyền kiểm soát tiền bạc.
          </p>
        </div>

        <div className="fx-pillars-grid">
          {/* Pillar 01 */}
          <div
            className="fx-pillar-card"
            style={{
              ['--pillar-accent' as any]: 'var(--purple)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '28px',
                  fontWeight: 800,
                  color: 'var(--purple)',
                  letterSpacing: '-0.02em',
                  lineHeight: 1,
                }}
              >
                01
              </span>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '10px',
                  fontWeight: 750,
                  letterSpacing: '0.08em',
                  padding: '4px 10px',
                  borderRadius: '100px',
                  background: 'var(--purple-soft)',
                  color: 'var(--purple)',
                }}
              >
                CỐT LÕI FCF
              </span>
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: 850, margin: '0 0 10px', lineHeight: 1.35, color: 'var(--text)' }}>
              Dòng tiền Tự do (Free Cash Flow)
            </h3>
            <p style={{ fontSize: '13.5px', color: 'var(--muted)', margin: '0 0 22px', lineHeight: 1.65, flexGrow: 1 }}>
              Tách bạch triệt để chi tiêu thiết yếu khỏi tiền tích lũy và trả nợ gốc. Nhìn thấy chính xác số tiền thực tế bạn có toàn quyền định đoạt mỗi tháng.
            </p>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                paddingTop: '14px',
                borderTop: '1px solid var(--border)',
                fontSize: '12px',
                fontWeight: 650,
                color: 'var(--purple)',
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--purple)' }} />
              <span>Làm chủ dòng tiền thặng dư</span>
            </div>
          </div>

          {/* Pillar 02 */}
          <div
            className="fx-pillar-card"
            style={{
              ['--pillar-accent' as any]: '#06b6d4',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '28px',
                  fontWeight: 800,
                  color: '#06b6d4',
                  letterSpacing: '-0.02em',
                  lineHeight: 1,
                }}
              >
                02
              </span>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '10px',
                  fontWeight: 750,
                  letterSpacing: '0.08em',
                  padding: '4px 10px',
                  borderRadius: '100px',
                  background: 'rgba(6, 182, 212, 0.12)',
                  color: '#06b6d4',
                }}
              >
                CHU KỲ LƯƠNG
              </span>
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: 850, margin: '0 0 10px', lineHeight: 1.35, color: 'var(--text)' }}>
              Ngân sách Chu kỳ Lương (Salary Cycle)
            </h3>
            <p style={{ fontSize: '13.5px', color: 'var(--muted)', margin: '0 0 22px', lineHeight: 1.65, flexGrow: 1 }}>
              Không còn gò bó theo tháng dương lịch. Thiết lập chu kỳ tài chính từ ngày nhận lương đến kỳ lương kế tiếp để không bao giờ bị hụt tiền trước ngày có lương.
            </p>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                paddingTop: '14px',
                borderTop: '1px solid var(--border)',
                fontSize: '12px',
                fontWeight: 650,
                color: '#06b6d4',
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#06b6d4' }} />
              <span>Chấm dứt hụt tiền cuối tháng</span>
            </div>
          </div>

          {/* Pillar 03 */}
          <div
            className="fx-pillar-card"
            style={{
              ['--pillar-accent' as any]: 'var(--green)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '28px',
                  fontWeight: 800,
                  color: 'var(--green)',
                  letterSpacing: '-0.02em',
                  lineHeight: 1,
                }}
              >
                03
              </span>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '10px',
                  fontWeight: 750,
                  letterSpacing: '0.08em',
                  padding: '4px 10px',
                  borderRadius: '100px',
                  background: 'var(--green-soft)',
                  color: 'var(--green)',
                }}
              >
                KẾ TOÁN KÉP
              </span>
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: 850, margin: '0 0 10px', lineHeight: 1.35, color: 'var(--text)' }}>
              Bảo toàn Kế toán &amp; Zero Double-Counting
            </h3>
            <p style={{ fontSize: '13.5px', color: 'var(--muted)', margin: '0 0 22px', lineHeight: 1.65, flexGrow: 1 }}>
              Thanh toán sao kê thẻ tín dụng hay luân chuyển ví được ghi nhận là hoán đổi tài sản. Hệ thống tuyệt đối không tính trùng thành chi phí sinh hoạt.
            </p>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                paddingTop: '14px',
                borderTop: '1px solid var(--border)',
                fontSize: '12px',
                fontWeight: 650,
                color: 'var(--green)',
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--green)' }} />
              <span>Khớp 100% số dư thực tế</span>
            </div>
          </div>

          {/* Pillar 04 */}
          <div
            className="fx-pillar-card"
            style={{
              ['--pillar-accent' as any]: '#f59e0b',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '28px',
                  fontWeight: 800,
                  color: '#f59e0b',
                  letterSpacing: '-0.02em',
                  lineHeight: 1,
                }}
              >
                04
              </span>
              <span
                style={{
                  fontFamily: '"Space Mono", monospace',
                  fontSize: '10px',
                  fontWeight: 750,
                  letterSpacing: '0.08em',
                  padding: '4px 10px',
                  borderRadius: '100px',
                  background: 'rgba(245, 158, 11, 0.12)',
                  color: '#f59e0b',
                }}
              >
                MINH BẠCH BÁO CÁO
              </span>
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: 850, margin: '0 0 10px', lineHeight: 1.35, color: 'var(--text)' }}>
              Báo cáo Trực quan &amp; Động lực Tích lũy
            </h3>
            <p style={{ fontSize: '13.5px', color: 'var(--muted)', margin: '0 0 22px', lineHeight: 1.65, flexGrow: 1 }}>
              Trực quan hóa cấu trúc thu chi đa chiều, phân tích tỷ lệ tích lũy ròng và tích hợp mini-game Saving Spin tiếp thêm niềm vui tích lũy mỗi ngày.
            </p>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                paddingTop: '14px',
                borderTop: '1px solid var(--border)',
                fontSize: '12px',
                fontWeight: 650,
                color: '#f59e0b',
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#f59e0b' }} />
              <span>Tăng trưởng tài sản bền vững</span>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================================== */}
      {/* 4. SECTION 2: WORKFLOW (3 Bước Kiến tạo Tự do)                 */}
      {/* ============================================================== */}
      <section
        id="workflow"
        className="fx-guest-section"
        style={{ borderTop: '1px solid var(--border)' }}
      >
        <div style={{ marginBottom: '56px' }}>
          <div className="fx-eyebrow" style={{ color: 'var(--purple)', marginBottom: '14px' }}>
            02 / QUY TRÌNH TINH GỌN
          </div>
          <h2
            className="fx-editorial-title"
            style={{ fontSize: 'clamp(36px, 4.5vw, 64px)', margin: '0 0 18px' }}
          >
            Ba bước để bắt đầu làm chủ dòng tiền.
          </h2>
          <p style={{ fontSize: '15px', color: 'var(--muted)', maxWidth: '640px', margin: 0, lineHeight: 1.75 }}>
            Quy trình khởi tạo nhanh chóng, không rườm rà. Bạn có thể sử dụng ngay lập tức mà không cần kết nối tài khoản ngân hàng nhạy cảm.
          </p>
        </div>

        <div className="fx-workflow-grid">
          {/* Step 1 */}
          <div className="fx-workflow-card">
            <div
              style={{
                fontFamily: '"Space Mono", monospace',
                fontSize: '32px',
                fontWeight: 700,
                color: 'var(--purple)',
                marginBottom: '18px',
              }}
            >
              01
            </div>
            <h3 style={{ fontSize: '20px', fontWeight: 800, margin: '0 0 12px' }}>
              Khởi tạo Không gian Tài chính
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--muted)', margin: 0, lineHeight: 1.75 }}>
              Đăng nhập 1 chạm với Google hoặc Email. Không gian quản lý tài chính của bạn được kích hoạt tự động, an toàn và riêng tư tuyệt đối.
            </p>
          </div>

          {/* Step 2 */}
          <div className="fx-workflow-card">
            <div
              style={{
                fontFamily: '"Space Mono", monospace',
                fontSize: '32px',
                fontWeight: 700,
                color: 'var(--green)',
                marginBottom: '18px',
              }}
            >
              02
            </div>
            <h3 style={{ fontSize: '20px', fontWeight: 800, margin: '0 0 12px' }}>
              Thiết lập Ngân sách &amp; Mục tiêu
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--muted)', margin: 0, lineHeight: 1.75 }}>
              Chọn ngày nhận lương định kỳ, tạo hạn mức cho các danh mục sinh hoạt thiết yếu và tạo quỹ tích lũy dự phòng cho tương lai.
            </p>
          </div>

          {/* Step 3 */}
          <div className="fx-workflow-card">
            <div
              style={{
                fontFamily: '"Space Mono", monospace',
                fontSize: '32px',
                fontWeight: 700,
                color: '#06b6d4',
                marginBottom: '18px',
              }}
            >
              03
            </div>
            <h3 style={{ fontSize: '20px', fontWeight: 800, margin: '0 0 12px' }}>
              Ghi nhận &amp; Quan sát Dòng tiền
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--muted)', margin: 0, lineHeight: 1.75 }}>
              Nhập chi tiêu với bàn phím số thông minh, đối soát số dư đa ví và theo dõi chỉ số dòng tiền tự do tăng trưởng mỗi ngày.
            </p>
          </div>
        </div>
      </section>

      {/* ============================================================== */}
      {/* 5. SECTION 3: ECOSYSTEM SYNC (User Experience Focused)          */}
      {/* ============================================================== */}
      <section
        id="ecosystem"
        className="fx-guest-section"
        style={{ borderTop: '1px solid var(--border)' }}
      >
        <div className="fx-ecosystem-grid">
          <div>
            <div className="fx-eyebrow" style={{ color: 'var(--purple)', marginBottom: '14px' }}>
              03 / TRẢI NGHIỆM ĐỒNG BỘ
            </div>
            <h2
              className="fx-editorial-title"
              style={{ fontSize: 'clamp(36px, 4.5vw, 64px)', margin: '0 0 22px' }}
            >
              Mọi dữ liệu luôn bên bạn, trên mọi thiết bị.
            </h2>
            <p style={{ fontSize: '15px', color: 'var(--muted)', margin: '0 0 32px', lineHeight: 1.8 }}>
              Một tài khoản duy nhất kết nối liền mạch giữa điện thoại và máy tính. Bạn có thể ghi chép nhanh một ly cà phê trên đường đi, và xem lại toàn bộ báo cáo chi tiêu chi tiết khi ngồi trước màn hình lớn.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '18px', marginBottom: '36px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <span style={{ color: 'var(--green)', fontSize: '16px', fontWeight: 800 }}>✓</span>
                <span style={{ fontSize: '14.5px', fontWeight: 700 }}>Tự động cập nhật tức thì trên cả điện thoại và máy tính</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <span style={{ color: 'var(--green)', fontSize: '16px', fontWeight: 800 }}>✓</span>
                <span style={{ fontSize: '14.5px', fontWeight: 700 }}>Hoạt động mượt mà mọi lúc, không lo gián đoạn</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <span style={{ color: 'var(--green)', fontSize: '16px', fontWeight: 800 }}>✓</span>
                <span style={{ fontSize: '14.5px', fontWeight: 700 }}>Giao diện tinh tế, trực quan và dễ sử dụng</span>
              </div>
            </div>
          </div>

          {/* User-Centric Multiplatform Glass Card */}
          <div className="fx-card fx-hyper-glass fx-ecosystem-card">
            <div
              className="fx-app-icon-squircle"
              style={{
                width: '84px',
                height: '84px',
                borderRadius: '24px',
                padding: '14px',
                margin: '0 auto 22px',
              }}
            >
              <Image
                src="/finlux_logo.png"
                alt="FinLux App Icon"
                width={84}
                height={84}
                priority
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'contain',
                }}
              />
            </div>
            <h3 style={{ fontSize: '22px', fontWeight: 850, margin: '0 0 12px' }}>
              Một Không Gian Tài Chính Thống Nhất
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--muted)', margin: '0 0 32px', lineHeight: 1.7 }}>
              Không cần sao lưu thủ công hay chuyển đổi phức tạp. Bật ứng dụng lên và mọi thứ đã sẵn sàng cho bạn.
            </p>

            <div className="fx-ecosystem-subgrid">
              <div
                style={{
                  padding: '18px 20px',
                  borderRadius: '20px',
                  background: theme === 'light' ? '#ffffff' : 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid var(--border)',
                }}
              >
                <div style={{ fontSize: '11px', color: 'var(--muted)', fontWeight: 700 }}>TRÊN ĐIỆN THOẠI</div>
                <div style={{ fontSize: '15px', fontWeight: 800, marginTop: '6px' }}>Ghi chép nhanh</div>
                <div style={{ fontSize: '12px', color: 'var(--purple)', marginTop: '3px', fontWeight: 600 }}>Thuận tiện mọi lúc mọi nơi</div>
              </div>

              <div
                style={{
                  padding: '18px 20px',
                  borderRadius: '20px',
                  background: theme === 'light' ? '#ffffff' : 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid var(--border)',
                }}
              >
                <div style={{ fontSize: '11px', color: 'var(--muted)', fontWeight: 700 }}>TRÊN MÁY TÍNH</div>
                <div style={{ fontSize: '15px', fontWeight: 800, marginTop: '6px' }}>Kế hoạch toàn diện</div>
                <div style={{ fontSize: '12px', color: '#06b6d4', marginTop: '3px', fontWeight: 600 }}>Báo cáo &amp; Phân tích sâu</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================================== */}
      {/* 6. CALL TO ACTION BANNER (Gộp toàn diện tính năng & Bắt đầu)    */}
      {/* ============================================================== */}
      <section
        id="highlights"
        className="fx-guest-section"
        style={{ paddingBottom: '120px' }}
      >
        <div
          className="fx-card fx-hyper-glass fx-cta-banner-card"
          style={{
            background: theme === 'light'
              ? 'linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(240, 244, 255, 0.9))'
              : 'linear-gradient(135deg, rgba(26, 33, 56, 0.9), rgba(15, 19, 36, 0.95))',
            border: '1px solid rgba(101, 91, 220, 0.3)',
            boxShadow: '0 30px 80px rgba(101, 91, 220, 0.2)',
          }}
        >
          <div className="fx-eyebrow" style={{ color: 'var(--purple)', marginBottom: '16px', letterSpacing: '0.2em' }}>
            HỆ SINH THÁI TOÀN DIỆN • BẮT ĐẦU NGAY HÔM NAY
          </div>
          <h2
            className="fx-editorial-title"
            style={{ fontSize: 'clamp(38px, 5vw, 68px)', margin: '0 0 20px', lineHeight: 1.15 }}
          >
            Sẵn sàng làm chủ dòng tiền của bạn?
          </h2>
          <p
            style={{
              fontSize: '15.5px',
              color: 'var(--muted)',
              maxWidth: '720px',
              margin: '0 auto 36px',
              lineHeight: 1.8,
            }}
          >
            Vòng quay Saving Spin, hoạch định mục tiêu dài hạn, bàn phím số 3 giây, săn deal hoàn tiền cùng chế độ Liquid Glass — và nhiều tính năng hấp dẫn khác đang chờ bạn khám phá. Miễn phí trọn đời, đồng bộ tức thì.
          </p>

          <button
            type="button"
            onClick={() => (onOpenAuth ? onOpenAuth() : router.push('/login'))}
            className="fx-btn fx-neon-btn fx-cta-banner-btn"
          >
            <FinluxIcon name="shield" />
            <span>Đăng nhập hoặc Bắt đầu ngay</span>
            <span>→</span>
          </button>
        </div>
      </section>

      {/* ============================================================== */}
      {/* 7. FOOTER                                                      */}
      {/* ============================================================== */}
      <footer
        className="fx-glass-strip fx-guest-footer"
        style={{
          borderTop: '1px solid var(--border)',
          position: 'relative',
          zIndex: 10,
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div
            className="fx-app-icon-squircle"
            style={{ width: '32px', height: '32px', borderRadius: '10px', padding: '4px' }}
          >
            <Image src="/finlux_logo.png" alt="FinLux" width={32} height={32} style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
          </div>
          <span style={{ fontSize: '13px', fontWeight: 800 }}>FinLux</span>
          <span style={{ fontSize: '12px', color: 'var(--muted)' }}>• Quản trị Tài chính Cá nhân Tinh hoa</span>
        </div>

        {/* GitHub Developers Credits */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '12px', color: 'var(--muted)', fontWeight: 550 }}>Đồng phát triển:</span>
          
          <a
            href="https://github.com/khoaiprovip123"
            target="_blank"
            rel="noopener noreferrer"
            className="fx-footer-dev-link"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              fontSize: '12px',
              fontWeight: 650,
              color: 'var(--text)',
              textDecoration: 'none',
              padding: '4px 10px',
              borderRadius: '999px',
              background: 'rgba(255, 255, 255, 0.65)',
              border: '1px solid var(--border)',
              transition: 'all 0.2s ease',
            }}
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
            </svg>
            <span>khoaiprovip123</span>
          </a>

          <a
            href="https://github.com/thanhlongts2k"
            target="_blank"
            rel="noopener noreferrer"
            className="fx-footer-dev-link"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              fontSize: '12px',
              fontWeight: 650,
              color: 'var(--text)',
              textDecoration: 'none',
              padding: '4px 10px',
              borderRadius: '999px',
              background: 'rgba(255, 255, 255, 0.65)',
              border: '1px solid var(--border)',
              transition: 'all 0.2s ease',
            }}
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
            </svg>
            <span>Long Louis (thanhlongts2k)</span>
          </a>
        </div>

        <div style={{ fontSize: '12px', color: 'var(--muted)', fontWeight: 550 }}>
          © 2026 FinLux. Đồng bộ Android &amp; Web Platform.
        </div>
      </footer>
    </div>
  );
}
