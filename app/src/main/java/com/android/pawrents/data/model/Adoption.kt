package com.android.pawrents.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Adoption(
    val status: String = "",
    val adoptionId: String = "",
    val adoptUserId: String = "",
    val adoptName: String = "",
    val adoptAddress: String = "",
    val adoptCost: String = "",
    val adoptAllergy: Boolean = false,
    val adoptOwn: Boolean = false,
    val adoptBusy: String = "",
    val ownerId: String = "",
    val petId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val contactBefore: Long? = null
): Parcelable
