package com.giftinapp.business.customer

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.SignUpActivity
import com.giftinapp.business.business.SetRewardDeal
import com.giftinapp.business.databinding.FragmentMerchantStoryListBinding
import com.giftinapp.business.model.MerchantStoryListPojo
import com.giftinapp.business.model.MerchantStoryPojo
import com.giftinapp.business.model.StatusReachAndWorthPojo
import com.giftinapp.business.model.StoryHeaderPojo
import com.giftinapp.business.utility.*
import com.giftinapp.business.utility.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import java.io.Serializable

open class MerchantStoryList : BaseFragment<FragmentMerchantStoryListBinding>(), MerchantStoryListAdapter.StoryClickable {
    lateinit var merchantStoryListAdapter:MerchantStoryListAdapter

    lateinit var merchantStoryListRecyclerView:RecyclerView

    lateinit var merchantRecyclerViewLayoutManager:LinearLayoutManager

    private lateinit var storySession: StorySession

    private lateinit var sessionManager: SessionManager

    private lateinit var etSearchStoryId:EditText
    private lateinit var tvNoStory:TextView
    private lateinit var tvNoBrandFollowed:TextView
    private lateinit var tvNoBrandFollowedClicker:TextView

    lateinit var pgLoading:ProgressBar

    private var builder: AlertDialog.Builder? = null

    var isStoryHasHeader = false

    var countDoc = 0
    var followingCount=0
    var sizeOfDoc = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_story_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvNoStory = view.findViewById(R.id.tvNoStory)
        tvNoBrandFollowed = view.findViewById(R.id.tvNoBrandFollowed)
        tvNoBrandFollowedClicker = view.findViewById(R.id.tvNoBrandFollowedClicker)
        merchantStoryListRecyclerView = view.findViewById(R.id.rvMerchantStoryList)
        merchantRecyclerViewLayoutManager = LinearLayoutManager(requireContext())
        merchantRecyclerViewLayoutManager.orientation = LinearLayoutManager.VERTICAL
        merchantStoryListRecyclerView.layoutManager = merchantRecyclerViewLayoutManager
        merchantStoryListRecyclerView.setHasFixedSize(true)


        merchantStoryListAdapter = MerchantStoryListAdapter(this)
        merchantStoryListRecyclerView.adapter = merchantStoryListAdapter

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

        tvNoBrandFollowedClicker.setOnClickListener {
            try {
                findNavController().navigate(R.id.brandPreferenceFragment)
            }catch (e:Exception){
                findNavController().navigate(R.id.brandPreferenceFragment2)
            }
        }

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

