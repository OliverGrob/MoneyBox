package com.ogrob.moneybox.presentation.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.OptionsViewModel
import com.ogrob.moneybox.databinding.FragmentOptionsBinding
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE

class OptionsFragment : Fragment() {

    private val optionsViewModel: OptionsViewModel by lazy {
        ViewModelProviders.of(this).get(OptionsViewModel::class.java)
    }

    private lateinit var binding: FragmentOptionsBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_options, container, false)

        binding.saveOptionsButton.setOnClickListener { onSaveOptions(it) }

        val amountGoal = optionsViewModel.retrieveAmountGoalFromSharedPreferences(binding.root.context)

        binding.optionsAmountGoalEditText.setText(
            if (amountGoal == SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE)
                EMPTY_STRING
            else
                amountGoal.toString())

        return binding.root
    }

    private fun onSaveOptions(view: View) {
        val goalAmount = if (binding.optionsAmountGoalEditText.text.isEmpty())
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
        else
            binding.optionsAmountGoalEditText.text.toString().toFloat()

        optionsViewModel.updateAmountGoalInSharedPreferences(binding.root.context, goalAmount)

        Toast.makeText(context, "Changes Saved!", Toast.LENGTH_SHORT).show()
    }
}