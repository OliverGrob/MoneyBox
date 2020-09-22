package com.ogrob.moneybox.utils.backdrop

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.ogrob.moneybox.R

class BackdropContainer(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

    private lateinit var toolbarIconClick: ToolbarIconClick
    private lateinit var imageView: ImageView

    private val menuIcon: Drawable?
    private val closeIcon: Drawable?
    private var backDropHeight: Float

    private lateinit var interpolator: LinearInterpolator
    private val duration: Long


    init {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.BackdropContainer, 0, 0)

        menuIcon = typedArray.getDrawable(R.styleable.BackdropContainer_menuIcon)
        closeIcon = typedArray.getDrawable(R.styleable.BackdropContainer_closeIcon)
        duration = typedArray.getInt(R.styleable.BackdropContainer_duration, 1000).toLong()

        typedArray.recycle()

        val metrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
        backDropHeight = metrics.heightPixels.toFloat()
    }

    fun dropHeight(peek: Int): BackdropContainer {
        backDropHeight -= backDropHeight - peek.toFloat()
        return this
    }

    fun dropInterpolator(interpolator: LinearInterpolator): BackdropContainer {
        this.interpolator = interpolator
        return this
    }

    fun imageView(imageView: ImageView): BackdropContainer {
        this.imageView = imageView
        return this
    }

    fun build() {
        if (checkTotalView()) {
            toolbarIconClick = ToolbarIconClick(
                getFrontView(),
                getBackView(),
                menuIcon,
                closeIcon,
                backDropHeight,
                interpolator,
                duration
            )

            imageView.setOnClickListener(toolbarIconClick)
        } else {
            throw ArrayIndexOutOfBoundsException("Backdrop should contain only two child")
        }
    }

    private fun checkTotalView(): Boolean {
        return childCount <= 2
    }

    private fun getFrontView(): View {
        return getChildAt(1)
    }

    private fun getBackView(): View {
        return getChildAt(0)
    }

    fun showBackView() {
        toolbarIconClick.open()
    }

    fun closeBackView() {
        toolbarIconClick.close()
    }

}