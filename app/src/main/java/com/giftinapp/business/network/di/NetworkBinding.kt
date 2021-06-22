package com.giftinapp.business.network.di

import com.giftinapp.business.network.services.cashoutservices.CashOutApiServiceMethod
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepository
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepositoryImpl
import com.giftinapp.business.network.services.cashoutservices.CashOutServiceMethodImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
abstract class NetworkBinding {

    @Binds
    abstract fun bindCashOutApiSeriveMethod(cashoutServiceMethodImpl: CashOutServiceMethodImpl): CashOutApiServiceMethod

    @Binds
    abstract fun bindCashOutApiServiceRepository(cashOutApiServiceRepositoryImpl: CashOutApiServiceRepositoryImpl): CashOutApiServiceRepository
}