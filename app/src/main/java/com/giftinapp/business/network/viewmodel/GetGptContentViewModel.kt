package com.giftinapp.business.network.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.giftinapp.business.BuildConfig
import com.giftinapp.business.local.dao.GptContentDao
import com.giftinapp.business.local.dao.isCacheStale
import com.giftinapp.business.local.entities.GptDBContent
import com.giftinapp.business.model.GptContent
import com.giftinapp.business.model.UnsplashImageResponse
import com.giftinapp.business.model.gptcontent.Choice
import com.giftinapp.business.model.gptcontent.GptPrompt
import com.giftinapp.business.network.repository.gptcontentrepository.GptApiServiceRepo
import com.giftinapp.business.utility.NetworkHelper
import com.giftinapp.business.utility.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class GetGptContentViewModel @Inject constructor(
    private val gptApiServiceRepo: GptApiServiceRepo,
    private val gptContentDao: GptContentDao,
    private val networkHelper: NetworkHelper): ViewModel(){
    private val _gptContent = MutableLiveData<Resource<List<Choice>>>()
    val gptContent: LiveData<Resource<List<Choice>>>

    get() = _gptContent

    private val _gptAllContent = mutableListOf<Choice>()
    val gptAllContent: MutableList<Choice>
    get() = _gptAllContent

    fun getAgriculturePoem(prompt: GptPrompt): Flow<GptContent> = flow {

        val cachedContent = accessGptDBContent(prompt)
        if (cachedContent != null && !isCacheStale(cachedContent.lastSynced)) {
            emit(GptContent(cachedContent.title, cachedContent.text, cachedContent.imageUrl,cachedContent.imageOwner,cachedContent.imageOwnerUsername,cachedContent.imageOwnerLink))
            return@flow
        }

        val gptRemoteContent = gptApiServiceRepo.getGptContent(prompt)
        val text = formatText(gptRemoteContent.choices[0].text,prompt.max_tokens)
        val unsplashImage = generateImage(text,prompt.prompt.split(" ").takeLast(2).joinToString(separator = " "))
        val title = generateTitle(text)

        val newGptContent = GptDBContent(prompt.prompt, title, text, unsplashImage.image, unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink,System.currentTimeMillis())
        saveNewGptContentToDB(newGptContent)
        emit(GptContent(title,text,unsplashImage.image,unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink))
    }

    fun getAgricultureAllergy(prompt: GptPrompt): Flow<GptContent> = flow {

        val cachedContent = accessGptDBContent(prompt)
        if (cachedContent != null && !isCacheStale(cachedContent.lastSynced)) {
            emit(GptContent(cachedContent.title, cachedContent.text, cachedContent.imageUrl,cachedContent.imageOwner,cachedContent.imageOwnerUsername,cachedContent.imageOwnerLink))
            return@flow
        }

        val text = formatText(gptApiServiceRepo.getGptContent(prompt).choices[0].text,prompt.max_tokens)
        val unsplashImage = generateImage(text,prompt.prompt.split(" ").takeLast(2).joinToString(separator = " "))
        val title = generateTitle(text)

        val newGptContent = GptDBContent(prompt.prompt, title, text, unsplashImage.image, unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink,System.currentTimeMillis())
        saveNewGptContentToDB(newGptContent)

        emit(GptContent(title,text,unsplashImage.image,unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink))
    }

    fun getAgricultureArticle(prompt: GptPrompt): Flow<GptContent> = flow {

        val cachedContent = accessGptDBContent(prompt)
        if (cachedContent != null && !isCacheStale(cachedContent.lastSynced)) {
            emit(GptContent(cachedContent.title, cachedContent.text, cachedContent.imageUrl, cachedContent.imageOwner,cachedContent.imageOwnerUsername,cachedContent.imageOwnerLink))
            return@flow
        }

        val text = formatText(gptApiServiceRepo.getGptContent(prompt).choices[0].text,prompt.max_tokens)
        val unsplashImage = generateImage(text, prompt.prompt.split(" ").takeLast(2).joinToString(separator = " "))
        val title = generateTitle(text)

        val newGptContent = GptDBContent(prompt.prompt, title, text, unsplashImage.image, unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink,System.currentTimeMillis())
        saveNewGptContentToDB(newGptContent)

        emit(GptContent(title,text,unsplashImage.image,unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink))
    }

    fun getAgricultureNews(prompt: GptPrompt): Flow<GptContent> = flow {
        val cachedContent = accessGptDBContent(prompt)
        if (cachedContent != null && !isCacheStale(cachedContent.lastSynced)) {
            emit(GptContent(cachedContent.title, cachedContent.text, cachedContent.imageUrl, cachedContent.imageOwner,cachedContent.imageOwnerUsername,cachedContent.imageOwnerLink))
            return@flow
        }

        val text = formatText(gptApiServiceRepo.getGptContent(prompt).choices[0].text,prompt.max_tokens)
        val unsplashImage = generateImage(text, prompt.prompt.split(" ").takeLast(2).joinToString(separator = " "))
        val title = generateTitle(text)

        val newGptContent = GptDBContent(prompt.prompt, title, text, unsplashImage.image, unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink,System.currentTimeMillis())
        saveNewGptContentToDB(newGptContent)

        emit(GptContent(title,text,unsplashImage.image,unsplashImage.ownerName,unsplashImage.ownerUsername,unsplashImage.ownerLink))
    }

    private suspend fun generateImage(newsTopic: String,header:String): UnsplashImageResponse = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()

            // Set the Unsplash API endpoint URL and parameters
            val url = "https://api.unsplash.com/photos/random"
            val query = newsTopic.replace(" ", ",").split(" ")
            val request = Request.Builder()
                .url("$url/?query=$header")
                .addHeader("Authorization", "Client-ID ${BuildConfig.UNSPLASH_API_KEY}")
                .build()

            // Send the API request and retrieve the response
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            Log.d("ResponseBody",responseBody)
            // Parse the response and retrieve the image URL
            val jsonObject = JSONObject(responseBody)
            val urlsObject = jsonObject.getJSONObject("urls")
            val imageUrl = urlsObject.getString("regular")
            val ownerObject = jsonObject.getJSONObject("user")
            val ownerName = ownerObject.getString("name")
            val ownerUsername = ownerObject.getString("username")
            val ownerLinkObject = ownerObject.getJSONObject("links")
            val ownerLink = "https://unsplash.com/"+ownerObject.getString("username")
            UnsplashImageResponse(imageUrl,ownerName,ownerUsername,ownerLink)
        } catch (e: IOException) {
            Log.e("MainActivity", "Error generating image", e)
            UnsplashImageResponse("","","","")
        }
    }

    private suspend fun generateTitle(content:String):String{
        return gptApiServiceRepo.getGptContent(GptPrompt("Generate a title based on the following content:\\n\\n$content\\n\\nTitle:","text-davinci-002",0.7,50)).choices[0].text
    }

    private fun formatText(text:String, token:Int):String{
        val words = text.split(" ")
        if (words.size > token) {
            val truncatedWords = words.subList(0, token)
            return "${truncatedWords.joinToString(" ")} ..."
        }
        return text
    }

    private fun accessGptDBContent(prompt: GptPrompt): GptDBContent? {
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit(Callable<GptDBContent> {
            gptContentDao.getContentByPrompt(prompt.prompt)
        })
        return future.get()
    }

    private suspend fun saveNewGptContentToDB(newGptContent: GptDBContent) {
        gptContentDao.saveContent(newGptContent)
    }

    fun getAnalyticsSummary(prompt: GptPrompt): Flow<String> = flow {

        val gptRemoteContent = gptApiServiceRepo.getGptContent(prompt)
        val text = formatText(gptRemoteContent.choices[0].text,prompt.max_tokens)

        emit(text)
    }

}


