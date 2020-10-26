package com.ogrob.moneybox.ui.expense

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense

class ExpenseAllFragment : ExpenseBaseFragment() {


    override fun getExpensesBasedOnFragmentAndFilters() {
        val filterValuesFromSharedPreferences = getFilterValuesFromSharedPreferences()

        expenseViewModel.getAllFilteredExpenses(filterValuesFromSharedPreferences)
//        expenseViewModel.getAllFilteredExpenses_OLD(filterValuesFromSharedPreferences)
    }

    override fun onAddNewExpense(view: View) {

    }

    override fun setupFragmentSpecificViews() {

    }

    override fun populateRecyclerView(filteredExpenses: List<Expense>) {
        val yearRecyclerViewAdapter = AllExpensesRecyclerViewAdapter()
        binding.expenseBackdropFrontView.recyclerView.adapter = yearRecyclerViewAdapter
        binding.expenseBackdropFrontView.recyclerView.layoutManager = LinearLayoutManager(context)

        yearRecyclerViewAdapter.setAdapterDataList(expenseViewModel.groupExpensesByYearAndMonth(filteredExpenses))
    }

    override fun createAvailableFixedIntervals(): Array<FixedInterval> {
        return arrayOf()
    }

    override fun getFilteredExpenses_OLD(): LiveData<List<CategoryWithExpenses>> {
        return liveData {  }
    }

    override fun getFixedInterval(): FixedInterval {
        return FixedInterval.DAY
    }

}