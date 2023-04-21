package com.giftinapp.business.network.di

import com.giftinapp.business.network.services.cashoutservices.CashOutApiServiceMethod
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepository
import com.giftinapp.business.network.repository.cashoutrepository.CashOutApiServiceRepositoryImpl
import com.giftinapp.business.network.repository.gptcontentrepository.GptApiServiceRepo
import com.giftinapp.business.network.repository.gptcontentrepository.GptApiServiceRepoImpl
import com.giftinapp.business.network.repository.postrepository.PostApiServiceRepo
import com.giftinapp.business.network.repository.postrepository.PostApiServiceRepoImpl
import com.giftinapp.business.network.services.cashoutservices.CashOutServiceMethodImpl
import com.giftinapp.business.network.services.gptcontentservice.GptApiServiceImpl
import com.giftinapp.business.network.services.gptcontentservice.GptApiServiceMethod
import com.giftinapp.business.network.services.postservice.PostApiServiceImpl
import com.giftinapp.business.network.services.postservice.PostApiServiceMethod
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

    @Binds
    abstract fun bindPostApiServiceMethod(postApiServiceImpl: PostApiServiceImpl): PostApiServiceMethod

    @Binds
    abstract fun bindPostApiServiceRepo(postApiServiceRepoImpl: PostApiServiceRepoImpl): PostApiServiceRepo

    @Binds
    abstract fun bindGptApiServiceMethod(gptApiServiceImpl: GptApiServiceImpl): GptApiServiceMethod

    @Binds
    abstract fun bindGptApiServiceRepo(gptApiServiceRepoImpl: GptApiServiceRepoImpl): GptApiServiceRepo
}