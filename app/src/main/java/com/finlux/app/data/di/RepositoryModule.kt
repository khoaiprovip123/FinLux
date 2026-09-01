package com.finlux.app.data.di

import com.finlux.app.BuildConfig
import com.finlux.app.data.demo.DemoFinluxRepository
import com.finlux.app.data.demo.DemoSalaryCycleRepository
import com.finlux.app.data.demo.DemoSavingSpinRepository
import com.finlux.app.data.demo.DemoTransactionRangeRepository
import com.finlux.app.data.local.datastore.DataStoreThemePreferenceRepository
import com.finlux.app.data.local.reminder.AlarmReminderScheduler
import com.finlux.app.data.local.savingspin.AlarmSavingSpinScheduler
import com.finlux.app.data.remote.firebase.FirebaseAuthRepository
import com.finlux.app.data.remote.firebase.FirebaseBudgetRepository
import com.finlux.app.data.remote.firebase.FirebaseCategoryRepository
import com.finlux.app.data.remote.firebase.FirebaseDashboardRepository
import com.finlux.app.data.remote.firebase.FirebaseDebtRepository
import com.finlux.app.data.remote.firebase.FirebaseGoalRepository
import com.finlux.app.data.remote.firebase.FirebaseNotificationRepository
import com.finlux.app.data.remote.firebase.FirebaseReceiptStorageRepository
import com.finlux.app.data.remote.firebase.FirebaseReminderRepository
import com.finlux.app.data.remote.firebase.FirebaseSalaryCycleRepository
import com.finlux.app.data.remote.firebase.FirebaseSavingSpinRepository
import com.finlux.app.data.remote.firebase.FirebaseTransactionRangeRepository
import com.finlux.app.data.remote.firebase.FirebaseTransactionRepository
import com.finlux.app.data.remote.firebase.FirebaseWalletRepository
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.core.time.SystemFinanceClock
import com.finlux.app.domain.usecase.DefaultFinancialPeriodResolver
import com.finlux.app.domain.usecase.DefaultSalaryCycleCalculator
import com.finlux.app.domain.usecase.FinancialPeriodResolver
import com.finlux.app.domain.usecase.SalaryCycleCalculator
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.DebtRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.ReceiptStorageRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SavingSpinRepository
import com.finlux.app.domain.repository.SavingSpinScheduler
import com.finlux.app.domain.repository.ThemePreferenceRepository
import com.finlux.app.domain.repository.TransactionRangeRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.UiPreferencesRepository
import com.finlux.app.domain.repository.WalletRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        implementation: DataStoreThemePreferenceRepository,
    ): ThemePreferenceRepository

    @Binds
    @Singleton
    abstract fun bindUiPreferencesRepository(
        implementation: DataStoreThemePreferenceRepository,
    ): UiPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        implementation: AlarmReminderScheduler,
    ): ReminderScheduler

    @Binds
    @Singleton
    abstract fun bindDebtPreferenceRepository(
        implementation: com.finlux.app.data.local.datastore.DataStoreDebtPreferenceRepository,
    ): com.finlux.app.domain.repository.DebtPreferenceRepository

    @Binds
    @Singleton
    abstract fun bindFinanceClock(
        implementation: SystemFinanceClock,
    ): FinanceClock

    @Binds
    @Singleton
    abstract fun bindSalaryCycleCalculator(
        implementation: DefaultSalaryCycleCalculator,
    ): SalaryCycleCalculator

    @Binds
    @Singleton
    abstract fun bindFinancialPeriodResolver(
        implementation: DefaultFinancialPeriodResolver,
    ): FinancialPeriodResolver

    @Binds
    @Singleton
    abstract fun bindSalaryCycleScheduler(
        implementation: com.finlux.app.data.local.salary.AlarmSalaryCycleScheduler,
    ): com.finlux.app.domain.repository.SalaryCycleScheduler

    @Binds
    @Singleton
    abstract fun bindSavingSpinScheduler(
        implementation: AlarmSavingSpinScheduler,
    ): SavingSpinScheduler
}

@Module
@InstallIn(SingletonComponent::class)
object FinanceRepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
        storage: FirebaseStorage?,
        messaging: FirebaseMessaging?,
    ): AuthRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null && storage != null) {
            FirebaseAuthRepository(auth, firestore, storage, messaging)
        } else demo

    @Provides
    @Singleton
    fun provideTransactionRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): TransactionRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseTransactionRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideWalletRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): WalletRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseWalletRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideCategoryRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): CategoryRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseCategoryRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideBudgetRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): BudgetRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseBudgetRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideReminderRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): ReminderRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseReminderRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideGoalRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): GoalRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseGoalRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideReceiptStorageRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        storage: FirebaseStorage?,
    ): ReceiptStorageRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && storage != null) {
            FirebaseReceiptStorageRepository(auth, storage)
        } else demo

    @Provides
    @Singleton
    fun provideDashboardRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): DashboardRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseDashboardRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideNotificationRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): NotificationRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseNotificationRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideDebtRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): DebtRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseDebtRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideTransactionRangeRepository(
        demo: DemoTransactionRangeRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): TransactionRangeRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseTransactionRangeRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideSalaryCycleRepository(
        demo: DemoSalaryCycleRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): SalaryCycleRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseSalaryCycleRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideDealRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): com.finlux.app.domain.repository.DealRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            com.finlux.app.data.remote.firebase.FirebaseDealRepository(auth, firestore)
        } else demo

    @Provides
    @Singleton
    fun provideSavingSpinRepository(
        demo: DemoSavingSpinRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): SavingSpinRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) {
            FirebaseSavingSpinRepository(auth, firestore)
        } else demo
}
