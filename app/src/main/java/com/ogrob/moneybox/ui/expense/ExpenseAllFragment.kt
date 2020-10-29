package com.ogrob.moneybox.ui.expense

import android.graphics.Paint
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.data.helper.SavedValuesFromSharedPreferences
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.*
import java.time.LocalDate

class ExpenseAllFragment : ExpenseBaseFragment() {


    override fun getExpensesBasedOnFragmentAndFilters(savedValuesFromSharedPreferences: SavedValuesFromSharedPreferences) {
        expenseViewModel.getAllFilteredExpenses(savedValuesFromSharedPreferences)
    }

    override fun onAddNewExpense(view: View) {
        view.findNavController().navigate(ExpenseAllFragmentDirections.actionExpenseAllFragmentToExpenseAddAndEditFragment(
            EMPTY_STRING,
            EMPTY_STRING,
            LocalDate.now().toString(),
            NEW_EXPENSE_PLACEHOLDER_ID,
            SYSTEM_BASE_CURRENCY_STRING,
            NO_CATEGORY_ID,
            resources.getString(R.string.add_expense_button)
        ))
    }

    override fun setupFragmentSpecificViews() {
        binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.setOnClickListener { onChangeTotalAverageInterval(it) }
        binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun onChangeTotalAverageInterval(view: View) {
        val fixedIntervals = arrayOf(
            FixedInterval.YEAR,
            FixedInterval.MONTH,
            FixedInterval.DAY
        )

        val fixedIntervalFromSharedPreferences = SharedPreferenceManager.getStringSharedPreference(
            binding.root.context,
            SHARED_PREFERENCES_SELECTED_FIXED_INTERVAL_KEY,
            SHARED_PREFERENCES_DEFAULT_SELECTED_FIXED_INTERVAL
        )
        val checkedItem = FixedInterval.valueOf(fixedIntervalFromSharedPreferences).ordinal

        AlertDialog.Builder(binding.root.context)
            .setTitle("Show average in")
            .setSingleChoiceItems(
                fixedIntervals
                    .map(FixedInterval::toString)
                    .toTypedArray(),
                checkedItem
            ) { dialog, which ->
                expenseViewModel.updateSelectedFixedIntervalWithAverage(fixedIntervals[which])
                dialog.cancel()
            }
            .create()
            .show()
    }

    override fun populateRecyclerView(filteredExpenses: List<Expense>) {
        val yearRecyclerViewAdapter = AllExpensesRecyclerViewAdapter()
        binding.expenseBackdropFrontView.recyclerView.adapter = yearRecyclerViewAdapter
        binding.expenseBackdropFrontView.recyclerView.layoutManager = LinearLayoutManager(context)

        yearRecyclerViewAdapter.setAdapterDataList(expenseViewModel.groupExpensesByYearAndMonth(filteredExpenses))
    }

}