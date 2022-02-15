package com.giftinapp.business.network.services.cashoutservices

import com.giftinapp.business.network.cashoutmodel.*
import com.giftinapp.business.network.services.cashoutservices.CashOutApiService
import com.giftinapp.business.network.services.cashoutservices.CashOutApiServiceMethod
import retrofit2.Response
import javax.inject.Inject

class CashOutServiceMethodImpl @Inject constructor(private val cashOutApiService: CashOutApiService): CashOutApiServiceMethod {
    override suspend fun verifyAccountNumber(authorization:String,account_number: String, bank_code: String): Response<VerifyAccountResponse> {
        return cashOutApiService.verifyAccountNumber(authorization,account_number,bank_code)
    }

    override suspend fun getBankLists(): Response<BankResponse> {
        return cashOutApiService.getBankLists()
    }

    override suspend fun initiateTransferProcess(authorization:String,transferRequestModel: InitiateTransferRequestModel): Response<InitiateTransferResponseModel> {
        return cashOutApiService.initiateTransferProcess(authorization,transferRequestModel)
    }

    override suspend fun transfer(authorization:String,transfer: TransferModel): Response<TransferModelResponse> {
        return cashOutApiService.transfer(authorization,transfer)
    }

}