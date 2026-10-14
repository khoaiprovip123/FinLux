import AuthScreen from '@/components/AuthScreen';

export const metadata = {
  title: 'Khôi phục Mật khẩu | FINLUX',
  description: 'Khôi phục mật khẩu tài khoản FinLux của bạn.',
};

export default function ForgotPasswordPage() {
  return <AuthScreen defaultMode="forgot" />;
}
