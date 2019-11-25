package com.ogrob.moneybox.presentation.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.data.viewmodel.StatisticsViewModel
import com.ogrob.moneybox.databinding.StatisticsDetailListItemBinding
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.presentation.expense.ExpenseDiffCallback

class StatisticsDetailRecyclerViewAdapter(private val statisticsViewModel: StatisticsViewModel,
                                          private val totalAmountFromSelectedExpenses: Double)
    : ListAdapter<Expense, StatisticsDetailRecyclerViewAdapter.StatisticsDetailViewHolder>(ExpenseDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsDetailViewHolder {
        return StatisticsDetailViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: StatisticsDetailViewHolder, position: Int) {
        val expense = getItem(position)
        holder.bind(statisticsViewModel, expense, totalAmountFromSelectedExpenses)
    }


    class StatisticsDetailViewHolder private constructor(val binding: StatisticsDetailListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(statisticsViewModel: StatisticsViewModel,
                 expense: Expense,
                 totalAmountFromSelectedExpenses: Double) {
            binding.expenseDetailedTextView.text =
                "${expense.description} - ${expense.amount} (${statisticsViewModel.calculatePercentageOfTotalAmount(expense, totalAmountFromSelectedExpenses)}%)"
        }


        companion object {
            fun from(parent: ViewGroup): StatisticsDetailViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StatisticsDetailListItemBinding.inflate(layoutInflater, parent, false)
                return StatisticsDetailViewHolder(binding)
            }
        }
    }
}