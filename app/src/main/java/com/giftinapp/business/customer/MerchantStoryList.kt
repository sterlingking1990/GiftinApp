package com.giftinapp.business.customer

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.business.SetRewardDeal
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.MerchantStoryPojo
import com.giftinapp.business.model.StoryHeaderPojo
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.StorySession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import java.io.Serializable

class MerchantStoryList : Fragment(), MerchantStoryListAdapter.StoryClickable {
    lateinit var merchantStoryListAdapter:MerchantStoryListAdapter

    lateinit var merchantStoryListRecyclerView:RecyclerView

    lateinit var merchantRecyclerViewLayoutManager:LinearLayoutManager

    private lateinit var storySession: StorySession

    private lateinit var sessionManager: SessionManager

    private lateinit var etSearchStoryId:EditText

    lateinit var pgLoading:ProgressBar

    private var builder: AlertDialog.Builder? = null

    var isStoryHasHeader = false

    var countDoc = 0
    var followingCount=0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_story_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        merchantStoryListRecyclerView = view.findViewById(R.id.rvMerchantStoryList)
        merchantRecyclerViewLayoutManager = LinearLayoutManager(requireContext())
        merchantRecyclerViewLayoutManager.orientation = LinearLayoutManager.VERTICAL
        merchantStoryListRecyclerView.layoutManager = merchantRecyclerViewLayoutManager
        merchantStoryListRecyclerView.setHasFixedSize(true)


        merchantStoryListAdapter = MerchantStoryListAdapter(this)

        storySession = StorySession(requireContext())

        sessionManager = SessionManager(requireContext())

        pgLoading = view.findViewById(R.id.pgLoadingStatus)

        builder = AlertDialog.Builder(requireContext())

        etSearchStoryId = view.findViewById(R.id.etSearchStoryId)

        sessionManager.setCurrentFragment("MerchantStoryList")

