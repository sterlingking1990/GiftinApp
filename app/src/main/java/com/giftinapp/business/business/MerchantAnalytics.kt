package com.giftinapp.business.business

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentMerchantAnalyticsBinding
import com.giftinapp.business.model.gptcontent.GptPrompt
import com.giftinapp.business.network.viewmodel.GetGptContentViewModel
import com.giftinapp.business.utility.visible
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MerchantAnalytics : Fragment() {
    private lateinit var binding:FragmentMerchantAnalyticsBinding

    private val getGptContentViewModel: GetGptContentViewModel by viewModels()
    var challengeId:String = ""
    private var numberOfSharers:Int? = null
    private var numberOfRewardees:Int? = null
    private var numberOfReach:Int = 0

    private var participationRate =0
    private var rewardRate = 0
    var conversionRate = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentMerchantAnalyticsBinding.inflate(layoutInflater,container,false)

        arguments?.let {
            challengeId = it.getString("challengeId").toString()
            numberOfReach = it.getInt("numberOfReach")

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getTotalParticipants()
        getTotalInfluencersRewarded()
        //computeStats()

    }

    private fun getTotalParticipants(){
        binding.pgLoadingNote.visible()
        binding.tvSummaryText.text = "loading summary of result..."
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        //get the total number of participants

        db.collection("sharedpostsrecord").document(challengeId).collection("sharers").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(it.result.documents.size>0) {
                        numberOfSharers = it.result.documents.size
                        computeStats()
                    }else{
                        binding.pgLoadingNote.isInvisible=true
                        Toast.makeText(requireContext(),"Analytics for this sharable is not ready yet, please check back",Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun getTotalInfluencersRewarded(){
        binding.pgLoadingNote.visible()
        binding.tvSummaryText.text = "loading summary of result..."
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("sharedposts").document(challengeId).collection("rewardees").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(it.result.documents.size>0) {
                        numberOfRewardees = it.result.documents.size
                        computeStats()
                    }else{
                        binding.pgLoadingNote.isInvisible=true
                        Toast.makeText(requireContext(),"Analytics for this sharable is not ready yet, please check back",Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun computeStats(){
        if(numberOfSharers!=null && numberOfRewardees!=null){
            participationRate = (numberOfSharers!!.toDouble().div(numberOfReach) * 100).toInt()
            rewardRate = (numberOfRewardees!!.toDouble().div(numberOfSharers!!) * 100).toInt()
            conversionRate = (numberOfRewardees!!.toDouble().div(numberOfReach) * 100).toInt()


            binding.tvParticipationRate.text = "$participationRate%"
            binding.tvRewardRate.text = "$rewardRate%"
            binding.tvConversionRate.text = "$conversionRate%"

            val promptText = "I used Brandible app as a Brand, I targeted $numberOfReach Influencers using the app and I asked them to share my brand content across their facebook post and story. $numberOfSharers influencers participated and $numberOfRewardees influencers among the participants got rewarded some BrC's each because they had met the criteria for rewarding. The Paticipation Rate I got from computation is ${participationRate}, The Rewarding rate is ${rewardRate} and the Conversion rate is ${conversionRate}. Imagine you are a campaign analyst, what would be your analysis of my metrics, provide me a summary to better understand how I did, and what advise would you give to me if any"
            val prompt = GptPrompt(promptText,"text-davinci-002",0.5,1000)
            getGptContentViewModel.getAnalyticsSummary(prompt = prompt)


            lifecycleScope.launch {
                try {
                    getGptContentViewModel.getAnalyticsSummary(prompt).collect { result ->
                        // Do something with the result

                        binding.pgLoadingNote.isInvisible=true
                        binding.tvSummaryText.text = result
                    }
                }catch (e:Exception){
                    Log.d("exception",e.toString())
                }

            }

        }
    }



}