import AuthScreen from '@/components/AuthScreen';

export const metadata = {
  title: 'Đăng nhập | FINLUX',
  description: 'Đăng nhập FinLux để quản lý tài chính cá nhân.',
};

export default function SignInPage() {
  return <AuthScreen defaultMode="login" />;
}
