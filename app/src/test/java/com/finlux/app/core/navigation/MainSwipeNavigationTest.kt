package com.finlux.app.core.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MainSwipeNavigationTest {
    @Test
    fun `left swipe moves to next main screen`() {
        assertEquals(Route.Wallets.value, mainRouteAfterSwipe(Route.Home.value, -90f, 72f))
        assertEquals(Route.Reports.value, mainRouteAfterSwipe(Route.Wallets.value, -90f, 72f))
        assertEquals(Route.Settings.value, mainRouteAfterSwipe(Route.Reports.value, -90f, 72f))
    }

    @Test
    fun `right swipe moves to previous main screen`() {
        assertEquals(Route.Reports.value, mainRouteAfterSwipe(Route.Settings.value, 90f, 72f))
        assertEquals(Route.Wallets.value, mainRouteAfterSwipe(Route.Reports.value, 90f, 72f))
    }

    @Test
    fun `short swipe and edge swipe do not navigate`() {
        assertNull(mainRouteAfterSwipe(Route.Home.value, -50f, 72f))
        assertNull(mainRouteAfterSwipe(Route.Home.value, 90f, 72f))
        assertNull(mainRouteAfterSwipe(Route.Settings.value, -90f, 72f))
        assertNull(mainRouteAfterSwipe(Route.Login.value, -90f, 72f))
    }

    @Test
    fun `mostly vertical gesture does not change main screen`() {
        assertNull(mainRouteAfterSwipe(Route.Reports.value, -100f, 120f, 72f))
        assertEquals(Route.Settings.value, mainRouteAfterSwipe(Route.Reports.value, -100f, 30f, 72f))
    }

    @Test
    fun `quick flick from reports opens settings before full distance threshold`() {
        assertEquals(Route.Settings.value, mainRouteAfterSwipe(Route.Reports.value, -38f, 8f, 72f, 240L))
        assertNull(mainRouteAfterSwipe(Route.Reports.value, -38f, 8f, 72f, 600L))
    }
}
