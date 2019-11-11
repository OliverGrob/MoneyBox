package com.ogrob.moneybox.presentation.expense

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseActivityViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseBinding
import com.ogrob.moneybox.databinding.NewCategoryAlertDialogBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpenseFragment : Fragment() {

    private val expenseActivityViewModel: ExpenseActivityViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseActivityViewModel::class.java)
    }

    private lateinit var binding: FragmentExpenseBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_expense, container, false)

        binding.addExpenseButton.setOnClickListener { onAddNewExpense(it) }
        binding.addCategoryButton.setOnClickListener { onAddNewCategory(it) }

        populateSpinnersAndExpensesRecyclerView()

        return binding.root
    }

    private fun populateSpinnersAndExpensesRecyclerView() {
        expenseActivityViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            val yearsWithExpense: List<Int> = expenseActivityViewModel.getYearsWithExpense()
            val categories: List<Category> = it.map(CategoryWithExpenses::category)

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, yearsWithExpense)
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            binding.yearSpinner.adapter = adapter

            binding.yearSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    populateMonthSpinner(yearsWithExpense[position], categories)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            binding.yearSpinner.setSelection(yearsWithExpense.lastIndex, true)
        })
    }

    private fun populateMonthSpinner(yearSelected: Int,
                                     categories: List<Category>) {
        val monthsInYearWithExpense: List<Month> = expenseActivityViewModel.getMonthsInYearWithExpense(yearSelected)

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, monthsInYearWithExpense)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        binding.monthSpinner.adapter = adapter

        binding.monthSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                populateExpenseRecyclerView(yearSelected, monthsInYearWithExpense[position], categories)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding.monthSpinner.setSelection(monthsInYearWithExpense.lastIndex, true)
    }

    private fun populateExpenseRecyclerView(yearSelected: Int,
                                            monthSelected: Month,
                                            categories: List<Category>) {
        val expenseRecyclerViewAdapter = ExpenseRecyclerViewAdapter(expenseActivityViewModel, categories)
        binding.expensesRecyclerView.adapter = expenseRecyclerViewAdapter
        binding.expensesRecyclerView.layoutManager = LinearLayoutManager(context)

        val selectedExpenses = expenseActivityViewModel.getExpensesForSelectedMonthInSelectedYear(yearSelected, monthSelected)
        expenseRecyclerViewAdapter.submitList(selectedExpenses)
        binding.totalMoneySpentTextView.text = "Total: ${expenseActivityViewModel.getTotalMoneySpentFormatted(selectedExpenses)}"
    }

    private fun onAddNewExpense(view: View) {
        view.findNavController().navigate(ExpenseFragmentDirections.actionExpenseFragmentToExpenseAddAndEditFragment(
            EMPTY_STRING,
            EMPTY_STRING,
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
            NEW_EXPENSE_PLACEHOLDER_ID,
            NO_CATEGORY_ID,
            "Add Expense"
        ))
    }

    private fun onAddNewCategory(view: View) {
        val alertDialogBinding: NewCategoryAlertDialogBinding = DataBindingUtil
            .inflate(LayoutInflater.from(context), R.layout.new_category_alert_dialog, null, false)
        val newCategoryEditText: EditText = alertDialogBinding.newCategoryEditText

        val newCategoryAlertDialog: AlertDialog = AlertDialog.Builder(context!!)
            .setTitle("New Category")
            .setView(alertDialogBinding.root)
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
