package com.giftinapp.business.network.repository.cashoutrepository

import com.giftinapp.business.network.cashoutmodel.*
import com.giftinapp.business.network.services.cashoutservices.CashOutApiServiceMethod
import retrofit2.Response
import javax.inject.Inject

class CashOutApiServiceRepositoryImpl @Inject constructor(private val cashOutApiServiceMethod: CashOutApiServiceMethod): CashOutApiServiceRepository {
    override suspend fun verifyAccountNumber(authorization:String,account_number: String, bank_code: String): Response<VerifyAccountResponse> {
        return cashOutApiServiceMethod.verifyAccountNumber(authorization,account_number,bank_code)
    }

    override suspend fun getBankLists(authorization:String,country: String): Response<BankResponse> {
        return cashOutApiServiceMethod.getBankLists(authorization,country)
    }

    override suspend fun initiateTransferProcess(authorization:String,transferRequestModel: InitiateTransferRequestModel): Response<InitiateTransferResponseModel> {

        return cashOutApiServiceMethod.initiateTransferProcess(authorization,transferRequestModel)

    }

    override suspend fun transfer(authorization:String,transfer: TransferModel): Response<TransferModelResponse> {

        return cashOutApiServiceMethod.transfer(authorization,transfer)
    }

}