package com.giftinapp.business.customer

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentBrandPreferenceBinding
import com.giftinapp.business.model.GiftingMerchantPojo
import com.giftinapp.business.model.GiftingMerchantViewPojo
import com.giftinapp.business.model.SendGiftPojo
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.net.URLEncoder
import java.util.*

open class BrandPreferenceFragment : BaseFragment<FragmentBrandPreferenceBinding>(), BrandPreferenceAdapter.ClickableIcon {
    private var brandPreferenceAdapter: BrandPreferenceAdapter? = null
    private var rvBrands: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    var sessionManager: SessionManager? = null

    var builder: AlertDialog.Builder? = null

    private var etSearchBrands: EditText? = null
    private var pgLoading:ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_brand_preference, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sessionManager?.setFollowingCount(0)

        etSearchBrands = view.findViewById(R.id.etSearchBrand)
        pgLoading = view.findViewById(R.id.pgLoading)

        brandPreferenceAdapter = BrandPreferenceAdapter(this)
        rvBrands = view.findViewById(R.id.rv_brands)
        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvBrands?.layoutManager = layoutManager
        sessionManager = SessionManager(requireContext())

        builder = AlertDialog.Builder(requireContext())

        loadBrands()

        etSearchBrands!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (etSearchBrands!!.length() < 1) {
                    loadBrands()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    brandPreferenceAdapter?.getFilter()?.filter("")
                } else {
                    brandPreferenceAdapter?.getFilter()?.filter(s)
                }
            }
        })
    }

    private fun loadBrands() {

        //getNumberOfFollowers()

        //Log.d("NumFollowers",sessionManager?.getFollowingCount().toString())
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        if (FirebaseAuth.getInstance().currentUser?.isEmailVerified  == true) {
            db.collection("merchants").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val giftingMerchantViewPojos = ArrayList<GiftingMerchantViewPojo>()
                        for (document in Objects.requireNonNull(task.result)) {

                            val giftingMerchantViewPojo = GiftingMerchantViewPojo()

                            val giftingMerchantPojo =
                                document.toObject(GiftingMerchantPojo::class.java)
                            if (giftingMerchantPojo.whatsapp == null || giftingMerchantPojo.instagram == null || giftingMerchantPojo.facebook == null || giftingMerchantPojo.address == null) {
                                giftingMerchantPojo.whatsapp = "not provided"
                                giftingMerchantPojo.address = "not provided"
                                giftingMerchantPojo.facebook = "not provided"
                                giftingMerchantPojo.instagram = "not provided"
                            }
                            giftingMerchantViewPojo.giftingMerchantPojo = giftingMerchantPojo
                            giftingMerchantViewPojo.numberOfCustomerGifted = 0
                            giftingMerchantViewPojo.merchantId = document.id
                            giftingMerchantViewPojo.giftingMerchantId =    if (document.getString("giftorId") != null) document.getString(
                                "giftorId"
                            ) else document.id
                            if (giftingMerchantViewPojo.merchantId != sessionManager?.getEmail()) {
                                pgLoading?.visibility = View.GONE
                                giftingMerchantViewPojos.add(giftingMerchantViewPojo)
                                brandPreferenceAdapter?.setGiftingMerchantList(
                                    giftingMerchantViewPojos
                                )
                                rvBrands?.adapter = brandPreferenceAdapter
                                brandPreferenceAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
        }else{
            builder!!.setMessage("You need to be a verified user in other to follow brands, please check your mail for verification")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog2: DialogInterface?, id2: Int ->
                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                    pgLoading?.visibility = View.GONE
                    //tvNoGift.setVisibility(View.VISIBLE)
                }
            val alert = builder!!.create()
            alert.show()
        }
    }

    override fun openMerchantFacebookDetail(facebookHandle: String) {
        try {
            if(facebookHandle.isNotEmpty() && facebookHandle!="not provided"){
            val url = "https://facebook.com/$facebookHandle"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        }catch (e: Exception) {
            showErrorCookieBar("No Facebook Installed", "Please Install Facebook to continue chat")
        }
    }

    override fun openMerchantInstagramDetail(instagramHandle: String) {
        try {
            if(instagramHandle.isNotEmpty() && instagramHandle!="not provided") {
                val url = "https://instagram.com/$instagramHandle"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        } catch (e: Exception) {
            showErrorCookieBar("No Instagram Installed","Please Install Instagram to continue chat")
        }

    }

    override fun openMerchantWhatsApp(whatsApp: String) {
        try {
            if(whatsApp.isNotEmpty() && whatsApp!="not provided") {
                val msg = "Hi, I am chatting you up from *Brandible*"
                val url = "https://api.whatsapp.com/send?phone=${
                    "+234$whatsApp" + "&text=" + URLEncoder.encode(
                        msg,
                        "UTF-8"
                    )
                }"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        } catch (e: Exception) {
            showErrorCookieBar("No WhatsApp Installed","Please Install WhatsApp to continue chat")
        }
    }

    override fun togglePreference(brandId: String, btnToggleBrandStatus: String) {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings
        if (FirebaseAuth.getInstance().currentUser?.isEmailVerified  == true) {
            if (btnToggleBrandStatus == "UNFOLLOW") {
                //influer will stop following, hence delete his account from the followers list
                db.collection("merchants").document(brandId).collection("followers")
                    .document(sessionManager?.getEmail().toString()).delete()
                sessionManager?.getFollowingCount()?.minus(1)
                    ?.let { sessionManager!!.setFollowingCount(it) }
            } else {
                sessionManager?.getFollowingCount()?.plus(1)
                    ?.let { sessionManager?.setFollowingCount(it) }
                //i will follow
                val sendGiftPojo = SendGiftPojo("empty")
                db.collection("merchants").document(brandId).collection("followers")
                    .document(sessionManager?.getEmail().toString()).set(sendGiftPojo)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {

                        }
                    }

            }

        }else{
            builder!!.setMessage("You need to be a verified user in other to follow brands, please check your mail for verification")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog2: DialogInterface?, id2: Int ->
                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                    pgLoading?.visibility = View.GONE
                    //tvNoGift.setVisibility(View.VISIBLE)
                }
            val alert = builder!!.create()
            alert.show()
        }
    }

    override fun onPause() {
        super.onPause()
        val mgr: View? = BrandPreferenceFragment().parentFragment?.view
        if (mgr != null) {
            val frag = FragmentManager.findFragment<Fragment>(mgr)
            val fm = fragmentManager
            fm!!.beginTransaction()
                .remove(frag)
                .commit()
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBrandPreferenceBinding = FragmentBrandPreferenceBinding.inflate(layoutInflater,container,false)

}