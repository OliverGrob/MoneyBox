package com.ogrob.moneybox.ui

import android.content.Context
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseFragment : Fragment() {

    override fun onAttach(context: Context) {
        requireActivity().toolbar.logo = null

        super.onAttach(context)
    }

}