package com.finlux.app.core.designsystem.component.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.time.FinanceTime
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

/**
 * FinluxWheelPicker - Smooth Cupertino / iOS Style Drum Wheel Picker in Jetpack Compose.
 *
 * Uses LazyColumn + rememberSnapFlingBehavior to achieve exact snap-to-center physics.
 * Displays 3 visible items with dynamic scaling (0.82f) and alpha fading (0.38f) around the center.
 * Features automatic bounce-back when swiping beyond future limits.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FinluxWheelPicker(
    items: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxValue: Int = items.lastOrNull() ?: 0,
    minValue: Int = items.firstOrNull() ?: 0,
    accentColor: Color? = null,
    formatLabel: (Int) -> String = { "%02d".format(Locale.US, it) },
) {
    val tokens = LocalFinluxTokens.current
    val effectiveAccent = accentColor ?: tokens.primary
    val itemHeight = 38.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val initialIndex = remember {
        items.indexOf(selectedValue).coerceAtLeast(0)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    val centeredIndex by remember {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            val offsetRatio = firstOffset / itemHeightPx
            if (offsetRatio > 0.5f) {
                (firstIndex + 1).coerceAtMost(items.lastIndex)
            } else {
                firstIndex.coerceAtMost(items.lastIndex)
            }
        }
    }

    LaunchedEffect(selectedValue) {
        val targetIdx = items.indexOf(selectedValue)
        if (targetIdx >= 0 && targetIdx != centeredIndex && !listState.isScrollInProgress) {
            listState.animateScrollToItem(targetIdx)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val targetIdx = centeredIndex.coerceIn(0, items.lastIndex)
            val currentVal = items[targetIdx]
            if (currentVal > maxValue) {
                val maxIdx = items.indexOf(maxValue).coerceAtLeast(0)
                listState.animateScrollToItem(maxIdx)
                onValueChange(maxValue)
            } else if (currentVal < minValue) {
                val minIdx = items.indexOf(minValue).coerceAtLeast(0)
                listState.animateScrollToItem(minIdx)
                onValueChange(minValue)
            } else {
                onValueChange(currentVal)
            }
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // Highlight indicator for the center item (Liquid Glass frame)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(itemHeight)
                .background(
                    color = effectiveAccent.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(10.dp),
                )
                .border(
                    BorderStroke(1.dp, effectiveAccent.copy(alpha = 0.30f)),
                    shape = RoundedCornerShape(10.dp),
                ),
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(items) { index, itemValue ->
                val distance = kotlin.math.abs(index - centeredIndex)
                val isPastLimit = itemValue > maxValue || itemValue < minValue

                val scale = when (distance) {
                    0 -> 1f
                    1 -> 0.82f
                    else -> 0.68f
                }
                val alpha = when {
                    isPastLimit -> 0.22f
                    distance == 0 -> 1f
                    distance == 1 -> 0.38f
                    else -> 0.15f
                }
                val color = when {
                    isPastLimit -> tokens.onSurfaceVariant.copy(alpha = 0.3f)
                    distance == 0 -> effectiveAccent
                    else -> tokens.onSurface
                }
                val fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Medium
                val fontSize = if (distance == 0) 21.sp else 16.sp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable(
                            enabled = !isPastLimit,
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = formatLabel(itemValue),
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = color,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            },
                    )
                }
            }
        }
    }
}

/**
 * Quick Date Chips (Cuộn ngang LazyRow).
 */
@Composable
fun FinluxQuickDateChips(
    selectedLocalDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    today: LocalDate,
    allowFutureDates: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val quickDates = remember(allowFutureDates, today) {
        if (!allowFutureDates) {
            listOf(
                "Hôm nay" to today,
                "Hôm qua" to today.minusDays(1),
                "2 ngày trước" to today.minusDays(2),
                "3 ngày trước" to today.minusDays(3),
                "Đầu tháng" to today.withDayOfMonth(1),
            )
        } else {
            listOf(
                "Hôm nay" to today,
                "Ngày mai" to today.plusDays(1),
                "3 ngày tới" to today.plusDays(3),
                "1 tuần tới" to today.plusWeeks(1),
                "1 tháng tới" to today.plusMonths(1),
                "Cuối tháng" to today.withDayOfMonth(today.lengthOfMonth()),
            )
        }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(quickDates) { (label, date) ->
            val isSelected = selectedLocalDate == date
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.14f) else tokens.surfaceSoft,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) accentColor else tokens.border,
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onDateSelected(date) },
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isSelected) accentColor else tokens.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/**
 * Quick Time Chips (Cuộn ngang LazyRow).
 */
