package com.giftinapp.business.customer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentPostDetailBinding
import com.giftinapp.business.network.viewmodel.postviewmodel.GetPostsViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailFragment : Fragment() {
    private lateinit var binding:FragmentPostDetailBinding

    var postTitle:String = ""
    var postContent:String = ""
    var postImage:String = ""
    var imageOwner:String = ""
    var imageOwnerLink:String = ""
    var imageOwnerUsername:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postTitle = arguments?.getString("PostTitle").toString()
        postContent = arguments?.getString("PostContent").toString()
        postImage = arguments?.getString("PostImage").toString()
        imageOwner = arguments?.getString("ImageOwner").toString()
        imageOwnerLink = arguments?.getString("ImageOwnerLink").toString()
        imageOwnerUsername = arguments?.getString("ImageOwnerUsername").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPostDetailBinding.inflate(layoutInflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayContent()
    }

    private fun displayContent(){
        binding.postTitle.text = postTitle
        binding.postContent.text = HtmlCompat.fromHtml(postContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvOwnerName.text = imageOwner
        Picasso.get().load(postImage).into(binding.postImage)

        Log.d("ImageOwnerLink", imageOwnerLink)
        binding.tvOwnerName.setOnClickListener {
            val url = "https://unsplash.com/$imageOwnerUsername?utm_source=Brandible&utm_medium=referral"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.brand.setOnClickListener {
            val url = "https://unsplash.com/?utm_source=Brandible&utm_medium=referral"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

}