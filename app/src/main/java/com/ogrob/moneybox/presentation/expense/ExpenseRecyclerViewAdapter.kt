package com.ogrob.moneybox.presentation.expense

import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NO_CATEGORY_DISPLAY_TEXT
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors


class ExpenseRecyclerViewAdapter(private val context: Context,
                                 private val expenseActivityViewModel: ExpenseActivityViewModel)
    : RecyclerView.Adapter<ExpenseRecyclerViewAdapter.ExpenseViewHolder>() {

    private var categories: List<Category>? = null
    private var expenses: List<Expense>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.expense_list_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (this.expenses.isNullOrEmpty()) 0 else this.expenses!!.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        if (!this.expenses.isNullOrEmpty()) this.expenses!![position].also { expense ->
            holder.expenseAdditionDateTextView.text = expense.additionDate.dayOfMonth.toString()
            holder.expenseAmountTextView.text = this.expenseActivityViewModel.formatMoneySpent(expense.amount)
            holder.expenseDescriptionTextView.text = expense.description
            holder.expenseCategoryTextView.text = this.findCategoryName(expense.categoryId)

            holder.expenseOptionsTextView.setOnClickListener {
                val popup = PopupMenu(this.context, holder.expenseOptionsTextView)
                popup.inflate(R.menu.expense_list_item_options)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.editExpense -> {
                            createExpenseEditAlertDialog(expense)
                            true
                        }
                        R.id.deleteExpense -> {
                            createExpenseDeleteAlertDialog(expense)
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
            }
        }
    }

    private fun createExpenseEditAlertDialog(expense: Expense) {
        val alertDialogView = LayoutInflater.from(this.context).inflate(R.layout.new_expense_alert_dialog, null)
        val newExpenseAmountEditText: EditText = alertDialogView.findViewById(R.id.newExpenseAmountEditText)
        newExpenseAmountEditText.setText(expense.amount.toString())
        val newExpenseDescriptionEditText: AutoCompleteTextView = alertDialogView.findViewById(R.id.newExpenseDescriptionEditText)
        newExpenseDescriptionEditText.setText(expense.description)
        val newExpenseDatePickerTextView: TextView = alertDialogView.findViewById(R.id.datePickerTextView)
        newExpenseDatePickerTextView.text = expense.additionDate.format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
        val newExpenseCategoryCheckboxToggleTextView: TextView = alertDialogView.findViewById(R.id.categoryCheckboxToggleTextView)
        val radioGroup: RadioGroup = alertDialogView.findViewById(R.id.categoryRadioGroup)
        val scrollView: ScrollView = alertDialogView.findViewById(R.id.categoryScrollView)


        newExpenseDescriptionEditText.setAdapter(
            ArrayAdapter(
                this.context,
                android.R.layout.simple_dropdown_item_1line,
                this.expenseActivityViewModel.getAllExpensesDescription())
        )
        newExpenseDescriptionEditText.threshold = 1


        populateRadioGroupWithCategories(radioGroup, expense.categoryId)


        val newExpenseAlertDialog: AlertDialog = AlertDialog.Builder(this.context)
            .setTitle("Edit Expense")
            .setView(alertDialogView)
            .setPositiveButton("Save") { _, _ ->
                expenseActivityViewModel.updateExpense(
                    expense.id,
                    newExpenseAmountEditText.text.toString(),
                    newExpenseDescriptionEditText.text.toString(),
                    newExpenseDatePickerTextView.text.toString(),
                    radioGroup.checkedRadioButtonId)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
        newExpenseAlertDialog.show()

        newExpenseAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true

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

        newExpenseDescriptionEditText.setOnItemClickListener { parent, _, position, _ ->
            val selectedExpenseToCopyFrom = this.expenseActivityViewModel.getExpenseByDescription(parent.getItemAtPosition(position).toString())

            newExpenseAmountEditText.setText(selectedExpenseToCopyFrom.amount.toString())
            radioGroup.check(selectedExpenseToCopyFrom.categoryId)
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
            this.context,
            datePickerListener,
            previousDatePicked.year,
            previousDatePicked.monthValue - 1,
            previousDatePicked.dayOfMonth
        ).show()
    }

    private fun populateRadioGroupWithCategories(radioGroup: RadioGroup, categoryId: Int) {
        this.categories!!.stream().forEach { category ->
            val radioButton = RadioButton(this.context)
            radioButton.id = category.id
            radioButton.text = if (category.id == NO_CATEGORY_ID) NO_CATEGORY_DISPLAY_TEXT else category.name
            radioButton.isChecked = categoryId == category.id
            radioButton.textSize = 15f
            radioGroup.addView(radioButton)
        }
    }

    private fun createExpenseDeleteAlertDialog(expense: Expense) {
        AlertDialog.Builder(this.context)
            .setTitle("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ -> expenseActivityViewModel.deleteExpense(expense) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun findCategoryName(categoryId: Int): String {
        return this.categories!!
            .stream()
            .filter { category -> category.id == categoryId }
            .map(Category::name)
            .findAny()
            .orElse(EMPTY_STRING)
    }

    fun setExpenses(categoryWithExpenses: List<CategoryWithExpenses>) {
        this.categories = categoryWithExpenses
            .stream()
            .map(CategoryWithExpenses::category)
            .collect(Collectors.toList())
        this.expenses = categoryWithExpenses
            .stream()
            .flatMap { currentCategoryWithExpenses -> currentCategoryWithExpenses.expenses.stream() }
            .sorted { expense1, expense2 -> expense1.additionDate.compareTo(expense2.additionDate) }
            .collect(Collectors.toList())
        this.notifyDataSetChanged()
    }


    class ExpenseViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        var expenseAdditionDateTextView: TextView = view.findViewById(R.id.expenseAdditionDateTextView)
        var expenseAmountTextView: TextView = view.findViewById(R.id.expenseAmountTextView)
        var expenseDescriptionTextView: TextView = view.findViewById(R.id.expenseDescriptionTextView)
        var expenseCategoryTextView: TextView = view.findViewById(R.id.expenseCategoryTextView)
        var expenseOptionsTextView: TextView = view.findViewById(R.id.expenseOptionsTextView)

    }
}