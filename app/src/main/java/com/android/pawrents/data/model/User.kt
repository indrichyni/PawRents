package com.android.pawrents.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val profilePic: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val location: String = "",
    val email: String = "",
    val telephone: String = "",
    val uid: String = "",
    val notifOwnPet: Boolean? = null,
    val notifAdoptPet: Boolean? = null
): Parcelable

//@Parcelize
//data class User(
//    var profilePic: String?  = "",
//    var username: String?  = "",
//    var firstName: String?  = "",
//    var lastName: String?  = "",
//    var gender: String?  = "",
//    var birthDate: String?  = "",
//    var location: String?  = "",
//    var email: String?  = "",
//    var telephone: String?  = "",
//    var uid: String?  = ""
//): Parcelable
