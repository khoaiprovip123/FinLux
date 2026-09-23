package com.finlux.app.core.sync

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quản lý đồng bộ sự kiện dữ liệu toàn ứng dụng.
 * Kích hoạt cập nhật tức thì (reactive push) cho các ViewModel (Home, Wallets, Transactions)
 * khi có sự kiện thay đổi dữ liệu lớn như Sao lưu & Phục hồi (Restore / Wipe).
 */
@Singleton
class DataSyncManager @Inject constructor() {

    private val _refreshTrigger = MutableSharedFlow<Long>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val refreshTrigger: SharedFlow<Long> = _refreshTrigger.asSharedFlow()

    init {
        _refreshTrigger.tryEmit(System.currentTimeMillis())
    }

    /**
     * Bắn tín hiệu thông báo dữ liệu vừa được khôi phục hoặc xóa toàn bộ.
     * Mọi Flow đang lắng nghe sẽ ép tải lại state mới nhất từ Firestore.
     */
    fun notifyDataRestored() {
        _refreshTrigger.tryEmit(System.currentTimeMillis())
    }
}
