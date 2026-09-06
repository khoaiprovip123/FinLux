package com.finlux.app.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.finlux.app.core.designsystem.component.FinluxAmountInputCard
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxScreenScaffold
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.ThemePreference
import org.junit.Rule
import org.junit.Test

class FinluxUiSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun amountInput_quickChipAndClear_workEndToEnd() {
        var amount by mutableStateOf("")

        composeRule.setContent {
            FinluxTheme(
                preference = ThemePreference.LIGHT,
                uiStyle = AppUiStyle.PRISM,
            ) {
                FinluxAmountInputCard(
                    amountDigits = amount,
                    onAmountChange = { amount = it },
                    quickAmounts = listOf(500_000L),
                )
            }
        }

        composeRule.onNodeWithText("+500k").performClick()
        composeRule.onNodeWithText("500.000", substring = true).assertExists()
        composeRule.onNodeWithContentDescription("Xóa số tiền").performClick()
        composeRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun emptyState_action_isVisibleAndClickable() {
        var clicked by mutableStateOf(false)

        composeRule.setContent {
            FinluxTheme(
                preference = ThemePreference.LIGHT,
                uiStyle = AppUiStyle.PRISM,
            ) {
                FinluxEmptyState(
                    title = "Chưa có dữ liệu",
                    description = "Tạo giao dịch đầu tiên để bắt đầu",
                    actionLabel = "Tạo ngay",
                    onActionClick = { clicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Chưa có dữ liệu").assertExists()
        composeRule.onNodeWithText("Tạo giao dịch đầu tiên để bắt đầu").assertExists()
        composeRule.onNodeWithText("Tạo ngay").performClick()
        assert(clicked)
    }

    @Test
    fun prismScreenScaffold_rendersContent() {
        composeRule.setContent {
            FinluxTheme(
                preference = ThemePreference.LIGHT,
                uiStyle = AppUiStyle.PRISM,
            ) {
                FinluxScreenScaffold {
                    androidx.compose.material3.Text("Nội dung FinLux")
                }
            }
        }

        composeRule.onNodeWithText("Nội dung FinLux").assertExists()
    }
}
