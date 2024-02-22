package com.leshen.letseatmobile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.leshen.letseatmobile.databinding.FragmentProfileBinding

class Profile : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding?.root

        val myTextView: TextView = view?.findViewById(R.id.helloText) ?: return null

        val joinLetsEatButton: Button = view.findViewById(R.id.joinLetsEat) ?: return null

        // Set click listener for the button
        joinLetsEatButton.setOnClickListener {
            val url = "http://31.179.139.182:420"  // strona webowa let's eat
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        
        val userInfo = viewModel.getUserInfo(auth)
        myTextView.text = "Hej, ${userInfo.username}"

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}