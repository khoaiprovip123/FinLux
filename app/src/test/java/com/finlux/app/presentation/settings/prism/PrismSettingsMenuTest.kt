package com.finlux.app.presentation.settings.prism

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PrismSettingsMenuTest {
    @Test
    fun `settings menu exposes every specified action exactly once`() {
        assertEquals(PrismSettingsAction.entries.size, prismSettingsActions.distinct().size)
        assertTrue(
            prismSettingsActions.containsAll(
                listOf(
                    PrismSettingsAction.ACCOUNT,
                    PrismSettingsAction.WALLETS,
                    PrismSettingsAction.BUDGET,
                    PrismSettingsAction.DEBT,
                    PrismSettingsAction.APPEARANCE,
                    PrismSettingsAction.CATEGORIES,
                    PrismSettingsAction.REMINDERS,
                    PrismSettingsAction.NOTIFICATIONS,
                    PrismSettingsAction.BACKUP,
                    PrismSettingsAction.SECURITY,
                    PrismSettingsAction.SUPPORT,
                    PrismSettingsAction.ABOUT,
                    PrismSettingsAction.UPDATE,
                ),
            ),
        )
    }

    @Test
    fun `navigable settings actions use existing app routes`() {
        val routes = prismSettingsActions.mapNotNull(PrismSettingsAction::route).toSet()

        assertEquals(setOf("wallets", "budget", "debt", "categories", "reminders", "notifications"), routes)
    }
}
