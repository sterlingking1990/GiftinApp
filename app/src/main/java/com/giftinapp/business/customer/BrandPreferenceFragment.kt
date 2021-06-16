package com.giftinapp.business.customer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.GiftingMerchantPojo
import com.giftinapp.business.model.GiftingMerchantViewPojo
import com.giftinapp.business.model.MerchantStoryPojo
import com.giftinapp.business.model.SendGiftPojo
import com.giftinapp.business.utility.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class BrandPreferenceFragment : Fragment(), BrandPreferenceAdapter.ClickableIcon {
    private var brandPreferenceAdapter: BrandPreferenceAdapter? = null
    private var rvBrands: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    var sessionManager: SessionManager? = null

    var builder: AlertDialog.Builder? = null

    private var etSearchBrands: EditText? = null

    var counter=0
    var numberOfFollowers=0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_brand_preference, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sessionManager?.setFollowingCount(0)

        etSearchBrands = view.findViewById(R.id.etSearchBrand)

        brandPreferenceAdapter = BrandPreferenceAdapter(this)
        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        sessionManager = SessionManager(requireContext())

        rvBrands = view.findViewById(R.id.rv_brands)

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

    private fun getNumberOfFollowers() {
        val db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        db.collection("merchants").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result: QuerySnapshot? = task.result
                        if (result != null) {
                            val listOfNumFollows= mutableListOf<Int>()

                            for (eachRes in result) {
                                counter+=1
                                db.collection("merchants").document(eachRes.id).collection("followers").get()
                                        .addOnCompleteListener { followersTask ->
                                            if (followersTask.isSuccessful) {
                                                followersTask.result?.forEach { eachFollower ->
                                                    if (eachFollower.id == sessionManager?.getEmail()) {
                                                        numberOfFollowers+=1
                                                        listOfNumFollows.add(numberOfFollowers)
                                                    }
                                                }
                                                Log.d("counter",counter.toString())
                                                Log.d("resultSize",result.documents.size.toString())

                                                if(counter==result.documents.size) {
                                                    Log.d("NumFollow", numberOfFollowers.toString())
                                                    sessionManager?.setFollowingCount(numberOfFollowers)
                                                }
                                            }
                                        }

                            }


                        }
                    }
                }
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

        db.collection("merchants").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val giftingMerchantViewPojos = ArrayList<GiftingMerchantViewPojo>()
                        for (document in Objects.requireNonNull(task.result)!!) {

                            val giftingMerchantViewPojo = GiftingMerchantViewPojo()

                                val giftingMerchantPojo = document.toObject(GiftingMerchantPojo::class.java)
                                if (giftingMerchantPojo.whatsapp == null || giftingMerchantPojo.instagram == null || giftingMerchantPojo.facebook == null || giftingMerchantPojo.address == null) {
                                    giftingMerchantPojo.whatsapp = "not provided"
                                    giftingMerchantPojo.address = "not provided"
                                    giftingMerchantPojo.facebook = "not provided"
                                    giftingMerchantPojo.instagram = "not provided"
                                }
                                giftingMerchantViewPojo.giftingMerchantPojo = giftingMerchantPojo
                                giftingMerchantViewPojo.numberOfCustomerGifted = 0
                                giftingMerchantViewPojo.giftingMerchantId = document.id
                            if (giftingMerchantViewPojo.giftingMerchantId != sessionManager?.getEmail()) {
                                giftingMerchantViewPojos.add(giftingMerchantViewPojo)
                                brandPreferenceAdapter?.setGiftingMerchantList(giftingMerchantViewPojos)
                                rvBrands?.layoutManager = layoutManager
                                rvBrands?.adapter = brandPreferenceAdapter
                                brandPreferenceAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
    }

    override fun openMerchantFacebookDetail(facebookHandle: String) {

    }

    override fun openMerchantInstagramDetail(instagramHandle: String) {

    }

    override fun openMerchantWhatsApp(whatsApp: String) {

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

        if(btnToggleBrandStatus == "UNFOLLOW"){
            //influer will stop following, hence delete his account from the followers list
            db.collection("merchants").document(brandId).collection("followers").document(sessionManager?.getEmail().toString()).delete()
            sessionManager?.getFollowingCount()?.minus(1)?.let { sessionManager!!.setFollowingCount(it) }
            loadBrands()
        }
        else{
            sessionManager?.getFollowingCount()?.plus(1)?.let { sessionManager?.setFollowingCount(it) }
            //i will follow
                val sendGiftPojo = SendGiftPojo("empty")
                db.collection("merchants").document(brandId).collection("followers").document(sessionManager?.getEmail().toString()).set(sendGiftPojo)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                loadBrands()
                            }
                        }

        }


    }

}