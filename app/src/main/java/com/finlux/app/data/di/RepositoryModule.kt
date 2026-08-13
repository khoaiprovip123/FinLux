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
    fun provideAuthRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
        storage: FirebaseStorage?,
    ): AuthRepository =
        if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null && storage != null) {
            FirebaseAuthRepository(auth, firestore, storage)
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
    ): WalletRepository = if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) FirebaseReadRepository(auth, firestore) else demo

    @Provides
    @Singleton
    fun provideCategoryRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): CategoryRepository = if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) FirebaseReadRepository(auth, firestore) else demo

    @Provides
    @Singleton
    fun provideBudgetRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): BudgetRepository = if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) FirebaseReadRepository(auth, firestore) else demo

    @Provides
    @Singleton
    fun provideReminderRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): ReminderRepository = if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) FirebaseReadRepository(auth, firestore) else demo

    @Provides
    @Singleton
    fun provideGoalRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): GoalRepository = if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) FirebaseReadRepository(auth, firestore) else demo

    @Provides
    @Singleton
    fun provideReceiptStorageRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        storage: FirebaseStorage?,
    ): ReceiptStorageRepository = if (BuildConfig.FIREBASE_CONFIGURED && auth != null && storage != null) {
        FirebaseReceiptStorageRepository(auth, storage)
    } else demo

    @Provides
    @Singleton
    fun provideDashboardRepository(
        demo: DemoFinluxRepository,
        auth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
    ): DashboardRepository = if (BuildConfig.FIREBASE_CONFIGURED && auth != null && firestore != null) FirebaseReadRepository(auth, firestore) else demo
}
