package com.ogrob.moneybox.presentation.expense

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import java.util.stream.Collectors

class ExpenseRecycleViewAdapter(private val context: Context)
    : RecyclerView.Adapter<ExpenseRecycleViewAdapter.ExpenseViewHolder>() {

    private var categories: List<Category>? = null
    private var expenses: List<Expense>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_expenselistitem, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (this.expenses.isNullOrEmpty()) 0 else this.expenses!!.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        if (!this.expenses.isNullOrEmpty()) this.expenses!![position].also {
            holder.expenseAdditionDate.text = it.additionDate.dayOfMonth.toString()
            holder.expenseAmount.text = if (it.amount == it.amount.toInt().toDouble()) it.amount.toInt().toString() else it.amount.toString()
            holder.expenseDescription.text = it.description + " " + this.findCategoryName(it.categoryId)

            holder.expenseDescription.setOnLongClickListener {
                Toast.makeText(context, categories!!.stream().map(Category::name).collect(Collectors.joining(", ")), Toast.LENGTH_LONG).show()
                return@setOnLongClickListener true
            }
        }
    }

    private fun findCategoryName(categoryId: Int): String {
        return this.categories!!
            .stream()
            .filter { category -> category.id == categoryId }
            .map(Category::name)
            .findAny()
            .orElse("")
    }

    fun setExpenses(expenses: List<CategoryWithExpenses>) {
        this.categories = expenses
            .stream()
            .map(CategoryWithExpenses::category)
            .collect(Collectors.toList())
        this.expenses = expenses
            .stream()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.stream() }
            .sorted { expense1, expense2 -> expense1.additionDate.compareTo(expense2.additionDate) }
            .collect(Collectors.toList())
        this.notifyDataSetChanged()
    }


    class ExpenseViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        var expenseAdditionDate: TextView = view.findViewById(R.id.expenseAdditionDateTextView)
        var expenseAmount: TextView = view.findViewById(R.id.expenseAmountTextView)
        var expenseDescription: TextView = view.findViewById(R.id.expenseDescriptionTextView)

    }
}