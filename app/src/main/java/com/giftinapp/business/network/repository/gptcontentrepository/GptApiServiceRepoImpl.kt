package com.giftinapp.business.network.repository.gptcontentrepository

import com.giftinapp.business.model.gptcontent.CompletionResponse
import com.giftinapp.business.model.gptcontent.GptPrompt
import com.giftinapp.business.network.services.gptcontentservice.GptApiServiceMethod
import retrofit2.Call
import javax.inject.Inject

class GptApiServiceRepoImpl @Inject constructor(private val gptApiServiceMethod: GptApiServiceMethod): GptApiServiceRepo {
    override suspend fun getGptContent(prompt: GptPrompt): CompletionResponse {
        return gptApiServiceMethod.getGptContent(prompt)
    }

}