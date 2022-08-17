package com.giftinapp.business.customer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.giftinapp.business.R
import com.google.firebase.auth.FirebaseAuth
import android.content.DialogInterface
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.giftinapp.business.model.DeliveryInfoPojo
import com.google.firebase.firestore.DocumentSnapshot
import android.text.Html
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.giftinapp.business.databinding.FragmentSettingsBinding
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseFragment
import com.google.android.gms.tasks.Task
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
    var selectedGiftinId: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etFacebook = view.findViewById(R.id.et_facebook)
        etInstagram = view.findViewById(R.id.et_instagram)
        etWhatsApp = view.findViewById(R.id.et_whatsapp)
        tvGiftingId = view.findViewById(R.id.tv_gifting_id)
        btnUpdateInfo = view.findViewById(R.id.btn_update_info)
        sessionManager = SessionManager(requireContext())
        builder = AlertDialog.Builder(requireContext())
        val giftinId =
            arrayOf("use my email", "use my facebook", "use my instagram", "use my whatsapp")
        val spGiftinIdAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, giftinId)
        spGiftinIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGiftinId = view.findViewById(R.id.sp_gifting_id)
        spGiftinId.adapter = spGiftinIdAdapter
        fetchInfoOnStart()
        btnUpdateInfo.setOnClickListener(View.OnClickListener { v: View? ->
            if (FirebaseAuth.getInstance().currentUser!!
                    .isEmailVerified
            ) {
                updateUserInfo(
                    etFacebook.getText().toString(),
                    etInstagram.getText().toString(),
                    etWhatsApp.getText().toString()
                )
            } else {
                showMessageDialog(title = "Unverified Account", message = "You need to verify your account before updating your info, please check your mail to verify your account",
                    disMissable = false, posBtnText = "OK", listener = {
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
        })
        tvGiftingId.setOnClickListener(View.OnClickListener { v: View? ->
            builder!!.setMessage("This is used by brands as an Id when rewarding you. Please choose options that brands can relate with easily")
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
                    "use my facebook" -> selectedGiftinId = etFacebook.getText().toString()
                    "use my email" -> selectedGiftinId = sessionManager!!.getEmail()
                    "use my instagram" -> selectedGiftinId = etInstagram.getText().toString()
                    "use my whatsapp" -> selectedGiftinId = etWhatsApp.getText().toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
        val facebookInput = if (facebook.isEmpty()) "not provided" else facebook
        val instagramInput = if (instagram.isEmpty()) "not provided" else instagram
        val whatsAppInput = if (whatsapp.isEmpty()) "not provided" else whatsapp
        val giftinId =
            if (selectedGiftinId!!.isEmpty()) sessionManager!!.getEmail() else selectedGiftinId
        if (validateDetails(facebookInput, instagramInput, whatsAppInput)) {
            val deliveryInfoPojo = DeliveryInfoPojo()
            deliveryInfoPojo.facebook = facebookInput
            deliveryInfoPojo.instagram = instagramInput
            deliveryInfoPojo.whatsapp = whatsAppInput
            db.collection("users").document(email!!).update(
                "facebook",
                facebookInput,
                "instagram",
                instagramInput,
                "whatsapp",
                whatsAppInput,
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
            showErrorCookieBar(title = "Invalid Entry", message = "one or more info provided is invalid, leave blank for detail you wish not to provide")
//            builder!!.setMessage("one or more info provided is invalid, leave blank for detail you wish not to provide")
//                .setCancelable(false)
//                .setPositiveButton("OK") { dialog: DialogInterface?, id: Int -> }
//            val alert = builder!!.create()
//            alert.show()
        }
    }

    private fun validateDetails(
        num1Input: String,
        num2Input: String,
        addressInput: String
    ): Boolean {
        return true
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
                            "Influencer Id" + "<b><p>" + "* " + documentSnapshot.getString("giftingId") + "</p></b> "
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