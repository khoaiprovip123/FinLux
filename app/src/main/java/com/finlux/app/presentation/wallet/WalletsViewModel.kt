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
import com.finlux.app.domain.model.WalletType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class WalletActionState(val busy: Boolean = false, val message: String? = null)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalletsViewModel @Inject constructor(
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    salaryCycleRepository: SalaryCycleRepository,
    private val saveWallet: SaveWalletUseCase,
    private val deleteWallet: DeleteWalletUseCase,
    private val transferMoney: TransferMoneyUseCase,
    private val dataSyncManager: com.finlux.app.core.sync.DataSyncManager? = null,
) : ViewModel() {
    private val refreshFlow = dataSyncManager?.refreshTrigger ?: flowOf(0L)

    val wallets = refreshFlow
        .flatMapLatest { walletRepository.observeWallets() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories = categoryRepository.observeCategories()
        .map { it.associateBy(Category::id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val recentTransactions = refreshFlow
        .flatMapLatest { transactionRepository.observeRecent(500) }
        .map { it.collapseInternalTransferPairs() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val financeZone = salaryCycleRepository.observeConfig()
        .map { FinanceTime.zoneOf(it.financeTimeZone) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ZoneId.systemDefault())

    private val mutableActionState = MutableStateFlow(WalletActionState())
    val actionState = mutableActionState.asStateFlow()

    private val mutableIsBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden = mutableIsBalanceHidden.asStateFlow()

    fun toggleHideBalance() {
        mutableIsBalanceHidden.value = !mutableIsBalanceHidden.value
    }

    fun save(wallet: Wallet, onSaved: () -> Unit) = viewModelScope.launch {
        mutableActionState.value = WalletActionState(busy = true)
        when (val result = saveWallet(wallet)) {
            is AppResult.Success -> { mutableActionState.value = WalletActionState(message = "Đã lưu ví"); onSaved() }
            is AppResult.Error -> mutableActionState.value = WalletActionState(message = result.message)
        }
    }

    fun archiveWallet(wallet: Wallet, onArchived: () -> Unit = {}) = save(
        wallet.copy(status = "archived", archivedAt = Instant.now()),
        onSaved = onArchived,
    )

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

    fun categorizeWallets(wallets: List<Wallet>): Map<WalletType, List<Wallet>> =
        wallets.groupBy(Wallet::type)

    fun calculateNetWorth(wallets: List<Wallet>): Long {
        val regularAssets = wallets.filter { it.type != WalletType.CARD && it.status == "active" }
            .sumOf { it.balance.value }
        val creditCardDebt = wallets.filter { it.type == WalletType.CARD && it.status == "active" }
            .sumOf { if (it.balance.value < 0L) -it.balance.value else 0L }
        return regularAssets - creditCardDebt
    }
}
