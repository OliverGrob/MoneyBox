package com.ogrob.moneybox.ui.expense

import android.graphics.Paint
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class ExpenseMonthFragment : ExpenseBaseFragment() {

    private lateinit var args: ExpenseMonthFragmentArgs


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        args = ExpenseMonthFragmentArgs.fromBundle(requireArguments())

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAddNewExpense(view: View) {
        view.findNavController().navigate(ExpenseMonthFragmentDirections.actionExpenseMonthFragmentToExpenseAddAndEditFragment(
            EMPTY_STRING,
            EMPTY_STRING,
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
            NEW_EXPENSE_PLACEHOLDER_ID,
            NO_CATEGORY_ID,
            resources.getString(R.string.add_expense_button)
        ))
    }

    override fun initHeaderSelectionTextViewsAndAddClickListeners() {
        binding.expenseBackdropFrontView.headerSelectionAllTextView.text = resources.getString(R.string.header_selection_all_text)
        binding.expenseBackdropFrontView.headerSelectionAllTextView.visibility = View.VISIBLE
        binding.expenseBackdropFrontView.headerSelectionAllTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.expenseBackdropFrontView.headerSelectionAllTextView.setOnClickListener {
            it.findNavController().navigate(ExpenseMonthFragmentDirections.actionExpenseMonthFragmentToExpenseYearFragment())
        }

        binding.expenseBackdropFrontView.headerSelectionFirstRightArrowTextView.visibility = View.VISIBLE

        binding.expenseBackdropFrontView.headerSelectionYearTextView.text = args.year.toString()
        binding.expenseBackdropFrontView.headerSelectionYearTextView.visibility = View.VISIBLE
        binding.expenseBackdropFrontView.headerSelectionYearTextView.paintFlags = 0


        binding.expenseBackdropFrontView.headerSelectionSecondRightArrowTextView.visibility = View.GONE
        binding.expenseBackdropFrontView.headerSelectionMonthTextView.visibility = View.GONE
    }

    override fun getFilteredExpenses() : LiveData<List<CategoryWithExpenses>> {
        return expenseViewModel.getFilteredExpensesForFixedIntervalUsingAllFilters(args.year)
    }

    override fun populateRecyclerView(
        categories: List<Category>,
        expenses: List<Expense>
    ) {
        val monthRecyclerViewAdapter = MonthRecyclerViewAdapter()
        binding.expenseBackdropFrontView.recyclerView.adapter = monthRecyclerViewAdapter
        binding.expenseBackdropFrontView.recyclerView.layoutManager = LinearLayoutManager(context)

        val itemDecor = DividerItemDecoration(context, HORIZONTAL)
        if (binding.expenseBackdropFrontView.recyclerView.itemDecorationCount == 0)
            binding.expenseBackdropFrontView.recyclerView.addItemDecoration(itemDecor)

        monthRecyclerViewAdapter.submitList(expenseViewModel.groupExpensesByMonthInYear(expenses, args.year))
    }

    override fun getFixedInterval(): FixedInterval = FixedInterval.MONTH

    override fun createAvailableFixedIntervals(): Array<FixedInterval> =
        arrayOf(
            FixedInterval.MONTH,
            FixedInterval.DAY
        )

    override fun getExpensesWithoutCategoryFiltering(): LiveData<List<CategoryWithExpenses>> =
        expenseViewModel.getExpensesWithoutCategoryFiltering(args.year)

}