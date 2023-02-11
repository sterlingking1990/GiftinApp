package com.giftinapp.business.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.giftinapp.business.InfluencerActivity
import com.giftinapp.business.InfluencerSharersActivity
import com.giftinapp.business.R
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.util.*


class ShareNEarnFragment : Fragment() {

    lateinit var tvEmail:TextView
    lateinit var callbackManager: CallbackManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share_n_earn, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callbackManager = CallbackManager.Factory.create()


        val btnLoginFb = view.findViewById<MaterialButton>(R.id.btnLoginFb)
        tvEmail = view.findViewById(R.id.tvFBemail)

//        btnLoginFb.setReadPermissions(listOf("email","public_profile","user_gender","user_birthday"))
//        btnLoginFb.setFragment(this)

        // Callback registration
        btnLoginFb.setOnClickListener {
            LoginManager.getInstance().logIn(requireParentFragment(),
                listOf("email","public_profile","pages_show_list","pages_read_engagement","pages_manage_posts"))
        }

        LoginManager.getInstance().registerCallback(callbackManager, object:FacebookCallback<LoginResult>{
            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }

            override fun onSuccess(result: LoginResult) {
                startActivity( Intent(requireContext(),InfluencerSharersActivity::class.java))
                //findNavController().navigate(R.id.myReferralDealFragment)
            }

        })

    }

    private fun getFacebookData(obj: JSONObject) {
        val email = obj.getString("email")
        tvEmail.text = "Email is $email"
    }


}