package com.giftinapp.business.customer

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentSettingsBinding
import com.giftinapp.business.model.DeliveryInfoPojo
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import org.aviran.cookiebar2.CookieBar

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    private lateinit var etFacebook: EditText
    private lateinit var etInstagram: EditText
    private lateinit var etWhatsApp: EditText
    private lateinit var tvGiftingId: TextView
    private lateinit var btnUpdateInfo: Button
    var sessionManager: SessionManager? = null
    var builder: AlertDialog.Builder? = null
    private lateinit var spGiftinId: Spinner
    private lateinit var chkIsSubscribed: CheckBox
    var selectedGiftinId: String? = ""
    var isChallenge:Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val animation = android.view.animation.AnimationUtils.loadAnimation(requireContext(),R.anim.bounce);
        etFacebook = view.findViewById(R.id.et_facebook)
        etInstagram = view.findViewById(R.id.et_instagram)
        etWhatsApp = view.findViewById(R.id.et_whatsapp)
        tvGiftingId = view.findViewById(R.id.tv_gifting_id)
        btnUpdateInfo = view.findViewById(R.id.btn_update_info)
        chkIsSubscribed = view.findViewById(R.id.chkISubscribed)
        sessionManager = SessionManager(requireContext())
        builder = AlertDialog.Builder(requireContext())
        val giftinId =
            arrayOf("use email", "use brandible id")
        val spGiftinIdAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, giftinId)
        spGiftinIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGiftinId = view.findViewById(R.id.sp_gifting_id)
        spGiftinId.adapter = spGiftinIdAdapter
        fetchInfoOnStart()
        btnUpdateInfo.setOnClickListener { v: View? ->
            v?.startAnimation(animation)
            if (FirebaseAuth.getInstance().currentUser!!
                    .isEmailVerified
            ) {
                updateUserInfo(
                    etFacebook.text.toString(),
                    etInstagram.text.toString(),
                    etWhatsApp.text.toString()
                )
            } else {
                showMessageDialog(title = "Unverified Account",
                    message = "You need to verify your account before updating your info, please check your mail to verify your account",
                    disMissable = false,
                    posBtnText = "OK",
                    listener = {
                        FirebaseAuth.getInstance().currentUser!!
                            .sendEmailVerification()
                    }
                )
//                builder!!.setMessage("You need to verify your account before updating your info, please check your mail to verify your account")
//                    .setCancelable(false)
//                    .setPositiveButton("OK") { dialog: DialogInterface?, id: Int ->
//                        FirebaseAuth.getInstance().currentUser!!
//                            .sendEmailVerification()
//                    }
//                val alert = builder!!.create()
//                alert.show()
            }
        }
        tvGiftingId.setOnClickListener(View.OnClickListener { v: View? ->
            builder!!.setMessage("This will be used as your Id for every activity across the app. Please choose option that brands can relate with easily")
                .setCancelable(true)
                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int -> }
            val alert = builder!!.create()
            alert.show()
        })
        spGiftinId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                when (spGiftinIdAdapter.getItem(position)) {
                    "use brandible id" -> selectedGiftinId = etFacebook.getText().toString()
                    "use email" -> selectedGiftinId = sessionManager!!.getEmail()
                   // "use my instagram" -> selectedGiftinId = etInstagram.getText().toString()
                   // "use my whatsapp" -> selectedGiftinId = etWhatsApp.getText().toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        checkIfSubscribedToTopic()
        chkIsSubscribed.setOnCheckedChangeListener{ _, isChecked ->
            subscribeOrUnsubscribe(isChecked)
        }
    }

    private fun checkIfSubscribedToTopic(){
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("users").document(sessionManager?.getEmail().toString()).get().addOnCompleteListener {
            if(it.isSuccessful){
                val users = it.result
                val isSubscribed = users.getBoolean("isSubscribedToInfluencerTopic")
                if(isSubscribed == true){
                    chkIsSubscribed.isChecked = true
                    chkIsSubscribed.text = "You Subscribed to Influencer Updates"
                }
            }
        }
    }

    private fun subscribeOrUnsubscribe(isChecked:Boolean){
        if(isChecked){
            Firebase.messaging.subscribeToTopic("Influencer")
                .addOnCompleteListener { task ->
                    var msg = "You will receive updates about Influencers"
                    if (!task.isSuccessful) {
                        msg = "Subscribe failed"
                    }
                    Log.d("InfluencerSub", msg)
                    //Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    chkIsSubscribed.text = "You subscribed to Influencer updates"
                    updateFBWithSubscriptionDetails(true)
                }
        }else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic("Influencer")
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        //Toast.makeText(requireContext(),"You have successfully unsubscribed",Toast.LENGTH_LONG).show()
                        chkIsSubscribed.text = "You opted out of Influencer updates"
                        updateFBWithSubscriptionDetails(false)
                    }
                }
        }
    }

    private fun updateFBWithSubscriptionDetails(isSubscribed:Boolean){
        val textUpdate = if (isSubscribed) "Subscribed to" else "Opted out of"
        val msg = "You have successfully $textUpdate Influencer updates"
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("users").document(sessionManager?.getEmail().toString()).update("isSubscribedToInfluencerTopic",isSubscribed)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d("FBUpdate","True")
                    //Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateUserInfo(facebook: String, instagram: String, whatsapp: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        val email = sessionManager!!.getEmail()
//        val facebookInput = facebook.ifEmpty { "not provided" }
//        val instagramInput = instagram.ifEmpty { "not provided" }
//        val whatsAppInput = whatsapp.ifEmpty { "not provided" }
        val giftinId =
            if (selectedGiftinId!!.isEmpty()) sessionManager!!.getEmail() else selectedGiftinId

        //check if giftorIdInput already exists and is more than one- then tell the user they cant update it o
        db.collection("users").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val allEmailResults = it.result
                    var counter = 0
                    for (i in allEmailResults){
                        val giftorId = i.get("giftingId")
                        if(giftorId == facebook && i.id != sessionManager!!.getEmail()){
                            Log.d("GiftorId",giftorId.toString())
                            Log.d("GiftingId",giftinId.toString())
                            counter+=1
                        }
                    }
                    if(counter>=1){
                        showErrorCookieBar(
                            title = "Id already exists",
                            message = "Please select another Id as this Already exists by another user"
                        )
                    }else{
                        if (validateDetails(facebook, instagram, whatsapp)) {
                            val deliveryInfoPojo = DeliveryInfoPojo()
                            deliveryInfoPojo.facebook = facebook
                            deliveryInfoPojo.instagram = instagram
                            deliveryInfoPojo.whatsapp = whatsapp
                            db.collection("users").document(email!!).update(
                                "facebook",
                                facebook,
                                "instagram",
                                instagram,
                                "whatsapp",
                                whatsapp,
                                "giftingId",
                                giftinId
                            )
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showCookieBar(title = "Info Update Successful", message = "Your details have been updated successfully", position = CookieBar.BOTTOM)
                                    }
                                }
                            fetchInfoOnStart()
                        } else {
                            showErrorCookieBar(
                                title = "Id already exists",
                                message = "Please select another Id as this Already exists by another user"
                            )
//            builder!!.setMessage("one or more info provided is invalid, leave blank for detail you wish not to provide")
//                .setCancelable(false)
//                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int -> }
//            val alert = builder!!.create()
//            alert.show()
                        }
                    }
                }
            }

    }

    private fun validateDetails(
        brandibleId: String,
        num2Input: String,
        addressInput: String
    ): Boolean {
        return brandibleId.isNotEmpty()
    }

    private fun fetchInfoOnStart() {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db.collection("users").document(sessionManager!!.getEmail()!!).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot.exists()) {
                        etFacebook.setText(documentSnapshot["facebook"].toString())
                        etInstagram.setText(documentSnapshot["instagram"].toString())
                        etWhatsApp.setText(documentSnapshot["whatsapp"].toString())
                        val giftinIdText =
                            "Your Id" + "<b><p>" + "* " + documentSnapshot.getString("giftingId") + "</p></b> "
                        tvGiftingId.text = Html.fromHtml(giftinIdText)
                    }
                }
            }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding = FragmentSettingsBinding.inflate(layoutInflater,container,false)

}