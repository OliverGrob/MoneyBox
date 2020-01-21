package com.ogrob.moneybox.presentation.expense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.MonthListItemBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.presentation.helper.ExpensesByMonth

class MonthRecyclerViewAdapter(private val expenseViewModel: ExpenseViewModel,
                               private val categories: List<Category>)
    : ListAdapter<ExpensesByMonth, MonthRecyclerViewAdapter.MonthViewHolder>(MonthDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        return MonthViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val expensesByMonth = getItem(position)
        holder.bind(expensesByMonth, expenseViewModel, categories)
    }


    class MonthViewHolder private constructor(val binding: MonthListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            expensesByMonth: ExpensesByMonth,
            expenseViewModel: ExpenseViewModel,
            categories: List<Category>
        ) {
            binding.monthCardTextView.text = expensesByMonth.month.toString().toLowerCase().capitalize()
            binding.monthCardTotalTextView.text =
                binding.root.resources.getString(R.string.money_amount_in_parenthesis, expensesByMonth.totalMoneySpentInMonth)

            binding.monthCardCollapseButton.setOnClickListener { onToggleExpenses(it) }
            binding.monthCardTextView.setOnClickListener {
                expenseViewModel.setSelectedYear(expensesByMonth.year)
                expenseViewModel.setSelectedMonthIndex(expensesByMonth.month.value)
            }

            populateExpensesRecyclerViews(expensesByMonth.expenses, expenseViewModel, categories)
        }

        private fun onToggleExpenses(view: View) {
            if (binding.dayRecyclerView.visibility == View.GONE) {
                view.background = binding.root.resources.getDrawable(R.drawable.ic_expand_less_white_24dp, null)
                binding.dayRecyclerView.visibility = View.VISIBLE
            } else {
                view.background = binding.root.resources.getDrawable(R.drawable.ic_expand_more_white_24dp, null)
                binding.dayRecyclerView.visibility = View.GONE
            }
        }

        private fun populateExpensesRecyclerViews(
            expenses: List<Expense>,
            expenseViewModel: ExpenseViewModel,
            categories: List<Category>
        ) {
            val monthRecyclerViewAdapter = ExpenseRecyclerViewAdapter(expenseViewModel, categories)
            binding.dayRecyclerView.adapter = monthRecyclerViewAdapter
            binding.dayRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)

            val sortedExpenses = expenseViewModel.sortExpensesByDayOfAdditionDate(expenses)

            monthRecyclerViewAdapter.submitList(sortedExpenses)
        }


        companion object {
            fun from(parent: ViewGroup): MonthViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MonthListItemBinding.inflate(layoutInflater, parent, false)
                return MonthViewHolder(binding)
            }
        }
    }
}


class MonthDiffCallback : DiffUtil.ItemCallback<ExpensesByMonth>() {

    override fun areItemsTheSame(oldItem: ExpensesByMonth, newItem: ExpensesByMonth): Boolean {
        return oldItem.month == newItem.month && oldItem.expenses == newItem.expenses
    }

    override fun areContentsTheSame(oldItem: ExpensesByMonth, newItem: ExpensesByMonth): Boolean {
        return oldItem.month == newItem.month && oldItem.expenses == newItem.expenses
    }

}