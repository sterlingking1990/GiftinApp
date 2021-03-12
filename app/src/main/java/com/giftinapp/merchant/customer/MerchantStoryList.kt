package com.giftinapp.merchant.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.merchant.R
import com.giftinapp.merchant.model.MerchantStoryListPojo
import com.giftinapp.merchant.model.MerchantStoryPojo
import com.giftinapp.merchant.utility.StorySession
import com.google.android.gms.common.ErrorDialogFragment.newInstance
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.io.Serializable
import java.lang.reflect.Array.newInstance
import javax.xml.validation.SchemaFactory.newInstance
import kotlin.concurrent.fixedRateTimer

class MerchantStoryList : Fragment(), MerchantStoryListAdapter.StoryClickable {
    lateinit var merchantStoryListAdapter:MerchantStoryListAdapter

    lateinit var merchantStoryListRecyclerView:RecyclerView

    lateinit var merchantRecyclerViewLayoutManager:LinearLayoutManager

    private lateinit var storySession: StorySession

    lateinit var pgLoading:ProgressBar



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_story_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        merchantStoryListRecyclerView = view.findViewById(R.id.rvMerchantStoryList)
        merchantRecyclerViewLayoutManager = LinearLayoutManager(requireContext())
        merchantRecyclerViewLayoutManager.orientation = LinearLayoutManager.VERTICAL

        merchantStoryListAdapter = MerchantStoryListAdapter(this)

        storySession = StorySession(requireContext())

        pgLoading = view.findViewById(R.id.pgLoadingStatus)

        loadRewardStoryList()

    }

    private fun loadRewardStoryList() {
        pgLoading.visibility = View.VISIBLE
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
                .addOnCompleteListener { task->
                    if(task.isSuccessful){
                        val story:MutableList<MerchantStoryPojo> = mutableListOf()
                        val result = task.result
                        if (result != null) {
                            for(eachRes in result){
                                db.collection("merchants").document(eachRes.id).collection("statuslist").get()
                                        .addOnCompleteListener { tast2->
                                            if(tast2.isSuccessful){
                                                val listOfStories:ArrayList<MerchantStoryListPojo> = ArrayList()
                                                for(eachList in tast2.result!!){
                                                    listOfStories.add(MerchantStoryListPojo(eachList.id, eachList.getString("gift_url"), null))
                                                }

                                                storySession.setStoryStatusList(listOfStories)

                                            }

                                        }

                                story.add(MerchantStoryPojo(eachRes.getString("giftorId").toString(), storySession.getStoryStatusList()))
                            }

                            if(story.size>0){
                                pgLoading.visibility = View.GONE
                                merchantStoryListAdapter.setMerchantStatus(story, requireContext())
                                merchantStoryListRecyclerView.layoutManager = merchantRecyclerViewLayoutManager
                                merchantStoryListRecyclerView.adapter=merchantStoryListAdapter
                            }
                        }
                    }
                }


    }

    override fun onStoryClicked(merchantStoryList: List<MerchantStoryListPojo>) {
        val fragment = CustomerRewardStories()
        val fm = parentFragmentManager
        val arguments= Bundle()
        arguments.putSerializable("storyList",merchantStoryList as Serializable)

        fragment.arguments = arguments

        CustomerRewardStories().arguments = arguments
            fm.beginTransaction()
                    .replace(R.id.fr_game, fragment)
                    .addToBackStack(null)
                    .commit()
    }


}