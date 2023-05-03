package com.giftinapp.business.utility.helpers

import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    fun nowDateBeforePublishedDate(publishedDate:String):Boolean{
        val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.ENGLISH);
        val pubDate = sdf.parse(publishedDate);


        val calendar = Calendar.getInstance();

        calendar.time = pubDate!!;
        calendar.add(Calendar.HOUR, 24);

        val now = Date()
        val cal: Calendar =
            GregorianCalendar()

        cal.time = now

        Log.d("IsWithinTime", (cal.time<=calendar.time).toString())

        return cal.time<=calendar.time
    }


}