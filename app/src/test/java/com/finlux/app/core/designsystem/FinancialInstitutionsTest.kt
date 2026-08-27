package com.finlux.app.core.designsystem

import com.finlux.app.domain.model.WalletType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FinancialInstitutionsTest {

    @Test
    fun `institutions catalog contains major Vietnamese banks and e-wallets`() {
        val list = VietnameseFinancialInstitutions
        assertTrue(list.size >= 25, "Expected at least 25 financial institutions, found ${list.size}")

        val vcb = list.firstOrNull { it.code == "VCB" }
        assertNotNull(vcb)
        assertEquals("Vietcombank", vcb?.shortName)
        assertEquals(WalletType.BANK, vcb?.type)

        val momo = list.firstOrNull { it.code == "MOMO" }
        assertNotNull(momo)
        assertEquals("MoMo", momo?.shortName)
        assertEquals(WalletType.EWALLET, momo?.type)

        val mb = list.firstOrNull { it.code == "MB" }
        assertNotNull(mb)
        assertEquals("MB Bank", mb?.shortName)
    }

    @Test
    fun `findInstitutionForWallet matches keywords and abbreviations accurately`() {
        assertEquals("VCB", findInstitutionForWallet("Vietcombank")?.code)
        assertEquals("VCB", findInstitutionForWallet("vcb")?.code)
        assertEquals("TCB", findInstitutionForWallet("Techcombank")?.code)
        assertEquals("TCB", findInstitutionForWallet("tcb")?.code)
        assertEquals("MB", findInstitutionForWallet("MB Bank")?.code)
        assertEquals("MB", findInstitutionForWallet("mbbank")?.code)
        assertEquals("MOMO", findInstitutionForWallet("Ví MoMo")?.code)
        assertEquals("ZALOPAY", findInstitutionForWallet("ZaloPay")?.code)
        assertEquals("VTMONEY", findInstitutionForWallet("Viettel Money")?.code)
        assertEquals("SHOPEEPAY", findInstitutionForWallet("ShopeePay")?.code)
        assertEquals("CASH", findInstitutionForWallet("Tiền mặt")?.code)
        assertEquals("SAVE", findInstitutionForWallet("Sổ tiết kiệm")?.code)
    }
}
