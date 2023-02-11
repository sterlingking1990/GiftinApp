package com.giftinapp.business.business

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.giftinapp.business.R
import com.giftinapp.business.model.FetchBanksResponse
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
    lateinit var etRewardingTimeView:TextInputLayout
    lateinit var etShareStartTimeView:TextInputLayout
    lateinit var btnSaveSharingSettings:MaterialButton

    lateinit var etShareDuration:EditText
    lateinit var etMinView:EditText

    private lateinit var spTargetCountry: SearchableSpinner
    private var targetCountry: String? = null

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

        btnSaveSharingSettings = view.findViewById(R.id.btnFinishSettingShareCondition)
        etShareDuration = view.findViewById(R.id.etDurationForShare)
        etMinView = view.findViewById(R.id.etMinViewRewarding)
        spTargetCountry = view.findViewById(R.id.spTargetCountry)
        spTargetCountry.onItemSelectedListener = this

        loadTargetCountry()
        etShareStartTime.setOnTouchListener(onTouchListener)
            //TimePickerFragment.newInstance(::showTimePicked).show(requireActivity().supportFragmentManager, "timePicker")

        etRewardingTime.setOnTouchListener(onTouchListener2)

        btnSaveSharingSettings.setOnClickListener {
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
            etShareStartTime.text.toString(),etShareDuration.text.toString().toInt(),etRewardingTime.text.toString(),etMinView.text.toString().toInt(),targetCountry
        )
        callback?.invoke(sharableCondition)
    }
    companion object {

        fun newInstance(
            callBack:(SharableCondition)-> Unit
        ): SharableConditionFragment {

            val fragment = SharableConditionFragment()
            fragment.callback = callBack

            return fragment
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        targetCountry = p0?.getItemAtPosition(p2).toString()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


}