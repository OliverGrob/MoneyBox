package com.ogrob.moneybox.presentation.category

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentCategoryBinding
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID

class CategoryFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseViewModel::class.java)
    }

    private lateinit var binding: FragmentCategoryBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false)

        binding.deleteUnusedCategoriesButton.setOnClickListener { onDeleteUnusedCategories(it) }
        binding.addCategoryButton.setOnClickListener { onAddNewCategory(it) }

        populateCategoryRecyclerView()

        return binding.root
    }

    private fun populateCategoryRecyclerView() {
        expenseViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            val categoryRecyclerViewAdapter = CategoryRecyclerViewAdapter()
            binding.categoryRecyclerView.adapter = categoryRecyclerViewAdapter
            binding.categoryRecyclerView.layoutManager = LinearLayoutManager(context)
            categoryRecyclerViewAdapter.submitList(it)
        })
    }

    private fun onDeleteUnusedCategories(view: View) {
        expenseViewModel.getAllCategoriesWithExpenses().observe(this, Observer {
            expenseViewModel.deleteUnusedCategories(it)
            Toast.makeText(context, "Unused categories have been deleted!", Toast.LENGTH_SHORT).show()
        })
    }

    private fun onAddNewCategory(view: View) {
        view.findNavController().navigate(CategoryFragmentDirections.actionCategoryFragmentToCategoryAddAndEditFragment(
            NEW_CATEGORY_PLACEHOLDER_ID,
            EMPTY_STRING,
            Color.GREEN,
            "Add Category"
        ))
    }

}