package com.giftinapp.business

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.material.button.MaterialButton


class InfluencerSharersActivity : AppCompatActivity() {
    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_influencer_sharers)

//        val accessToken = AccessToken.getCurrentAccessToken()
//
//        Log.d("AccessToken",accessToken?.token.toString())
        callbackManager = CallbackManager.Factory.create()
        val btnLoginFb = findViewById<LoginButton>(R.id.btnLoginFb)

//        val accessToken = AccessToken.getCurrentAccessToken()
//        val isLoggedIn = accessToken != null && !accessToken.isExpired
//        if(isLoggedIn){
//            startActivity(Intent(this,InfluencerActivity::class.java))
//        }
        btnLoginFb.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this,
                listOf("email","public_profile","user_posts"))

//            LoginManager.getInstance().logInWithReadPermissions(this,
//                listOf("email","public_profile"))
        }

//        "pages_show_list","pages_read_engagement","pages_manage_posts"
        LoginManager.getInstance().registerCallback(callbackManager, object:
            FacebookCallback<LoginResult> {
            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }

            override fun onSuccess(result: LoginResult) {
                try {
                    startActivity(
                        Intent(
                            this@InfluencerSharersActivity,
                            InfluencerActivity::class.java
                        )
                    )
                }catch (e:Exception){
                    startActivity(
                        Intent(
                            this@InfluencerSharersActivity,
                            MerchantActivity::class.java
                        )
                    )
                }
                //findNavController().navigate(R.id.myReferralDealFragment)
            }

        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}