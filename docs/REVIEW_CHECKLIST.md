# CODE REVIEW CHECKLIST (REVIEW_CHECKLIST.md)

## 1. Architecture & Layers Check
- [ ] Code có vi phạm ranh giới Clean Architecture không? (Domain có bị dính Android/Firebase dependencies không?)
- [ ] ViewModel có gọi trực tiếp SDK ngoại vi mà không qua UseCase / Repository không?
- [ ] StateFlow trong ViewModel có bị gán trực tiếp mutable flow ra ngoài không?

## 2. Financial Integrity & Security Check
- [ ] Mọi thao tác ghi ảnh hưởng đến `wallet.balance` đã được bọc trong `Firestore Transaction` chưa?
- [ ] Có validation kiểm tra `amount > 0` cho giao dịch không?
- [ ] Các thông tin nhạy cảm (Keys, Passwords, Token) có bị lỡ log ra Logcat hoặc commit lên Git không?

## 3. UI/UX & Compose Performance Check
- [ ] Màn hình Compose có bị giật/lag hay Recomposition liên tục không?
- [ ] Đã kiểm tra giao diện trên cả màn hình nhỏ và màn hình tablet/gập chưa?
- [ ] Edge-to-Edge System Insets (`imePadding`, `navigationBarsPadding`) đã được xử lý đúng chưa?

## 4. Testing & Code Quality Check
- [ ] Các UseCases mới đã có Unit Test tương ứng chưa?
- [ ] Mọi Unit Test có chạy qua 100% không? (`.\gradlew.bat testDebugUnitTest`)
- [ ] Có cảnh báo biên dịch (warnings) hay code thừa không được dọn dẹp không?
