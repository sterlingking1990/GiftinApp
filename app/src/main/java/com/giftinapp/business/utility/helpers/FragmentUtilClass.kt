package com.giftinapp.business.utility.helpers

import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.giftinapp.business.R
import com.giftinapp.business.dialogs.LoadingDialog
import com.giftinapp.business.dialogs.MessageDialog
import org.aviran.cookiebar2.CookieBar
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType

abstract class FragmentUtilClass: Fragment() {
    private var loading: LoadingDialog? = null
    private var messageDialog: MessageDialog? = null


    fun showLoading() {
        loading = LoadingDialog.newInstance()
        loading?.show(requireActivity().supportFragmentManager, LoadingDialog::javaClass.name)
    }

    fun hideLoading() {
        loading?.dismiss()
    }


    protected fun showCookieBar(
        title: String = "",
        message: String,
        autoDismiss: Boolean = true,
        position: Int = CookieBar.TOP,
        delay: Long = 3000L,
        @ColorRes color: Int = R.color.colorPrimary,
        @DrawableRes logo: Int = R.drawable.ic_brandible_icon,
        @AnimRes leftSlide: Int = R.anim.slide_in_from_bottom,
        @AnimRes rightSlide: Int = R.anim.slide_out_to_bottom,
    ) {
        CookieBar.build(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .setBackgroundColor(color)
            .setIcon(logo)
            .setDuration(delay)
            .setCookiePosition(position)
            .setEnableAutoDismiss(autoDismiss)
            .setAnimationIn(leftSlide, leftSlide)
            .setAnimationOut(rightSlide, rightSlide)
            .show()
    }

    protected fun showErrorCookieBar(
        title: String = "",
        message: String,
        autoDismiss: Boolean = true,
        delay: Long = 3000L,
        @ColorRes color: Int = R.color.red,
        @DrawableRes logo: Int = R.drawable.ic_brandible_icon,
    ) {
        CookieBar.build(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .setBackgroundColor(color)
            .setIcon(logo)
            .setDuration(delay)
            .setEnableAutoDismiss(autoDismiss)
            .show()
    }


    fun showMessageDialog(
        message: String,
        title: String? = null,
        hasNegativeBtn: Boolean = false,
        negbtnText: String? = null,
        posBtnText: String? = null,
        listener: (() -> Unit)? = null,
        disMissable: Boolean = false,
    ) {
        messageDialog = MessageDialog.newInstance(
            message,
            title,
            hasNegativeBtn,
            negbtnText,
            posBtnText,
            listener,
            disMissable
        )
        messageDialog?.show(requireActivity().supportFragmentManager, MessageDialog::javaClass.name)
    }

    protected fun showHelpBar(
        title: String = "",
        content: String,
        targetView: View,
        textSize:Int = 12,
        titleSize:Int = 14,
        listener:((View) -> Unit)? = null,
    ){
        GuideView.Builder(requireContext())
            .setTitle(title)
            .setContentText(content)
            .setDismissType(DismissType.anywhere)
            .setTargetView(targetView)
            .setContentTextSize(textSize)
            .setTitleTextSize(titleSize)
            .setGuideListener(listener)
            .build()
            .show()
    }
}