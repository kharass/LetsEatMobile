package com.leshen.letseatmobile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel : ViewModel() {
    fun getUserInfo(auth: FirebaseAuth): UserInfo {
        val userEmail = auth.currentUser?.email
        val username = userEmail?.substringBefore('@') ?: ""
        return UserInfo(username, userEmail)
    }
}

data class UserInfo(val username: String, val userEmail: String?)