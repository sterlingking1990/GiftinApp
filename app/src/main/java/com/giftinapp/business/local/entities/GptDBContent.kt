package com.giftinapp.business.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gpt_db_content")
data class GptDBContent(
    @PrimaryKey val prompt: String,
    val title: String,
    val text: String,
    val imageUrl: String,
    val imageOwner:String,
    val imageOwnerUsername:String,
    val imageOwnerLink:String,
    val lastSynced: Long
)