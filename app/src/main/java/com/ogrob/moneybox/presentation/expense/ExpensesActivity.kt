package com.ogrob.moneybox.presentation.expense

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpensesActivity : AppCompatActivity() {

    private val expenseActivityViewModel: ExpenseActivityViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseActivityViewModel::class.java) }

    private val yearSpinner by lazy { findViewById<Spinner>(R.id.yearSpinner) }
    private val monthSpinner by lazy { findViewById<Spinner>(R.id.monthSpinner) }
    private val expensesRecyclerView by lazy { findViewById<RecyclerView>(R.id.expensesRecycleView) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        this.initSpinnersAndExpensesRecycleView()
    }

    private fun initSpinnersAndExpensesRecycleView() {
        this.expenseActivityViewModel.getAllCategoryWithExpenses().observe(this, Observer {
            val yearsWithExpense: List<Int> = this.expenseActivityViewModel.getYearsWithExpense()

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsWithExpense)
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            yearSpinner.adapter = adapter

            yearSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    initMonthSpinner(yearsWithExpense[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            yearSpinner.setSelection(yearsWithExpense.lastIndex, true)
        })
    }

    private fun initMonthSpinner(yearSelected: Int) {
//        this.expenseActivityViewModel.getAllCategoryWithExpenses().observe(this, Observer {
            val monthsInYearWithExpense: List<Month> = this.expenseActivityViewModel.getMonthsInYearWithExpense(yearSelected)

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthsInYearWithExpense)
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            monthSpinner.adapter = adapter

            monthSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    initExpenseRecycleView(yearSelected, monthsInYearWithExpense[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            monthSpinner.setSelection(monthsInYearWithExpense.lastIndex, true)
//        })
    }

    private fun initExpenseRecycleView(yearSelected: Int, monthSelected: Month) {
        val expenseRecycleViewAdapter = ExpenseRecycleViewAdapter(this, this.expenseActivityViewModel)
        expensesRecyclerView.adapter = expenseRecycleViewAdapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

//        this.expenseActivityViewModel.getAllCategoryWithExpenses().observe(this, Observer {
            val selectedExpenses = expenseActivityViewModel.getExpensesForSelectedMonthInSelectedYear(yearSelected, monthSelected)
            expenseRecycleViewAdapter.setExpenses(selectedExpenses)
            val currentTotal = expenseActivityViewModel.getTotalMoneySpent(selectedExpenses)
            findViewById<TextView>(R.id.totalMoneySpentTextView).text =
                if (currentTotal == currentTotal.toInt().toDouble()) currentTotal.toInt().toString() else currentTotal.toString()
//        })
    }

    fun onAddNewExpense(view: View) {
        val alertDialogView = layoutInflater.inflate(R.layout.new_expense_alert_dialog, null)
        val newExpenseAmountEditText: EditText = alertDialogView.findViewById(R.id.newExpenseAmountEditText)
        val newExpenseDescriptionEditText: AutoCompleteTextView = alertDialogView.findViewById(R.id.newExpenseDescriptionEditText)
        val newExpenseDatePickerTextView: TextView = alertDialogView.findViewById(R.id.datePickerTextView)
        newExpenseDatePickerTextView.text = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
        val newExpenseCategoryCheckboxToggleTextView: TextView = alertDialogView.findViewById(R.id.categoryCheckboxToggleTextView)
        val radioGroup: RadioGroup = alertDialogView.findViewById(R.id.categoryRadioGroup)
        val scrollView: ScrollView = alertDialogView.findViewById(R.id.categoryScrollView)


        newExpenseDescriptionEditText.setAdapter(ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            this.expenseActivityViewModel.getAllExpensesDescription()))
        newExpenseDescriptionEditText.threshold = 1


        val newExpenseAlertDialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("New Expense")
            .setView(alertDialogView)
            .setPositiveButton("Add Expense") { _, _ ->
                    expenseActivityViewModel.addNewExpense(
                        newExpenseAmountEditText.text.toString(),
                        newExpenseDescriptionEditText.text.toString(),
                        newExpenseDatePickerTextView.text.toString(),
                        if (radioGroup.checkedRadioButtonId != -1) radioGroup.checkedRadioButtonId else 1)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
        newExpenseAlertDialog.show()

        newExpenseAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        val newExpenseEditTextTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                newExpenseAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    newExpenseAmountEditText.text.isNotBlank() && newExpenseDescriptionEditText.text.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

        }

        populateRadioGroupWithCategories(radioGroup)

        newExpenseAmountEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
        newExpenseDescriptionEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
        newExpenseDatePickerTextView.setOnClickListener { onPickDate(it) }
        newExpenseCategoryCheckboxToggleTextView.setOnClickListener {
            if ((it as TextView).text == "Category ▶") {
                it.text = "Category ▼"
                scrollView.visibility = View.VISIBLE
            } else {
                it.text = "Category ▶"
                scrollView.visibility = View.GONE
            }
        }
    }

    private fun onPickDate(view: View) {
        val datePickerTextView: TextView = view as TextView


        val datePickerListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                // because of compatibility with Calendar, months are from 0-11
                val datePicked: LocalDate = LocalDate.of(year, month + 1, dayOfMonth)
                datePickerTextView.text = LocalDateTime.of(datePicked, LocalTime.now()).format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
            }


        val previousDatePicked = LocalDate.parse(datePickerTextView.text, DateTimeFormatter.ISO_LOCAL_DATE)

        DatePickerDialog(
            this,
            datePickerListener,
            previousDatePicked.year,
            previousDatePicked.monthValue - 1,
            previousDatePicked.dayOfMonth
        ).show()
    }

    private fun populateRadioGroupWithCategories(radioGroup: RadioGroup) {
        this.expenseActivityViewModel.getAllCategories().observe(this, Observer { categories ->
            categories.stream().forEach { category ->
                val radioButton = RadioButton(this)
                radioButton.id = category.id
                radioButton.text = category.name
                radioButton.isChecked = false
                radioButton.textSize = 15f
                radioGroup.addView(radioButton)
            }
        })
    }

    fun onAddNewCategory(view: View) {
        val alertDialogView = layoutInflater.inflate(R.layout.new_category_alert_dialog, null)
        val newCategoryEditText: EditText = alertDialogView.findViewById(R.id.newCategoryEditText)

        val newCategoryAlertDialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(alertDialogView)
            .setPositiveButton("Add Category") { _, _ ->
                expenseActivityViewModel.addNewCategory(newCategoryEditText.text.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
        newCategoryAlertDialog.show()

        newCategoryAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        val newExpenseEditTextTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                newCategoryAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    newCategoryEditText.text.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

        }

        newCategoryEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
    }

}
