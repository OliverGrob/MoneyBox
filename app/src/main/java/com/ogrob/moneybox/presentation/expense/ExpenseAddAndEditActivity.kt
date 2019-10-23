package com.ogrob.moneybox.presentation.expense

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseActivityViewModel
import com.ogrob.moneybox.databinding.ActivityExpenseAddAndEditBinding
import com.ogrob.moneybox.utils.NO_CATEGORY_DISPLAY_TEXT
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExpenseAddAndEditActivity : AppCompatActivity() {

    private val expenseActivityViewModel: ExpenseActivityViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseActivityViewModel::class.java) }

    private lateinit var binding: ActivityExpenseAddAndEditBinding

    private val expenseId by lazy { intent.getIntExtra("expenseId", -1) }
    private val categoryId by lazy { intent.getIntExtra("categoryId", 1) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_expense_add_and_edit)

        title = intent.getStringExtra("activityTitle")

        this.initTextViewsAndButtons()
        this.configureDescriptionAutoComplete()
        this.populateCategoryRadioGroup()
        this.applyTextWatcherAndOnClickListeners()
    }

    private fun initTextViewsAndButtons() {
        this.binding.expenseAmountEditText.setText(intent.getStringExtra("expenseAmount"))
        this.binding.expenseDescriptionEditText.setText(intent.getStringExtra("expenseDescription"))
        this.binding.expenseDatePickerTextView.text = intent.getStringExtra("expenseAdditionDate")
        this.binding.expenseCategoryCheckboxToggleTextView.text = "Category ▶"
        this.binding.expenseAddEditPositiveButton.isEnabled = this.expenseId != -1
        this.binding.expenseAddEditPositiveButton.text = intent.getStringExtra("positiveButtonText")
    }

    private fun configureDescriptionAutoComplete() {
        this.expenseActivityViewModel.getAllCategoriesWithExpenses().observe(this, Observer {
            this.binding.expenseDescriptionEditText.setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    this.expenseActivityViewModel.getAllExpensesDescription(it))
            )
            this.binding.expenseDescriptionEditText.threshold = 1
        })
    }

    private fun populateCategoryRadioGroup() {
        this.expenseActivityViewModel.getAllCategories().observe(this, Observer { categories ->
            categories
                .forEach { category ->
                    val radioButton = RadioButton(this)
                    radioButton.id = category.id
                    radioButton.text = if (category.id == NO_CATEGORY_ID) NO_CATEGORY_DISPLAY_TEXT else category.name
                    radioButton.isChecked = category.id == this.categoryId
                    radioButton.textSize = 15f
                    this.binding.categoryRadioGroup.addView(radioButton)
                }
        })
    }

    private fun applyTextWatcherAndOnClickListeners() {
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
            val selectedExpenseToCopyFrom = this.expenseActivityViewModel.getExpenseByDescription(parent.getItemAtPosition(position).toString())

            this.binding.expenseAmountEditText.setText(selectedExpenseToCopyFrom.amount.toString())
            this.binding.categoryRadioGroup.check(selectedExpenseToCopyFrom.categoryId)
        }
    }

    private fun onPickDate() {
        val datePickerListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                // because of compatibility with Calendar, months are from 0-11
                val datePicked: LocalDate = LocalDate.of(year, month + 1, dayOfMonth)
                this.binding.expenseDatePickerTextView.text = LocalDateTime.of(datePicked, LocalTime.now()).format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
            }


        val previousDatePicked = LocalDate.parse(this.binding.expenseDatePickerTextView.text, DateTimeFormatter.ISO_LOCAL_DATE)

        DatePickerDialog(
            this,
            datePickerListener,
            previousDatePicked.year,
            previousDatePicked.monthValue - 1,
            previousDatePicked.dayOfMonth
        ).show()
    }

    fun onPositive(view: View) {
        if (this.expenseId == -1)
            this.expenseActivityViewModel.addNewExpense(
                this.binding.expenseAmountEditText.text.toString(),
                this.binding.expenseDescriptionEditText.text.toString(),
                this.binding.expenseDatePickerTextView.text.toString(),
                this.binding.categoryRadioGroup.checkedRadioButtonId)

        else
            this.expenseActivityViewModel.updateExpense(
                this.expenseId,
                this.binding.expenseAmountEditText.text.toString(),
                this.binding.expenseDescriptionEditText.text.toString(),
                this.binding.expenseDatePickerTextView.text.toString(),
                this.binding.categoryRadioGroup.checkedRadioButtonId)

        finish()
    }

    fun onCancel(view: View) {
        finish()
    }

}
