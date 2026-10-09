'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { FinluxIcon, FinluxLogo } from '@/components/icons/FinluxIcons';
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

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
}

type AuthMode = 'login' | 'register' | 'forgot';

export default function AuthModal({ isOpen, onClose }: AuthModalProps) {
  const { showToast } = useFinance();

  const [mode, setMode] = useState<AuthMode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const resetForm = () => {
    setEmail('');
    setPassword('');
    setConfirmPassword('');
    setDisplayName('');
    setErrorMessage(null);
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleGoogleSignIn = async () => {
    setErrorMessage(null);
    setLoading(true);

    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: 'select_account' });
      const result = await signInWithPopup(auth, provider);
      const user = result.user;

      const name = user.displayName || user.email?.split('@')[0] || 'Người dùng Google';

      // 1. Seed/merge user doc
      await setDoc(
        doc(db, 'users', user.uid),
        {
          displayName: name,
          email: user.email,
          photoUrl: user.photoURL || '',
          themePref: 'system',
          updatedAt: serverTimestamp(),
        },
        { merge: true }
      );

      // 2. Wallets: Seed cash wallet if user has 0 wallets
      const walletsSnap = await getDocs(collection(db, 'users', user.uid, 'wallets'));
      if (walletsSnap.empty) {
        await setDoc(doc(db, 'users', user.uid, 'wallets', 'cash'), {
          name: 'Tiền mặt',
          type: 'cash',
          balance: 0,
          color: '#1F6FBF',
          isDefault: true,
          createdAt: serverTimestamp(),
        });
      }

      showToast(`Đăng nhập Google thành công! Đang đồng bộ dữ liệu...`);
      handleClose();
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
      showToast('Đăng nhập thành công! Đang đồng bộ dữ liệu...');
      handleClose();
    } catch (err: unknown) {
      const error = err as { code?: string; message?: string };
      console.error('Login error:', error);
      if (
        error.code === 'auth/invalid-credential' ||
        error.code === 'auth/user-not-found' ||
        error.code === 'auth/wrong-password'
      ) {
        setErrorMessage('Email hoặc mật khẩu không chính xác.');
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

    setLoading(true);

    try {
      const userCredential = await createUserWithEmailAndPassword(auth, email.trim(), password);
      const user = userCredential.user;

      // Update display name
      const name = displayName.trim() || email.split('@')[0];
      await updateProfile(user, { displayName: name });

      // Seed initial user document in Firestore matching mobile app
      await setDoc(
        doc(db, 'users', user.uid),
        {
          displayName: name,
          email: user.email,
          createdAt: serverTimestamp(),
          themePref: 'system',
        },
        { merge: true }
      );

      // Check if wallets exist, seed default cash wallet if empty
      const walletsSnap = await getDocs(collection(db, 'users', user.uid, 'wallets'));
      if (walletsSnap.empty) {
        await setDoc(doc(db, 'users', user.uid, 'wallets', 'cash'), {
          name: 'Tiền mặt',
          type: 'cash',
          balance: 0,
          color: '#1F6FBF',
          isDefault: true,
          createdAt: serverTimestamp(),
        });
      }

      showToast('Đăng ký tài khoản thành công! Dữ liệu đã sẵn sàng đồng bộ.');
      handleClose();
    } catch (err: unknown) {
      const error = err as { code?: string; message?: string };
      console.error('Register error:', error);
      if (error.code === 'auth/email-already-in-use') {
        setErrorMessage('Email này đã được đăng ký. Bạn có thể đăng nhập ngay.');
      } else if (error.code === 'auth/invalid-email') {
        setErrorMessage('Địa chỉ email không hợp lệ.');
      } else if (error.code === 'auth/weak-password') {
        setErrorMessage('Mật khẩu quá yếu. Vui lòng chọn mật khẩu phức tạp hơn.');
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
      showToast('Đã gửi email hướng dẫn khôi phục mật khẩu!');
      setMode('login');
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
      className="fx-modal-overlay"
      onClick={(e) => {
        if (e.target === e.currentTarget) handleClose();
      }}
      role="dialog"
      aria-modal="true"
    >
      <div className="fx-modal" style={{ maxWidth: '440px' }}>
        {/* Header */}
        <div className="fx-modal-top">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <FinluxLogo size={36} />
            <div>
              <h2 style={{ fontSize: '18px', fontWeight: 800 }}>
                {mode === 'login' && 'Đăng nhập FinLux'}
                {mode === 'register' && 'Tạo tài khoản mới'}
                {mode === 'forgot' && 'Khôi phục mật khẩu'}
              </h2>
              <p className="fx-top-crumb" style={{ margin: 0 }}>
                ĐỒNG BỘ CLOUD APP &amp; WEB
              </p>
            </div>
          </div>
          <button
            type="button"
            className="fx-icon-btn"
            onClick={handleClose}
            aria-label="Đóng"
          >
            <FinluxIcon name="close" />
          </button>
        </div>

        {/* Quick Google Sign In */}
        {mode !== 'forgot' && (
          <div style={{ marginTop: '16px' }}>
            <button
              type="button"
              className="fx-btn fx-btn-subtle"
              style={{
                width: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '12px',
                height: '46px',
                fontWeight: 700,
                fontSize: '13px',
                borderRadius: '12px',
                border: '1px solid var(--border)',
                background: 'var(--surface-soft)',
                cursor: loading ? 'not-allowed' : 'pointer',
                boxShadow: '0 2px 8px rgba(0,0,0,0.03)',
              }}
              onClick={handleGoogleSignIn}
              disabled={loading}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" style={{ flexShrink: 0 }}>
                <path
                  fill="#4285F4"
                  d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.15z"
                />
                <path
                  fill="#34A853"
                  d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.94H1.26v3.15C3.25 21.37 7.34 24 12 24z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.28 14.26c-.25-.72-.38-1.49-.38-2.26s.13-1.54.38-2.26V6.59H1.26C.46 8.19 0 9.99 0 12s.46 3.81 1.26 5.41l4.02-3.15z"
                />
                <path
                  fill="#EA4335"
                  d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.34 0 3.25 2.63 1.26 6.59l4.02 3.15c.95-2.84 3.6-4.99 6.72-4.99z"
                />
              </svg>
              <span>{loading ? 'Đang kết nối...' : 'Tiếp tục với Google (Đồng bộ App)'}</span>
            </button>

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                margin: '16px 0 12px',
                gap: '12px',
              }}
            >
              <div style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
              <span
                style={{
                  fontSize: '10px',
                  color: 'var(--text-muted)',
                  fontWeight: 700,
                  letterSpacing: '0.6px',
                  textTransform: 'uppercase',
                }}
              >
                Hoặc tài khoản Email
              </span>
              <div style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
            </div>
          </div>
        )}

        {/* Tab switchers */}
        {mode !== 'forgot' && (
          <div
            style={{
              display: 'flex',
              background: 'var(--surface-soft)',
              padding: '4px',
              borderRadius: '12px',
              margin: '0 0 16px',
              border: '1px solid var(--border)',
            }}
          >
            <button
              type="button"
              className={`fx-filter ${mode === 'login' ? 'active' : ''}`}
              style={{ flex: 1, textAlign: 'center', border: 'none' }}
              onClick={() => {
                setMode('login');
                setErrorMessage(null);
              }}
            >
              Đăng nhập
            </button>
            <button
              type="button"
              className={`fx-filter ${mode === 'register' ? 'active' : ''}`}
              style={{ flex: 1, textAlign: 'center', border: 'none' }}
              onClick={() => {
                setMode('register');
                setErrorMessage(null);
              }}
            >
              Đăng ký mới
            </button>
          </div>
        )}

        {/* Error Alert */}
        {errorMessage && (
          <div
            style={{
              padding: '10px 14px',
              borderRadius: '10px',
              background: 'var(--red-soft)',
              color: 'var(--red)',
              fontSize: '11px',
              fontWeight: 700,
              marginBottom: '14px',
              lineHeight: 1.5,
            }}
          >
            {errorMessage}
          </div>
        )}

        {/* Login Form */}
        {mode === 'login' && (
          <form onSubmit={handleLogin} className="fx-form-grid">
            <div className="fx-form-field wide">
              <label htmlFor="auth-email">Email tài khoản</label>
              <input
                id="auth-email"
                type="email"
                placeholder="example@gmail.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div className="fx-form-field wide">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label htmlFor="auth-password">Mật khẩu</label>
                <button
                  type="button"
                  className="fx-link"
                  style={{ fontSize: '10px', padding: 0 }}
                  onClick={() => {
                    setMode('forgot');
                    setErrorMessage(null);
                  }}
                >
                  Quên mật khẩu?
                </button>
              </div>
              <input
                id="auth-password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <div className="fx-form-field wide fx-modal-actions" style={{ marginTop: '16px' }}>
              <button
                type="button"
                className="fx-btn fx-btn-subtle"
                onClick={handleClose}
                disabled={loading}
              >
                Hủy
              </button>
              <button
                type="submit"
                className="fx-btn"
                disabled={loading}
              >
                <FinluxIcon name="check" />
                <span>{loading ? 'Đang xác thực...' : 'Đăng nhập & Đồng bộ'}</span>
              </button>
            </div>
          </form>
        )}

        {/* Register Form */}
        {mode === 'register' && (
          <form onSubmit={handleRegister} className="fx-form-grid">
            <div className="fx-form-field wide">
              <label htmlFor="reg-name">Tên hiển thị</label>
              <input
                id="reg-name"
                type="text"
                placeholder="Ví dụ: Nguyễn Văn A"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div className="fx-form-field wide">
              <label htmlFor="reg-email">Email đăng ký</label>
              <input
                id="reg-email"
                type="email"
                placeholder="example@gmail.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="fx-form-field">
              <label htmlFor="reg-password">Mật khẩu</label>
              <input
                id="reg-password"
                type="password"
                placeholder="Ít nhất 6 ký tự"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <div className="fx-form-field">
              <label htmlFor="reg-confirm">Xác nhận mật khẩu</label>
              <input
                id="reg-confirm"
                type="password"
                placeholder="Nhập lại mật khẩu"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>

            <div className="fx-form-field wide fx-modal-actions" style={{ marginTop: '16px' }}>
              <button
                type="button"
                className="fx-btn fx-btn-subtle"
                onClick={handleClose}
                disabled={loading}
              >
                Hủy
              </button>
              <button
                type="submit"
                className="fx-btn"
                disabled={loading}
              >
                <FinluxIcon name="plus" />
                <span>{loading ? 'Đang tạo tài khoản...' : 'Đăng ký tài khoản'}</span>
              </button>
            </div>
          </form>
        )}

        {/* Forgot Password Form */}
        {mode === 'forgot' && (
          <form onSubmit={handleForgotPassword} className="fx-form-grid">
            <p className="fx-small-note wide" style={{ margin: '0 0 10px' }}>
              Nhập email bạn đã đăng ký để nhận liên kết đặt lại mật khẩu mới.
            </p>

            <div className="fx-form-field wide">
              <label htmlFor="forgot-email">Email của bạn</label>
              <input
                id="forgot-email"
                type="email"
                placeholder="example@gmail.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div className="fx-form-field wide fx-modal-actions" style={{ marginTop: '16px' }}>
              <button
                type="button"
                className="fx-btn fx-btn-subtle"
                onClick={() => {
                  setMode('login');
                  setErrorMessage(null);
                }}
                disabled={loading}
              >
                Quay lại
              </button>
              <button
                type="submit"
                className="fx-btn"
                disabled={loading}
              >
                <span>{loading ? 'Đang gửi...' : 'Gửi liên kết'}</span>
              </button>
            </div>
          </form>
        )}

        {/* Note */}
        <div className="fx-muted-box" style={{ marginTop: '20px', fontSize: '11px' }}>
          Tài khoản này dùng chung 100% với ứng dụng di động FinLux Android. Dữ liệu ví, giao dịch, ngân sách được đồng bộ tức thời hai chiều qua Firestore.
        </div>
      </div>
    </div>
  );
}
