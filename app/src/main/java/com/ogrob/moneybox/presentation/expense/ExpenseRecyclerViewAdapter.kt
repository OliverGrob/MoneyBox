package com.ogrob.moneybox.presentation.expense

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.presentation.ExpenseActivityViewModel
import java.time.format.DateTimeFormatter


class ExpenseRecyclerViewAdapter(private val context: Context,
                                 private val expenseActivityViewModel: ExpenseActivityViewModel
)
    : RecyclerView.Adapter<ExpenseRecyclerViewAdapter.ExpenseViewHolder>() {

    private var categories: List<Category>? = null
    private var expenses: List<Expense>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.expense_list_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (this.expenses.isNullOrEmpty()) 0 else this.expenses!!.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        if (!this.expenses.isNullOrEmpty()) this.expenses!![position].also { expense ->
            holder.expenseAdditionDateTextView.text = expense.additionDate.dayOfMonth.toString()
            holder.expenseAmountTextView.text = this.expenseActivityViewModel.formatMoneySpent(expense.amount)
            holder.expenseDescriptionTextView.text = expense.description
            holder.expenseCategoryTextView.text = this.findCategoryName(expense.categoryId)

            holder.expenseOptionsTextView.setOnClickListener {
                val popup = PopupMenu(this.context, holder.expenseOptionsTextView)
                popup.inflate(R.menu.expense_list_item_options)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.editExpense -> {
                            createExpenseEditAlertDialog(expense)
                            true
                        }
                        R.id.deleteExpense -> {
                            createExpenseDeleteAlertDialog(expense)
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
            }
        }
    }

    private fun createExpenseEditAlertDialog(expense: Expense) {
        val intent = Intent(this.context, ExpenseAddAndEditActivity::class.java)
        intent.putExtra("activityTitle", "Edit Expense")
        intent.putExtra("expenseAmount", expense.amount.toString())
        intent.putExtra("expenseDescription", expense.description)
        intent.putExtra("expenseAdditionDate", expense.additionDate.format(DateTimeFormatter.ISO_LOCAL_DATE).toString())
        intent.putExtra("expenseId", expense.id)
        intent.putExtra("categoryId", expense.categoryId)
        intent.putExtra("positiveButtonText", "Save")
        this.context.startActivity(intent)
    }

    private fun createExpenseDeleteAlertDialog(expense: Expense) {
        AlertDialog.Builder(this.context)
            .setTitle("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ -> expenseActivityViewModel.deleteExpense(expense) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun findCategoryName(categoryId: Int): String {
        return this.categories!!
            .filter { category -> category.id == categoryId }
            .map(Category::name)
            .single()
    }

    fun setExpenses(categoryWithExpenses: List<CategoryWithExpenses>) {
        this.categories = categoryWithExpenses
            .map(CategoryWithExpenses::category)

        this.expenses = categoryWithExpenses
            .flatMap { currentCategoryWithExpenses -> currentCategoryWithExpenses.expenses }
            .sortedWith(compareBy(Expense::additionDate))

        this.notifyDataSetChanged()
    }


    class ExpenseViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        var expenseAdditionDateTextView: TextView = view.findViewById(R.id.expenseAdditionDateTextView)
        var expenseAmountTextView: TextView = view.findViewById(R.id.expenseAmountTextView)
        var expenseDescriptionTextView: TextView = view.findViewById(R.id.expenseDescriptionTextView)
        var expenseCategoryTextView: TextView = view.findViewById(R.id.expenseCategoryTextView)
        var expenseOptionsTextView: TextView = view.findViewById(R.id.expenseOptionsTextView)

    }
}