package com.leshen.letseatmobile

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    private lateinit var pb:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        Log.d("LitanyOfIgnition", "The soul of the Machine God surrounds thee.")
        Log.d("LitanyOfIgnition", "The power of the Machine God invests thee.")
        Log.d("LitanyOfIgnition", "The hate of the Machine God drives thee.")
        Log.d("LitanyOfIgnition", "The Machine God endows thee with life.")
        Log.d("LitanyOfIgnition", "Live!")
    }

    fun showProgressBar()
    {
        pb = Dialog(this)
        pb.setContentView(R.layout.progress_bar)
        pb.setCancelable(false)
        pb.show()
    }

    fun hideProgressBar()
    {
        pb.dismiss()
    }

    fun showToast(activity: Activity, msg:String)
    {
        Toast.makeText(activity,msg, Toast.LENGTH_SHORT).show()
    }

}