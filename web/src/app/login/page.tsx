import AuthScreen from '@/components/AuthScreen';

export const metadata = {
  title: 'Đăng nhập | FINLUX',
  description: 'Đăng nhập FinLux để đồng bộ dữ liệu tài chính với ứng dụng di động.',
};

export default function LoginPage() {
  return <AuthScreen defaultMode="login" />;
}
