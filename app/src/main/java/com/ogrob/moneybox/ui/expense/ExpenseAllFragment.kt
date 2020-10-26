package com.ogrob.moneybox.ui.expense

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import com.ogrob.moneybox.utils.SYSTEM_BASE_CURRENCY_STRING
import java.time.LocalDate

class ExpenseAllFragment : ExpenseBaseFragment() {


    override fun getExpensesBasedOnFragmentAndFilters(filterValuesFromSharedPreferences: Triple<Set<String>, Set<String>, Set<String>>) {
        expenseViewModel.getAllFilteredExpenses(filterValuesFromSharedPreferences)
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