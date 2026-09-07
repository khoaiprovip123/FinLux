package com.finlux.app.data.local.salary

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SalaryCycleReceiverTest {

    @Test
    fun `ACTION_SALARY_PAYDAY constant matches expected intent action`() {
        assertEquals("com.finlux.app.ACTION_SALARY_PAYDAY", ACTION_SALARY_PAYDAY)
    }

    @Test
    fun `salary payday alarm request code matches documented ID`() {
        assertEquals(9925, SALARY_PAYDAY_ALARM_REQUEST_CODE)
    }
}
