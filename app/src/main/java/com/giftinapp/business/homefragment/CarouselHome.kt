package com.giftinapp.business.homefragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentCarouselHomeBinding
import com.giftinapp.business.utility.RemoteConfigUtil
import com.giftinapp.business.utility.base.BaseFragment
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType


class CarouselHome : BaseFragment<FragmentCarouselHomeBinding>() {

    private lateinit var binding: FragmentCarouselHomeBinding
    private lateinit var remoteConfigUtil: RemoteConfigUtil
    private lateinit var imageList:List<Uri>

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCarouselHomeBinding {
        binding = FragmentCarouselHomeBinding.inflate(layoutInflater,container,false)

        remoteConfigUtil = RemoteConfigUtil()

        binding.btnExploreBrand.setOnClickListener {
            openWebView(remoteConfigUtil.getBrandLink())
        }

        return binding
    }

    private fun openWebView(brandLink: String) {
        val intent = Intent()
        intent.data = Uri.parse(brandLink)
        intent.action = Intent.ACTION_VIEW
        startActivity(intent)
    }

}