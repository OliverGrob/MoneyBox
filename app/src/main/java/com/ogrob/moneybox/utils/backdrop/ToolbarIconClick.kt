package com.ogrob.moneybox.utils.backdrop

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageButton


class ToolbarIconClick(
    private val frontView: View,
    private val backView: View,
    private val menuIcon: Drawable?,
    private val closeIcon: Drawable?,
    private val height: Float,
    private val interpolator: LinearInterpolator,
    private val duration: Long
) : View.OnClickListener {

    private var dropped = false
    private val animatorSet = AnimatorSet()
    private var toolbarIcon: AppCompatImageButton? = null


    fun open() {
        if (!dropped) {
            onClick(toolbarIcon)
        }
    }

    fun close() {
        if (dropped) {
            onClick(toolbarIcon)
        }
    }

    override fun onClick(view: View?) {
        if (toolbarIcon == null) {
            toolbarIcon = view as AppCompatImageButton?
        }

        dropped = !dropped

        animatorSet.removeAllListeners()
        animatorSet.end()
        animatorSet.cancel()

        updateIcon(view!!)


        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(
            frontView,
            "translationY",
            if (dropped) height else 0f
        )
        animatorSet.play(objectAnimator)
        objectAnimator.duration = duration
        objectAnimator.interpolator = interpolator
        objectAnimator.start()
    }

    private fun updateIcon(view: View) {
        if (menuIcon != null && closeIcon != null) {
            if (dropped) {
                toolbarIcon!!.setImageDrawable(closeIcon)
            } else {
                toolbarIcon!!.setImageDrawable(menuIcon)
            }
        }
    }
}