@Composable
fun FinluxQuickTimeChips(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    selectedLocalDate: LocalDate,
    today: LocalDate,
    allowFutureTime: Boolean,
    zoneId: ZoneId,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isDateToday = selectedLocalDate == today
    val nowCurrent = remember(zoneId) { LocalTime.now(zoneId) }

    val timeOptions = remember(nowCurrent) {
        listOf(
            "Bây giờ" to (nowCurrent.hour to nowCurrent.minute),
            "Sáng 07:30" to (7 to 30),
            "Trưa 12:00" to (12 to 0),
            "Chiều 14:30" to (14 to 30),
            "Tối 19:30" to (19 to 30),
            "Đêm 22:00" to (22 to 0),
        )
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(timeOptions) { (label, time) ->
            val (h, m) = time
            val isChipFutureForToday = !allowFutureTime && isDateToday &&
                (h > nowCurrent.hour || (h == nowCurrent.hour && m > nowCurrent.minute))
            val isChipSelected = selectedHour == h && selectedMinute == m

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isChipSelected) accentColor.copy(alpha = 0.14f) else tokens.surface,
                border = BorderStroke(
                    1.dp,
                    if (isChipSelected) accentColor else tokens.border,
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .alpha(if (isChipFutureForToday) 0.3f else 1f)
                    .clickable(
                        enabled = !isChipFutureForToday,
                        onClick = { onTimeSelected(h, m) },
                    ),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isChipSelected) accentColor else tokens.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }
        }
    }
}

/**
 * Calendar Grid Liquid Glass (Month Selector + 7-Column Grid).
 */
