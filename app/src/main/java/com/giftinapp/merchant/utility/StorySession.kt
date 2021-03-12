package com.giftinapp.merchant.utility

import android.content.Context
import android.content.SharedPreferences
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.MerchantStoryListPojo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StorySession(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val STORY_LIST = "story_list"
    }

    fun setStoryStatusList(list:MutableList<MerchantStoryListPojo>){
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(STORY_LIST,json)
        editor.apply()
    }

    fun getStoryStatusList():MutableList<MerchantStoryListPojo>{
        val gson = Gson()
        val json = prefs.getString(STORY_LIST,null)
        val type = object : TypeToken<MutableList<MerchantStoryListPojo>>(){}.type//converting the json to list
        return gson.fromJson(json,type)//returning the list
    }
}