package com.giftinapp.merchant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.ArrayList

class MyGiftCartAdapter(var giftCartItemClickable:MyGiftCartItemClickable):RecyclerView.Adapter<MyGiftCartAdapter.ViewHolder>() {

    private var listOfMyGift:List<MyCartPojo> = ArrayList()

    fun setMyGiftsList(myGifts:List<MyCartPojo>) {
        this.listOfMyGift=myGifts
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedTemplate =
                LayoutInflater.from(parent.context).inflate(R.layout.single_my_gift_detail, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            var ivMyGiftCartGiftImage = findViewById<ImageView>(R.id.iv_mygift_singleitem_image)
            var tvMyGiftCartGiftName= findViewById<TextView>(R.id.tv_mygift_singleitem_caption)
            var tvMyGiftCartGiftTrack=findViewById<TextView>(R.id.tv_mygift_singleitem_progressBar_Text)
            var pgMyGiftCartGiftTrack=findViewById<ProgressBar>(R.id.pg_mygift_singleitem_progressBar)

            tvMyGiftCartGiftName.text=listOfMyGift[position].gift_name
            tvMyGiftCartGiftTrack.text= listOfMyGift[position].gift_track.toString() + " %"
            pgMyGiftCartGiftTrack.progress=listOfMyGift[position].gift_track

            //Loading image using Picasso
            Picasso.get().load(listOfMyGift[position].gift_url).into(ivMyGiftCartGiftImage);
        }

        holder.itemView.setOnClickListener {
            var item= listOfMyGift[position]
            giftCartItemClickable.onGiftClick(item)
        }
    }

    override fun getItemCount(): Int {
     return listOfMyGift.size
    }


    interface MyGiftCartItemClickable{
        fun onGiftClick(itemId: MyCartPojo)
    }
}