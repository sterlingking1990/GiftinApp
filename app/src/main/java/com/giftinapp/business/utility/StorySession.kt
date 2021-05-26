package com.giftinapp.business.utility

import android.content.Context
import android.content.SharedPreferences
import com.giftinapp.business.R
import com.giftinapp.business.model.MerchantStoryListPojo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StorySession(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val STORY_LIST = "story_list"
    }

    fun setStoryStatusList(list: ArrayList<MerchantStoryListPojo>){
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(STORY_LIST,json)
        editor.apply()
    }

    fun getStoryStatusList():ArrayList<MerchantStoryListPojo>{
        val emptyList = Gson().toJson(ArrayList<MerchantStoryListPojo>())
        return Gson().fromJson(
                prefs.getString(STORY_LIST, emptyList),
                object : TypeToken<ArrayList<MerchantStoryListPojo>>() {
                }.type
        )
    }


    fun clearData(){
        prefs.all.clear()
    }
}

