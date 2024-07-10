package com.android.pawrents.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Favorite(
    val favoriteId: String = "",
    val userId: String = "",
    val petId: String = ""
) : Parcelable