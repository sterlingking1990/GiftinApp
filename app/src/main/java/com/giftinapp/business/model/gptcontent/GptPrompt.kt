package com.giftinapp.business.model.gptcontent


data class GptPrompt(
    var prompt:String,
    var model:String,
    var temperature:Double,
    var max_tokens: Int
)
