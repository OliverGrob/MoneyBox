package com.ogrob.moneybox.presentation.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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

        binding.yearTextView.setOnClickListener { onInitYearAndMonthSelectionAlertDialogs() }
        binding.monthTextView.setOnClickListener { onInitYearAndMonthSelectionAlertDialogs() }
        binding.addExpenseButton.setOnClickListener { onAddNewExpense(it) }

        expenseViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            populateExpensesRecyclerView(it)
        })

        return binding.root
    }

    private fun onInitYearAndMonthSelectionAlertDialogs() {
        expenseViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            val categories: List<Category> = it.map(CategoryWithExpenses::category)

            val triple = createYearInformationTriple()
            val yearsWithExpense: Array<String> = triple.first
            val selectedYearIndex: Int = triple.second
            var selectedYear = triple.third

            AlertDialog.Builder(binding.root.context)
                .setTitle("Select year:")
                .setSingleChoiceItems(
                    yearsWithExpense,
                    selectedYearIndex
                ) { _, which -> selectedYear = yearsWithExpense[which] }
                .setPositiveButton("Next") { dialog, _ ->
                    populateMonthAlertDialog(selectedYear, categories)
                    dialog.cancel()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        })
    }

    private fun populateMonthAlertDialog(selectedYear: String,
                                         categories: List<Category>) {
        val triple = createMonthInformationTriple(selectedYear)
        val monthsInYearWithExpense: Array<String> = triple.first
        val selectedMonthIndex: Int = triple.second
        var selectedMonth = triple.third

        AlertDialog.Builder(binding.root.context)
            .setTitle("Select month in $selectedYear:")
            .setSingleChoiceItems(
                monthsInYearWithExpense,
                selectedMonthIndex
            ) { _, which -> selectedMonth = monthsInYearWithExpense[which] }
            .setPositiveButton("Done") { dialog, _ ->
                onSelectMonth(selectedYear, selectedMonth, categories)
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun onSelectMonth(selectedYear: String,
                              selectedMonth: String,
                              categories: List<Category>) {
        val selectedYearFromSharedPreferences = expenseViewModel.retrievePreferenceFromSharedPreferences(
            binding.root.context,
            SHARED_PREFERENCES_SELECTED_YEAR_KEY)
        val selectedMonthFromSharedPreferences = expenseViewModel.retrievePreferenceFromSharedPreferences(
            binding.root.context,
            SHARED_PREFERENCES_SELECTED_MONTH_KEY)

        val newSelectionSameAsCurrent =
            selectedYearFromSharedPreferences == selectedYear && selectedMonthFromSharedPreferences == selectedMonth

        if (newSelectionSameAsCurrent)
            return

        expenseViewModel.updatePreferenceInSharedPreferences(
            binding.root.context,
            SHARED_PREFERENCES_SELECTED_YEAR_KEY,
            selectedYear)

        expenseViewModel.updatePreferenceInSharedPreferences(
            binding.root.context,
            SHARED_PREFERENCES_SELECTED_MONTH_KEY,
            selectedMonth)

        binding.yearTextView.text = selectedYear
        binding.monthTextView.text = selectedMonth

        populateExpenseRecyclerView(
            selectedYear.toInt(),
            Month.valueOf(selectedMonth),
            categories)
    }

    private fun populateExpensesRecyclerView(categoryWithExpenses: List<CategoryWithExpenses>) {
        val categories: List<Category> = categoryWithExpenses.map(CategoryWithExpenses::category)

        val selectedYear = createYearInformationTriple().third
        val selectedMonth = createMonthInformationTriple(selectedYear).third

        binding.yearTextView.text = selectedYear
        binding.monthTextView.text = selectedMonth

        populateExpenseRecyclerView(
            selectedYear.toInt(),
            Month.valueOf(selectedMonth),
            categories)
    }

    private fun createYearInformationTriple(): Triple<Array<String>, Int, String> {
        val yearsWithExpense: Array<String> = expenseViewModel.getYearsWithExpense()
            .map(Int::toString)
            .toTypedArray()

        val selectedYearFromSharedPreferences =
            expenseViewModel.retrievePreferenceFromSharedPreferences(
                binding.root.context,
                SHARED_PREFERENCES_SELECTED_YEAR_KEY)

        val selectedYearIndex: Int =
            if (selectedYearFromSharedPreferences.isBlank())
                yearsWithExpense.lastIndex
            else
                yearsWithExpense.indexOf(selectedYearFromSharedPreferences)

        return Triple(yearsWithExpense, selectedYearIndex, selectedYearFromSharedPreferences)
    }

    private fun createMonthInformationTriple(selectedYear: String): Triple<Array<String>, Int, String> {
        val monthsInYearWithExpense: Array<String> =
            expenseViewModel.getMonthsInYearWithExpense(selectedYear.toInt())
                .map(Month::toString)
                .toTypedArray()

        var selectedMonth =
            expenseViewModel.retrievePreferenceFromSharedPreferences(
                binding.root.context,
                SHARED_PREFERENCES_SELECTED_MONTH_KEY)

        val selectedMonthIndex: Int =
            if (selectedMonth.isBlank() || !monthsInYearWithExpense.contains(selectedMonth)) {
                selectedMonth = monthsInYearWithExpense[monthsInYearWithExpense.lastIndex]
                monthsInYearWithExpense.lastIndex
            } else
                monthsInYearWithExpense.indexOf(selectedMonth)

        return Triple(monthsInYearWithExpense, selectedMonthIndex, selectedMonth)
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
