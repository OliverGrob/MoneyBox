package com.ogrob.moneybox.ui.expense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.MonthListItemBinding
import com.ogrob.moneybox.ui.helper.ExpensesByMonth

class MonthRecyclerViewAdapter
    : ListAdapter<ExpensesByMonth, MonthRecyclerViewAdapter.MonthViewHolder>(MonthDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        return MonthViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val expensesByMonth = getItem(position)
        holder.bind(expensesByMonth)
    }


    class MonthViewHolder private constructor(val binding: MonthListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expensesByMonth: ExpensesByMonth) {
            binding.monthCardTextView.text = expensesByMonth.month.toString().toLowerCase().capitalize()
            binding.monthCardTotalItemsTextView.text = displayExpensesCount(expensesByMonth.expenses.size)
            binding.monthCardTotalAmountTextView.text =
                binding.root.resources.getString(R.string.money_amount_in_parenthesis, expensesByMonth.totalMoneySpentInMonth)

            itemView.setOnClickListener {
                it.findNavController().navigate(
                    ExpenseMonthFragmentDirections.actionExpenseMonthFragmentToExpenseDayFragment(expensesByMonth.year, expensesByMonth.month.value))
            }
        }

        private fun displayExpensesCount(expenseCount: Int): String {
            return if (expenseCount == 1)
                binding.root.resources.getString(R.string.expenses_counter_text_singular, expenseCount)
            else
                binding.root.resources.getString(R.string.expenses_counter_text_plural, expenseCount)
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