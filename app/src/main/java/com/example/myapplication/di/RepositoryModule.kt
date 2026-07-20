package com.example.myapplication.di

import com.example.myapplication.data.repository.FirebaseAuthRepository
import com.example.myapplication.data.repository.OfflineFirstStreaksRepository
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.StreaksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStreaksRepository(
        offlineFirstStreaksRepository: OfflineFirstStreaksRepository
    ): StreaksRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository
}
