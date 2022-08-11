package com.giftinapp.business.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.giftinapp.business.R
import com.giftinapp.business.databinding.FragmentLoadingDialogBinding


class LoadingDialog private constructor(): DialogFragment() {

    private lateinit var binding: FragmentLoadingDialogBinding

    companion object {
        fun newInstance(): LoadingDialog {
            return LoadingDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        isCancelable = false
        binding = FragmentLoadingDialogBinding.inflate(inflater,container,false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }


    fun showLoading() {
        val dialog = LoadingDialog.newInstance()
        dialog.show(requireActivity().supportFragmentManager, LoadingDialog::javaClass.name)
    }

    fun hideLoading() {
        LoadingDialog().dismiss()
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, this.javaClass.name)
    }

    fun dismissDialog() {
        dismiss()
    }

}