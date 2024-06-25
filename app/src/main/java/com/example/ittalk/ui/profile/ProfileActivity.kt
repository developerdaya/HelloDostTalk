package com.example.ittalk.ui.profile

import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.ittalk.R
import com.example.ittalk.databinding.AboutLayoutSheetBinding
import com.example.ittalk.databinding.ActivityProfileBinding
import com.example.ittalk.databinding.LogoutLayoutBinding
import com.example.ittalk.ui.home.HomeActivity
import com.example.ittalk.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.kira.healthcare.utils.SharedPrefrenceUtil

class ProfileActivity : AppCompatActivity() {
    lateinit var binding:ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var google: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(512,512)
        initControl()

        FirebaseApp.initializeApp(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        google = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
        Glide.with(this)
            .load(SharedPrefrenceUtil.getInstance(this).profilePic)
            .into(binding.ivProfile)
        binding.mUserName.text = SharedPrefrenceUtil.getInstance(this).first_name
        binding.mUserEmail.text = SharedPrefrenceUtil.getInstance(this).email
        setupOnDisconnect()

    }

    fun setupOnDisconnect()
    {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        usersRef.child(SharedPrefrenceUtil.getInstance(this).socialId).child("online").onDisconnect().setValue(false)

    }


    fun showDialog(context: Context)
    {
        val binding = LogoutLayoutBinding.inflate(LayoutInflater.from(context))
        val mBuilder = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
        mBuilder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBuilder.setCancelable(false)
        mBuilder.show()
        binding.btnNo.setOnClickListener {
            mBuilder.dismiss()
        }
         binding.btnYes.setOnClickListener {
            mBuilder.dismiss()
             google.signOut()
             val database = Firebase.database
             val usersRef = database.getReference("users")
             usersRef.child(SharedPrefrenceUtil.getInstance(this).socialId).removeValue()
             SharedPrefrenceUtil.getInstance(this).first_name = ""
             SharedPrefrenceUtil.getInstance(this).isOnline = true
             val intent = Intent(this, LoginActivity::class.java)
             startActivity(intent)
             overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
             finishAffinity()
        }


    }

    private fun openAboutUs() {
        val dialog = BottomSheetDialog(this)
        val binding = AboutLayoutSheetBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.show()

    }

    private fun initControl() {
        binding.shareWhatsapp.setOnClickListener {
            openWhatsApp()
        }

           binding.mShareApp.setOnClickListener {
                   val sharingIntent = Intent(Intent.ACTION_SEND)
                   sharingIntent.type = "text/plain"
                   val shareBody = "Please, Follow this link to Download the1 APP \n www.google.com" // your message here
                   sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
                   sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                   startActivity(Intent.createChooser(sharingIntent, "Share via"))
           }
             binding.mRateApp.setOnClickListener {
                 rateApp()
        }

     binding.mAboutUs.setOnClickListener {
         openAboutUs()
        }

  binding.mLogout.setOnClickListener {
     showDialog(this)
        }



    }

    private fun rateApp() {
        val packageName = this.packageName
        try {
            this.startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (e: android.content.ActivityNotFoundException) {
            // If Google Play Store app is not installed, open the app page in a browser
            this.startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    fun openWhatsApp()
    {
        val intent = Intent(ACTION_SEND)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        intent.putExtra(EXTRA_TEXT, "Join Flashask with this referral code JMP4X5")
        if (intent.resolveActivity(packageManager) == null)
        {
            Toast.makeText(this, "Please install WhatApp first.", Toast.LENGTH_SHORT).show()
            return
        }
        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        startActivity(intent)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        finishAffinity()
    }
}