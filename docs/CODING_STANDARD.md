# CODING STANDARD & CONVENTIONS (CODING_STANDARD.md)

## 1. Clean Architecture Principles
1. **Lớp Domain**: Không chứa bất kỳ phụ thuộc nào vào Android SDK (`android.*`) hay các thư viện bên thứ ba (Firebase, DataStore). Chỉ sử dụng Pure Kotlin.
2. **Lớp Data**: Thực thi các Repository interfaces của Domain. Không phơi bày các entity của database (như Firestore Document Snapshot) ra bên ngoài Data layer.
3. **Lớp Presentation**: ViewModel chỉ giao tiếp với Domain Layer qua UseCases. 100% UI State được quản lý qua `StateFlow` immutable.

## 2. Naming Conventions
- **Classes / Interfaces**: `PascalCase` (ví dụ: `AddTransactionUseCase`, `AuthRepository`).
- **Functions / Properties**: `camelCase` (ví dụ: `calculateBalance`, `totalAmount`).
- **Compose Screen / Components**: `PascalCase` kết thúc bằng tên loại (ví dụ: `DashboardScreen`, `GlassCard`).
- **Resource IDs / Drawables**: `snake_case` (ví dụ: `ic_google_g`, `auth_wallet_3d_v2`).

## 3. Jetpack Compose Rules
- **Stability & Recomposition**: Sử dụng `@Stable` hoặc `@Immutable` cho các data holder class truyền vào Composable.
- **Modifier First**: Tham số `modifier: Modifier = Modifier` bắt buộc là tham số tùy chọn đầu tiên trong Composable công khai.
- **No Side Effects in Recomposition**: 100% Side-effects phải được bọc trong `LaunchedEffect`, `DisposableEffect` hoặc `rememberCoroutineScope`.

## 4. Concurrency & Asynchronous Rules
- Mọi hàm bất đồng bộ trong Repositories/UseCases phải khai báo là `suspend` hoặc trả về `Flow<T>`.
- Luôn chỉ định đúng CoroutineDispatcher (`Dispatchers.IO` cho IO/Network, `Dispatchers.Default` cho tính toán nặng).

## 5. Commit Message Conventions
Định dạng commit theo chuẩn **Conventional Commits**:
- `feat: <mô tả>` — Thêm tính năng mới.
- `fix: <mô tả>` — Sửa lỗi.
- `docs: <mô tả>` — Cập nhật tài liệu.
- `refactor: <mô tả>` — Tái cấu trúc code không đổi hành vi.
- `test: <mô tả>` — Thêm hoặc sửa unit test.
