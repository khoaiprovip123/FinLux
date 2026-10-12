'use client';

import React, { useState, useEffect, Suspense } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { useFinance } from '@/context/FinanceContext';
import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  updateProfile,
  sendPasswordResetEmail,
  GoogleAuthProvider,
  signInWithPopup,
} from 'firebase/auth';
import { auth, db } from '@/lib/firebase';
import {
  doc,
  setDoc,
  serverTimestamp,
  collection,
  getDocs,
} from 'firebase/firestore';

type AuthMode = 'login' | 'register' | 'forgot';

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, authLoading, theme, toggleTheme, showToast } = useFinance();

  const initialMode = (searchParams.get('mode') as AuthMode) || 'login';
  const [mode, setMode] = useState<AuthMode>(initialMode);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [agreedToTerms, setAgreedToTerms] = useState(true);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Đồng bộ mode nếu URL search param thay đổi
  useEffect(() => {
    const m = searchParams.get('mode') as AuthMode;
    if (m === 'register' || m === 'forgot' || m === 'login') {
      setMode(m);
    }
  }, [searchParams]);

  // Nếu người dùng đã đăng nhập, tự động chuyển về trang chính
  useEffect(() => {
    if (!authLoading && user) {
      router.replace('/');
    }
  }, [user, authLoading, router]);

  const switchMode = (newMode: AuthMode) => {
    setMode(newMode);
    setErrorMessage(null);
    const targetUrl = newMode === 'login' ? '/login' : `/login?mode=${newMode}`;
    router.replace(targetUrl, { scroll: false });
  };

  const handleGoogleSignIn = async () => {
    setErrorMessage(null);
    setLoading(true);

    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: 'select_account' });
      const result = await signInWithPopup(auth, provider);
      const googleUser = result.user;

      const name = googleUser.displayName || googleUser.email?.split('@')[0] || 'Người dùng Google';

      // 1. Seed/merge user doc
      await setDoc(
        doc(db, 'users', googleUser.uid),
        {
          displayName: name,
          email: googleUser.email,
          photoUrl: googleUser.photoURL || '',
          themePref: 'system',
          updatedAt: serverTimestamp(),
        },
        { merge: true }
      );

      // 2. Wallets: Seed cash wallet if user has 0 wallets
      const walletsSnap = await getDocs(collection(db, 'users', googleUser.uid, 'wallets'));
      if (walletsSnap.empty) {
        await setDoc(doc(db, 'users', googleUser.uid, 'wallets', 'cash'), {
          name: 'Tiền mặt',
          type: 'cash',
          balance: 0,
          color: '#1F6FBF',
          isDefault: true,
          createdAt: serverTimestamp(),
        });
      }

      showToast('Đăng nhập Google thành công! Đang chuyển hướng...');
      router.replace('/');
    } catch (err: unknown) {
      const error = err as { code?: string; message?: string };
      console.error('Google sign-in error:', error);
      if (
        error.code === 'auth/popup-closed-by-user' ||
        error.code === 'auth/cancelled-popup-request'
      ) {
        return;
      }
      if (error.code === 'auth/popup-blocked') {
        setErrorMessage('Trình duyệt đã chặn cửa sổ đăng nhập Google. Vui lòng cấp quyền mở popup.');
      } else if (error.code === 'auth/account-exists-with-different-credential') {
        setErrorMessage('Tài khoản này đã được liên kết với một phương thức đăng nhập khác.');
      } else if (error.code === 'auth/unauthorized-domain') {
        setErrorMessage(
          'Tên miền chưa được thêm vào Authorized Domains trên Firebase Console. Vui lòng truy cập Firebase Console > Authentication > Settings để thêm finlux-0wxc.onrender.com.'
        );
      } else {
        setErrorMessage(error.message || 'Đăng nhập Google thất bại. Vui lòng thử lại.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setLoading(true);

    try {
      await signInWithEmailAndPassword(auth, email.trim(), password);
      showToast('Đăng nhập thành công! Đang chuyển hướng...');
      router.replace('/');
    } catch (err: unknown) {
      const error = err as { code?: string; message?: string };
      console.error('Login error:', error);
      if (
        error.code === 'auth/invalid-credential' ||
        error.code === 'auth/user-not-found' ||
        error.code === 'auth/wrong-password'
      ) {
        setErrorMessage('Email hoặc mật khẩu không chính xác.');
      } else if (error.code === 'auth/operation-not-allowed') {
        setErrorMessage('Phương thức đăng nhập Email/Mật khẩu chưa được bật trên Firebase Console (Authentication > Sign-in method). Vui lòng dùng "Tiếp tục với Google" hoặc liên hệ Admin.');
      } else if (error.code === 'auth/invalid-email') {
        setErrorMessage('Định dạng email không hợp lệ.');
      } else if (error.code === 'auth/too-many-requests') {
        setErrorMessage('Quá nhiều lần thử thất bại. Vui lòng thử lại sau ít phút.');
      } else {
        setErrorMessage(error.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (password.length < 6) {
      setErrorMessage('Mật khẩu phải có ít nhất 6 ký tự.');
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage('Mật khẩu xác nhận không khớp.');
      return;
    }
    if (!agreedToTerms) {
      setErrorMessage('Vui lòng đồng ý với Điều khoản dịch vụ & Chính sách bảo mật của FinLux để tiếp tục.');
      return;
    }

    setLoading(true);

    try {
      const userCredential = await createUserWithEmailAndPassword(auth, email.trim(), password);
      const newUser = userCredential.user;

      const name = displayName.trim() || email.split('@')[0];
      await updateProfile(newUser, { displayName: name });

      await setDoc(
        doc(db, 'users', newUser.uid),
        {
          displayName: name,
          email: newUser.email,
          createdAt: serverTimestamp(),
          themePref: 'system',
        },
        { merge: true }
      );

      const walletsSnap = await getDocs(collection(db, 'users', newUser.uid, 'wallets'));
      if (walletsSnap.empty) {
        await setDoc(doc(db, 'users', newUser.uid, 'wallets', 'cash'), {
          name: 'Tiền mặt',
          type: 'cash',
          balance: 0,
          color: '#1F6FBF',
          isDefault: true,
          createdAt: serverTimestamp(),
        });
      }

      showToast('Đăng ký tài khoản thành công! Đang chuyển hướng...');
      router.replace('/');
    } catch (err: unknown) {
      const error = err as { code?: string; message?: string };
      console.error('Register error:', error);
      if (error.code === 'auth/email-already-in-use') {
        setErrorMessage('Email này đã được đăng ký. Bạn có thể chuyển sang Đăng nhập ngay.');
      } else if (error.code === 'auth/operation-not-allowed') {
        setErrorMessage('Phương thức đăng ký Email chưa được kích hoạt trên Firebase Console (Authentication > Sign-in method). Vui lòng dùng "Tiếp tục với Google" hoặc liên hệ Admin.');
      } else if (error.code === 'auth/invalid-email') {
        setErrorMessage('Địa chỉ email không hợp lệ.');
      } else if (error.code === 'auth/weak-password') {
        setErrorMessage('Mật khẩu quá yếu. Vui lòng chọn mật khẩu từ 6 ký tự trở lên.');
      } else {
        setErrorMessage(error.message || 'Không thể tạo tài khoản. Vui lòng thử lại.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    if (!email.trim()) {
      setErrorMessage('Vui lòng nhập địa chỉ email của bạn.');
      return;
    }

    setLoading(true);
    try {
      await sendPasswordResetEmail(auth, email.trim());
      showToast('Đã gửi email khôi phục mật khẩu! Vui lòng kiểm tra hộp thư.');
      switchMode('login');
    } catch (err: unknown) {
      const error = err as { code?: string; message?: string };
      console.error('Password reset error:', error);
      if (error.code === 'auth/user-not-found') {
        setErrorMessage('Không tìm thấy tài khoản với email này.');
      } else {
        setErrorMessage(error.message || 'Không thể gửi email đặt lại mật khẩu.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="fx-scene-atelier"
      data-theme={theme}
      style={{
        minHeight: '100dvh',
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
            href="/terms"
            style={{
              padding: '9px 16px',
              borderRadius: '999px',
              fontSize: '12.5px',
              fontWeight: 650,
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              color: 'var(--muted)',
              textDecoration: 'none',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.04)',
              transition: 'all 0.2s ease',
            }}
          >
            <span>🛡️</span>
            <span>Điều khoản &amp; Bảo mật</span>
          </Link>

          <Link
            href="/"
            style={{
              padding: '9px 18px',
              borderRadius: '999px',
              fontSize: '13px',
              fontWeight: 650,
              background: 'rgba(255, 255, 255, 0.7)',
              border: '1px solid var(--border)',
              color: 'var(--text)',
              textDecoration: 'none',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              backdropFilter: 'blur(12px)',
              transition: 'all 0.2s ease',
            }}
          >
            <span>← Trang chủ</span>
          </Link>
        </div>
      </header>

      {/* Main Authentication Center Container */}
      <main
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '24px 16px max(64px, calc(32px + env(safe-area-inset-bottom, 24px)))',
          zIndex: 10,
          position: 'relative',
        }}
      >
        <div
          style={{
            width: '100%',
            maxWidth: '470px',
            borderRadius: '32px',
            background:
              theme === 'light'
                ? 'linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(246, 248, 255, 0.90))'
                : 'linear-gradient(180deg, rgba(22, 28, 48, 0.95), rgba(15, 20, 36, 0.92))',
            border: '1.5px solid rgba(255, 255, 255, 0.95)',
            boxShadow:
              theme === 'light'
                ? '0 24px 60px rgba(25, 45, 95, 0.10), 0 2px 2px rgba(255, 255, 255, 1) inset'
                : '0 28px 70px rgba(0, 0, 0, 0.55), 0 1px 1px rgba(255, 255, 255, 0.12) inset',
            backdropFilter: 'blur(30px)',
            WebkitBackdropFilter: 'blur(30px)',
            padding: '36px 32px',
          }}
        >
          {/* Card Brand Header */}
          <div style={{ textAlign: 'center', marginBottom: '22px' }}>
            <div
              style={{
                width: '64px',
                height: '64px',
                margin: '0 auto 12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Image
                src="/finlux_logo.png"
                alt="FinLux Brand Icon"
                width={64}
                height={64}
                priority
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'contain',
                  filter: 'drop-shadow(0 8px 20px rgba(101, 91, 220, 0.25))',
                }}
              />
            </div>

            <h1
              style={{
                fontSize: '22px',
                fontWeight: 800,
                letterSpacing: '-0.025em',
                color: 'var(--text)',
                margin: '0 0 6px',
              }}
            >
              {mode === 'login' && 'Đăng nhập FinLux'}
              {mode === 'register' && 'Đăng ký Tài khoản FinLux'}
              {mode === 'forgot' && 'Khôi phục Mật khẩu'}
            </h1>
            <div
              style={{
                fontSize: '11px',
                fontWeight: 700,
                color: 'var(--purple)',
                letterSpacing: '0.12em',
                textTransform: 'uppercase',
              }}
            >
              {mode === 'register' ? '100% Miễn phí • Đồng bộ Cloud tức thì' : 'Đồng bộ Cloud App & Web'}
            </div>
          </div>

          {/* Google Sign-in Primary Button */}
          {mode !== 'forgot' && (
            <>
              <button
                type="button"
                onClick={handleGoogleSignIn}
                disabled={loading}
                style={{
                  width: '100%',
                  padding: '13px 20px',
                  borderRadius: '16px',
                  background: 'var(--surface)',
                  border: '1.5px solid var(--border)',
                  color: 'var(--text)',
                  fontSize: '14px',
                  fontWeight: 700,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '12px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  boxShadow: '0 4px 16px rgba(0, 0, 0, 0.04)',
                  transition: 'all 0.25s ease',
                  opacity: loading ? 0.7 : 1,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-1.5px)';
                  e.currentTarget.style.boxShadow = '0 8px 24px rgba(101, 91, 220, 0.12)';
                  e.currentTarget.style.borderColor = 'var(--purple)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'none';
                  e.currentTarget.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.04)';
                  e.currentTarget.style.borderColor = 'var(--border)';
                }}
              >
                <svg width="20" height="20" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.05h3.9c2.28-2.1 3.645-5.18 3.645-9.15z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.9-3.05c-1.08.72-2.45 1.16-4.03 1.16-3.13 0-5.78-2.11-6.73-4.96H1.21v3.15C3.25 21.43 7.35 24 12 24z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.27 14.24c-.25-.72-.39-1.49-.39-2.24s.14-1.52.39-2.24V6.61H1.21C.44 8.14 0 9.97 0 12c0 2.03.44 3.86 1.21 5.39l4.06-3.15z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.35 0 3.25 2.57 1.21 6.61l4.06 3.15c.95-2.85 3.6-4.96 6.73-4.96z"
                  />
                </svg>
                <span>Tiếp tục với Google (Đồng bộ App)</span>
              </button>

              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  margin: '20px 0 16px',
                  gap: '12px',
                }}
              >
                <div style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 700,
                    color: 'var(--muted)',
                    letterSpacing: '0.08em',
                  }}
                >
                  HOẶC TÀI KHOẢN EMAIL
                </span>
                <div style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
              </div>

              {/* Mode Switcher Tabs */}
              <div
                style={{
                  display: 'flex',
                  padding: '4px',
                  background: 'var(--border)',
                  borderRadius: '14px',
                  marginBottom: '20px',
                  gap: '4px',
                }}
              >
                <button
                  type="button"
                  onClick={() => switchMode('login')}
                  style={{
                    flex: 1,
                    padding: '8px 12px',
                    borderRadius: '10px',
                    border: 'none',
                    fontSize: '12.5px',
                    fontWeight: 700,
                    cursor: 'pointer',
                    background: mode === 'login' ? 'var(--surface)' : 'transparent',
                    color: mode === 'login' ? 'var(--text)' : 'var(--muted)',
                    boxShadow: mode === 'login' ? '0 2px 8px rgba(0, 0, 0, 0.08)' : 'none',
                    transition: 'all 0.2s ease',
                  }}
                >
                  Đăng nhập
                </button>
                <button
                  type="button"
                  onClick={() => switchMode('register')}
                  style={{
                    flex: 1,
                    padding: '8px 12px',
                    borderRadius: '10px',
                    border: 'none',
                    fontSize: '12.5px',
                    fontWeight: 700,
                    cursor: 'pointer',
                    background: mode === 'register' ? 'var(--surface)' : 'transparent',
                    color: mode === 'register' ? 'var(--text)' : 'var(--muted)',
                    boxShadow: mode === 'register' ? '0 2px 8px rgba(0, 0, 0, 0.08)' : 'none',
                    transition: 'all 0.2s ease',
                  }}
                >
                  Đăng ký mới
                </button>
              </div>
            </>
          )}

          {/* Error Message Alert */}
          {errorMessage && (
            <div
              style={{
                padding: '12px 16px',
                borderRadius: '14px',
                background: 'rgba(239, 68, 68, 0.1)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                color: '#ef4444',
                fontSize: '12.5px',
                lineHeight: 1.5,
                fontWeight: 600,
                marginBottom: '18px',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '8px',
              }}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" style={{ flexShrink: 0, marginTop: '2px' }}>
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Form Handling */}
          <form
            onSubmit={
              mode === 'login'
                ? handleLogin
                : mode === 'register'
                  ? handleRegister
                  : handleForgotPassword
            }
          >
            {/* Display Name (Register only) */}
            {mode === 'register' && (
              <div style={{ marginBottom: '16px' }}>
                <label
                  style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: 700,
                    color: 'var(--muted)',
                    marginBottom: '6px',
                  }}
                >
                  Họ và tên
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="text"
                    placeholder="Ví dụ: Nguyễn Văn An"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '12px 14px 12px 38px',
                      borderRadius: '14px',
                      border: '1px solid var(--border)',
                      background: 'var(--surface)',
                      color: 'var(--text)',
                      fontSize: '14px',
                      outline: 'none',
                      boxSizing: 'border-box',
                    }}
                  />
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="var(--muted)"
                    strokeWidth="2"
                    style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }}
                  >
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </div>
              </div>
            )}

            {/* Email Address */}
            <div style={{ marginBottom: '16px' }}>
              <label
                style={{
                  display: 'block',
                  fontSize: '12px',
                  fontWeight: 700,
                  color: 'var(--muted)',
                  marginBottom: '6px',
                }}
              >
                Email tài khoản
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  type="email"
                  required
                  placeholder="example@gmail.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  inputMode="email"
                  autoCapitalize="none"
                  autoCorrect="off"
                  autoComplete="email"
                  style={{
                    width: '100%',
                    padding: '12px 14px 12px 38px',
                    borderRadius: '14px',
                    border: '1px solid var(--border)',
                    background: 'var(--surface)',
                    color: 'var(--text)',
                    fontSize: '14px',
                    outline: 'none',
                    boxSizing: 'border-box',
                  }}
                />
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="var(--muted)"
                  strokeWidth="2"
                  style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }}
                >
                  <rect x="2" y="4" width="20" height="16" rx="2" />
                  <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                </svg>
              </div>
            </div>

            {/* Password (Login & Register) */}
            {mode !== 'forgot' && (
              <div style={{ marginBottom: '16px' }}>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '6px',
                  }}
                >
                  <label
                    style={{
                      fontSize: '12px',
                      fontWeight: 700,
                      color: 'var(--muted)',
                    }}
                  >
                    Mật khẩu
                  </label>
                  {mode === 'login' && (
                    <button
                      type="button"
                      onClick={() => switchMode('forgot')}
                      style={{
                        background: 'none',
                        border: 'none',
                        color: 'var(--purple)',
                        fontSize: '11.5px',
                        fontWeight: 700,
                        cursor: 'pointer',
                        padding: 0,
                      }}
                    >
                      Quên mật khẩu?
                    </button>
                  )}
                </div>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '12px 42px 12px 38px',
                      borderRadius: '14px',
                      border: '1px solid var(--border)',
                      background: 'var(--surface)',
                      color: 'var(--text)',
                      fontSize: '14px',
                      outline: 'none',
                      boxSizing: 'border-box',
                    }}
                  />
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="var(--muted)"
                    strokeWidth="2"
                    style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }}
                  >
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label="Hiện mật khẩu"
                    style={{
                      position: 'absolute',
                      right: '12px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'none',
                      border: 'none',
                      color: 'var(--muted)',
                      cursor: 'pointer',
                      padding: '4px',
                    }}
                  >
                    {showPassword ? (
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                        <line x1="1" y1="1" x2="23" y2="23" />
                      </svg>
                    ) : (
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    )}
                  </button>
                </div>
              </div>
            )}

            {/* Confirm Password (Register only) */}
            {mode === 'register' && (
              <div style={{ marginBottom: '16px' }}>
                <label
                  style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: 700,
                    color: 'var(--muted)',
                    marginBottom: '6px',
                  }}
                >
                  Xác nhận mật khẩu
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    placeholder="••••••••"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '12px 14px 12px 38px',
                      borderRadius: '14px',
                      border: '1px solid var(--border)',
                      background: 'var(--surface)',
                      color: 'var(--text)',
                      fontSize: '14px',
                      outline: 'none',
                      boxSizing: 'border-box',
                    }}
                  />
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="var(--muted)"
                    strokeWidth="2"
                    style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }}
                  >
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                </div>
              </div>
            )}

            {/* Terms & Privacy Agreement Checkbox (Register only) */}
            {mode === 'register' && (
              <div
                style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '10px',
                  marginBottom: '20px',
                  padding: '10px 12px',
                  borderRadius: '12px',
                  background: 'rgba(101, 91, 220, 0.05)',
                  border: '1px solid rgba(101, 91, 220, 0.12)',
                }}
              >
                <input
                  type="checkbox"
                  id="reg-terms-check"
                  checked={agreedToTerms}
                  onChange={(e) => setAgreedToTerms(e.target.checked)}
                  style={{
                    marginTop: '2.5px',
                    width: '16px',
                    height: '16px',
                    accentColor: 'var(--purple)',
                    cursor: 'pointer',
                    flexShrink: 0,
                  }}
                />
                <label
                  htmlFor="reg-terms-check"
                  style={{
                    fontSize: '12px',
                    lineHeight: 1.5,
                    color: 'var(--text)',
                    cursor: 'pointer',
                  }}
                >
                  Tôi đồng ý với{' '}
                  <Link
                    href="/terms"
                    target="_blank"
                    style={{
                      color: 'var(--purple)',
                      fontWeight: 700,
                      textDecoration: 'underline',
                    }}
                  >
                    Điều khoản dịch vụ &amp; Chính sách bảo mật
                  </Link>{' '}
                  (100% Miễn phí trọn đời, cam kết không khai thác thương mại dữ liệu cá nhân).
                </label>
              </div>
            )}

            {/* Submit Primary CTA */}
            <button
              type="submit"
              disabled={loading}
              className="fx-btn fx-neon-btn"
              style={{
                width: '100%',
                padding: '14px 20px',
                borderRadius: '16px',
                fontSize: '14.5px',
                fontWeight: 800,
                cursor: loading ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                opacity: loading ? 0.7 : 1,
                marginTop: '8px',
              }}
            >
              {loading ? (
                <>
                  <div
                    style={{
                      width: '16px',
                      height: '16px',
                      borderRadius: '50%',
                      border: '2px solid rgba(255, 255, 255, 0.4)',
                      borderTopColor: '#ffffff',
                      animation: 'spin 0.8s linear infinite',
                    }}
                  />
                  <span>Đang xử lý...</span>
                </>
              ) : (
                <>
                  <span>
                    {mode === 'login' && 'Đăng nhập & Đồng bộ'}
                    {mode === 'register' && 'Tạo tài khoản & Bắt đầu Miễn phí'}
                    {mode === 'forgot' && 'Gửi link khôi phục mật khẩu'}
                  </span>
                  <span>→</span>
                </>
              )}
            </button>
          </form>

          {/* Mode Switch Helper Link */}
          <div style={{ textAlign: 'center', marginTop: '16px' }}>
            {mode === 'login' && (
              <span style={{ fontSize: '12.5px', color: 'var(--muted)' }}>
                Chưa có tài khoản?{' '}
                <button
                  type="button"
                  onClick={() => switchMode('register')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--purple)',
                    fontWeight: 750,
                    cursor: 'pointer',
                    padding: 0,
                    textDecoration: 'underline',
                  }}
                >
                  Đăng ký miễn phí ngay
                </button>
              </span>
            )}
            {mode === 'register' && (
              <span style={{ fontSize: '12.5px', color: 'var(--muted)' }}>
                Đã có tài khoản?{' '}
                <button
                  type="button"
                  onClick={() => switchMode('login')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--purple)',
                    fontWeight: 750,
                    cursor: 'pointer',
                    padding: 0,
                    textDecoration: 'underline',
                  }}
                >
                  Đăng nhập tại đây
                </button>
              </span>
            )}
            {mode === 'forgot' && (
              <button
                type="button"
                onClick={() => switchMode('login')}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--purple)',
                  fontSize: '12.5px',
                  fontWeight: 700,
                  cursor: 'pointer',
                }}
              >
                ← Quay lại Đăng nhập
              </button>
            )}
          </div>

          {/* Trust & Privacy Guarantee Card */}
          <div
            style={{
              marginTop: '24px',
              padding: '16px',
              borderRadius: '18px',
              background:
                theme === 'light'
                  ? 'rgba(101, 91, 220, 0.04)'
                  : 'rgba(101, 91, 220, 0.08)',
              border: '1px solid rgba(101, 91, 220, 0.15)',
              fontSize: '12px',
              lineHeight: 1.6,
              color: 'var(--muted)',
            }}
          >
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                fontWeight: 750,
                color: 'var(--text)',
                marginBottom: '8px',
                fontSize: '12.5px',
              }}
            >
              <span>🛡️</span>
              <span>Cam kết Bảo mật &amp; Quyền Riêng tư FinLux</span>
            </div>
            <ul style={{ margin: '0 0 10px', paddingLeft: '18px', fontSize: '11.5px', lineHeight: 1.65 }}>
              <li>
                <strong>100% Hoàn toàn Miễn phí:</strong> Không phí kích hoạt, không gói trả phí.
              </li>
              <li>
                <strong>Không khai thác dữ liệu:</strong> Dữ liệu chỉ dùng để hiển thị cho bạn, tuyệt đối không chia sẻ hoặc bán cho bên thứ ba.
              </li>
              <li>
                <strong>Đồng bộ thời gian thực:</strong> Dùng chung 100% với ứng dụng di động <strong>FinLux Android</strong> qua Google Firestore.
              </li>
            </ul>

            <div style={{ textAlign: 'center', paddingTop: '6px', borderTop: '1px solid rgba(101, 91, 220, 0.1)' }}>
              <Link
                href="/terms"
                style={{
                  color: 'var(--purple)',
                  fontWeight: 700,
                  fontSize: '11.5px',
                  textDecoration: 'none',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                }}
              >
                <span>Xem toàn văn Điều khoản &amp; Chính sách bảo mật</span>
                <span>→</span>
              </Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div
          style={{
            minHeight: '100vh',
            display: 'grid',
            placeItems: 'center',
            background: 'var(--bg)',
          }}
        >
          <div
            style={{
              width: '40px',
              height: '40px',
              borderRadius: '50%',
              border: '3px solid var(--border)',
              borderTopColor: 'var(--purple)',
              animation: 'spin 0.8s linear infinite',
            }}
          />
        </div>
      }
    >
      <LoginForm />
    </Suspense>
  );
}
