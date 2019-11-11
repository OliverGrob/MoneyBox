package com.ogrob.moneybox.presentation.expense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseActivityViewModel
import com.ogrob.moneybox.databinding.ExpenseListItemBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Expense
import java.time.format.DateTimeFormatter

class ExpenseRecyclerViewAdapter(private val expenseActivityViewModel: ExpenseActivityViewModel,
                                 private val categories: List<Category>)
    : ListAdapter<Expense, ExpenseRecyclerViewAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = getItem(position)
        holder.bind(expenseActivityViewModel, expense, categories)
    }


    class ExpenseViewHolder private constructor(val binding: ExpenseListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseActivityViewModel: ExpenseActivityViewModel,
                 expense: Expense,
                 categories: List<Category>) {
            this.binding.expense = expense
            this.binding.category = categories.single { category -> category.id == expense.categoryId }
            this.binding.executePendingBindings()

            this.binding.expenseOptionsTextView.setOnClickListener { createPopupMenu(it, expense, expenseActivityViewModel) }
        }

        private fun createPopupMenu(it: View,
                                    expense: Expense,
                                    expenseActivityViewModel: ExpenseActivityViewModel) {
            val popup = PopupMenu(this.itemView.context, this.binding.expenseOptionsTextView)
            popup.inflate(R.menu.expense_list_item_options)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.editExpense -> {
                        createExpenseEditAlertDialog(it, expense)
                        true
                    }
                    R.id.deleteExpense -> {
                        createExpenseDeleteAlertDialog(expenseActivityViewModel, expense)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }

        private fun createExpenseEditAlertDialog(view: View, expense: Expense) {
            view.findNavController().navigate(ExpenseFragmentDirections.actionExpenseFragmentToExpenseAddAndEditFragment(
                expense.amount.toString(),
                expense.description,
                expense.additionDate.format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
                expense.id,
                expense.categoryId,
                "Save"
            ))
        }

        private fun createExpenseDeleteAlertDialog(expenseActivityViewModel: ExpenseActivityViewModel, expense: Expense) {
            AlertDialog.Builder(this.itemView.context)
                .setTitle("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete") { _, _ -> expenseActivityViewModel.deleteExpense(expense) }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }


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