package com.ogrob.moneybox.ui.expense

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseAddAndEditViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseAddAndEditBinding
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.hideKeyboard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExpenseAddAndEditFragment : BaseFragment() {

    private val expenseAddAndEditViewModel: ExpenseAddAndEditViewModel by viewModels()

    private lateinit var binding: FragmentExpenseAddAndEditBinding

    private lateinit var args: ExpenseAddAndEditFragmentArgs


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentExpenseAddAndEditBinding.inflate(inflater)

        args = ExpenseAddAndEditFragmentArgs.fromBundle(requireArguments())

        initTextViewsAndButtons()
        configureDescriptionAutoComplete()
        populateCategoryRadioGroup()
        applyTextWatchers()
        applyOnClickListeners()

        return binding.root
    }

    private fun initTextViewsAndButtons() {
        if (args.positiveButtonText == resources.getString(R.string.save_button))
            binding.expenseCopyTextInputLayout.visibility = View.GONE
        else
            binding.expenseCopyTextInputLayout.visibility = View.VISIBLE

        binding.expenseAmountEditText.setText(args.expenseAmount)
        binding.expenseDescriptionEditText.setText(args.expenseDescription)
        binding.expenseDatePickerTextView.text = args.expenseAdditionDate
        binding.expenseCurrencyTextView.text = args.currency
        binding.expenseAddEditPositiveButton.isEnabled = args.expenseId != NEW_EXPENSE_PLACEHOLDER_ID
        binding.expenseAddEditPositiveButton.text = args.positiveButtonText
    }

    private fun configureDescriptionAutoComplete() {
        expenseAddAndEditViewModel.getAllCategoriesWithExpenses_OLD().observe(viewLifecycleOwner) {
            binding.expenseCopyEditText.setAdapter(
                ArrayAdapter(
                    binding.root.context,
                    android.R.layout.simple_dropdown_item_1line,
                    expenseAddAndEditViewModel.getAllExpensesDescription(it))
            )
            binding.expenseCopyEditText.threshold = 1
        }
    }

    private fun populateCategoryRadioGroup() {
        expenseAddAndEditViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            categories
                .forEach { category ->
                    val radioButton = RadioButton(context)
                    radioButton.id = category.id.toInt()
                    radioButton.text = category.name
                    radioButton.isChecked = category.id == this.args.categoryId
                    radioButton.textSize = 15f
                    binding.categoryRadioGroup.addView(radioButton)
                }
        }

        expenseAddAndEditViewModel.getAllCategories()
    }

    private fun applyTextWatchers() {
        val newExpenseEditTextTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                binding.expenseAddEditPositiveButton.isEnabled =
                    !binding.expenseAmountEditText.text.isNullOrEmpty()
                            && binding.expenseAmountEditText.text.toString().toDoubleOrNull() != null
                            && !binding.expenseDescriptionEditText.text.isNullOrBlank()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

        }

        binding.expenseAmountEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
        binding.expenseDescriptionEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
    }

    private fun applyOnClickListeners() {
        binding.root.setOnClickListener { it.hideKeyboard() }
        binding.expenseAddEditPositiveButton.setOnClickListener {
            it.hideKeyboard()
            addNewOrEditedExpense(it)
        }
        binding.expenseAddEditCancelButton.setOnClickListener {
            val additionDate = LocalDate.parse(args.expenseAdditionDate, DateTimeFormatter.ISO_LOCAL_DATE)
            it.hideKeyboard()
            it.findNavController().navigate(
                ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseSelectedFragment(additionDate.year, additionDate.monthValue))
        }

        binding.expenseCopyEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedExpenseToCopyFrom = expenseAddAndEditViewModel.getExpenseByDescription(parent.getItemAtPosition(position).toString())

            binding.expenseAmountEditText.setText(selectedExpenseToCopyFrom.amount.toString())
            binding.expenseDescriptionEditText.setText(selectedExpenseToCopyFrom.description)
            binding.categoryRadioGroup.check(selectedExpenseToCopyFrom.categoryId.toInt())
        }

        binding.expenseDatePickerTextView.setOnClickListener { onPickDate() }
        binding.expenseCurrencyTextView.setOnClickListener { onCreateCurrencyAlertDialog() }
        binding.expenseCategoryLinearLayout.setOnClickListener {
            if (expenseAddAndEditViewModel.isCategoryDropdownOpen()) {
                expenseAddAndEditViewModel.closeCategoryDropdown()
                binding.expenseCategoryCheckboxToggleTextView.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_expand_more_white_24dp, null)
                binding.categoryScrollView.visibility = View.GONE
            } else {
                expenseAddAndEditViewModel.openCategoryDropdown()
                binding.expenseCategoryCheckboxToggleTextView.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_expand_less_white_24dp, null)
                binding.categoryScrollView.visibility = View.VISIBLE
            }
        }
    }

    private fun onPickDate() {
        val datePickerListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                // because of compatibility with Calendar, months are from 0-11
                val datePicked: LocalDate = LocalDate.of(year, month + 1, dayOfMonth)
                binding.expenseDatePickerTextView.text = LocalDateTime.of(datePicked, LocalTime.now()).format(
                    DateTimeFormatter.ISO_LOCAL_DATE).toString()
            }


        val previousDatePicked = LocalDate.parse(binding.expenseDatePickerTextView.text, DateTimeFormatter.ISO_LOCAL_DATE)

        DatePickerDialog(
            binding.root.context,
            datePickerListener,
            previousDatePicked.year,
            previousDatePicked.monthValue - 1,
            previousDatePicked.dayOfMonth
        ).show()
    }

    private fun onCreateCurrencyAlertDialog() {
        val currencies = Currency.values().toList()
        val selectedCurrencyIndex = currencies.indexOf(Currency.valueOf(binding.expenseCurrencyTextView.text.toString()))

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
        binding.expenseCurrencyTextView.text = currency.name
    }

    private fun addNewOrEditedExpense(view: View) {
        expenseAddAndEditViewModel.addOrEditExpense(
            args.expenseId,
            binding.expenseAmountEditText.text.toString(),
            binding.expenseDescriptionEditText.text.toString(),
            binding.expenseDatePickerTextView.text.toString(),
            binding.expenseCurrencyTextView.text.toString(),
            binding.categoryRadioGroup.checkedRadioButtonId.toLong()
        )

        val additionDate = LocalDate.parse(
            binding.expenseDatePickerTextView.text,
            DateTimeFormatter.ISO_LOCAL_DATE
        )
        view.findNavController().navigate(
            ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseSelectedFragment(
                additionDate.year,
                additionDate.monthValue
            )
        )
    }

}
