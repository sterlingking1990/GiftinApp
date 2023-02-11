package com.giftinapp.business.homefragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.giftinapp.business.R
import com.giftinapp.business.model.posts.Post
import com.giftinapp.business.network.services.postservice.PostApiService
import retrofit2.Call
import retrofit2.Response


class BrandibleHome : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brandible_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val postApiService = PostApiService.create().fetchPosts()
//
//        postApiService.enqueue(object: retrofit2.Callback<List<Post>>{
//            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
//                Log.d("Posts",response.body().toString())
//            }
//
//            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
//                Log.d("Error",t.message.toString())
//            }
//
//        })
    }

    private fun observePosts(){

    }
}