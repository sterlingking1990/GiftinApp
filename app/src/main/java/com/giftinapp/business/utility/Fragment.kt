package com.giftinapp.business.utility

import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun Fragment.showBottomSheet(fragment: BottomSheetDialogFragment) {
    fragment.show(childFragmentManager, fragment.javaClass.name)
}