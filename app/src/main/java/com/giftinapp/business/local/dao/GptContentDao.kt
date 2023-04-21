package com.giftinapp.business.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.giftinapp.business.local.entities.GptDBContent

@Dao
interface GptContentDao {
    @Query("SELECT * FROM gpt_db_content WHERE prompt = :prompt")
    fun getContentByPrompt(prompt: String): GptDBContent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveContent(content: GptDBContent)
}

private const val CACHE_TIMEOUT = 1 * 24 * 60 * 60 * 1000 // 1 day in milliseconds

fun isCacheStale(lastSynced: Long): Boolean {
    return System.currentTimeMillis() - lastSynced > CACHE_TIMEOUT
}