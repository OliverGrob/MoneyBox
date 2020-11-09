package com.ogrob.moneybox.ui.expense

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseAddAndEditViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseAddAndEditBinding
import com.ogrob.moneybox.persistence.model.Category
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
        applyTextWatchers()
        applyOnClickListeners()
        initObservers()

        return binding.root
    }

    private fun initTextViewsAndButtons() {
        if (args.positiveButtonText == resources.getString(R.string.save_button)) {
            binding.expenseCopyTextInputLayout.visibility = View.GONE
            binding.expenseAddEditDeleteButton.visibility = View.VISIBLE
        } else {
            binding.expenseCopyTextInputLayout.visibility = View.VISIBLE
            binding.expenseAddEditDeleteButton.visibility = View.GONE
        }

        binding.expenseAmountEditText.setText(args.expenseAmount)
        binding.expenseDescriptionEditText.setText(args.expenseDescription)
        binding.expenseDatePickerButton.text = args.expenseAdditionDate
        binding.expenseCurrencyButton.text = args.currency
        binding.expenseCategoryButton.text = args.categoryName
        binding.expenseCategoryButton.tag = args.categoryId.toString()
        binding.expenseAddEditPositiveButton.isEnabled = args.expenseId != NEW_EXPENSE_PLACEHOLDER_ID
        binding.expenseAddEditPositiveButton.text = args.positiveButtonText
    }

    private fun configureDescriptionAutoComplete() {
        expenseAddAndEditViewModel.unfilteredExpenses.observe(viewLifecycleOwner) {
            binding.expenseCopyEditText.setAdapter(
                ArrayAdapter(
                    binding.root.context,
                    android.R.layout.simple_dropdown_item_1line,
                    expenseAddAndEditViewModel.getAllExpensesDescription(it))
            )
            binding.expenseCopyEditText.threshold = 1
        }

        expenseAddAndEditViewModel.getAllCategoriesWithExpenses()
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
        binding.expenseAddEditDeleteButton.setOnClickListener {
            it.hideKeyboard()
            showDeleteAlertDialog(it)
        }

        binding.expenseCopyEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedExpenseToCopyFrom = expenseAddAndEditViewModel.getExpenseByDescription(parent.getItemAtPosition(position).toString())

            binding.expenseAmountEditText.setText(selectedExpenseToCopyFrom.amount.toString())
            binding.expenseDescriptionEditText.setText(selectedExpenseToCopyFrom.description)
            binding.expenseCurrencyButton.text = selectedExpenseToCopyFrom.currency.toString()
            expenseAddAndEditViewModel.getExpensesCategory(selectedExpenseToCopyFrom.categoryId)
            binding.expenseCategoryButton.tag = selectedExpenseToCopyFrom.categoryId
        }

        binding.expenseDatePickerButton.setOnClickListener { onPickDate() }
        binding.expenseCurrencyButton.setOnClickListener { onCreateCurrencySelectionAlertDialog() }
        binding.expenseCategoryButton.setOnClickListener { onCreateCategorySelectionAlertDialog() }
    }

    private fun addNewOrEditedExpense(view: View) {
        expenseAddAndEditViewModel.addOrEditExpense(
            args.expenseId,
            binding.expenseAmountEditText.text.toString(),
            binding.expenseDescriptionEditText.text.toString(),
            binding.expenseDatePickerButton.text.toString(),
            binding.expenseCurrencyButton.text.toString(),
            binding.expenseCategoryButton.tag.toString().toLong()
        )

        val additionDate = LocalDate.parse(
            binding.expenseDatePickerButton.text,
            DateTimeFormatter.ISO_LOCAL_DATE
        )

        expenseAddAndEditViewModel.checkIfExchangeRateForDateIsAlreadyAdded(additionDate)

        view.findNavController().navigate(
            ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseSelectedFragment(
                additionDate.year,
                additionDate.monthValue
            )
        )
    }

    private fun showDeleteAlertDialog(view: View) {
        MaterialAlertDialogBuilder(binding.root.context, R.style.AlertDialogTheme)
            .setTitle("Delete expense")
            .setMessage("Are you sure you want to delete \'${args.expenseDescription}\'")
            .setNeutralButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Delete") { dialog, which ->
                deleteExpense(view)
            }
            .show()
    }

    private fun deleteExpense(view: View) {
        expenseAddAndEditViewModel.deleteExpenseById(args.expenseId)

        val additionDate = LocalDate.parse(
            binding.expenseDatePickerButton.text,
            DateTimeFormatter.ISO_LOCAL_DATE
        )
        view.findNavController().navigate(
            ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseSelectedFragment(
                additionDate.year,
                additionDate.monthValue
            )
        )
    }

    private fun onPickDate() {
        val datePickerListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                // because of compatibility with Calendar, months are from 0-11
                val datePicked: LocalDate = LocalDate.of(year, month + 1, dayOfMonth)
                binding.expenseDatePickerButton.text = LocalDateTime.of(datePicked, LocalTime.now()).format(
                    DateTimeFormatter.ISO_LOCAL_DATE).toString()
            }


        val previousDatePicked = LocalDate.parse(binding.expenseDatePickerButton.text, DateTimeFormatter.ISO_LOCAL_DATE)

        DatePickerDialog(
            binding.root.context,
            datePickerListener,
            previousDatePicked.year,
            previousDatePicked.monthValue - 1,
            previousDatePicked.dayOfMonth
        ).show()
    }

    private fun onCreateCurrencySelectionAlertDialog() {
        val currencies = Currency.values().toList()
        val selectedCurrencyIndex = currencies.indexOf(Currency.valueOf(binding.expenseCurrencyButton.text.toString()))

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
        binding.expenseCurrencyButton.text = currency.name
    }

    private fun onCreateCategorySelectionAlertDialog() {
        expenseAddAndEditViewModel.getAllCategories()
    }

    private fun onSelectCategory(category: Category) {
        binding.expenseCategoryButton.text = category.name
        binding.expenseCategoryButton.tag = category.id
    }

    private fun initObservers() {
        expenseAddAndEditViewModel.allCategories.observe(viewLifecycleOwner) {
            val selectedCurrency = it.first { category -> category.id == binding.expenseCategoryButton.tag.toString().toLong() }
            val selectedCurrencyIndex = it.indexOf(selectedCurrency)

            AlertDialog.Builder(binding.root.context)
                .setTitle("Select Currency")
                .setSingleChoiceItems(
                    it
                        .map(Category::name)
                        .toTypedArray(),
                    selectedCurrencyIndex
                ) { dialog, which ->
                    onSelectCategory(it[which])
                    dialog.cancel()
                }
                .create()
                .show()
        }

        expenseAddAndEditViewModel.expensesCategory.observe(viewLifecycleOwner) {
            binding.expenseCategoryButton.text = it.name
        }

        expenseAddAndEditViewModel.isExchangeRateNotAdded.observe(viewLifecycleOwner) {
            if (it) {
                // TODO - check internet access, if there is send request, if not save date to shared pref
                val additionDate = LocalDate.parse(binding.expenseDatePickerButton.text, DateTimeFormatter.ISO_LOCAL_DATE)
                expenseAddAndEditViewModel.getExchangeRatesForDateFromApi(additionDate)
            }
        }
    }

}
