package com.leshen.letseatmobile

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.leshen.letseatmobile.login.GetStartedActivity

class MainViewModel : ViewModel() {

    fun signOut(context: Context, auth: FirebaseAuth) {
        if (auth.currentUser != null) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, GetStartedActivity::class.java)
            context.startActivity(intent)
        }
    }
}