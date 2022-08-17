package com.giftinapp.business.utility.base

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.giftinapp.business.dialogs.LoadingDialog
import com.giftinapp.business.utility.helpers.ActivityUtilClass
import com.giftinapp.business.utility.helpers.FragmentUtilClass

abstract class BaseActivity<VB: ViewBinding>: ActivityUtilClass() {
    private var _binding: VB? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {

            _binding = getActivityBinding(layoutInflater)
            return setContentView(binding.root)
        }catch (e:Exception){
            Log.d("InflationE",e.message.toString())
        }
    }

    abstract fun getActivityBinding(inflater: LayoutInflater):VB

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        _binding = null
    }
}

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