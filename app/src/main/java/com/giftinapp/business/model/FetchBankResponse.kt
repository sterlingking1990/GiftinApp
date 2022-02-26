package com.giftinapp.business.model

import com.giftinapp.business.network.cashoutmodel.DataXXX
import com.google.gson.annotations.SerializedName

data class FetchBanksResponse(

    @SerializedName("data")
    val banks: List<BankItem>
)

data class BankItem(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("code")
    val code: String? = null
)