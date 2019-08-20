package com.ogrob.moneybox.presentation.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.presentation.ExpenseActivityViewModel
import com.ogrob.moneybox.utils.NO_CATEGORY_DISPLAY_TEXT
import com.ogrob.moneybox.utils.NO_CATEGORY_ID

class CategoryRecyclerViewAdapter(private val context: Context,
                                  private val expenseActivityViewModel: ExpenseActivityViewModel
)
    : RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {

    private var categoriesWithExpenses: List<CategoryWithExpenses>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.category_list_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (this.categoriesWithExpenses.isNullOrEmpty()) 0 else this.categoriesWithExpenses!!.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        if (!this.categoriesWithExpenses.isNullOrEmpty()) this.categoriesWithExpenses!![position].also { categoryWithExpense ->
            if (categoryWithExpense.category.id == NO_CATEGORY_ID) {
                holder.categoryNameTextView.text = NO_CATEGORY_DISPLAY_TEXT + " (" + categoryWithExpense.expenses.size + ")"
                holder.categoryEditTextView.visibility = View.GONE
                return
            }

            holder.categoryNameTextView.text = categoryWithExpense.category.name + " (" + categoryWithExpense.expenses.size + ")"

            holder.categoryEditTextView.setOnClickListener {
                val categoryNameEditText = EditText(context)
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                categoryNameEditText.setText(categoryWithExpense.category.name)
                categoryNameEditText.layoutParams = layoutParams


                AlertDialog.Builder(this.context)
                    .setTitle("Edit category")
                    .setView(categoryNameEditText)
                    .setPositiveButton("Save") { _, _ ->
                        expenseActivityViewModel.updateCategory(
                            categoryWithExpense.category.id,
                            categoryNameEditText.text.toString())
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    .create()
                    .show()
            }

            // Deleting single category is not working currently, foreign key set default is not setting the id properly
//            holder.categoryDeleteTextView.setOnClickListener {
//                AlertDialog.Builder(this.context)
//                    .setTitle("Are you sure you want to delete this category?")
//                    .setPositiveButton("Delete") { _, _ -> expenseActivityViewModel.deleteCategory(category) }
//                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
//                    .create()
//                    .show()
//            }
        }
    }

    fun setCategories(categoriesWithExpenses: List<CategoryWithExpenses>) {
        this.categoriesWithExpenses = categoriesWithExpenses
        this.notifyDataSetChanged()
    }


    class CategoryViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        var categoryNameTextView: TextView = view.findViewById(R.id.categoryNameTextView)
        var categoryEditTextView: TextView = view.findViewById(R.id.categoryEditTextView)
//        var categoryDeleteTextView: TextView = view.findViewById(R.id.categoryDeleteTextView)

    }
}