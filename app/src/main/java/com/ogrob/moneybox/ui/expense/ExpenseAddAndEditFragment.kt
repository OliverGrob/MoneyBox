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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseAddAndEditBinding
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.hideKeyboard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExpenseAddAndEditFragment : BaseFragment() {

    private val expenseViewModel: ExpenseViewModel by viewModels()

    private lateinit var binding: FragmentExpenseAddAndEditBinding

    private lateinit var args: ExpenseAddAndEditFragmentArgs


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentExpenseAddAndEditBinding.inflate(inflater)

        args = ExpenseAddAndEditFragmentArgs.fromBundle(requireArguments())

        binding.root.setOnClickListener { it.hideKeyboard() }
        binding.expenseAddEditPositiveButton.setOnClickListener {
            it.hideKeyboard()
            addNewOrEditedExpense(it)
        }
        binding.expenseAddEditCancelButton.setOnClickListener {
            val additionDate = LocalDate.parse(args.expenseAdditionDate, DateTimeFormatter.ISO_LOCAL_DATE)
            it.hideKeyboard()
            it.findNavController().navigate(
                ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseDayFragment(additionDate.year, additionDate.monthValue))
        }

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
        binding.expenseAddEditPositiveButton.isEnabled = args.expenseId != NEW_EXPENSE_PLACEHOLDER_ID
        binding.expenseAddEditPositiveButton.text = args.positiveButtonText
    }

    private fun configureDescriptionAutoComplete() {
        this.expenseViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            this.binding.expenseCopyEditText.setAdapter(
                ArrayAdapter(
                    binding.root.context,
                    android.R.layout.simple_dropdown_item_1line,
                    this.expenseViewModel.getAllExpensesDescription(it))
            )
            this.binding.expenseCopyEditText.threshold = 1
        })
    }

    private fun populateCategoryRadioGroup() {
        this.expenseViewModel.getAllCategories().observe(viewLifecycleOwner, Observer { categories ->
            categories
                .forEach { category ->
                    val radioButton = RadioButton(context)
                    radioButton.id = category.id.toInt()
                    radioButton.text = category.name
                    radioButton.isChecked = category.id == this.args.categoryId
                    radioButton.textSize = 15f
                    this.binding.categoryRadioGroup.addView(radioButton)
                }
        })
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

        this.binding.expenseAmountEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
        this.binding.expenseDescriptionEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
    }

    private fun applyOnClickListeners() {
        this.binding.expenseCopyEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedExpenseToCopyFrom = this.expenseViewModel.getExpenseByDescription(parent.getItemAtPosition(position).toString())

            this.binding.expenseAmountEditText.setText(selectedExpenseToCopyFrom.amount.toString())
            this.binding.expenseDescriptionEditText.setText(selectedExpenseToCopyFrom.description)
            this.binding.categoryRadioGroup.check(selectedExpenseToCopyFrom.categoryId.toInt())
        }

        this.binding.expenseDatePickerTextView.setOnClickListener { onPickDate() }
        this.binding.expenseCategoryLinearLayout.setOnClickListener {
            if (expenseViewModel.isCategoryDropdownOpen()) {
                expenseViewModel.closeCategoryDropdown()
                binding.expenseCategoryCheckboxToggleTextView.background = resources.getDrawable(R.drawable.ic_expand_more_white_24dp, null)
                this.binding.categoryScrollView.visibility = View.GONE
            } else {
                expenseViewModel.openCategoryDropdown()
                binding.expenseCategoryCheckboxToggleTextView.background = resources.getDrawable(R.drawable.ic_expand_less_white_24dp, null)
                this.binding.categoryScrollView.visibility = View.VISIBLE
            }
        }
    }

    private fun onPickDate() {
        val datePickerListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                // because of compatibility with Calendar, months are from 0-11
                val datePicked: LocalDate = LocalDate.of(year, month + 1, dayOfMonth)
                this.binding.expenseDatePickerTextView.text = LocalDateTime.of(datePicked, LocalTime.now()).format(
                    DateTimeFormatter.ISO_LOCAL_DATE).toString()
            }


        val previousDatePicked = LocalDate.parse(this.binding.expenseDatePickerTextView.text, DateTimeFormatter.ISO_LOCAL_DATE)

        DatePickerDialog(
            binding.root.context,
            datePickerListener,
            previousDatePicked.year,
            previousDatePicked.monthValue - 1,
            previousDatePicked.dayOfMonth
        ).show()
    }

    private fun addNewOrEditedExpense(view: View) {
        this.expenseViewModel.addOrEditExpense(
            this.args.expenseId,
            this.binding.expenseAmountEditText.text.toString(),
            this.binding.expenseDescriptionEditText.text.toString(),
            this.binding.expenseDatePickerTextView.text.toString(),
            this.binding.categoryRadioGroup.checkedRadioButtonId.toLong()
        )

        val additionDate = LocalDate.parse(
            binding.expenseDatePickerTextView.text,
            DateTimeFormatter.ISO_LOCAL_DATE
        )
        view.findNavController().navigate(
            ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseDayFragment(
                additionDate.year,
                additionDate.monthValue
            )
        )
    }

}
