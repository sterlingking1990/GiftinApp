package com.giftinapp.business.customer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.business.RespondersList
import com.giftinapp.business.business.RespondersListAdapter
import com.giftinapp.business.model.RespondersResponseModel
import com.giftinapp.business.model.SendGiftPojo
import com.giftinapp.business.propstates.ResponderApprovalState
import com.giftinapp.business.propstates.ResponderResponseState
import com.giftinapp.business.utility.ListenToSubmittedTaskResponse
import com.giftinapp.business.utility.SessionManager
import com.giftinapp.business.utility.gone
import com.giftinapp.business.utility.visible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class ViewRespondersAndRespondFragment : BottomSheetDialogFragment(), ViewRespondersAndRespondAdapter.ClickableRespondersResponse {

    private val challengeOwner by lazy { arguments?.getString(CHALLENGE_OWNER) }
    private val challengeType by lazy { arguments?.getString(CHALLENGE_TYPE) }
    private val challengeId by lazy { arguments?.getString(CHALLENGE_ID) }
    private val submitTaskResponseState: ResponderResponseState by viewModels()

    private lateinit var respondersListAdapter: ViewRespondersAndRespondAdapter
    lateinit var respondersLayoutManager: LinearLayoutManager

    private lateinit var sessionManager: SessionManager

    private lateinit var rvTaskResponders: RecyclerView

    lateinit var pgRespondersLoading: ProgressBar

    lateinit var btnSendResponse: FloatingActionButton

    lateinit var etTaskResponse:TextView

    lateinit var pgLoading:ProgressBar

    lateinit var tvNoResponse:TextView

    private var callback: ((approvedRespondersResponse: Boolean) -> Unit)? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_responders_and_respond, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pgRespondersLoading = ProgressBar(requireContext())
        rvTaskResponders = view.findViewById(R.id.rvTaskResponders)
        sessionManager = SessionManager(requireContext())

        respondersLayoutManager = LinearLayoutManager(requireContext())
        respondersLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvTaskResponders.layoutManager = respondersLayoutManager

        respondersListAdapter = ViewRespondersAndRespondAdapter(this)

        etTaskResponse = view.findViewById(R.id.etResponseToTask)
        pgLoading = view.findViewById(R.id.pgSendResponseLoading)
        tvNoResponse = view.findViewById(R.id.tvNoResponderResponse)
        btnSendResponse = view.findViewById(R.id.fabSendTaskResponse)
        btnSendResponse.setOnClickListener {
            pgLoading.visible()
            if(!etTaskResponse.text.toString().isNullOrEmpty()) {
                sendTaskResponse()
            }
        }

        submitTaskResponseState.responseSubmittedObservable.observe(viewLifecycleOwner){
            Log.d("Value",it.toString())
            if(it){
                fetchResponders()
            }
        }
        fetchResponders()
    }

    private fun sendTaskResponse(){
        Log.d("ChallengeType",challengeType.toString())
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val responseText = etTaskResponse.text.toString()

        val responseModel = RespondersResponseModel(respondersName = sessionManager.getEmail(),review=responseText,"",challengeOwner.toString(),challengeType)

        val sendGiftPojo= SendGiftPojo(empty = "")
        db.collection("challenge").document(challengeOwner.toString()).set(sendGiftPojo)
            .addOnCompleteListener {
                if(it.isSuccessful){
                   db.collection("challenge").document(challengeOwner.toString()).collection("challengelist").document(challengeId.toString()).set(sendGiftPojo)
                       .addOnCompleteListener {it2->
                           if(it2.isSuccessful){
                               db.collection("challenge").document(challengeOwner.toString()).collection("challengelist").document(challengeId.toString()).collection("responders").document(sessionManager.getEmail().toString()).set(responseModel)
                                   .addOnCompleteListener {it3->
                                       if(it3.isSuccessful){
                                           pgLoading.gone()
                                           etTaskResponse.text=""
                                           Toast.makeText(requireContext(),"Your task response has been submitted, please check back for reward",Toast.LENGTH_LONG).show()
                                           submitTaskResponseState.submitTaskResponse(true)
                                           callback?.invoke(true)
                                       }
                                   }
                           }
                       }

                }
            }
    }

    private fun fetchResponders(){
        pgLoading.visible()
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        challengeOwner?.let {
            db.collection("challenge").document(it).collection("challengelist").document(challengeId.toString())
                .collection("responders").get()
                .addOnCompleteListener {list->
                    if(list.isSuccessful) {
                        val allResponders = list.result.documents
                        val respondersResponseModel = arrayListOf<RespondersResponseModel>()
                        if (!list.result.isEmpty) {
                            for (eachRes in allResponders) {
                                val respondersName = eachRes.get("respondersName")
                                val respondersReview = eachRes.get("review")
                                val status = eachRes.get("status")
                                val challengeTy = eachRes.getString("challengeType")

                                respondersResponseModel.add(
                                    RespondersResponseModel(
                                        respondersName = respondersName.toString(),
                                        respondersReview.toString(),
                                        status = status.toString(),
                                        challengeOwner.toString(),
                                        challengeTy
                                    )
                                )
                                respondersListAdapter.setRespondersList(respondersResponseModel)
                                rvTaskResponders.adapter = respondersListAdapter
                                respondersListAdapter.notifyDataSetChanged()
                                pgLoading.gone()

                            }
                            submitTaskResponseState.submitTaskResponse(false)
                            pgLoading.visibility = View.GONE
                            tvNoResponse.visibility = View.GONE
                        }else{
                            pgLoading.visibility = View.GONE
                            tvNoResponse.visibility = View.VISIBLE
                        }
                    }

                }
        }
    }

    companion object {

        private const val CHALLENGE_OWNER = "challengeOwner"
        private const val CHALLENGE_ID = "challengeId"
        private const val CHALLENGE_TYPE = "challengeType"

        fun newInstance(challengeOwner: String?, challengeId: String?,challengeType:String, callback: (Boolean)->Unit): ViewRespondersAndRespondFragment {
            val args = Bundle()
            args.putString(CHALLENGE_OWNER, challengeOwner)
            args.putString(CHALLENGE_ID,challengeId)
            args.putString(CHALLENGE_TYPE,challengeType)

            val fragment = ViewRespondersAndRespondFragment()
            fragment.callback = callback
            fragment.arguments = args
            return fragment
        }
    }

}