package com.example.ittalk.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ittalk.R
import com.example.ittalk.databinding.ActivityLoginBinding
import com.example.ittalk.ui.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kira.healthcare.utils.SharedPrefrenceUtil

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var google: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(512, 512)
        FirebaseApp.initializeApp(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        google = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnLogin.setOnClickListener { view: View? ->
           // Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent: Intent = google.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.e("Daya", account?.id ?: "")
            Log.e("Daya", account?.displayName ?: "")
            Log.e("Daya", account?.familyName ?: "")
            Log.e("Daya", account?.email ?: "")
            Log.e("TAG", if (account?.photoUrl.toString() != "null") account?.photoUrl.toString() else "")
            val firstName = account?.displayName?.split(" ")?.get(0)
            var email  = account?.email
            var photoUrl  = account?.photoUrl
            var socialID  = account?.id
            Log.e("Daya", "$firstName  $email $photoUrl")
            SharedPrefrenceUtil.getInstance(this).socialId = socialID.toString()

            val names = account?.displayName?.split(" ")
            var secondName: String? = null
            if (names?.size!! > 1)
            {
                secondName = names[1]
            }
            if (!secondName.isNullOrEmpty())
            {
                SharedPrefrenceUtil.getInstance(this).first_name = firstName+" "+secondName.toString()
            }
            else
            {
                SharedPrefrenceUtil.getInstance(this).first_name = firstName.toString()

            }

            SharedPrefrenceUtil.getInstance(this).email = email.toString()
            SharedPrefrenceUtil.getInstance(this).profilePic = photoUrl.toString()
         //   Toast.makeText(this, "Login view Google", Toast.LENGTH_SHORT).show()
             val intent = Intent(this, HomeActivity::class.java)
             startActivity(intent)
             overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
             finishAffinity()

        } catch (e: ApiException)
        {
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
            //updateUI(null)

            Log.d("Daya", "handleSignInResult: ${e.statusCode}")
            Log.d("Daya", "handleSignInResult: ${e.message}")
            Log.d("Daya", "handleSignInResult: ${e.localizedMessage}")

            google.signOut()
        }
    }




}
