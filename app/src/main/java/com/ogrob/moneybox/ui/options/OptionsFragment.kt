package com.ogrob.moneybox.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.ogrob.moneybox.data.viewmodel.OptionsViewModel
import com.ogrob.moneybox.databinding.FragmentOptionsBinding
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
import com.ogrob.moneybox.utils.hideKeyboard

class OptionsFragment : BaseFragment() {

    private val optionsViewModel: OptionsViewModel by viewModels()

    private lateinit var binding: FragmentOptionsBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentOptionsBinding.inflate(inflater)

        binding.root.setOnClickListener { it.hideKeyboard() }
        binding.saveOptionsButton.setOnClickListener {
            it.hideKeyboard()
            onSaveOptions(it)
        }

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