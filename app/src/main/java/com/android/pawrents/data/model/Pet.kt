package com.android.pawrents.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pet(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val age: Int = 0,
    val weight: Float = 0.0f,
    val breed: String = "",
    val color: String = "",
    val description: String = "",
    val category: String = "",
    val vaccine: String = "",
    val gender: String = "",
    val photoLink: String = "",
    val timestamp: Long = 0L,
    val userId: String = "",
    val adoptUserId: String? = null
): Parcelable
