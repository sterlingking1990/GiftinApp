package com.giftinapp.business.utility.helpers

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.giftinapp.business.business.SharableConditionFragment
import com.giftinapp.business.model.SharableCondition
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private var callback: ((hour: Int,min:Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val year = c.get(Calendar.YEAR)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(requireContext(), this, hour, minute, DateFormat.is24HourFormat(requireContext()))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        val timeInString = convertToStandardTime(hourOfDay,minute)
        callback?.invoke(hourOfDay,minute)
    }

    companion object {
        fun newInstance(
            callBack:(hour:Int,min:Int)-> Unit
        ): TimePickerFragment {

            val fragment = TimePickerFragment()
            fragment.callback = callBack

            return fragment
        }
    }

    private fun convertToStandardTime(h:Int,m:Int):String{
            if(h in 0..11) {
                return "AM"
            }
        return "PM"
    }
}