package com.finlux.app.data.di

import android.content.Context
import com.finlux.app.BuildConfig
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DealRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.backup.ExportBackupUseCase
import com.finlux.app.domain.usecase.backup.RestoreBackupUseCase
import com.finlux.app.domain.usecase.backup.ValidateBackupUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideExportBackupUseCase(
        walletRepository: WalletRepository,
        categoryRepository: CategoryRepository,
        transactionRepository: TransactionRepository,
        budgetRepository: BudgetRepository,
        debtRepository: DebtRepository,
        goalRepository: GoalRepository,
        reminderRepository: ReminderRepository,
        salaryCycleRepository: SalaryCycleRepository,
        dealRepository: DealRepository,
        savingSpinRepository: SavingSpinRepository,
        @ApplicationContext context: Context,
    ): ExportBackupUseCase = ExportBackupUseCase(
        walletRepository = walletRepository,
        categoryRepository = categoryRepository,
        transactionRepository = transactionRepository,
        budgetRepository = budgetRepository,
        debtRepository = debtRepository,
        goalRepository = goalRepository,
        reminderRepository = reminderRepository,
        salaryCycleRepository = salaryCycleRepository,
        dealRepository = dealRepository,
        savingSpinRepository = savingSpinRepository,
        appVersionName = BuildConfig.VERSION_NAME,
        appVersionCode = BuildConfig.VERSION_CODE,
        cacheDir = context.cacheDir,
    )

    @Provides
    @Singleton
    fun provideValidateBackupUseCase(): ValidateBackupUseCase = ValidateBackupUseCase()

    @Provides
    @Singleton
    fun provideRestoreBackupUseCase(
        walletRepository: WalletRepository,
        categoryRepository: CategoryRepository,
        transactionRepository: TransactionRepository,
        budgetRepository: BudgetRepository,
        debtRepository: DebtRepository,
        goalRepository: GoalRepository,
        reminderRepository: ReminderRepository,
        salaryCycleRepository: SalaryCycleRepository,
        dealRepository: DealRepository,
        savingSpinRepository: SavingSpinRepository,
        reminderScheduler: ReminderScheduler,
    ): RestoreBackupUseCase = RestoreBackupUseCase(
        walletRepository = walletRepository,
        categoryRepository = categoryRepository,
        transactionRepository = transactionRepository,
        budgetRepository = budgetRepository,
        debtRepository = debtRepository,
        goalRepository = goalRepository,
        reminderRepository = reminderRepository,
        salaryCycleRepository = salaryCycleRepository,
        dealRepository = dealRepository,
        savingSpinRepository = savingSpinRepository,
        reminderScheduler = reminderScheduler,
    )

    @Provides
    fun provideIoDispatcher(): kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
}
