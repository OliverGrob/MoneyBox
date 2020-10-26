package com.ogrob.moneybox.ui.expense

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import java.time.Month

class ExpenseSelectedFragment : ExpenseBaseFragment() {

    private lateinit var args: ExpenseSelectedFragmentArgs


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        args = ExpenseSelectedFragmentArgs.fromBundle(requireArguments())

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getExpensesBasedOnFragmentAndFilters() {
        val filterValuesFromSharedPreferences = getFilterValuesFromSharedPreferences()

        expenseViewModel.getAllCategoriesWithExpensesForSelectedYearAndMonth(
            filterValuesFromSharedPreferences,
            args.year,
            Month.values()[args.monthIndex - 1]
        )
//        expenseViewModel.getAllCategoriesWithExpensesForSelectedYearAndMonth_OLD(
//            filterValuesFromSharedPreferences,
//            args.year,
//            Month.values()[args.monthIndex - 1]
//        )
    }

    override fun onAddNewExpense(view: View) {

    }

    override fun setupFragmentSpecificViews() {
        binding.expenseBackdropFrontView.headerBackButtonTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.expenseBackdropFrontView.headerBackButtonTextView.visibility = View.VISIBLE
        binding.expenseBackdropFrontView.headerBackButtonTextView.setOnClickListener {
            binding.backdropContainer.closeBackView()
            findNavController().navigate(ExpenseSelectedFragmentDirections.actionExpenseSelectedFragmentToExpenseAllFragment())
        }
    }

    override fun populateRecyclerView(filteredExpenses: List<Expense>) {
        expenseViewModel.allCategories.observe(viewLifecycleOwner, Observer {
            val yearRecyclerViewAdapter = SelectedExpensesRecyclerViewAdapter(it)
            binding.expenseBackdropFrontView.recyclerView.adapter = yearRecyclerViewAdapter
            binding.expenseBackdropFrontView.recyclerView.layoutManager = LinearLayoutManager(context)

            yearRecyclerViewAdapter.setAdapterDataList(expenseViewModel.createNormalAndHeaderExpenses(filteredExpenses))
        })

        expenseViewModel.getAllCategories()
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