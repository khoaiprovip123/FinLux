# Phase Plan — Performance, Scalability & Observability

## 1. Mục tiêu

Phát hiện lỗi production nhanh, kiểm soát Firebase cost và tránh scheduler/query không scale.

## 2. Phase PO-1 — Crash & Error Monitoring

Tích hợp:
- Firebase Crashlytics;
- non-fatal exception logging;
- release/version metadata;
- breadcrumb cho critical financial flow nhưng không log dữ liệu nhạy cảm.

Không log password, token, account number đầy đủ hoặc dữ liệu receipt không cần thiết.

## 3. Phase PO-2 — Performance Monitoring

Theo dõi:
- cold start;
- Home load;
- Reports load;
- Firestore query latency;
- export PDF/XLSX;
- Compose jank.

## 4. Phase PO-3 — Structured Logs

Cloud Functions log fields:
- eventId;
- operation;
- periodKey;
- result;
- duration;
- retry count;
- anonymized user correlation nếu cần.

## 5. Phase PO-4 — Firebase query optimization

- Source-controlled indexes.
- Không collection scan toàn user mỗi ngày nếu có thể.
- Query due jobs bằng `nextExecutionAt`.
- Pagination cho transaction/report lớn.
- Aggregate strategy cho dashboard/report nếu data tăng mạnh.

## 6. Phase PO-5 — Scheduled Function scale

Thay:
```
load all users → loop
```

bằng:
```
query due entities → bounded batch → idempotent processing
```

Reminder cần pagination >500 và retry-safe.

## 7. Phase PO-6 — Offline behavior

Định nghĩa sản phẩm:
- cached reads offline;
- financial atomic writes yêu cầu online;
- UI hiển thị sync/error state.

Không gọi “fully offline transaction” nếu Firestore Transaction cần server.

## 8. Phase PO-7 — App startup & Compose

- tránh eager initialization không cần;
- derivedStateOf/remember hợp lý;
- LazyColumn stable keys;
- tránh recomposition chart lớn;
- move calculation khỏi Composable;
- profiling trước tối ưu.

## 9. Phase PO-8 — Cost guardrails

Theo dõi:
- Firestore reads/user/day;
- Functions invocations;
- Storage usage;
- FCM fanout;
- report query volume.

## 10. Acceptance

- Crash dashboard có version/build.
- Critical flow error có trace.
- Không full-scan scheduler lớn.
- Query/index documented.
