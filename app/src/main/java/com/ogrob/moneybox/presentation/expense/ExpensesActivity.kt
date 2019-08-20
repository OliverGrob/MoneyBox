package com.ogrob.moneybox.presentation.expense

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.presentation.ExpenseActivityViewModel
import com.ogrob.moneybox.presentation.category.CategoriesActivity
import com.ogrob.moneybox.utils.EMPTY_STRING
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpensesActivity : AppCompatActivity() {

    private val expenseActivityViewModel: ExpenseActivityViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseActivityViewModel::class.java) }

    private val yearSpinner by lazy { findViewById<Spinner>(R.id.yearSpinner) }
    private val monthSpinner by lazy { findViewById<Spinner>(R.id.monthSpinner) }
    private val expensesRecyclerView by lazy { findViewById<RecyclerView>(R.id.expensesRecyclerView) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        this.populateSpinnersAndExpensesRecyclerView()
    }

    private fun populateSpinnersAndExpensesRecyclerView() {
        this.expenseActivityViewModel.getAllCategoriesWithExpenses().observe(this, Observer {
            val yearsWithExpense: List<Int> = this.expenseActivityViewModel.getYearsWithExpense()

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsWithExpense)
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            yearSpinner.adapter = adapter

            yearSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    populateMonthSpinner(yearsWithExpense[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            yearSpinner.setSelection(yearsWithExpense.lastIndex, true)
        })
    }

    private fun populateMonthSpinner(yearSelected: Int) {
        val monthsInYearWithExpense: List<Month> = this.expenseActivityViewModel.getMonthsInYearWithExpense(yearSelected)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthsInYearWithExpense)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        monthSpinner.adapter = adapter

        monthSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                populateExpenseRecyclerView(yearSelected, monthsInYearWithExpense[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        monthSpinner.setSelection(monthsInYearWithExpense.lastIndex, true)
    }

    private fun populateExpenseRecyclerView(yearSelected: Int, monthSelected: Month) {
        val expenseRecyclerViewAdapter = ExpenseRecyclerViewAdapter(this, this.expenseActivityViewModel)
        expensesRecyclerView.adapter = expenseRecyclerViewAdapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

        val selectedExpenses = expenseActivityViewModel.getExpensesForSelectedMonthInSelectedYear(yearSelected, monthSelected)
        expenseRecyclerViewAdapter.setExpenses(selectedExpenses)
        findViewById<TextView>(R.id.totalMoneySpentTextView).text = "Total: ${expenseActivityViewModel.getTotalMoneySpentFormatted(selectedExpenses)}"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.expense_activity_options, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem) =
        when (menuItem.itemId) {
            R.id.manageCategories -> {
                startActivity(Intent(this, CategoriesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }

    fun onAddNewExpense(view: View) {
        val intent = Intent(this, ExpenseAddAndEditActivity::class.java)
        intent.putExtra("activityTitle", "New Expense")
        intent.putExtra("expenseAmount", EMPTY_STRING)
        intent.putExtra("expenseDescription", EMPTY_STRING)
        intent.putExtra("expenseAdditionDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString())
        intent.putExtra("positiveButtonText", "Add Expense")
        startActivity(intent)
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
