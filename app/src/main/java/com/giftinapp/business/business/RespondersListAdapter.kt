package com.giftinapp.business.business

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.giftinapp.business.R
import com.giftinapp.business.model.RespondersResponseModel
import org.w3c.dom.Text

class RespondersListAdapter(private val clickableResponse:ClickableResponse):RecyclerView.Adapter<RespondersListAdapter.ViewItemHolder>() {

    private var respondersList = arrayListOf<RespondersResponseModel>()

    fun setRespondersList(respondersList: ArrayList<RespondersResponseModel>){
        this.respondersList = respondersList
    }

    inner class ViewItemHolder(item: View): RecyclerView.ViewHolder(item)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewItemHolder {
        val inflatedLayout = LayoutInflater.from(parent.context).inflate(R.layout.single_item_responders,parent,false)
        return ViewItemHolder(inflatedLayout)
    }

    override fun onBindViewHolder(holder: ViewItemHolder, position: Int) {
        holder.itemView.apply {
            val responderName = this.findViewById<TextView>(R.id.tvResponderName)
            val responderResponse = this.findViewById<TextView>(R.id.tvResponderResponse)
            val btnApproveResponderResponse = this.findViewById<Button>(R.id.btnApproveResponderResponse)

            val status = respondersList[position].status
            val respondersName = respondersList[position].respondersName
            val respondersResponse = respondersList[position].review
            val challengeType = respondersList[position].challengeType

            if(challengeType=="taskable"){
                btnApproveResponderResponse.isVisible=true
            }
            responderName.text = respondersName
            responderResponse.text = respondersResponse

            if(status=="approved"){
                btnApproveResponderResponse.text = "Approved"
            }

            btnApproveResponderResponse.setOnClickListener {
                clickableResponse.approveRespondersResponse(status.toString(),respondersName.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return respondersList.size
    }


    interface ClickableResponse{
        fun approveRespondersResponse(status:String,respondersName:String)

    }
}