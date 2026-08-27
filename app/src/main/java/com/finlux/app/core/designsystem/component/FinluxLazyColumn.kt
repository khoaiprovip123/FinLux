package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.theme.LocalFinluxSpacing

/**
 * Lo?i danh sách — xác d?nh contentPadding t? d?ng t? FinluxSpacing tokens.
 *
 * - [TAB_MAIN]: Màn hình chính có BottomBar ? bottom = `spacing.bottomBarClearance` (96.dp)
 * - [DETAIL]: Màn hình con / chi ti?t ? bottom = `spacing.compactClearance` (24.dp)
 */
enum class FinluxListType {
    TAB_MAIN,
    DETAIL,
}

/**
 * FinluxLazyColumn — LazyColumn chu?n dùng chung v?i auto-padding t? FinluxSpacing tokens.
 *
 * **Tính nang:**
 * - T? d?ng tính `contentPadding` t? `LocalFinluxSpacing` theo [listType].
 * - Slot `emptyState`: T? render khi [isEmpty] = true, thay th? toàn b? n?i dung.
 * - Cho phép override thêm padding du?i qua [extraBottomPadding].
 *
 * **Ví d? s? d?ng:**
 * ```kotlin
 * FinluxLazyColumn(
 *     modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
 *     listType = FinluxListType.DETAIL,
 *     isEmpty = state.transactions.isEmpty(),
 *     emptyState = {
 *         FinluxEmptyState(
 *             title = "Chua có giao d?ch",
 *             icon = Icons.Default.Inbox,
 *         )
 *     },
 * ) {
 *     items(state.transactions) { tx -> TransactionRow(tx) }
 * }
 * ```
 *
 * @param modifier Modifier cho LazyColumn
 * @param listType Lo?i danh sách — quy d?nh contentPadding bottom t? d?ng
 * @param extraBottomPadding Padding du?i b? sung (c?ng thêm vào bottom c?a listType)
 * @param isEmpty Khi true, render [emptyState] thay vì [content]
 * @param emptyState Slot empty state — thu?ng là FinluxEmptyState(...)
 * @param state LazyListState — truy?n khi c?n ki?m soát scroll t? bên ngoài
 * @param verticalArrangementSpacing Kho?ng cách gi?a các item (m?c d?nh = spacing.cardGap)
 * @param content LazyListScope block ch?a items thông thu?ng
 */
@Composable
fun FinluxLazyColumn(
    modifier: Modifier = Modifier,
    listType: FinluxListType = FinluxListType.DETAIL,
    extraBottomPadding: Dp = 0.dp,
    isEmpty: Boolean = false,
    emptyState: @Composable () -> Unit = {},
    state: LazyListState = rememberLazyListState(),
    verticalArrangementSpacing: Dp? = null,
    content: LazyListScope.() -> Unit,
) {
    val spacing = LocalFinluxSpacing.current

    val bottomPadding = when (listType) {
        FinluxListType.TAB_MAIN -> spacing.bottomBarClearance + extraBottomPadding
        FinluxListType.DETAIL -> spacing.compactClearance + extraBottomPadding
    }

    val resolvedSpacing = verticalArrangementSpacing ?: spacing.cardGap

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(
            horizontal = spacing.contentHorizontal,
            vertical = 0.dp,
        ).let {
            PaddingValues(
                start = spacing.contentHorizontal,
                end = spacing.contentHorizontal,
                top = spacing.screenTop,
                bottom = bottomPadding,
            )
        },
        verticalArrangement = Arrangement.spacedBy(resolvedSpacing),
        horizontalAlignment = Alignment.Start,
    ) {
        if (isEmpty) {
            item(key = "finlux_empty_state") {
                emptyState()
            }
        } else {
            content()
        }
    }
}
