package com.giftinapp.business

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.splashScreenDisplay)
        setContentView(R.layout.activity_splash)

        val myintent = Intent(this, SignUpActivity::class.java)

        Handler().postDelayed(Runnable {
            startActivity(myintent)
            finish()
        }, 1000)
    }
}