package com.giftinapp.business.network.services.cashoutservices

import com.giftinapp.business.network.cashoutmodel.*
import retrofit2.Response

interface CashOutApiServiceMethod {

    suspend fun verifyAccountNumber(authorization:String,account_number:String, bank_code:String):Response<VerifyAccountResponse>

    suspend fun getBankLists(authorization:String, country:String):Response<BankResponse>

    suspend fun initiateTransferProcess(authorization:String,transferRequestModel: InitiateTransferRequestModel):Response<InitiateTransferResponseModel>

    suspend fun transfer(authorization:String,transfer:TransferModel):Response<TransferModelResponse>

}