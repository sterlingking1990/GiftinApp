package com.giftinapp.business.network.services.gptcontentservice

import com.giftinapp.business.model.gptcontent.CompletionResponse
import com.giftinapp.business.model.gptcontent.GptPrompt
import retrofit2.Call

interface GptApiServiceMethod {

    suspend fun getGptContent(prompt:GptPrompt): CompletionResponse
}