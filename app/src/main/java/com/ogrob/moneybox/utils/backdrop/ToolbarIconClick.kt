package com.ogrob.moneybox.utils.backdrop

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView

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
    private var toolbarLogo: ImageView? = null


    fun open() {
        if (!dropped) {
            onClick(null)
        }
    }

    fun close() {
        if (dropped) {
            onClick(null)
        }
    }

    override fun onClick(view: View?) {
        if (toolbarLogo == null) {
            toolbarLogo = view as ImageView?
        }

        dropped = !dropped

        animatorSet.removeAllListeners()
        animatorSet.end()
        animatorSet.cancel()

        updateLogo()


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

    private fun updateLogo() {
        if (menuIcon != null && closeIcon != null) {
            if (dropped) {
                toolbarLogo!!.setImageDrawable(closeIcon)
            } else {
                toolbarLogo!!.setImageDrawable(menuIcon)
            }
        }
    }
}