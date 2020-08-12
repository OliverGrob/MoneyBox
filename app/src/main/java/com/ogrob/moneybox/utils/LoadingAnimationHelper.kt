package com.ogrob.moneybox.utils

import androidx.fragment.app.Fragment
import com.ogrob.moneybox.ui.MainActivity

fun Fragment.showLoadingAnimation() {
    (requireActivity() as MainActivity).showLoadingAnimation()
}

fun Fragment.hideLoadingAnimation() {
    (requireActivity() as MainActivity).hideLoadingAnimation()
}