        etSearchStoryId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (etSearchStoryId.length() < 1) {
                    loadRewardStoryList()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    merchantStoryListAdapter.filter.filter("")
                } else {
                    merchantStoryListAdapter.filter.filter(s)
                }
            }
        })

        checkFollowingRate()

        //getNumberOfFollowers()

        loadRewardStoryList()

    }


    private fun loadRewardStoryList() {

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
                                val merchantStoryPojos = ArrayList<MerchantStoryPojo>()

                                for (eachRes in result) {
                                    countDoc+=1
                                    pgLoading.visibility = View.VISIBLE
                                    db.collection("merchants").document(eachRes.id).collection("followers").get()
                                            .addOnCompleteListener { followersTask->
                                                if(followersTask.isSuccessful){
                                                    followersTask.result?.forEach { eachFollower->
                                                        if(eachFollower.id == sessionManager.getEmail()){
                                                            db.collection("merchants").document(eachRes.id).collection("statuslist").get()
                                                                    .addOnCompleteListener { task2 ->
                                                                        if (task2.isSuccessful) {

                                                                                //now we would get the document id and then the data for the document
                                                                                try {
                                                                                    val merchantStoryListPojos = ArrayList<MerchantStoryListPojo>()
                                                                                    val merchantStoryHeaderPojos = ArrayList<StoryHeaderPojo>()
                                                                                    for (eachList in task2.result!!) {
                                                                                        val merchantStoryListPojo = MerchantStoryListPojo()
                                                                                        merchantStoryListPojo.merchantStatusId = eachList.getString("merchantStatusId")
                                                                                        merchantStoryListPojo.seen = eachList.getBoolean("seen")
                                                                                        merchantStoryListPojo.storyTag = eachList.getString("storyTag")
                                                                                        merchantStoryListPojo.merchantStatusImageLink = eachList.getString("merchantStatusImageLink")
                                                                                        //val merchantStoryListPojo = eachList.toObject(MerchantStoryListPojo::class.java)
                                                                                        merchantStoryListPojo.merchantStatusId = eachList.id
                                                                                        merchantStoryListPojos.add(merchantStoryListPojo)

                                                                                    }

                                                                                    if (merchantStoryListPojos.size > 0) {
                                                                                        //this means business has more stories
                                                                                        val merchantStoryPojo = MerchantStoryPojo()
                                                                                        merchantStoryPojo.merchantId = if (eachRes.getString("giftorId") != null) eachRes.getString("giftorId") else eachRes.id
                                                                                        merchantStoryPojo.storyOwner = eachRes.id
                                                                                        merchantStoryPojo.merchantStoryList = merchantStoryListPojos
                                                                                        merchantStoryPojos.add(merchantStoryPojo)
                                                                                    } else {
                                                                                        if (eachRes.id == sessionManager.getEmail()) {
                                                                                            showMessage(true)
                                                                                            return@addOnCompleteListener
                                                                                        }
                                                                                    }

                                                                                    if (merchantStoryPojos.size > 0) {

                                                                                        if (eachRes.id == sessionManager.getEmail()) {

                                                                                            isStoryHasHeader = true
                                                                                        }
                                                                                        pgLoading.visibility = View.GONE
                                                                                        merchantStoryListAdapter.setMerchantStatus(merchantStoryPojos, requireContext(), isStoryHasHeader, followingCount)
                                                                                        merchantStoryListRecyclerView.adapter = merchantStoryListAdapter
                                                                                    }
                                                                                } catch (e: Exception) {
                                                                                    Log.d("NO STATUS", "Can't find record for no status")
                                                                                }
                                                                        }

                                                                    }
                                                        }
                                                    }

                                                }
                                            }
                                    }



                                }

                            }

                        }
                    }

    private fun checkFollowingRate(){
        Log.d("NumNum",sessionManager.getFollowingCount().toString())
        if(sessionManager.getFollowingCount()==0 && sessionManager.getUserMode()=="customer"){
            pgLoading.visibility = View.GONE
                    builder!!.setMessage("You are not following any brands yet,. You will be directed to list of Brands to follow")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dia: DialogInterface?, _: Int ->
                                // take user to rewarding merchants
                                openFragmentForInfluencer(BrandPreferenceFragment())
                            }
                    val alert = builder!!.create()
                    alert.show()
                }
                else if(sessionManager.getFollowingCount()==0 && sessionManager.getUserMode()=="business"){
                    //this person is a brand and needs to follow brands to view status
                    pgLoading.visibility = View.GONE
                    builder!!.setMessage("You are not following any brands yet,. You will be directed to list of Brands to follow")
                            .setCancelable(false)
                            .setPositiveButton("OK") { d: DialogInterface?, _: Int ->
                                // take user to rewarding merchants
                                openFragment(BrandPreferenceFragment())
                            }
                    val alert = builder!!.create()
                    alert.show()
                }
    }

    override fun onStoryClicked(merchantStoryList: ArrayList<MerchantStoryListPojo>, allList: ArrayList<MerchantStoryPojo>, currentStoryPos: Int, storyOwner: String) {
        Log.d("StoryOwner",storyOwner)

            val fragment = CustomerRewardStories()
            val fm = fragmentManager
            val arguments = Bundle()

            var fragmentType = R.id.fr_game
            arguments.putSerializable("storyList", merchantStoryList as Serializable)
            arguments.putSerializable("allStory", allList as Serializable)
            arguments.putInt("currentStoryPos", currentStoryPos)
            arguments.putString("storyOwner", storyOwner)
            arguments.putBoolean("hasHeader", isStoryHasHeader)
            if (isStoryHasHeader) {
                fragmentType = R.id.fr_layout_merchant
            }
        if(storyOwner != sessionManager.getEmail()) {
            fragmentType = R.id.fr_layout_merchant
        }
            fragment.arguments = arguments

            CustomerRewardStories().arguments = arguments
            fm?.beginTransaction()
                    ?.replace(fragmentType, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
    }


    fun showMessage(isDisplay: Boolean) {

        if(isDisplay) {
            builder!!.setMessage("When you publish status as a brand, it will be displayed here. Do you want to Publish your reward status now so you can begin engaging customers for more buy?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                        openFragment(SetRewardDeal())
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Later") { _: DialogInterface, _ ->
                    }
            val alert = builder!!.create()
            alert.show()
        }

    }


    fun openFragment(fragment: Fragment?) {
        val fm = fragmentManager
        fm!!.beginTransaction()
                .replace(R.id.fr_layout_merchant, fragment!!)
                .addToBackStack(null)
                .commit()
    }

    fun openFragmentForInfluencer(fragment: Fragment?){
        val fm = fragmentManager
        fm!!.beginTransaction()
                .replace(R.id.fr_game, fragment!!)
                .addToBackStack(null)
                .commit()
    }

}