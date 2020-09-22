package com.ogrob.moneybox.ui.expense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.YearAndMonthListItemBinding
import com.ogrob.moneybox.databinding.YearHeaderListItemBinding
import com.ogrob.moneybox.ui.helper.ExpensesByMonth
import com.ogrob.moneybox.ui.helper.ExpensesByYear
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_CURRENCY_KEY
import com.ogrob.moneybox.utils.SHARED_PREFERENCES_DEFAULT_CURRENCY
import com.ogrob.moneybox.utils.SharedPreferenceManager
import com.ogrob.moneybox.utils.formatExpenseCounterText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class AllExpensesRecyclerViewAdapter
    : RecyclerView.Adapter<AllExpensesRecyclerViewAdapter.BaseViewHolder<*>>() {

    private var adapterDataList: List<Any> = emptyList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> =
        when (viewType) {
            TYPE_YEAR_AND_MONTH -> YearHeaderViewHolder.from(parent)
            TYPE_YEAR_HEADER -> YearAndMonthViewHolder.from(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = adapterDataList[position]
        when (holder) {
            is YearHeaderViewHolder -> holder.bind(element as ExpensesByMonth)
            is YearAndMonthViewHolder -> holder.bind(element as ExpensesByYear)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (adapterDataList[position]) {
            is ExpensesByMonth -> TYPE_YEAR_AND_MONTH
            is ExpensesByYear -> TYPE_YEAR_HEADER
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }

    override fun getItemCount(): Int = adapterDataList.size

    fun setAdapterDataList(dataList: List<Any>) {
        adapterDataList = dataList
    }


    class YearHeaderViewHolder private constructor(val binding: YearAndMonthListItemBinding) : BaseViewHolder<ExpensesByMonth>(
        binding.root
    ) {

        override fun bind(item: ExpensesByMonth) {
            binding.yearTextView.text = item.year.toString()
            binding.monthTextView.text = item.month.name.substring(0, 3)
            binding.itemsTextView.text = formatExpenseCounterText(item.expenses.size)
            binding.totalAmountInDefaultCurrencyTextView.text =
                formatTotalMoneySpentInMonth(item.totalMoneySpentInMonth)

            val defaultCurrency = SharedPreferenceManager.getStringSharedPreference(
                binding.root.context,
                SHARED_PREFERENCES_CURRENCY_KEY,
                SHARED_PREFERENCES_DEFAULT_CURRENCY
            )
            binding.defaultCurrencyTextView.text =
                binding.root.resources.getString(
                    R.string.space_at_the_start, defaultCurrency.toLowerCase().capitalize()
                )

            itemView.setOnClickListener {
                it.findNavController().navigate(
                    ExpenseAllFragmentDirections.actionExpenseAllFragmentToExpenseSelectedFragment(
                        item.year,
                        item.month.value
                    )
                )
            }
        }

        private fun formatTotalMoneySpentInMonth(totalMoneySpentInMonth: Double): String {
            val decimalFormat = DecimalFormat()
            val decimalFormatSymbols = DecimalFormatSymbols()

            decimalFormatSymbols.groupingSeparator = '.'

            decimalFormat.groupingSize = 3
            decimalFormat.isGroupingUsed = true
            decimalFormat.decimalFormatSymbols = decimalFormatSymbols

            return decimalFormat.format(totalMoneySpentInMonth)
        }


        companion object {
            fun from(parent: ViewGroup): YearHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = YearAndMonthListItemBinding.inflate(layoutInflater, parent, false)
                return YearHeaderViewHolder(binding)
            }
        }

    }


    class YearAndMonthViewHolder private constructor(val binding: YearHeaderListItemBinding) : BaseViewHolder<ExpensesByYear>(
        binding.root
    ) {

        override fun bind(item: ExpensesByYear) {
            binding.yearTextView.text = item.year.toString()
            binding.yearItemsTextView.text = formatExpenseCounterText(item.expenses.size)
        }


        companion object {
            fun from(parent: ViewGroup): YearAndMonthViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = YearHeaderListItemBinding.inflate(layoutInflater, parent, false)
                return YearAndMonthViewHolder(binding)
            }
        }
    }


    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }


    companion object {
        private const val TYPE_YEAR_HEADER = 0
        private const val TYPE_YEAR_AND_MONTH = 1
    }
}