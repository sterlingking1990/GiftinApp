package com.giftinapp.business.network.services.gptcontentservice

import com.giftinapp.business.model.gptcontent.CompletionResponse
import com.giftinapp.business.model.gptcontent.GptPrompt
import retrofit2.Call
import javax.inject.Inject

class GptApiServiceImpl @Inject constructor(private val gptApiService: GptApiService): GptApiServiceMethod {
    override suspend fun getGptContent(prompt: GptPrompt): CompletionResponse {
        return gptApiService.getCompletion(prompt)
    }
}