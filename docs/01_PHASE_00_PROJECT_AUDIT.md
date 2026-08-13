# PHASE 00 — PROJECT AUDIT & BASELINE (01_PHASE_00_PROJECT_AUDIT.md)

## 1. Objective (Mục tiêu)
Thực hiện kiểm toán toàn diện mã nguồn hiện tại của dự án FinLux, đánh giá mức độ tuân thủ Clean Architecture, xác minh cấu hình Gradle/Hilt/Firebase, phát hiện nợ kỹ thuật (technical debt) và thiết lập môi trường phát triển chuẩn hóa trước khi bước vào các sprint tính năng.

## 2. Business & Technical Goals
- **Business Goal**: Đảm bảo toàn bộ tính năng hiện tại (Auth, Home, Expense, Reports, Settings) hoạt động ổn định 100% không có crash log hay data mismatch.
- **Technical Goal**: Đạt điểm 0 Lint Warning nghiêm trọng; loại bỏ hard-coded strings; đảm bảo 100% Repository interfaces nằm trong lớp Domain.

## 3. Scope of Audit
- [x] Cấu trúc package theo Clean Architecture (`core`, `domain`, `data`, `presentation`).
- [x] Đánh giá giao diện Liquid Glass & Responsive insets trên các cỡ màn hình Android.
- [x] Kiểm tra việc xử lý bọc `Firestore Transaction` đối với các thao tác số dư.
- [x] Cấu hình `.gitignore` loại bỏ build artifacts và file bảo mật.

## 4. Audit Checklist & Findings
- **Clean Architecture Compliance**: PASS — Lớp Domain độc lập hoàn toàn với Android SDK và Firebase.
- **State Management**: PASS — 100% ViewModels sử dụng `StateFlow` + `UiState` immutable pattern.
- **Concurrency**: PASS — Xử lý bất đồng bộ sử dụng Kotlin Coroutines với Structured Concurrency.

## 5. Coding Rules & Constraints for Phase 00
- Không sửa đổi logic nghiệp vụ của các Use Cases đã qua unit test ngoại trừ refactoring tối ưu.
- Mọi file Kotlin mới bắt buộc phải tuân thủ ktlint / detekt naming rules.

## 6. Definition of Done (DoD) & Exit Criteria
- [x] Gradle Sync & Build Debug APK thành công không phát sinh cảnh báo biên dịch.
- [x] Unit test suite vượt qua 100% (`.\gradlew.bat testDebugUnitTest`).
- [x] File kiểm toán được ghi lại đầy đủ làm mốc cơ sở (baseline) cho Phase 01.

## 7. AI Agent Execution Prompt
> "Thực hiện kiểm tra tĩnh toàn bộ mã nguồn Kotlin trong `app/src/main/java`, xác minh không có lệnh gọi trực tiếp SDK Firebase từ ViewModel, đảm bảo tất cả UseCases đều được inject qua Hilt."
