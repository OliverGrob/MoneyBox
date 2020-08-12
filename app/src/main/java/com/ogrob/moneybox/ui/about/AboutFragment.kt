package com.ogrob.moneybox.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ogrob.moneybox.databinding.FragmentAboutBinding
import com.ogrob.moneybox.ui.BaseFragment

class AboutFragment : BaseFragment() {

    private lateinit var binding: FragmentAboutBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAboutBinding.inflate(inflater)

        return binding.root
    }

}