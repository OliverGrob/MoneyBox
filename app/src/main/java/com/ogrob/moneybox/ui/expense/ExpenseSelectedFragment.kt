package com.ogrob.moneybox.ui.expense

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.SavedValuesFromSharedPreferences
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.*
import java.time.LocalDate
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

    override fun getExpensesBasedOnFragmentAndFilters(savedValuesFromSharedPreferences: SavedValuesFromSharedPreferences) {
        expenseViewModel.getAllCategoriesWithExpensesForSelectedYearAndMonth(
            savedValuesFromSharedPreferences,
            args.year,
            Month.values()[args.monthIndex - 1]
        )
    }

    override fun onAddNewExpense(view: View) {
        view.findNavController().navigate(ExpenseSelectedFragmentDirections.actionExpenseSelectedFragmentToExpenseAddAndEditFragment(
            EMPTY_STRING,
            EMPTY_STRING,
            LocalDate.now().toString(),
            NEW_EXPENSE_PLACEHOLDER_ID,
            SYSTEM_BASE_CURRENCY_STRING,
            NO_CATEGORY_NAME,
            NO_CATEGORY_ID,
            resources.getString(R.string.add_expense_button)
        ))
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
        expenseViewModel.allCategories.observe(viewLifecycleOwner) {
            val yearRecyclerViewAdapter = SelectedExpensesRecyclerViewAdapter(it)
            binding.expenseBackdropFrontView.recyclerView.adapter = yearRecyclerViewAdapter
            binding.expenseBackdropFrontView.recyclerView.layoutManager = LinearLayoutManager(context)

            yearRecyclerViewAdapter.setAdapterDataList(expenseViewModel.createNormalAndHeaderExpenses(filteredExpenses))
        }

        expenseViewModel.getAllCategories()
    }

}