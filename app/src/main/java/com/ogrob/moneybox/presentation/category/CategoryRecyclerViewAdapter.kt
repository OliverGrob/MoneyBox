package com.ogrob.moneybox.presentation.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.CategoryListItemBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.utils.NO_CATEGORY_ID

class CategoryRecyclerViewAdapter(private val expenseViewModel: ExpenseViewModel)
    : ListAdapter<CategoryWithExpenses, CategoryRecyclerViewAdapter.CategoryViewHolder>(CategoryDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryWithExpenses = getItem(position)
        holder.bind(expenseViewModel, categoryWithExpenses)
    }


    class CategoryViewHolder private constructor(val binding: CategoryListItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseViewModel: ExpenseViewModel,
                 categoryWithExpenses: CategoryWithExpenses) {
            binding.categoryNameTextView.text = categoryWithExpenses.category.name + " (" + categoryWithExpenses.expenses.size + ")"

            if (categoryWithExpenses.category.id == NO_CATEGORY_ID) {
                binding.categoryEditTextView.visibility = View.GONE
                return
            }

            binding.categoryEditTextView.setOnClickListener {
                val categoryNameEditText = createCategoryNameEditText(categoryWithExpenses.category)

                createCategoryEditAlertDialog(
                    categoryNameEditText,
                    expenseViewModel,
                    categoryWithExpenses.category
                )
            }

                // Deleting single category is not working currently, foreign key set default is not setting the id properly
//            holder.categoryDeleteTextView.setOnClickListener {
//                AlertDialog.Builder(this.context)
//                    .setTitle("Are you sure you want to delete this category?")
//                    .setPositiveButton("Delete") { _, _ -> expenseViewModel.deleteCategory(category) }
//                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
//                    .create()
//                    .show()
//            }
        }

        private fun createCategoryNameEditText(category: Category): EditText {
            val categoryNameEditText = EditText(itemView.context)
            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            categoryNameEditText.setText(category.name)
            categoryNameEditText.layoutParams = layoutParams

            return categoryNameEditText
        }

        private fun createCategoryEditAlertDialog(categoryNameEditText: EditText,
                                                  expenseViewModel: ExpenseViewModel,
                                                  category: Category) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Edit category")
                .setView(categoryNameEditText)
                .setPositiveButton("Save") { _, _ ->
                    expenseViewModel.updateCategory(
                        category.id,
                        categoryNameEditText.text.toString()
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }


        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CategoryListItemBinding.inflate(layoutInflater, parent, false)
                return CategoryViewHolder(binding)
            }
        }

    }
}


class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryWithExpenses>() {

    override fun areItemsTheSame(oldItem: CategoryWithExpenses, newItem: CategoryWithExpenses): Boolean {
        return oldItem.category.id == newItem.category.id
    }

    override fun areContentsTheSame(oldItem: CategoryWithExpenses, newItem: CategoryWithExpenses): Boolean {
        return oldItem == newItem
    }

}