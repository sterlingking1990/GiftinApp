package com.giftinapp.business.utility.helpers

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.giftinapp.business.R
import com.giftinapp.business.utility.gone
import com.giftinapp.business.utility.visible

class StatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

   // private var statusText: TextView
    private var descriptionText: TextView
    private var viewMoreText: TextView
    private var description: String = ""
    private var descriptionExpanded = false
    init {
        orientation = VERTICAL
        View.inflate(context, R.layout.status_view, this)
        //statusText = findViewById(R.id.status_text)
        descriptionText = findViewById(R.id.description_text)
        viewMoreText = findViewById(R.id.view_more_text)
        viewMoreText.text = ""
        viewMoreText.setOnClickListener { toggleDescription() }
    }

//    fun setStatus(status: String) {
//        statusText.text = status
//    }

    fun setDescription(description: String) {
        if(description.isEmpty()){
            this.descriptionText.gone()
        }else{
            this.descriptionText.visible()
        }
        this.description = description
        if (!descriptionExpanded) {
            if(description.length>120){
            val shortDescription = getShortDescription(description)
            descriptionText.text = shortDescription
            viewMoreText.text = context.getText(R.string.view_more)
            }else{
                descriptionText.text = description
                viewMoreText.text = ""
            }
        } else {
            if(description.length>120){
                viewMoreText.text = context.getString(R.string.view_less)
            }else{
                viewMoreText.text = ""
            }
            descriptionText.text = description
        }
    }

    private fun toggleDescription() {
        descriptionExpanded = !descriptionExpanded
        if (descriptionExpanded) {
            descriptionText.text = description
            viewMoreText.text = context.getString(R.string.view_less)
        } else {
            val shortDescription = getShortDescription(description)
            descriptionText.text = shortDescription
            viewMoreText.text = context.getString(R.string.view_more)
        }
    }

    private fun getShortDescription(description: String): SpannableStringBuilder {
        val maxLength = 120
        var shortdescription = ""
        if (description.length > maxLength) {
            shortdescription = description.substring(0, maxLength) + "..."
        } else {
            shortdescription = description
        }
        val builder = SpannableStringBuilder(shortdescription)
        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)),
            builder.length - 3,
            builder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return builder
    }
}
