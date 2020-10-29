package com.ogrob.moneybox.ui.expense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.ExpenseHeaderListItemBinding
import com.ogrob.moneybox.databinding.ExpenseNormalListItemBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.ui.helper.ExpenseDTOForAdapter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class SelectedExpensesRecyclerViewAdapter(private val categories: List<Category>)
    : RecyclerView.Adapter<SelectedExpensesRecyclerViewAdapter.BaseViewHolder<ExpenseDTOForAdapter>>() {

    private lateinit var adapterDataList: List<ExpenseDTOForAdapter>


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ExpenseDTOForAdapter> =
        when (viewType) {
            TYPE_NORMAL_EXPENSE -> NormalExpenseViewHolder.from(parent, categories)
            TYPE_EXPENSE_WITH_HEADER -> ExpenseWithHeaderViewHolder.from(parent, categories)
            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(holder: BaseViewHolder<ExpenseDTOForAdapter>, position: Int) {
        val element = adapterDataList[position]
        when (holder) {
            is NormalExpenseViewHolder -> holder.bind(element)
            is ExpenseWithHeaderViewHolder -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (adapterDataList[position].isHeaderExpense) {
            false -> TYPE_NORMAL_EXPENSE
            true -> TYPE_EXPENSE_WITH_HEADER
        }

    override fun getItemCount(): Int = adapterDataList.size

    fun setAdapterDataList(dataList: List<ExpenseDTOForAdapter>) {
        adapterDataList = dataList
    }


    class NormalExpenseViewHolder private constructor(
        val binding: ExpenseNormalListItemBinding,
        private val categories: List<Category>
    ) : BaseViewHolder<ExpenseDTOForAdapter>(
        binding.root
    ) {

        override fun bind(item: ExpenseDTOForAdapter) {
            val expense = item.expense
            val category = this.categories.single { category -> category.id == expense.categoryId }

            binding.expenseNameTextView.text = expense.description
            binding.expenseCategoryNameTextView.text = category.name
            binding.expenseCategoryNameTextView.setTextColor(category.color)
            binding.categoryColorTextView.setBackgroundColor(category.color)
            binding.totalAmountInDefaultCurrencyTextView.text = formatTotalMoneySpentInMonth(expense.amount)

            binding.defaultCurrencyTextView.text =
                binding.root.resources.getString(
                    R.string.space_at_the_start, expense.currency.name.toLowerCase().capitalize()
                )

            itemView.setOnClickListener {
                it.findNavController().navigate(
                    ExpenseSelectedFragmentDirections.actionExpenseSelectedFragmentToExpenseAddAndEditFragment(
                        expense.amount.toString(),
                        expense.description,
                        expense.additionDate.toLocalDate().toString(),
                        expense.id,
                        expense.currency.name,
                        category.name,
                        expense.categoryId,
                        binding.root.resources.getString(R.string.save_button)
                    )
                )
            }
        }


        companion object {
            fun from(parent: ViewGroup, categories: List<Category>): NormalExpenseViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ExpenseNormalListItemBinding.inflate(layoutInflater, parent, false)
                return NormalExpenseViewHolder(binding, categories)
            }
        }

    }


    class ExpenseWithHeaderViewHolder private constructor(
        val binding: ExpenseHeaderListItemBinding,
        private val categories: List<Category>
    ) : BaseViewHolder<ExpenseDTOForAdapter>(
        binding.root
    ) {

        override fun bind(item: ExpenseDTOForAdapter) {
            val expense = item.expense
            val category = this.categories.single { category -> category.id == expense.categoryId }

            binding.yearTextView.text = expense.additionDate.year.toString()
            binding.monthTextView.text = expense.additionDate.month.name.substring(0, 3)
            binding.dayTextView.text = expense.additionDate.dayOfMonth.toString()

            binding.expenseNameTextView.text = expense.description
            binding.expenseCategoryNameTextView.text = category.name
            binding.expenseCategoryNameTextView.setTextColor(category.color)
            binding.categoryColorTextView.setBackgroundColor(category.color)
            binding.totalAmountInDefaultCurrencyTextView.text = formatTotalMoneySpentInMonth(expense.amount)

            binding.defaultCurrencyTextView.text =
                binding.root.resources.getString(
                    R.string.space_at_the_start, expense.currency.name.toLowerCase().capitalize()
                )

            itemView.setOnClickListener {
                it.findNavController().navigate(
                    ExpenseSelectedFragmentDirections.actionExpenseSelectedFragmentToExpenseAddAndEditFragment(
                        expense.amount.toString(),
                        expense.description,
                        expense.additionDate.toLocalDate().toString(),
                        expense.id,
                        expense.currency.name,
                        category.name,
                        expense.categoryId,
                        binding.root.resources.getString(R.string.save_button)
                    )
                )
            }
        }


        companion object {
            fun from(parent: ViewGroup, categories: List<Category>): ExpenseWithHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ExpenseHeaderListItemBinding.inflate(layoutInflater, parent, false)
                return ExpenseWithHeaderViewHolder(binding, categories)
            }
        }
    }


    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }


    companion object {
        private const val TYPE_EXPENSE_WITH_HEADER = 0
        private const val TYPE_NORMAL_EXPENSE = 1


        fun formatTotalMoneySpentInMonth(totalMoneySpentInMonth: Double): String {
            val decimalFormat = DecimalFormat()
            val decimalFormatSymbols = DecimalFormatSymbols()

            decimalFormatSymbols.groupingSeparator = '.'

            decimalFormat.groupingSize = 3
            decimalFormat.isGroupingUsed = true
            decimalFormat.decimalFormatSymbols = decimalFormatSymbols

            return decimalFormat.format(totalMoneySpentInMonth)
        }
    }
}