package com.giftinapp.business.model

import com.google.gson.annotations.SerializedName

data class FirebaseConfig(
    @SerializedName("version")
    val version: Int,
    @SerializedName("force_update")
    val force_update: Boolean,
) {
    companion object {
        const val UPDATE_STATUS_VERSION = "UPDATE_STATUS_VERSION"
    }
}
