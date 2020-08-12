package com.ogrob.moneybox.ui.expense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.MonthListItemBinding
import com.ogrob.moneybox.databinding.YearListItemBinding
import com.ogrob.moneybox.ui.helper.ExpensesByMonth
import com.ogrob.moneybox.ui.helper.ExpensesByYear

class YearRecyclerViewAdapter
    : RecyclerView.Adapter<YearRecyclerViewAdapter.BaseViewHolder<*>>() {

    private var adapterDataList: List<Any> = emptyList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> =
        when (viewType) {
            TYPE_MONTH -> MonthViewHolder.from(parent)
            TYPE_YEAR -> YearViewHolder.from(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = adapterDataList[position]
        when (holder) {
            is MonthViewHolder -> holder.bind(element as ExpensesByMonth)
            is YearViewHolder -> holder.bind(element as ExpensesByYear)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (adapterDataList[position]) {
            is ExpensesByMonth -> TYPE_MONTH
            is ExpensesByYear -> TYPE_YEAR
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }

    override fun getItemCount(): Int = adapterDataList.size

    fun setAdapterDataList(dataList: List<Any>) {
        adapterDataList = dataList
    }


    class MonthViewHolder private constructor(val binding: MonthListItemBinding) : BaseViewHolder<ExpensesByMonth>(binding.root) {

        override fun bind(item: ExpensesByMonth) {
            binding.monthCardTextView.text = item.month.toString().toLowerCase().capitalize()
            binding.monthCardTotalItemsTextView.text = displayExpensesCount(item.expenses.size)
            binding.monthCardTotalAmountTextView.text =
                binding.root.resources.getString(R.string.money_amount_in_parenthesis, item.totalMoneySpentInMonth)

            itemView.setOnClickListener {
                it.findNavController().navigate(
                    ExpenseYearFragmentDirections.actionExpenseYearFragmentToExpenseDayFragment(item.year, item.month.value))
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


    class YearViewHolder private constructor(val binding: YearListItemBinding) : BaseViewHolder<ExpensesByYear>(binding.root) {

        override fun bind(item: ExpensesByYear) {
            binding.yearCardTextView.text = item.year.toString()
            binding.yearCardTotalTextView.text =
                binding.root.resources.getString(R.string.money_amount_in_parenthesis, item.totalMoneySpentInYear)

            itemView.setOnClickListener {
                it.findNavController().navigate(
                    ExpenseYearFragmentDirections.actionExpenseYearFragmentToExpenseMonthFragment(item.year))
            }
        }


        companion object {
            fun from(parent: ViewGroup): YearViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = YearListItemBinding.inflate(layoutInflater, parent, false)
                return YearViewHolder(binding)
            }
        }
    }


    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }


    companion object {
        private const val TYPE_YEAR = 0
        private const val TYPE_MONTH = 1
    }
}