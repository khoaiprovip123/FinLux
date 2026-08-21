package com.finlux.app.data.di

import com.finlux.app.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth? =
        if (BuildConfig.FIREBASE_CONFIGURED) runCatching { FirebaseAuth.getInstance() }.getOrNull() else null

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore? =
        if (BuildConfig.FIREBASE_CONFIGURED) runCatching { FirebaseFirestore.getInstance() }.getOrNull() else null

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage? =
        if (BuildConfig.FIREBASE_CONFIGURED) runCatching { FirebaseStorage.getInstance() }.getOrNull() else null

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging? =
        if (BuildConfig.FIREBASE_CONFIGURED) runCatching { FirebaseMessaging.getInstance() }.getOrNull() else null
}
