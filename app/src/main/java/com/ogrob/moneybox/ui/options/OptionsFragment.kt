package com.ogrob.moneybox.ui.options

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ogrob.moneybox.data.viewmodel.OptionsViewModel
import com.ogrob.moneybox.databinding.FragmentOptionsBinding
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
import com.ogrob.moneybox.utils.hideKeyboard
import java.time.Month

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
            onSaveOptions()
        }


        binding.queryButton.setOnClickListener { startQuery() }


        initOptionFields()

        binding.optionsDefaultCurrencyTextView.setOnClickListener { onCreateCurrencyAlertDialog() }

        return binding.root
    }

    private fun startQuery() {
        optionsViewModel.buttonEnabled.observe(viewLifecycleOwner, Observer {
            binding.queryButton.isEnabled = it
        })

        val startTime = System.currentTimeMillis()
        optionsViewModel.size.observe(viewLifecycleOwner, Observer {
            val endTime = System.currentTimeMillis()
            binding.optionsDbSpeedTestTextView.text = "$it expense query time:  ${endTime - startTime}ms"
        })
        optionsViewModel.startSpeedQuery(2018, Month.OCTOBER)
    }

    private fun initOptionFields() {
        initAmountGoal()
        initDefaultCurrency()
    }

    private fun initAmountGoal() {
        val amountGoal = optionsViewModel.retrieveAmountGoalFromSharedPreferences(binding.root.context)
        binding.optionsAmountGoalEditText.setText(
            if (amountGoal == SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE)
                EMPTY_STRING
            else
                amountGoal.toString())
    }

    private fun initDefaultCurrency() {
        val defaultCurrency = optionsViewModel.retrieveDefaultCurrencyFromSharedPreferences(binding.root.context)
        binding.optionsDefaultCurrencyTextView.text = defaultCurrency
    }

    private fun onCreateCurrencyAlertDialog() {
        val currencies = Currency.values().toList()
        val currencyNamesArray = currencies
            .map(Currency::name)
            .toTypedArray()
        val selectedCurrencyIndex = currencies.indexOf(Currency.valueOf(binding.optionsDefaultCurrencyTextView.text.toString()))

        AlertDialog.Builder(binding.root.context)
            .setTitle("Select Currency")
            .setSingleChoiceItems(
                currencyNamesArray,
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

    private fun onSaveOptions() {
        saveAmountGoal()
        saveDefaultCurrency()

        Toast.makeText(context, "Changes Saved!", Toast.LENGTH_SHORT).show()
    }

    private fun saveAmountGoal() {
        val newGoalAmount = if (binding.optionsAmountGoalEditText.text.isEmpty())
            SHARED_PREFERENCES_AMOUNT_PER_MONTH_GOAL_DEFAULT_VALUE
        else
            binding.optionsAmountGoalEditText.text.toString().toFloat()

        optionsViewModel.updateAmountGoalInSharedPreferences(binding.root.context, newGoalAmount)
    }

    private fun saveDefaultCurrency() {
        val newDefaultCurrency = binding.optionsDefaultCurrencyTextView.text.toString()

        optionsViewModel.updateDefaultCurrencyInSharedPreferences(binding.root.context, newDefaultCurrency)
    }
}