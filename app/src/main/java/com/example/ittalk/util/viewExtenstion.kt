package com.example.ittalk.util

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ittalk.ui.login.LoginActivity

fun AppCompatActivity.showToast(msg:String)
{
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
fun AppCompatActivity.moveActivity(activity: LoginActivity)
{
   startActivity(Intent(this,activity::class.java))
}