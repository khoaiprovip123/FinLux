package com.finlux.app.data.di

import com.finlux.app.BuildConfig
import com.finlux.app.data.demo.DemoFinluxRepository
import com.finlux.app.data.local.datastore.DataStoreThemePreferenceRepository
import com.finlux.app.data.local.reminder.AlarmReminderScheduler
import com.finlux.app.data.remote.firebase.FirebaseAuthRepository
import com.finlux.app.data.remote.firebase.FirebaseReadRepository
import com.finlux.app.data.remote.firebase.FirebaseReceiptStorageRepository
import com.finlux.app.data.remote.firebase.FirebaseTransactionRepository
import com.finlux.app.domain.repository.AuthRepository
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.DashboardRepository
import com.finlux.app.domain.repository.GoalRepository
import com.finlux.app.domain.repository.ThemePreferenceRepository
import com.finlux.app.domain.repository.UiPreferencesRepository
import com.finlux.app.domain.repository.TransactionRepository
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.ReceiptStorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
}

@Module
@InstallIn(SingletonComponent::class)
object FinanceRepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(demo: DemoFinluxRepository): AuthRepository =
        if (BuildConfig.FIREBASE_CONFIGURED) {
            FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), FirebaseStorage.getInstance())
        } else demo

    @Provides
    @Singleton
    fun provideTransactionRepository(demo: DemoFinluxRepository): TransactionRepository =
        if (BuildConfig.FIREBASE_CONFIGURED) {
            FirebaseTransactionRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        } else demo

    @Provides
    @Singleton
    fun provideWalletRepository(
        demo: DemoFinluxRepository,
    ): WalletRepository = if (BuildConfig.FIREBASE_CONFIGURED) firebaseReadRepository() else demo

    @Provides
    @Singleton
    fun provideCategoryRepository(
        demo: DemoFinluxRepository,
    ): CategoryRepository = if (BuildConfig.FIREBASE_CONFIGURED) firebaseReadRepository() else demo

    @Provides
    @Singleton
    fun provideBudgetRepository(
        demo: DemoFinluxRepository,
    ): BudgetRepository = if (BuildConfig.FIREBASE_CONFIGURED) firebaseReadRepository() else demo

    @Provides
    @Singleton
    fun provideReminderRepository(
        demo: DemoFinluxRepository,
    ): ReminderRepository = if (BuildConfig.FIREBASE_CONFIGURED) firebaseReadRepository() else demo

    @Provides
    @Singleton
    fun provideGoalRepository(
        demo: DemoFinluxRepository,
    ): GoalRepository = if (BuildConfig.FIREBASE_CONFIGURED) firebaseReadRepository() else demo

    @Provides
    @Singleton
    fun provideReceiptStorageRepository(
        demo: DemoFinluxRepository,
    ): ReceiptStorageRepository = if (BuildConfig.FIREBASE_CONFIGURED) {
        FirebaseReceiptStorageRepository(FirebaseAuth.getInstance(), FirebaseStorage.getInstance())
    } else demo

    @Provides
    @Singleton
    fun provideDashboardRepository(
        demo: DemoFinluxRepository,
    ): DashboardRepository = if (BuildConfig.FIREBASE_CONFIGURED) firebaseReadRepository() else demo

    private fun firebaseReadRepository() =
        FirebaseReadRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
}
