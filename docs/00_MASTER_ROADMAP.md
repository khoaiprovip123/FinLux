# FinLux — Master SDLC Roadmap (00_MASTER_ROADMAP.md)

## 1. Giới thiệu dự án (Project Overview)
**FinLux** là hệ thống quản lý tài chính cá nhân cao cấp (Personal Finance Management Engine & Mobile Client) phát triển trên nền tảng **Android Native (Kotlin + Jetpack Compose)** kết hợp **Firebase Serverless Architecture**. Ứng dụng đột phá với ngôn ngữ thiết kế **Liquid Glass (iOS 26 Visual Style)**, hỗ trợ đa phong cách giao diện và cơ chế tính toán số dư nguyên tử (Atomic Financial Ledger).

## 2. Vision (Tầm nhìn)
Trở thành giải pháp quản lý tài chính cá nhân mã nguồn mở hàng đầu, kết hợp giữa trải nghiệm người dùng đẳng cấp (Liquid Glass UI), độ chính xác tuyệt đối trong giao dịch tài chính (Financial Engine Invariants) và trợ lý trí tuệ nhân tạo (AI Financial Advisor & OCR).

## 3. Product Goals (Mục tiêu sản phẩm)
- **Độ chính xác tài chính**: Bảo đảm toàn vẹn dữ liệu sổ cái (Ledger), số dư các ví (Wallet Balance) và dòng tiền chuyển đổi (Internal Transfers).
- **Trải nghiệm vượt trội**: Đạt 60 FPS mượt mà với hiệu ứng kính mờ (Liquid Glass), phản hồi rung (Haptic feedback) và micro-animations.
- **Offline-First & Realtime**: Sử dụng mượt mà không cần kết nối mạng; tự động đồng bộ thời gian thực ngay khi kết nối lại.

## 4. Technical Goals (Mục tiêu kỹ thuật)
- **Clean Architecture 3 lớp**: Presentation / Domain / Data tách biệt tuyệt đối.
- **Strict Financial Engine**: Cập nhật số dư 100% qua `Firestore Transaction` (BR-06, BR-14), loại bỏ hoàn toàn race condition.
- **Zero Memory / Resource Leak**: Tối ưu hóa Compose Recomposition, Bitmap recycling và Hilt ViewModel lifecycle.

## 5. Architecture Goal
```text
                  ┌───────────────────────────────────────────┐
                  │            Presentation Layer             │
                  │   Jetpack Compose + Material 3 + MVVM     │
                  └─────────────────────┬─────────────────────┘
                                        │ (UiState / UiEvent)
                                        ▼
                  ┌───────────────────────────────────────────┐
                  │               Domain Layer                │
                  │  Pure Kotlin Models, UseCases, Repos      │
                  └─────────────────────┬─────────────────────┘
                                        │ (Repository Contracts)
                                        ▼
                  ┌───────────────────────────────────────────┐
                  │                Data Layer                 │
                  │  DataStore, Firestore, Storage, Local DB  │
                  └───────────────────────────────────────────┘
```

## 6. Coding Standards & Conventions
- **Clean Architecture**: UseCase không chứa logic UI; ViewModel không gọi Firestore SDK trực tiếp.
- **Atomic Operations**: Mọi thao tác ghi giao dịch ảnh hưởng số dư bắt buộc bọc trong Firestore Transaction.
- **Immutability**: 100% Data classes và UiState sử dụng `val` immutable.

## 7. Git Workflow & Branch Strategy
- `main`: Nhánh production ổn định, luôn build thành công.
- `develop`: Nhánh tích hợp tính năng từ các Phase.
- `feature/phase-XX-<name>`: Nhánh phát triển theo từng Phase chuẩn SDLC.

## 8. Directory & Module Structure
```text
app/src/main/java/com/finlux/app/
├── core/                   # Design system, common utils, navigation
├── domain/                 # Models, Repository interfaces, UseCases
├── data/                   # DataStore, Firebase adapters, Repositories impl
└── presentation/           # Compose screens, ViewModels, UiStates
```

## 9. Danh sách 11 Phase Phát triển (Master Phases Overview)

| Phase | Tên Phase | Trọng tâm chính |
| :--- | :--- | :--- |
| **PHASE 00** | Project Audit & Baseline | Kiểm toán toàn bộ codebase, cấu hình linter, CI/CD pipeline |
| **PHASE 01** | Financial Core Engine | Xây dựng Sổ cái, Money Value Object, Invariants & Unit Tests |
| **PHASE 02** | Security & Rules | Security Rules cho Firestore & Storage, Emulator verification |
| **PHASE 03** | Wallet & Transaction Module | Quản lý Đa ví, Thao tác CRUD Thu/Chi, Chuyển tiền nội bộ |
| **PHASE 04** | Dashboard & Analytics | KPI tổng quan, Biểu đồ Line/Donut/Treemap, Hạn mức Ngân sách |
| **PHASE 05** | Liquid Glass Design System | Hệ màu kính mờ, Shader/RenderEffect, Motion & Haptic feedback |
| **PHASE 06** | Authentication & Session | Đăng nhập/Đăng ký 3D UI, Google/Apple/Facebook OAuth, Session |
| **PHASE 07** | Advanced Financial Engine | Tài sản, Nợ, Thẻ tín dụng, Khoản vay, Đầu tư & Net Worth |
| **PHASE 08** | OCR & Receipt Extraction | Quét hóa đơn tự động qua ML Kit / Vision AI, trích xuất dữ liệu |
| **PHASE 09** | AI Financial Assistant | Trợ lý phân tích dòng tiền, Dự báo chi tiêu qua MCP / Gemini |
| **PHASE 10** | Testing & Release Pipeline | Suite E2E Test, Regression, Chấm điểm Benchmark & Play Store |
