package com.giftinapp.business.utility.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.giftinapp.business.dialogs.LoadingDialog
import com.giftinapp.business.utility.helpers.FragmentUtilClass

abstract class BaseFragment<VB: ViewBinding>: FragmentUtilClass() {

    private var _binding: VB? = null
    private val binding get() = _binding!!

    private var loading: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = getFragmentBinding(inflater, container)
        return binding.root
    }

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}