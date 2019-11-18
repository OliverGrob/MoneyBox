package com.ogrob.moneybox.presentation.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.utils.*
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpenseFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseViewModel::class.java)
    }

    private lateinit var binding: FragmentExpenseBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_expense, container, false)

        binding.addExpenseButton.setOnClickListener { onAddNewExpense(it) }

        populateSpinnersAndExpensesRecyclerView()

        return binding.root
    }

    private fun populateSpinnersAndExpensesRecyclerView() {
        expenseViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            val yearsWithExpense: List<Int> = expenseViewModel.getYearsWithExpense()
            val categories: List<Category> = it.map(CategoryWithExpenses::category)

            val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, yearsWithExpense)
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            binding.yearSpinner.adapter = adapter

            binding.yearSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    populateMonthSpinner(yearsWithExpense[position], categories)
                    expenseViewModel.updatePreferenceInSharedPreferences(
                        binding.root.context,
                        SHARED_PREFERENCES_SELECTED_YEAR_KEY,
                        yearsWithExpense[position].toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            val selectedYear = expenseViewModel.retrievePreferenceFromSharedPreferences(binding.root.context, SHARED_PREFERENCES_SELECTED_YEAR_KEY)
            binding.yearSpinner.setSelection(
                if (selectedYear.isBlank()) yearsWithExpense.lastIndex else yearsWithExpense.indexOf(selectedYear.toInt()),
                true)
        })
    }

    private fun populateMonthSpinner(yearSelected: Int,
                                     categories: List<Category>) {
        val monthsInYearWithExpense: List<Month> = expenseViewModel.getMonthsInYearWithExpense(yearSelected)

        val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, monthsInYearWithExpense)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        binding.monthSpinner.adapter = adapter

        binding.monthSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                populateExpenseRecyclerView(yearSelected, monthsInYearWithExpense[position], categories)
                expenseViewModel.updatePreferenceInSharedPreferences(
                    binding.root.context,
                    SHARED_PREFERENCES_SELECTED_MONTH_KEY,
                    monthsInYearWithExpense[position].toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val selectedMonth = expenseViewModel.retrievePreferenceFromSharedPreferences(binding.root.context, SHARED_PREFERENCES_SELECTED_MONTH_KEY)
        binding.monthSpinner.setSelection(
            if (selectedMonth.isBlank()) monthsInYearWithExpense.lastIndex else monthsInYearWithExpense.indexOf(Month.valueOf(selectedMonth)),
            true)
    }

    private fun populateExpenseRecyclerView(yearSelected: Int,
                                            monthSelected: Month,
                                            categories: List<Category>) {
        val expenseRecyclerViewAdapter = ExpenseRecyclerViewAdapter(expenseViewModel, categories)
        binding.expensesRecyclerView.adapter = expenseRecyclerViewAdapter
        binding.expensesRecyclerView.layoutManager = LinearLayoutManager(context)

        val selectedExpenses = expenseViewModel.getExpensesForSelectedMonthInSelectedYear(yearSelected, monthSelected)
        expenseRecyclerViewAdapter.submitList(selectedExpenses)
        binding.totalMoneySpentTextView.text = "Total: ${expenseViewModel.getTotalMoneySpentFormatted(selectedExpenses)}"
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

}
