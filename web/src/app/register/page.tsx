import AuthScreen from '@/components/AuthScreen';

export const metadata = {
  title: 'Đăng ký Tài khoản | FINLUX',
  description: 'Đăng ký tài khoản FinLux miễn phí trọn đời, đồng bộ dữ liệu thời gian thực.',
};

export default function RegisterPage() {
  return <AuthScreen defaultMode="register" />;
}
