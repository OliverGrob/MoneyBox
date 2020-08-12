package com.ogrob.moneybox.ui.expense

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
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
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpenseDayFragment : ExpenseBaseFragment() {

    private lateinit var args: ExpenseDayFragmentArgs


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        args = ExpenseDayFragmentArgs.fromBundle(requireArguments())

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAddNewExpense(view: View) {
        view.findNavController().navigate(ExpenseDayFragmentDirections.actionExpenseDayFragmentToExpenseAddAndEditFragment(
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
            it.findNavController().navigate(ExpenseDayFragmentDirections.actionExpenseDayFragmentToExpenseYearFragment())
        }

        binding.expenseBackdropFrontView.headerSelectionFirstRightArrowTextView.visibility = View.VISIBLE

        binding.expenseBackdropFrontView.headerSelectionYearTextView.text = args.year.toString()
        binding.expenseBackdropFrontView.headerSelectionYearTextView.visibility = View.VISIBLE
        binding.expenseBackdropFrontView.headerSelectionYearTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.expenseBackdropFrontView.headerSelectionYearTextView.setOnClickListener {
            it.findNavController().navigate(ExpenseDayFragmentDirections.actionExpenseDayFragmentToExpenseMonthFragment(args.year))
        }

        binding.expenseBackdropFrontView.headerSelectionSecondRightArrowTextView.visibility = View.VISIBLE

        binding.expenseBackdropFrontView.headerSelectionMonthTextView.text = Month.values()[args.monthIndex - 1].toString().toLowerCase().capitalize()
        binding.expenseBackdropFrontView.headerSelectionMonthTextView.visibility = View.VISIBLE
    }

    override fun getFilteredExpenses() : LiveData<List<CategoryWithExpenses>> {
        return expenseViewModel.getFilteredExpensesForFixedIntervalUsingAllFilters(args.year, args.monthIndex)
    }

    override fun populateRecyclerView(
        categories: List<Category>,
        expenses: List<Expense>
    ) {
        val monthRecyclerViewAdapter = ExpenseRecyclerViewAdapter(expenseViewModel, categories)
        binding.expenseBackdropFrontView.recyclerView.adapter = monthRecyclerViewAdapter
        binding.expenseBackdropFrontView.recyclerView.layoutManager = LinearLayoutManager(context)

//        val itemDecor = DividerItemDecoration(context, HORIZONTAL)
//        if (binding.expenseBackdropFrontView.recyclerView.itemDecorationCount == 0)
//            binding.expenseBackdropFrontView.recyclerView.addItemDecoration(itemDecor)

        monthRecyclerViewAdapter.submitList(expenses)
    }

    override fun getFixedInterval(): FixedInterval = FixedInterval.DAY

    override fun createAvailableFixedIntervals(): Array<FixedInterval> =
        arrayOf(
            FixedInterval.DAY
        )

    override fun getExpensesWithoutCategoryFiltering(): LiveData<List<CategoryWithExpenses>> =
        expenseViewModel.getExpensesWithoutCategoryFiltering(args.year, args.monthIndex)

}