package com.giftinapp.merchant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*

class GiftlistAdapter(var giftItemClickable: GiftItemClickable): RecyclerView.Adapter<GiftlistAdapter.ViewHolder>(){

    private var giftList:List<GiftList> = ArrayList()

    fun setGiftList(giftList:List<GiftList>){
        this.giftList=giftList
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_item_reward, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.itemView.apply {
            var giftImage =findViewById<ImageView>(R.id.iv_giftlist_image)
            var giftCaption=findViewById<TextView>(R.id.tv_giftlist_caption)
            var giftCost=findViewById<TextView>(R.id.tv_giftlist_cost)

            //Loading image using Picasso
            Picasso.get().load(giftList[position].gift_url).into(giftImage);
            giftCaption.text=giftList[position].gift_name
            giftCost.text= giftList[position].gift_cost.toString()

        }
        holder.itemView.setOnClickListener {
            var item= giftList[position]
            giftItemClickable.onGiftClick(item)
        }
    }

    override fun getItemCount(): Int {
        return giftList.size
    }

    interface GiftItemClickable{
        fun onGiftClick(itemId: GiftList)
    }
}