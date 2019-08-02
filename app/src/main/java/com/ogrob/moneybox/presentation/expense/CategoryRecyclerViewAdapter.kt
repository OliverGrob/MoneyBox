package com.ogrob.moneybox.presentation.expense

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
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.utils.NO_CATEGORY_ID

class CategoryRecyclerViewAdapter(private val context: Context,
                                  private val expenseActivityViewModel: ExpenseActivityViewModel)
    : RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {

    private var categories: List<Category>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.category_list_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (this.categories.isNullOrEmpty()) 0 else this.categories!!.size - 1
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        if (!this.categories.isNullOrEmpty()) this.categories!![position + 1].also { category ->
            if (category.id == NO_CATEGORY_ID)
                return

            holder.categoryNameTextView.text = category.name

            holder.categoryEditTextView.setOnClickListener {
                val categoryNameEditText = EditText(context)
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                categoryNameEditText.setText(category.name)
                categoryNameEditText.layoutParams = layoutParams


                AlertDialog.Builder(this.context)
                    .setTitle("Edit category")
                    .setView(categoryNameEditText)
                    .setPositiveButton("Save") { _, _ ->
                        expenseActivityViewModel.updateCategory(
                            category.id,
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

    fun setCategories(categories: List<Category>) {
        this.categories = categories
        this.notifyDataSetChanged()
    }


    class CategoryViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        var categoryNameTextView: TextView = view.findViewById(R.id.categoryNameTextView)
        var categoryEditTextView: TextView = view.findViewById(R.id.categoryEditTextView)
//        var categoryDeleteTextView: TextView = view.findViewById(R.id.categoryDeleteTextView)

    }
}