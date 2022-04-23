package com.giftinapp.business.network.services.cashoutservices

import com.giftinapp.business.model.FetchBanksResponse
import com.giftinapp.business.network.cashoutmodel.*
import retrofit2.Response
import retrofit2.http.*

interface CashOutApiService {

    @GET("/bank/resolve/")
    suspend fun verifyAccountNumber(@Header("Authorization") authorization: String, @Query("account_number") account_number:String,
                                    @Query("bank_code") bank_code:String): Response<VerifyAccountResponse>

    @GET("/bank?country=nigeria")
    suspend fun getBankLists():FetchBanksResponse

    @POST("transferrecipient")
    suspend fun initiateTransferProcess(@Header("Authorization") authorization: String, @Body transferRequestModel: InitiateTransferRequestModel):Response<InitiateTransferResponseModel>

    @POST("/transfer")
    suspend fun transfer(@Header("Authorization") authorization: String, @Body transfer:TransferModel ):Response<TransferModelResponse>
}