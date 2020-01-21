package com.ogrob.moneybox.presentation.expense

import android.graphics.drawable.ClipDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.YearListItemBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.presentation.helper.ExpensesByMonth
import com.ogrob.moneybox.presentation.helper.ExpensesByYear

class YearRecyclerViewAdapter(private val expenseViewModel: ExpenseViewModel,
                              private val categories: List<Category>)
    : ListAdapter<ExpensesByYear, YearRecyclerViewAdapter.YearViewHolder>(YearDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        return YearViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        val expensesByYear = getItem(position)
        holder.bind(expensesByYear, expenseViewModel, categories)
    }


    class YearViewHolder private constructor(val binding: YearListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            expensesByYear: ExpensesByYear,
            expenseViewModel: ExpenseViewModel,
            categories: List<Category>
        ) {
            binding.yearCardTextView.text = expensesByYear.year.toString()
            binding.yearCardTotalTextView.text =
                binding.root.resources.getString(R.string.money_amount_in_parenthesis, expensesByYear.totalMoneySpentInYear)

            binding.yearCardCollapseButton.setOnClickListener { onToggleMonths(it) }
            binding.yearCardTextView.setOnClickListener { expenseViewModel.setSelectedYear(expensesByYear.year) }

            populateMonthRecyclerViews(expensesByYear.expensesByMonth, expenseViewModel, categories)
        }

        private fun onToggleMonths(view: View) {
            if (binding.monthRecyclerView.visibility == View.GONE) {
                view.background = binding.root.resources.getDrawable(R.drawable.ic_expand_less_white_24dp, null)
                binding.monthRecyclerView.visibility = View.VISIBLE
            } else {
                view.background = binding.root.resources.getDrawable(R.drawable.ic_expand_more_white_24dp, null)
                binding.monthRecyclerView.visibility = View.GONE
            }
        }

        private fun populateMonthRecyclerViews(
            expensesByMonth: List<ExpensesByMonth>,
            expenseViewModel: ExpenseViewModel,
            categories: List<Category>
        ) {
            val monthRecyclerViewAdapter = MonthRecyclerViewAdapter(expenseViewModel, categories)
            binding.monthRecyclerView.adapter = monthRecyclerViewAdapter
            binding.monthRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)

            val itemDecor = DividerItemDecoration(binding.root.context, ClipDrawable.HORIZONTAL)
            if (binding.monthRecyclerView.itemDecorationCount == 0)
                binding.monthRecyclerView.addItemDecoration(itemDecor)

            val expensesByMonthSorted = expenseViewModel.sortExpensesByMonthOfAdditionDate(expensesByMonth)

            monthRecyclerViewAdapter.submitList(expensesByMonthSorted)
        }


        companion object {
            fun from(parent: ViewGroup): YearViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = YearListItemBinding.inflate(layoutInflater, parent, false)
                return YearViewHolder(binding)
            }
        }
    }
}


class YearDiffCallback : DiffUtil.ItemCallback<ExpensesByYear>() {

    override fun areItemsTheSame(oldItem: ExpensesByYear, newItem: ExpensesByYear): Boolean {
        return oldItem.year == newItem.year && oldItem.expensesByMonth == newItem.expensesByMonth
    }

    override fun areContentsTheSame(oldItem: ExpensesByYear, newItem: ExpensesByYear): Boolean {
        return oldItem.year == newItem.year && oldItem.expensesByMonth == newItem.expensesByMonth
    }

}