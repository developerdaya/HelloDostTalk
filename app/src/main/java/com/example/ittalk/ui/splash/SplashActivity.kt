package com.example.ittalk.ui.splash
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.ittalk.R
import com.example.ittalk.ui.home.HomeActivity
import com.example.ittalk.ui.login.LoginActivity
import com.kira.healthcare.utils.SharedPrefrenceUtil

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(512,512)
        Handler().postDelayed({
            if (SharedPrefrenceUtil.getInstance(this).first_name!="")
            {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
            }
            else{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
            }


        }, 1500)

    }
}