package com.giftinapp.business.network.repository.gptcontentrepository

import com.giftinapp.business.model.gptcontent.CompletionResponse
import com.giftinapp.business.model.gptcontent.GptPrompt
import retrofit2.Call

interface GptApiServiceRepo {
    suspend fun getGptContent(prompt:GptPrompt): CompletionResponse
}