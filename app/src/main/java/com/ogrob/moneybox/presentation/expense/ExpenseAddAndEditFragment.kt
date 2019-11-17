package com.ogrob.moneybox.presentation.expense

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseAddAndEditBinding
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExpenseAddAndEditFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseViewModel::class.java)
    }

    private lateinit var binding: FragmentExpenseAddAndEditBinding

    private lateinit var args: ExpenseAddAndEditFragmentArgs


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_expense_add_and_edit, container, false)

        binding.expenseAddEditPositiveButton.setOnClickListener { addNewOrEditedExpense(it) }
        binding.expenseAddEditCancelButton.setOnClickListener {
            it.findNavController().navigate(ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseFragment())
        }

        this.args = ExpenseAddAndEditFragmentArgs.fromBundle(arguments!!)

        initTextViewsAndButtons()
        configureDescriptionAutoComplete()
        populateCategoryRadioGroup()
        applyTextWatchers()
        applyOnClickListeners()

        return binding.root
    }

    private fun initTextViewsAndButtons() {
        this.binding.expenseAmountEditText.setText(args.expenseAmount)
        this.binding.expenseDescriptionEditText.setText(args.expenseDescription)
        this.binding.expenseDatePickerTextView.text = args.expenseAdditionDate
        this.binding.expenseCategoryCheckboxToggleTextView.text = "Category ▶"
        this.binding.expenseAddEditPositiveButton.isEnabled = args.expenseId != NEW_EXPENSE_PLACEHOLDER_ID
        this.binding.expenseAddEditPositiveButton.text = args.positiveButtonText
    }

    private fun configureDescriptionAutoComplete() {
        this.expenseViewModel.getAllCategoriesWithExpenses().observe(this, Observer {
            this.binding.expenseDescriptionEditText.setAdapter(
                ArrayAdapter(
                    binding.root.context,
                    android.R.layout.simple_dropdown_item_1line,
                    this.expenseViewModel.getAllExpensesDescription(it))
            )
            this.binding.expenseDescriptionEditText.threshold = 1
        })
    }

    private fun populateCategoryRadioGroup() {
        this.expenseViewModel.getAllCategories().observe(this, Observer { categories ->
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
                    binding.expenseAmountEditText.text.isNotBlank() && binding.expenseDescriptionEditText.text.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

        }

        this.binding.expenseAmountEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
        this.binding.expenseDescriptionEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
    }

    private fun applyOnClickListeners() {
        this.binding.expenseDatePickerTextView.setOnClickListener { onPickDate() }
        this.binding.expenseCategoryCheckboxToggleTextView.setOnClickListener {
            if ((it as TextView).text == "Category ▶") {
                it.text = "Category ▼"
                this.binding.categoryScrollView.visibility = View.VISIBLE
            } else {
                it.text = "Category ▶"
                this.binding.categoryScrollView.visibility = View.GONE
            }
        }

        this.binding.expenseDescriptionEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedExpenseToCopyFrom = this.expenseViewModel.getExpenseByDescription(parent.getItemAtPosition(position).toString())

            this.binding.expenseAmountEditText.setText(selectedExpenseToCopyFrom.amount.toString())
            this.binding.categoryRadioGroup.check(selectedExpenseToCopyFrom.categoryId.toInt())
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
                this.binding.categoryRadioGroup.checkedRadioButtonId.toLong())

        view.findNavController().navigate(ExpenseAddAndEditFragmentDirections.actionExpenseAddAndEditFragmentToExpenseFragment())
    }

}
