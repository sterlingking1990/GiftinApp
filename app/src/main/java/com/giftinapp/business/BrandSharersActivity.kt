package com.giftinapp.business

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton

class BrandSharersActivity : AppCompatActivity() {
    lateinit var callbackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_sharers)

        callbackManager = CallbackManager.Factory.create()
        val btnLoginFb = findViewById<LoginButton>(R.id.btnLoginFb)

        btnLoginFb.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this,
                listOf("email","public_profile"))
        }

        LoginManager.getInstance().registerCallback(callbackManager, object:
            FacebookCallback<LoginResult> {
            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }

            override fun onSuccess(result: LoginResult) {
                    startActivity(
                        Intent(
                            this@BrandSharersActivity,
                            MerchantActivity::class.java
                        )
                    )
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}