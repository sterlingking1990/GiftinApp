package com.giftinapp.business.utility.helpers

import java.text.SimpleDateFormat
import java.util.*

class DateHelper {

    fun setPublishedAtDate():String{
        val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm",Locale.ENGLISH);
        val now = Date()
        val cal: Calendar =
            GregorianCalendar()

        cal.time = now

        return sdf.format(cal.time)
    }
}