package com.ogrob.moneybox.ui.expense

import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.view.View
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpenseYearFragment : ExpenseBaseFragment() {


    override fun onAddNewExpense(view: View) {
        view.findNavController().navigate(ExpenseYearFragmentDirections.actionExpenseYearFragmentToExpenseAddAndEditFragment(
            EMPTY_STRING,
            EMPTY_STRING,
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
            NEW_EXPENSE_PLACEHOLDER_ID,
            NO_CATEGORY_ID,
            resources.getString(R.string.add_expense_button)
        ))
    }

    override fun initHeaderSelectionTextViewsAndAddClickListeners() {
        binding.expenseBackdropFrontView.headerSelectionAllTextView.visibility = View.GONE
        binding.expenseBackdropFrontView.headerSelectionFirstRightArrowTextView.visibility = View.GONE
        binding.expenseBackdropFrontView.headerSelectionYearTextView.visibility = View.GONE
        binding.expenseBackdropFrontView.headerSelectionSecondRightArrowTextView.visibility = View.GONE
        binding.expenseBackdropFrontView.headerSelectionMonthTextView.visibility = View.GONE
    }

    override fun getFilteredExpenses() : LiveData<List<CategoryWithExpenses>> {
        return expenseViewModel.getFilteredExpensesForFixedIntervalUsingAllFilters()
    }

    override fun populateRecyclerView(
        categories: List<Category>,
        expenses: List<Expense>
    ) {
        val yearRecyclerViewAdapter = YearRecyclerViewAdapter()
        binding.expenseBackdropFrontView.recyclerView.adapter = yearRecyclerViewAdapter
        binding.expenseBackdropFrontView.recyclerView.layoutManager = LinearLayoutManager(context)

        val itemDecor = DividerItemDecoration(context, HORIZONTAL)
        if (binding.expenseBackdropFrontView.recyclerView.itemDecorationCount == 0)
            binding.expenseBackdropFrontView.recyclerView.addItemDecoration(itemDecor)

        yearRecyclerViewAdapter.setAdapterDataList(expenseViewModel.groupExpensesByYearAndMonth(expenses))
    }

    override fun getFixedInterval(): FixedInterval = FixedInterval.YEAR

    override fun createAvailableFixedIntervals(): Array<FixedInterval> =
        arrayOf(
            FixedInterval.YEAR,
            FixedInterval.MONTH,
            FixedInterval.DAY
        )

    override fun getExpensesWithoutCategoryFiltering(): LiveData<List<CategoryWithExpenses>> =
        expenseViewModel.getExpensesWithoutCategoryFiltering()

}