        if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
            db.collection("merchants").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result: QuerySnapshot? = task.result
                        val merchantStoryPojos = ArrayList<MerchantStoryPojo>()
                        if (result != null) {
                            var noBrandFollowed = 0
                            sizeOfDoc = result.size()
                            for (eachRes in result) {
                                countDoc += 1
                                pgLoading.visibility = View.VISIBLE
                                db.collection("merchants").document(eachRes.id)
                                    .collection("followers").get()
                                    .addOnCompleteListener { followersTask ->
                                        if (followersTask.isSuccessful) {

                                            followersTask.result?.forEach { eachFollower ->
                                                if (eachFollower.id == sessionManager.getEmail()) {
                                                    noBrandFollowed+=1
                                                    db.collection("merchants").document(eachRes.id)
                                                        .collection("statuslist").get()
                                                        .addOnCompleteListener { task2 ->
                                                            if (task2.isSuccessful) {

                                                                //now we would get the document id and then the data for the document
                                                                try {
                                                                    val merchantStoryListPojos =
                                                                        ArrayList<MerchantStoryListPojo>()
                                                                    val merchantStoryHeaderPojos =
                                                                        ArrayList<StoryHeaderPojo>()
                                                                    for (eachList in task2.result!!) {
                                                                        val merchantStoryListPojo =
                                                                            MerchantStoryListPojo()
                                                                        merchantStoryListPojo.merchantStatusId =
                                                                            eachList.getString("merchantStatusId")
                                                                        merchantStoryListPojo.merchantOwnerId = eachList.getString("merchantOwnerId")
                                                                        merchantStoryListPojo.seen =
                                                                            eachList.getBoolean("seen")
                                                                        merchantStoryListPojo.storyTag =
                                                                            eachList.getString("storyTag")
                                                                        merchantStoryListPojo.storyAudioLink = eachList.getString("storyAudioLink")?:""
                                                                        merchantStoryListPojo.mediaDuration =
                                                                            eachList.getString("mediaDuration")
                                                                        merchantStoryListPojo.merchantStatusImageLink =
                                                                            eachList.getString("merchantStatusImageLink")?:""
                                                                        merchantStoryListPojo.merchantStatusVideoLink = eachList.getString("merchantStatusVideoLink")?:""
                                                                        merchantStoryListPojo.videoArtWork = eachList.getString("videoArtWork")?:""
                                                                        merchantStoryListPojo.statusReachAndWorthPojo =
                                                                            eachList.get("statusReachAndWorthPojo",StatusReachAndWorthPojo::class.java)
                                                                        //val merchantStoryListPojo = eachList.toObject(MerchantStoryListPojo::class.java)
//                                                                        merchantStoryListPojo.merchantStatusId =
//                                                                            eachList.id
                                                                        merchantStoryListPojos.add(
                                                                            merchantStoryListPojo
                                                                        )

                                                                    }

                                                                    if (merchantStoryListPojos.size > 0) {
                                                                        //this means business has more stories
                                                                        val merchantStoryPojo =
                                                                            MerchantStoryPojo()
                                                                        merchantStoryPojo.merchantId =
                                                                            if (eachRes.getString("giftorId") != null) eachRes.getString(
                                                                                "giftorId"
                                                                            ) else eachRes.id
                                                                        merchantStoryPojo.storyOwner =
                                                                            eachRes.id
                                                                        merchantStoryPojo.merchantStoryList =
                                                                            merchantStoryListPojos
                                                                        merchantStoryPojos.add(
                                                                            merchantStoryPojo
                                                                        )
                                                                    } else {
                                                                        if (eachRes.id == sessionManager.getEmail()) {
                                                                            showMessage()
                                                                            return@addOnCompleteListener
                                                                        }
                                                                    }

                                                                    if (merchantStoryPojos.size > 0) {

                                                                        if (eachRes.id == sessionManager.getEmail()) {
                                                                            isStoryHasHeader = true
                                                                        }
                                                                        pgLoading.visibility =
                                                                            View.GONE
                                                                        tvNoStory.gone()
                                                                        merchantStoryListAdapter.setMerchantStatus(
                                                                            merchantStoryPojos,
                                                                            requireContext(),
                                                                            isStoryHasHeader,
                                                                            followingCount
                                                                        )
                                                                        merchantStoryListRecyclerView.adapter =
                                                                            merchantStoryListAdapter
                                                                    }else{
                                                                            tvNoStory.visible()
                                                                            pgLoading.gone()
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Log.d(
                                                                        "NOSTATUS",
                                                                        "Can't find record for no status"
                                                                    )
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
        } else {
            showMessageDialog(title = "Not Verified", message = "You need to be a verified user in other to view brand stories, promotions and deals so at to get rewards",
            disMissable = false, posBtnText = "Ok", listener = {
                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                    pgLoading.visibility = View.GONE
                })
//            builder!!.setMessage("")
//                .setCancelable(false)
//                .setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
//                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
//                    pgLoading.visibility = View.GONE
//                }
//            val alert = builder!!.create()
//            alert.show()
        }
    }

    private fun checkFollowingRate(){
        if(sessionManager.getFollowingCount()==0 && sessionManager.getUserMode()=="customer"){
            pgLoading.visibility = View.GONE
            showMessageDialog(title = "Follow Brands", message = "You are not following any brands yet,. You will be directed to list of Brands to follow",
                hasNegativeBtn = false, posBtnText = "OK", disMissable = false, listener = {
                    findNavController().navigate(R.id.brandPreferenceFragment)
                    //openFragmentForInfluencer(BrandPreferenceFragment())
                }
            )
//                    builder!!.setMessage("You are not following any brands yet,. You will be directed to list of Brands to follow")
//                            .setCancelable(false)
//                            .setPositiveButton("OK") { dia: DialogInterface?, _: Int ->
//                                // take user to rewarding merchants
//                                openFragmentForInfluencer(BrandPreferenceFragment())
//                            }
//                    val alert = builder!!.create()
//                    alert.show()
                }
                else if(sessionManager.getFollowingCount()==0 && sessionManager.getUserMode()=="business"){
                    //this person is a brand and needs to follow brands to view status
                    pgLoading.visibility = View.GONE
            showMessageDialog(title = "Follow Brands", message = "You are not following any brands yet,. You will be directed to list of Brands to follow",
                hasNegativeBtn = false, posBtnText = "OK", disMissable = false, listener = {
                    findNavController().navigate(R.id.brandPreferenceFragment)
                    //openFragment(BrandPreferenceFragment())
                }
            )
//                    builder!!.setMessage("")
//                            .setCancelable(false)
//                            .setPositiveButton("OK") { d: DialogInterface?, _: Int ->
//                                // take user to rewarding merchants
//                                openFragment(BrandPreferenceFragment())
//                            }
//                    val alert = builder!!.create()
//                    alert.show()
                }
    }

    override fun onStoryClicked(merchantStoryList: ArrayList<MerchantStoryListPojo>, allList: ArrayList<MerchantStoryPojo>, currentStoryPos: Int, storyOwner: String) {

        Log.d("position",currentStoryPos.toString())
           // val fragment = CustomerRewardStories()
            //val fm = fragmentManager
            val arguments = Bundle()

            //var fragmentType = R.id.fr_game
            arguments.putSerializable("storyList", merchantStoryList as Serializable)
            arguments.putSerializable("allStory", allList as Serializable)
            arguments.putInt("currentStoryPos", currentStoryPos)
            arguments.putString("storyOwner", storyOwner)
            arguments.putBoolean("hasHeader", isStoryHasHeader)

        try {
            findNavController().navigate(R.id.customerRewardStories,arguments)
        }catch (e:Exception){
            findNavController().navigate(R.id.customerRewardStories2, arguments)
        }
//            if (isStoryHasHeader || storyOwner == sessionManager.getEmail()) {
//                //fragmentType = R.id.fr_layout_merchant
//                Log.d("Amhere", "Am here before")
//                findNavController().navigate(R.id.customerRewardStories2,arguments)
//            }else{
//                findNavController().navigate(R.id.customerRewardStories, arguments)
//            }

    }

    override fun onReviewClicked(
        merchantStoryList: ArrayList<MerchantStoryListPojo>,
        storyOwner: String
    ) {

        showBottomSheet(ReviewFragment.newInstance(storyOwner))
        //Toast.makeText(requireContext(),"I am going to take user to reviews and ability to leave a review sticker",Toast.LENGTH_LONG).show()
    }


    private fun showMessage() {

            showMessageDialog(title = "Stand Out as Brand", message = "Be the first to publish a story. Click OK publish some stories and get Brandible Influencers engaged with your brand?",
                hasNegativeBtn = false, posBtnText = "OK",listener = {
                    findNavController().navigate(R.id.setRewardDeal)
                    //openFragment(SetRewardDeal())
                                                                                           }
                ,disMissable = false
            )
//            builder!!.setMessage()
//                    .setCancelable(false)
//                    .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
//                        openFragment(SetRewardDeal())
//                        dialogInterface.dismiss()
//                    }
//                    .setNegativeButton("Later") { _: DialogInterface, _ ->
//                    }
//            val alert = builder!!.create()
//            alert.show()

    }


//    fun openFragment(fragment: Fragment?) {
//        val fm = fragmentManager
//        fm!!.beginTransaction()
//                .replace(R.id.fr_layout_merchant, fragment!!)
//                .addToBackStack(null)
//                .commit()
//    }

    //private fun openFragmentForInfluencer(fragment: Fragment?){
//        val fm = fragmentManager
//        fm!!.beginTransaction()
//                .replace(R.id.fr_game, fragment!!)
//                .addToBackStack(null)
//                .commit()
    //}

//    override fun onPause() {
//        super.onPause()
//        Log.d("OnPauseCalled","OnPauseCalled")
//        try {
//            if(findNavController().currentDestination?.id!=R.id.customerRewardStories) {
//                activity?.finish()
//            }
//            if(findNavController().currentDestination?.id!=R.id.customerRewardStories2){
//                activity?.finish()
//            }
//        }catch (e:Exception){
//            Log.d("MerchantStoryListPause",e.message.toString())
//        }
//    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMerchantStoryListBinding = FragmentMerchantStoryListBinding.inflate(layoutInflater,container,false)

}