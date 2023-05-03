package com.giftinapp.business.business

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.customer.ReviewAdapter
import com.giftinapp.business.customer.ReviewFragment
import com.giftinapp.business.model.MerchantWalletPojo
import com.giftinapp.business.model.RespondersResponseModel
import com.giftinapp.business.propstates.ResponderApprovalState
import com.giftinapp.business.propstates.ReviewState
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.gone
import com.giftinapp.business.utility.visible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class RespondersList : BottomSheetDialogFragment(),RespondersListAdapter.ClickableResponse {

    private val challengeOwner by lazy { arguments?.getString(CHALLENGE_OWNER) }
    private val challengeId by lazy { arguments?.getString(CHALLENGE_ID) }
    private val challengeWorth by lazy { arguments?.getInt(CHALLENGE_WORTH)}

    private val approvedRespondersResponseState: ResponderApprovalState by viewModels()

    private lateinit var respondersListAdapter: RespondersListAdapter
    lateinit var respondersLayoutManager: LinearLayoutManager

    private lateinit var sessionManager: SessionManager

    private lateinit var rvRespondersList:RecyclerView

    lateinit var pgRespondersLoading:ProgressBar

    private var callback: ((approvedRespondersResponse: Boolean) -> Unit)? = null

    var approvedStatus = false

    var remoteConfigUtil: RemoteConfigUtil? = null

    var revenue_multiplier = 0.1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_responders_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        remoteConfigUtil = RemoteConfigUtil()
        revenue_multiplier = remoteConfigUtil!!.getRevenueMultiplier().asDouble()
        pgRespondersLoading = ProgressBar(requireContext())
        rvRespondersList = view.findViewById(R.id.rvResponders)
        sessionManager = SessionManager(requireContext())

        respondersLayoutManager = LinearLayoutManager(requireContext())
        respondersLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvRespondersList.layoutManager = respondersLayoutManager

        respondersListAdapter = RespondersListAdapter(this)

        approvedRespondersResponseState.approveResponderReviewObservable.observe(viewLifecycleOwner){
            Log.d("Value",it.toString())
            if(it){
                updateMerchantWallet()
                fetchResponders()
            }
        }
        fetchResponders()
    }

    private fun updateMerchantWallet(){
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        //get wallet balance
        db.collection("merchants").document(sessionManager.getEmail().toString()).collection("reward_wallet").document("deposit").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val result = it.result
                    val walletAmount = result.get("merchant_wallet_amount")

                    val totalAmount = walletAmount as Long - (challengeWorth?.toLong() ?: 0)
                    Log.d("totalAmount",totalAmount.toString())
                    updateTotalAmount(totalAmount)

                }
            }
    }

    private fun updateTotalAmount(totalAmount:Long){
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val merchantWalletPojo = MerchantWalletPojo()
        merchantWalletPojo.merchant_wallet_amount = totalAmount

        Log.d("TOTALAMOUNTUpdate",totalAmount.toString())

        //get wallet balance
        db.collection("merchants").document(sessionManager.getEmail().toString()).collection("reward_wallet").document("deposit").set(merchantWalletPojo)
    }

    private fun fetchResponders(){
        pgRespondersLoading.visible()
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        db.collection("challenge").document(sessionManager.getEmail().toString()).collection("challengelist").document(challengeId.toString())
            .collection("responders").get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val allResponders = it.result.documents
                    val respondersResponseModel = arrayListOf<RespondersResponseModel>()

                    for(eachRes in allResponders){
                        val respondersName = eachRes.get("respondersName")
                        val respondersReview = eachRes.get("review")
                        val status = eachRes.get("status")
                        val challengeTypeFromDb = eachRes.getString("challengeType")

                        respondersResponseModel.add(RespondersResponseModel(respondersName = respondersName.toString(),respondersReview.toString(), status = status.toString(),sessionManager.getEmail().toString(),challengeTypeFromDb))
                        respondersListAdapter.setRespondersList(respondersResponseModel)
                        rvRespondersList.adapter = respondersListAdapter
                        respondersListAdapter.notifyDataSetChanged()
                        pgRespondersLoading.gone()

                    }
                }

            }
    }

    companion object {

        private const val CHALLENGE_OWNER = "challengeOwner"
        private const val CHALLENGE_ID = "challengeId"
        private const val CHALLENGE_WORTH = "challengeWorth"
        private const val CHALLENGE_TYPE = "challengeType"

        fun newInstance(
            challengeOwner: String?,
            challengeId: String?,
            challengeWorth: Int,
            challengeType:String,
            callBack:(Boolean)-> Unit
        ): RespondersList {
            val args = Bundle()
            args.putString(CHALLENGE_OWNER, challengeOwner)
            args.putString(CHALLENGE_ID,challengeId)
            args.putInt(CHALLENGE_WORTH, challengeWorth)
            args.putString(CHALLENGE_TYPE,challengeType)

            val fragment = RespondersList()
            fragment.callback = callBack

            fragment.arguments = args
            return fragment
        }
    }

    override fun approveRespondersResponse(status: String, respondersName:String) {
        if(status!="approved"){
            //approve the challenge review sent by responder
            val db = FirebaseFirestore.getInstance()

            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings

            Log.d("ChallengeOwner",challengeOwner.toString())
            Log.d("ChallengeId",challengeId.toString())
            db.collection("challenge").document(sessionManager.getEmail().toString()).collection("challengelist").document(challengeId.toString()).collection("responders").document(respondersName).update("status","approved")
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        //update the reward coin
                        db.collection("users").document(respondersName).collection("rewards").document("GiftinAppBonus").get()
                            .addOnCompleteListener { user->
                                if(user.isSuccessful){
                                    val rewardAmt = user.result.getDouble("gift_coin")?.toInt()
                                    val total = (challengeWorth?.minus((revenue_multiplier * challengeWorth!!)))?.plus(rewardAmt!!)
                                    db.collection("users").document(respondersName).collection("rewards").document("GiftinAppBonus").update("gift_coin",total)
                                        .addOnCompleteListener {rewardUpdated ->
                                            if(rewardUpdated.isSuccessful){
                                                Toast.makeText(requireContext(),"The reward has been sent",Toast.LENGTH_LONG).show()
                                                approvedRespondersResponseState.approveResponderReview(true)
                                                callback?.invoke(true)
                                            }
                                        }
                                }
                            }
                    }
                }

        }
    }
}