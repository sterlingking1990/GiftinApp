package com.giftinapp.business.business

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.giftinapp.business.R
import com.giftinapp.business.model.SharableCondition
import com.giftinapp.business.utility.helpers.TimePickerFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import java.text.SimpleDateFormat
import java.util.*

class SharableConditionFragment : BottomSheetDialogFragment(), AdapterView.OnItemSelectedListener {

    private var callback: ((sharableCondition: SharableCondition) -> Unit)? = null
    lateinit var  etShareStartTime:EditText
    lateinit var etRewardingTime:EditText
    lateinit var etMinLikeForRewarding:EditText
    lateinit var etMinShareForRewarding:EditText
    lateinit var etDaysPostLasting:EditText
    lateinit var etRewardingTimeView:TextInputLayout
    lateinit var etShareStartTimeView:TextInputLayout
    lateinit var btnSaveSharingSettings:MaterialButton

    lateinit var etShareDuration:EditText
    lateinit var etMinView:EditText

    private lateinit var spTargetCountry: SearchableSpinner
    private var targetCountry: String? = null

    lateinit var etMinBizView:EditText
    lateinit var etMinBizReactions:EditText
    private var fbSharedPlatform:String=""

    var noLikesForRewarding:Int?=null
    var noSharesForRewarding:Int? = null
    var noViewForRewarding:Int? = null
    var noReactionsForRewarding:Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sharable_condition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         etShareStartTime = view.findViewById(R.id.etShareStartTimeInput)
        etShareStartTimeView = view.findViewById(R.id.etShareStartTime)
        etRewardingTimeView = view.findViewById(R.id.etRewardingTime)
        etRewardingTime = view.findViewById(R.id.etRewardingTimeInput)
        etMinLikeForRewarding = view.findViewById(R.id.etMinLikeForRewarding)
        etMinShareForRewarding = view.findViewById(R.id.etMinShareForRewarding)
        etDaysPostLasting = view.findViewById(R.id.etDaysPostingLasts)
        btnSaveSharingSettings = view.findViewById(R.id.btnFinishSettingShareCondition)
        etShareDuration = view.findViewById(R.id.etDurationForShare)
        etMinView = view.findViewById(R.id.etMinViewRewarding)
        spTargetCountry = view.findViewById(R.id.spTargetCountry)
        etMinBizView = view.findViewById(R.id.etMinBizView)
        etMinBizReactions = view.findViewById(R.id.etMinBizReaction)

        when(fbSharedPlatform) {
            "post-feed" -> {
                etMinLikeForRewarding.visibility = View.VISIBLE
                etMinShareForRewarding.visibility = View.VISIBLE
            }
            "post-story" -> {
                etMinBizView.visibility = View.VISIBLE
                etMinBizReactions.visibility = View.VISIBLE
            }
            else->{
                etMinLikeForRewarding.visibility = View.VISIBLE
                etMinShareForRewarding.visibility = View.VISIBLE
                etMinBizView.visibility = View.VISIBLE
                etMinBizReactions.visibility = View.VISIBLE
            }
        }

        spTargetCountry.onItemSelectedListener = this

        loadTargetCountry()
        etShareStartTime.setOnTouchListener(onTouchListener)
            //TimePickerFragment.newInstance(::showTimePicked).show(requireActivity().supportFragmentManager, "timePicker")

        etRewardingTime.setOnTouchListener(onTouchListener2)

        btnSaveSharingSettings.setOnClickListener {
            noLikesForRewarding = if(etMinLikeForRewarding.text.toString()==""){
                null
            }else{
                etMinLikeForRewarding.text.toString().toInt()
            }
            noSharesForRewarding = if(etMinShareForRewarding.text.toString()==""){
                null
            }else{
                etMinShareForRewarding.text.toString().toInt()
            }
            noViewForRewarding = if(etMinBizView.text.toString()==""){
                null
            }else{
                etMinBizView.text.toString().toInt()
            }
            noReactionsForRewarding = if(etMinBizReactions.text.toString()==""){
                null
            }else{
                etMinBizReactions.text.toString().toInt()
            }
            saveSharingSettings()
        }
    }

    private val onTouchListener = View.OnTouchListener { v, event ->
        v.performClick()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                TimePickerFragment.newInstance(::showTimePicked).show(requireActivity().supportFragmentManager, "timePicker")
                return@OnTouchListener true
            }
        }
        false
    }

    private val onTouchListener2 = View.OnTouchListener { v, event ->
        v.performClick()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                TimePickerFragment.newInstance(::showRewardTimePicked).show(requireActivity().supportFragmentManager, "timePicker")
                return@OnTouchListener true
            }
        }
        false
    }

    private fun loadTargetCountry(){
        val list = arrayListOf("Nigeria","Kenya","All")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            list.distinct()
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTargetCountry.adapter = adapter
    }

    private fun showTimePicked(hourPicked: Int, minPicked: Int) {
        Log.d("Hour",hourPicked.toString())
        Log.d("MinPicked",minPicked.toString())
        val sdf = SimpleDateFormat("MM-dd-yyyy");
        val now = Date()
        val cal: Calendar =
            GregorianCalendar()

        cal.time = now
        val shareTime = sdf.format(cal.time) + " $hourPicked:$minPicked"

        etShareStartTime.setText(shareTime)
    }

    private fun showRewardTimePicked(hourPicked: Int, minPicked: Int) {
        Log.d("Hour",hourPicked.toString())
        Log.d("MinPicked",minPicked.toString())
        etRewardingTime.setText("$hourPicked:$minPicked")
    }

    private fun saveSharingSettings(){
        val sharableCondition = SharableCondition(
            etShareStartTime.text.toString(),
            etShareDuration.text.toString().toInt(),
            null,
            null,
            null,
            noLikesForRewarding,
            noSharesForRewarding,
            etDaysPostLasting.text.toString().toInt(),
            fbSharedPlatform,
            noViewForRewarding,
            noReactionsForRewarding
        )
        callback?.invoke(sharableCondition)
    }
    companion object {

        fun newInstance(
            s: String,
            callBack: (SharableCondition) -> Unit
        ): SharableConditionFragment {

            val fragment = SharableConditionFragment()
            fragment.callback = callBack
            fragment.fbSharedPlatform = s

            return fragment
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        targetCountry = p0?.getItemAtPosition(p2).toString()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


}