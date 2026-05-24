package com.l2wifi.di

import com.l2wifi.data.repository.AccountRepositoryImpl
import com.l2wifi.data.repository.ConnectionRepositoryImpl
import com.l2wifi.data.repository.HistoryRepositoryImpl
import com.l2wifi.domain.repository.AccountRepository
import com.l2wifi.domain.repository.ConnectionRepository
import com.l2wifi.domain.repository.HistoryRepository
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
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindConnectionRepository(impl: ConnectionRepositoryImpl): ConnectionRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}
