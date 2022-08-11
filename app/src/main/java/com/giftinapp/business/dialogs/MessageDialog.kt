package com.giftinapp.business.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.giftinapp.business.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MessageDialog private constructor(): DialogFragment() {

    private val title by lazy { arguments?.getString(TITLE) }
    private val message by lazy { arguments?.getString(MESSAGE) }
    private val hasNegativeBtn by lazy { arguments?.getBoolean(HAS_NEG_BTN) }
    private val negbtnText by lazy { arguments?.getString(NEG_BTN_TEXT) }
    private val positiveBtnText by lazy { arguments?.getString(POSITIVE_BTN_TEXT) }
    private val disMissable by lazy { arguments?.getBoolean(DIS_MISSABLE) }

    private var callback: (() -> Unit)? = null

    companion object {
        private const val TITLE = "title"
        private const val MESSAGE = "message"
        private const val HAS_NEG_BTN = "has_neg_btn"
        private const val NEG_BTN_TEXT = "neg_btn_text"
        private const val POSITIVE_BTN_TEXT = "pos_btn_text"
        private const val DIS_MISSABLE = "dis_missable"
        fun newInstance(
            message: String, title: String? = null, hasNegativeBtn: Boolean = false,
            negbtnText: String? = null, posBtnText: String? = null, listener: (() -> Unit)? = null,
            disMissable: Boolean
        ): MessageDialog {
            val fragment = MessageDialog()
            val bundle = Bundle().apply {
                putString(TITLE, title)
                putString(MESSAGE, message)
                putBoolean(HAS_NEG_BTN, hasNegativeBtn)
                putString(NEG_BTN_TEXT, negbtnText)
                putString(POSITIVE_BTN_TEXT, posBtnText)
                putBoolean(DIS_MISSABLE, disMissable)
            }
            fragment.arguments = bundle
            fragment.callback = listener
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setIcon(R.drawable.ic_brandible_icon)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(disMissable ?: false)
        builder.setPositiveButton(positiveBtnText ?: getString(R.string.ok)) { _, _ ->
            callback?.invoke()
            dismiss()
        }
        if (hasNegativeBtn == true) {
            builder.setNegativeButton(negbtnText) { _, _ ->
                dismiss()
            }
        }
        isCancelable = false

        return builder.create().apply {
            window?.attributes?.windowAnimations = R.style.dialog_style
        }
    }

}