package com.giftinapp.business.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.RespondersResponseModel

class ViewRespondersAndRespondAdapter(val clickableRespondersResponse:ClickableRespondersResponse):RecyclerView.Adapter<ViewRespondersAndRespondAdapter.ViewItemHolder>() {

    private var respondersList = arrayListOf<RespondersResponseModel>()

    fun setRespondersList(respondersList: ArrayList<RespondersResponseModel>){
        this.respondersList = respondersList
    }

    inner class ViewItemHolder(item: View): RecyclerView.ViewHolder(item)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewItemHolder {
        val inflatedLayout = LayoutInflater.from(parent.context).inflate(R.layout.single_item_responders_and_respond_view,parent,false)
        return ViewItemHolder(inflatedLayout)
    }

    override fun onBindViewHolder(holder: ViewItemHolder, position: Int) {
        holder.itemView.apply {
            val responderName = this.findViewById<TextView>(R.id.tvResponderName)
            val responderResponse = this.findViewById<TextView>(R.id.tvResponderResponse)

            val status = respondersList[position].status
            val respondersName = respondersList[position].respondersName
            val respondersResponse = respondersList[position].review

            responderName.text = respondersName
            responderResponse.text = respondersResponse

        }
    }

    override fun getItemCount(): Int {
        return respondersList.size
    }

    interface ClickableRespondersResponse{

    }
}