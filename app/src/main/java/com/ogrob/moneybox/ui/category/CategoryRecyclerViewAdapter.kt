package com.ogrob.moneybox.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.CategoryListItemBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.utils.NO_CATEGORY_ID

class CategoryRecyclerViewAdapter
    : ListAdapter<CategoryWithExpenses, CategoryRecyclerViewAdapter.CategoryViewHolder>(CategoryDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryWithExpenses = getItem(position)
        holder.bind(categoryWithExpenses)
    }


    class CategoryViewHolder private constructor(val binding: CategoryListItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryWithExpenses: CategoryWithExpenses) {
            binding.categoryColorTextView.setBackgroundColor(categoryWithExpenses.category.color)
            binding.categoryNameTextView.text = binding.root.resources.getString(
                R.string.category_name_with_expense_count,
                categoryWithExpenses.category.name,
                categoryWithExpenses.expenses.size
            )

            if (categoryWithExpenses.category.id != NO_CATEGORY_ID) {
                itemView.setOnClickListener { navigateToCategoryEditFragment(it, categoryWithExpenses.category) }
                return
            }
        }

        private fun navigateToCategoryEditFragment(view: View, category: Category) {
            view.findNavController().navigate(CategoryFragmentDirections.actionCategoryFragmentToCategoryAddAndEditFragment(
                category.id,
                category.name,
                category.color,
                binding.root.resources.getString(R.string.save_button)
            ))
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