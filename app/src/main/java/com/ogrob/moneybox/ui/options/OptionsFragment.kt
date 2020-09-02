package com.ogrob.moneybox.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.ogrob.moneybox.data.viewmodel.OptionsViewModel
import com.ogrob.moneybox.databinding.FragmentOptionsBinding
import com.ogrob.moneybox.persistence.model.Currency
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

        initOptionFields()

        binding.optionsDefaultCurrencyTextView.setOnClickListener { onCreateCurrencyAlertDialog() }

        return binding.root
    }

    private fun initOptionFields() {
        val amountGoal = optionsViewModel.retrieveAmountGoalFromSharedPreferences(binding.root.context)
        binding.optionsAmountGoalEditText.setText(
            if (amountGoal == SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE)
                EMPTY_STRING
            else
                amountGoal.toString())


        val defaultCurrency = optionsViewModel.retrieveDefaultCurrencyFromSharedPreferences(binding.root.context)
        binding.optionsDefaultCurrencyTextView.text = defaultCurrency
    }

    private fun onCreateCurrencyAlertDialog() {
        val currencies = Currency.values().toList()
        val selectedCurrencyIndex = currencies.indexOf(Currency.valueOf(binding.optionsDefaultCurrencyTextView.text.toString()))

        AlertDialog.Builder(binding.root.context)
            .setTitle("Select Currency")
            .setSingleChoiceItems(
                currencies
                    .map(Currency::name)
                    .toTypedArray(),
                selectedCurrencyIndex
            ) { dialog, which ->
                onSelectCurrency(currencies[which])
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun onSelectCurrency(currency: Currency) {
        binding.optionsDefaultCurrencyTextView.text = currency.name
    }

    private fun onSaveOptions(view: View) {
        val newGoalAmount = if (binding.optionsAmountGoalEditText.text.isEmpty())
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
        else
            binding.optionsAmountGoalEditText.text.toString().toFloat()

        optionsViewModel.updateAmountGoalInSharedPreferences(binding.root.context, newGoalAmount)


        val newDefaultCurrency = binding.optionsDefaultCurrencyTextView.text.toString()

        optionsViewModel.updateDefaultCurrencyInSharedPreferences(binding.root.context, newDefaultCurrency)


        Toast.makeText(context, "Changes Saved!", Toast.LENGTH_SHORT).show()
    }
}