@Composable
fun FinluxCalendarView(
    viewingYearMonth: YearMonth,
    onViewingYearMonthChange: (YearMonth) -> Unit,
    selectedLocalDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    today: LocalDate,
    allowFutureDates: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val canGoNext = allowFutureDates || viewingYearMonth.isBefore(YearMonth.from(today))

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Month Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onViewingYearMonthChange(viewingYearMonth.minusMonths(1)) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Tháng trước",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                }

                Text(
                    text = "Tháng ${viewingYearMonth.monthValue} năm ${viewingYearMonth.year}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )

                IconButton(
                    onClick = {
                        if (canGoNext) onViewingYearMonthChange(viewingYearMonth.plusMonths(1))
                    },
                    enabled = canGoNext,
                    modifier = Modifier
                        .size(32.dp)
                        .alpha(if (canGoNext) 1f else 0.25f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Tháng sau",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            // Weekday Labels (T2 .. CN)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val weekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                weekdays.forEach { dayLabel ->
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }
            }

            // Month Days Grid (7 columns, zero overflow: size 36dp, row spacing 4dp)
            val firstDayOfMonth = viewingYearMonth.atDay(1)
            val leadingEmptyDays = (firstDayOfMonth.dayOfWeek.value - 1)
            val daysInMonth = viewingYearMonth.lengthOfMonth()
            val totalSlots = leadingEmptyDays + daysInMonth
            val rowsCount = (totalSlots + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (row in 0 until rowsCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        for (col in 0 until 7) {
                            val slotIndex = row * 7 + col
                            val dayNumber = slotIndex - leadingEmptyDays + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = viewingYearMonth.atDay(dayNumber)
                                val isFuture = !allowFutureDates && date.isAfter(today)
                                val isToday = date == today
                                val isSelected = date == selectedLocalDate

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(accentColor, CircleShape)
                                            } else if (isToday) {
                                                Modifier.border(BorderStroke(1.5.dp, accentColor), CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clickable(
                                            enabled = !isFuture,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = { onDateSelected(date) },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        ),
                                        color = when {
                                            isSelected -> Color.White
                                            isFuture -> tokens.onSurface.copy(alpha = 0.25f)
                                            isToday -> accentColor
                                            else -> tokens.onSurface
                                        },
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * FinluxDateTimePickerSheet - Premium Liquid Glass BottomSheet for Date & Time Selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxDateTimePickerSheet(
    selectedDateTime: Instant,
    onDateTimeSelected: (Instant) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Thời gian giao dịch",
    accentColor: Color? = null,
    allowFutureDates: Boolean = false,
    zoneId: ZoneId = FinanceTime.defaultZone,
) {
    val tokens = LocalFinluxTokens.current
    val effectiveAccent = accentColor ?: tokens.primary
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentZoned = remember(selectedDateTime, zoneId) { selectedDateTime.atZone(zoneId) }
    var selectedLocalDate by remember(selectedDateTime, zoneId) { mutableStateOf(currentZoned.toLocalDate()) }
    var selectedHour by remember(selectedDateTime, zoneId) { mutableIntStateOf(currentZoned.hour) }
    var selectedMinute by remember(selectedDateTime, zoneId) { mutableIntStateOf(currentZoned.minute) }
    var viewingYearMonth by remember(selectedDateTime, zoneId) { mutableStateOf(YearMonth.from(currentZoned)) }

    val today = remember(zoneId) { LocalDate.now(zoneId) }

    fun clampTimeIfNeeded(date: LocalDate, hour: Int, minute: Int): Pair<Int, Int> {
        if (!allowFutureDates && date == today) {
            val currentNow = LocalTime.now(zoneId)
            if (hour > currentNow.hour || (hour == currentNow.hour && minute > currentNow.minute)) {
                return currentNow.hour to currentNow.minute
            }
        }
        return hour to minute
    }

    val (clampedHour, clampedMinute) = clampTimeIfNeeded(selectedLocalDate, selectedHour, selectedMinute)
    if (clampedHour != selectedHour || clampedMinute != selectedMinute) {
        selectedHour = clampedHour
        selectedMinute = clampedMinute
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(width = 38.dp, height = 4.dp),
                ) {}
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = effectiveAccent.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = effectiveAccent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Quick Date Chips (LazyRow)
            FinluxQuickDateChips(
                selectedLocalDate = selectedLocalDate,
                onDateSelected = { date ->
                    selectedLocalDate = date
                    viewingYearMonth = YearMonth.from(date)
                    val (h, m) = clampTimeIfNeeded(date, selectedHour, selectedMinute)
                    selectedHour = h
                    selectedMinute = m
                },
                today = today,
                allowFutureDates = allowFutureDates,
                accentColor = effectiveAccent,
            )

            // Calendar View
            FinluxCalendarView(
                viewingYearMonth = viewingYearMonth,
                onViewingYearMonthChange = { viewingYearMonth = it },
                selectedLocalDate = selectedLocalDate,
                onDateSelected = { date ->
                    selectedLocalDate = date
                    val (h, m) = clampTimeIfNeeded(date, selectedHour, selectedMinute)
                    selectedHour = h
                    selectedMinute = m
                },
                today = today,
                allowFutureDates = allowFutureDates,
                accentColor = effectiveAccent,
            )

            // Time Selector Card (Cupertino Wheel Layout)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = effectiveAccent,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "GIỜ GIAO DỊCH (24H)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                            ),
                            color = tokens.onSurfaceVariant,
                        )
                    }

                    // Quick Time Chips (LazyRow)
                    FinluxQuickTimeChips(
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute,
                        onTimeSelected = { h, m ->
                            selectedHour = h
                            selectedMinute = m
                        },
                        selectedLocalDate = selectedLocalDate,
                        today = today,
                        allowFutureTime = allowFutureDates,
                        zoneId = zoneId,
                        accentColor = effectiveAccent,
                    )

                    // Cupertino Wheel Picker: Hour & Minute
                    val maxHour = if (!allowFutureDates && selectedLocalDate == today) LocalTime.now(zoneId).hour else 23
                    val maxMinute = if (!allowFutureDates && selectedLocalDate == today && selectedHour == LocalTime.now(zoneId).hour) {
                        LocalTime.now(zoneId).minute
                    } else 59

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Giờ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = tokens.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            FinluxWheelPicker(
                                items = (0..23).toList(),
                                selectedValue = selectedHour,
                                maxValue = maxHour,
                                accentColor = effectiveAccent,
                                onValueChange = { h ->
                                    selectedHour = h
                                    val newMaxMin = if (!allowFutureDates && selectedLocalDate == today && h == LocalTime.now(zoneId).hour) {
                                        LocalTime.now(zoneId).minute
                                    } else 59
                                    if (selectedMinute > newMaxMin) {
                                        selectedMinute = newMaxMin
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = 18.dp),
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Phút",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = tokens.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            FinluxWheelPicker(
                                items = (0..59).toList(),
                                selectedValue = selectedMinute,
                                maxValue = maxMinute,
                                accentColor = effectiveAccent,
                                onValueChange = { m ->
                                    selectedMinute = m
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            // Confirm Button
            val selectedInstant = remember(selectedLocalDate, selectedHour, selectedMinute, zoneId) {
                selectedLocalDate.atTime(selectedHour, selectedMinute).atZone(zoneId).toInstant()
            }
            val formattedDateLabel = remember(selectedInstant, zoneId) {
                formatSmartDateTime(selectedInstant, zoneId)
            }

            Button(
                onClick = {
                    onDateTimeSelected(selectedInstant)
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = effectiveAccent,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Áp dụng: $formattedDateLabel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

/**
 * FinluxTimePickerSheet - Premium Liquid Glass BottomSheet chuyên chọn Giờ:Phút.
 *
 * Dùng cho SavingSpinSettingsScreen, RemindersScreen, Alarm configurations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxTimePickerSheet(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Chọn giờ nhắc",
    subtitle: String? = null,
    accentColor: Color? = null,
    allowFutureTime: Boolean = true,
    targetDate: LocalDate? = null,
    zoneId: ZoneId = FinanceTime.defaultZone,
) {
    val tokens = LocalFinluxTokens.current
    val effectiveAccent = accentColor ?: tokens.primary
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedHour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    val today = remember(zoneId) { LocalDate.now(zoneId) }
    val nowTime = remember(zoneId) { LocalTime.now(zoneId) }
    val isRestrictedToday = !allowFutureTime && (targetDate == null || targetDate == today)

    val maxHour = if (isRestrictedToday) nowTime.hour else 23
    val maxMinute = if (isRestrictedToday && selectedHour == nowTime.hour) nowTime.minute else 59

    if (selectedHour > maxHour) {
        selectedHour = maxHour
    }
    if (selectedMinute > maxMinute) {
        selectedMinute = maxMinute
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(width = 38.dp, height = 4.dp),
                ) {}
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = effectiveAccent.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = effectiveAccent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                ),
                                color = tokens.onSurfaceVariant,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Quick Time Chips
            FinluxQuickTimeChips(
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                onTimeSelected = { h, m ->
                    selectedHour = h
                    selectedMinute = m
                },
                selectedLocalDate = targetDate ?: today,
                today = today,
                allowFutureTime = allowFutureTime,
                zoneId = zoneId,
                accentColor = effectiveAccent,
            )

            // Cupertino Drum Wheel Picker
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Giờ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = tokens.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        FinluxWheelPicker(
                            items = (0..23).toList(),
                            selectedValue = selectedHour,
                            maxValue = maxHour,
                            accentColor = effectiveAccent,
                            onValueChange = { h ->
                                selectedHour = h
                                val newMaxMin = if (isRestrictedToday && h == nowTime.hour) nowTime.minute else 59
                                if (selectedMinute > newMaxMin) {
                                    selectedMinute = newMaxMin
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 18.dp),
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Phút",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = tokens.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        FinluxWheelPicker(
                            items = (0..59).toList(),
                            selectedValue = selectedMinute,
                            maxValue = maxMinute,
                            accentColor = effectiveAccent,
                            onValueChange = { m ->
                                selectedMinute = m
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Confirm Button
            val formattedTime = remember(selectedHour, selectedMinute) {
                String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
            }

            Button(
                onClick = {
                    onTimeSelected(selectedHour, selectedMinute)
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = effectiveAccent,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Xác nhận giờ: $formattedTime",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

/**
 * FinluxDatePickerSheet - Premium Liquid Glass BottomSheet chuyên chọn Ngày.
 *
 * Dùng cho GoalsScreen, RemindersScreen, Filter ngày độc lập.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxDatePickerSheet(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Chọn ngày",
    subtitle: String? = null,
    accentColor: Color? = null,
    allowFutureDates: Boolean = false,
    zoneId: ZoneId = FinanceTime.defaultZone,
) {
    val tokens = LocalFinluxTokens.current
    val effectiveAccent = accentColor ?: tokens.primary
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentSelectedDate by remember(selectedDate) { mutableStateOf(selectedDate) }
    var viewingYearMonth by remember(selectedDate) { mutableStateOf(YearMonth.from(selectedDate)) }
    val today = remember(zoneId) { LocalDate.now(zoneId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(width = 38.dp, height = 4.dp),
                ) {}
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = effectiveAccent.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = effectiveAccent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                ),
                                color = tokens.onSurfaceVariant,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Quick Date Chips
            FinluxQuickDateChips(
                selectedLocalDate = currentSelectedDate,
                onDateSelected = { date ->
                    currentSelectedDate = date
                    viewingYearMonth = YearMonth.from(date)
                },
                today = today,
                allowFutureDates = allowFutureDates,
                accentColor = effectiveAccent,
            )

            // Calendar View
            FinluxCalendarView(
                viewingYearMonth = viewingYearMonth,
                onViewingYearMonthChange = { viewingYearMonth = it },
                selectedLocalDate = currentSelectedDate,
                onDateSelected = { date ->
                    currentSelectedDate = date
                },
                today = today,
                allowFutureDates = allowFutureDates,
                accentColor = effectiveAccent,
            )

            // Confirm Button
            val formattedDateLabel = remember(currentSelectedDate) {
                FinanceTime.formatDate(currentSelectedDate)
            }

            Button(
                onClick = {
                    onDateSelected(currentSelectedDate)
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = effectiveAccent,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Xác nhận ngày: $formattedDateLabel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
