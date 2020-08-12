package com.ogrob.moneybox.utils.backdrop

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import com.ogrob.moneybox.R

class BackdropContainer(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarIconClick: ToolbarIconClick

    private val menuIcon: Drawable?
    private val closeIcon: Drawable?
    private var backDropHeight: Float

    private lateinit var interpolator: LinearInterpolator
    private val duration: Long


    init {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.BackdropContainer, 0, 0)

//        menuIcon = typedArray.getDrawable(R.styleable.BackdropContainer_menuIcon)
//        closeIcon = typedArray.getDrawable(R.styleable.BackdropContainer_closeIcon)
        menuIcon = resources.getDrawable(R.drawable.ic_filter_list_white_24dp, null)
        closeIcon = resources.getDrawable(R.drawable.ic_close_white_24dp, null)
        duration = typedArray.getInt(R.styleable.BackdropContainer_duration, 1000).toLong()

        typedArray.recycle()

        val metrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
        backDropHeight = metrics.heightPixels.toFloat()
    }

    fun attachToolbar(toolbar: Toolbar): BackdropContainer {
        this.toolbar = toolbar
//        this.toolbar.navigationIcon = menuIcon
        return this
    }

    fun dropHeight(peek: Int): BackdropContainer {
        backDropHeight -= backDropHeight - peek.toFloat()
        return this
    }

    fun dropInterpolator(interpolator: LinearInterpolator): BackdropContainer {
        this.interpolator = interpolator
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

            toolbar.setLogo(R.drawable.ic_filter_list_white_24dp)
            toolbar.getChildAt(1).setOnClickListener(toolbarIconClick)
            val params = toolbar.getChildAt(1).layoutParams as MarginLayoutParams
            params.rightMargin = 100
            toolbar.getChildAt(1).layoutParams = params
//            toolbar.setNavigationOnClickListener(toolbarIconClick)
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