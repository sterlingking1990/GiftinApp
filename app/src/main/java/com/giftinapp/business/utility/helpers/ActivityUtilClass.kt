package com.giftinapp.business.utility.helpers

import android.app.Activity
import android.view.MenuItem
import android.view.View
import androidx.activity.ComponentActivity
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.giftinapp.business.R
import com.giftinapp.business.dialogs.LoadingDialog
import com.giftinapp.business.dialogs.MessageDialog
import org.aviran.cookiebar2.CookieBar
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType

abstract class ActivityUtilClass: AppCompatActivity() {
    private var loading: LoadingDialog? = null
    private var messageDialog: MessageDialog? = null
    private var showTipView: GuideView? = null

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
        CookieBar.build(this)
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
        CookieBar.build(this)
            .setTitle(title)
            .setMessage(message)
            .setBackgroundColor(color)
            .setIcon(logo)
            .setDuration(delay)
            .setEnableAutoDismiss(autoDismiss)
            .show()
    }

    protected fun showHelpBar(
        title: String = "",
        content: String,
        targetView: View,
        textSize:Int = 12,
        titleSize:Int = 14,
        listener:((View) -> Unit)? = null,
    ){
        GuideView.Builder(this)
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

    protected fun showMessageDialog(
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
        messageDialog?.show(supportFragmentManager, MessageDialog::javaClass.name)
    }

    protected fun showMessage(showNegBtn:Boolean, dismissable:Boolean, message:String, negBtnText:String?,listener: (() -> Unit)?){
        return showMessageDialog(
            title = "Update",
            message = message,
            hasNegativeBtn = showNegBtn,
            negbtnText = negBtnText,
            posBtnText = "Update",
            disMissable = dismissable,
            listener = listener
        )
    }

}