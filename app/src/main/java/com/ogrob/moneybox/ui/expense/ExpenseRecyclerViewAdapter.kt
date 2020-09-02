package com.ogrob.moneybox.ui.expense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.ExpenseListItemBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Expense
import java.time.format.DateTimeFormatter

class ExpenseRecyclerViewAdapter(private val expenseViewModel: ExpenseViewModel,
                                 private val categories: List<Category>)
    : ListAdapter<Expense, ExpenseRecyclerViewAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = getItem(position)
        holder.bind(expenseViewModel, expense, categories)
    }


    class ExpenseViewHolder private constructor(val binding: ExpenseListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseViewModel: ExpenseViewModel,
                 expense: Expense,
                 categories: List<Category>) {
            binding.expense = expense
            binding.category = categories.single { category -> category.id == expense.categoryId }
            binding.executePendingBindings()

            itemView.setOnClickListener { navigateToExpenseEditFragment(it, expense) }
        }

        private fun navigateToExpenseEditFragment(view: View, expense: Expense) {
            view.findNavController().navigate(ExpenseDayFragmentDirections.actionExpenseDayFragmentToExpenseAddAndEditFragment(
                expense.amount.toString(),
                expense.description,
                expense.additionDate.format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
                expense.id,
                expense.currency.name,
                expense.categoryId,
                binding.root.resources.getString(R.string.save_button)
            ))
        }

//        private fun createExpenseDeleteAlertDialog(expenseViewModel: ExpenseViewModel, expense: Expense) {
//            AlertDialog.Builder(itemView.context)
//                .setTitle("Are you sure you want to delete this expense?")
//                .setPositiveButton("Delete") { _, _ -> expenseViewModel.deleteExpense(expense) }
//                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
//                .create()
//                .show()
//        }


        companion object {
            fun from(parent: ViewGroup): ExpenseViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ExpenseListItemBinding.inflate(layoutInflater, parent, false)
                return ExpenseViewHolder(binding)
            }
        }
    }
}


class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {

    override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
        return oldItem == newItem
    }

}