package com.ogrob.moneybox.presentation.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.data.viewmodel.StatisticsViewModel
import com.ogrob.moneybox.databinding.StatisticsListItemBinding
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.presentation.helper.ExpensesByMonth
import com.ogrob.moneybox.presentation.helper.ExpensesByYear

class StatisticsRecyclerViewAdapter(private val statisticsViewModel: StatisticsViewModel)
    : RecyclerView.Adapter<StatisticsRecyclerViewAdapter.StatisticsViewHolder>() {


    private lateinit var expensesByYearAndMonth: List<ExpensesByYear>


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        return StatisticsViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return expensesByYearAndMonth.size
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        val sortedExpensesByYearAndMonth = expensesByYearAndMonth[position]
        holder.bind(statisticsViewModel, sortedExpensesByYearAndMonth)
    }

    fun setExpensesByYearAndAmount(categoriesWithExpenses: List<CategoryWithExpenses>) {
        expensesByYearAndMonth = statisticsViewModel.sortExpensesByYearAndMonth(categoriesWithExpenses)

        notifyDataSetChanged()
    }


    class StatisticsViewHolder private constructor(val binding: StatisticsListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(statisticsViewModel: StatisticsViewModel, sortedExpensesByYearAndMonth: ExpensesByYear) {
            binding.statisticsYearTextView.text = "${sortedExpensesByYearAndMonth.year} - ${statisticsViewModel.formatMoneySpent(sortedExpensesByYearAndMonth.totalMoneySpentInYear)}"

            sortedExpensesByYearAndMonth.expensesByMonth.forEach {
                createAmountSpentInMonthTextView(statisticsViewModel, sortedExpensesByYearAndMonth.year, it)
            }
        }

        private fun createAmountSpentInMonthTextView(
            statisticsViewModel: StatisticsViewModel,
            selectedYear: Int,
            expensesSortedByMonth: ExpensesByMonth
        ) {
            val textView = TextView(itemView.context)
            val totalMoneySpentInMonth = statisticsViewModel.getTotalMoneySpent(expensesSortedByMonth.expenses)
            textView.text = "${expensesSortedByMonth.month.name.toLowerCase().capitalize()} - ${statisticsViewModel.formatMoneySpent(totalMoneySpentInMonth)}"
            textView.setTextColor(statisticsViewModel.getTextColorBasedOnSetMaxExpense(totalMoneySpentInMonth))

            textView.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
                .apply {
                    this.setMargins(
                        16,
                        this.topMargin,
                        this.rightMargin,
                        this.bottomMargin)
                }

            textView.textSize = 18F

            textView.setOnClickListener {
                it.findNavController().navigate(StatisticsFragmentDirections.actionStatisticsFragmentToStatisticsDetailFragment(
                    selectedYear,
                    expensesSortedByMonth.month.toString()
                ))
            }

            binding.statisticsMonthLinearLayout.addView(textView)
        }

        companion object {
            fun from(parent: ViewGroup): StatisticsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StatisticsListItemBinding.inflate(layoutInflater, parent, false)
                return StatisticsViewHolder(binding)
            }
        }
    }
}