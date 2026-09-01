package com.finlux.app.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.collapseInternalTransferPairs
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.DeleteWalletUseCase
import com.finlux.app.domain.usecase.SaveWalletUseCase
import com.finlux.app.domain.usecase.TransferMoneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class WalletActionState(val busy: Boolean = false, val message: String? = null)

@HiltViewModel
class WalletsViewModel @Inject constructor(
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    salaryCycleRepository: SalaryCycleRepository,
    private val saveWallet: SaveWalletUseCase,
    private val deleteWallet: DeleteWalletUseCase,
    private val transferMoney: TransferMoneyUseCase,
) : ViewModel() {
    val wallets = walletRepository.observeWallets().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = categoryRepository.observeCategories()
        .map { it.associateBy(Category::id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
    val recentTransactions = transactionRepository.observeRecent(500)
        .map { it.collapseInternalTransferPairs() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val financeZone = salaryCycleRepository.observeConfig()
        .map { FinanceTime.zoneOf(it.financeTimeZone) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ZoneId.systemDefault())

    private val mutableActionState = MutableStateFlow(WalletActionState())
    val actionState = mutableActionState.asStateFlow()

    fun save(wallet: Wallet, onSaved: () -> Unit) = viewModelScope.launch {
        mutableActionState.value = WalletActionState(busy = true)
        when (val result = saveWallet(wallet)) {
            is AppResult.Success -> { mutableActionState.value = WalletActionState(message = "Đã lưu ví"); onSaved() }
            is AppResult.Error -> mutableActionState.value = WalletActionState(message = result.message)
        }
    }

    fun delete(wallet: Wallet) = viewModelScope.launch {
        when (val result = deleteWallet(wallet)) {
            is AppResult.Success -> mutableActionState.value = WalletActionState(message = "Đã xóa ví")
            is AppResult.Error -> mutableActionState.value = WalletActionState(message = result.message)
        }
    }

    fun transfer(
        sourceId: String,
        destinationId: String,
        amount: Long,
        note: String,
        date: Instant = Instant.now(),
        onSaved: () -> Unit,
    ) = viewModelScope.launch {
        mutableActionState.value = WalletActionState(busy = true)
        when (val result = transferMoney(sourceId, destinationId, amount, note, date)) {
            is AppResult.Success -> { mutableActionState.value = WalletActionState(message = "Chuyển tiền thành công"); onSaved() }
            is AppResult.Error -> mutableActionState.value = WalletActionState(message = result.message)
        }
    }

    fun consumeMessage() { mutableActionState.value = mutableActionState.value.copy(message = null) }
}
