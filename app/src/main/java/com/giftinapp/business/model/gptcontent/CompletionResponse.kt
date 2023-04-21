package com.giftinapp.business.model.gptcontent

import com.google.gson.annotations.SerializedName

data class CompletionResponse(
    @SerializedName("choices")
    val choices: List<Choice>
)

data class Choice(
    @SerializedName("text")
    val text: String
)
