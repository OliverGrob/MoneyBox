package com.ogrob.moneybox.ui

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseFragment : Fragment() {

    override fun onAttach(context: Context) {
        requireActivity().toolbar.getChildAt(1).visibility = View.INVISIBLE

        super.onAttach(context)
    }

}