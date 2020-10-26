package com.ogrob.moneybox.ui.category

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentCategoryBinding
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.hideLoadingAnimation
import com.ogrob.moneybox.utils.showLoadingAnimation

class CategoryFragment : BaseFragment() {

    private val expenseViewModel: ExpenseViewModel by viewModels()

    private lateinit var binding: FragmentCategoryBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentCategoryBinding.inflate(inflater)

        showLoadingAnimation()

        binding.addCategoryFloatingActionButton.setOnClickListener { onAddNewCategory(it) }

        populateCategoryRecyclerView()

        return binding.root
    }

    private fun populateCategoryRecyclerView() {
        expenseViewModel.getAllCategoriesWithExpenses_OLD().observe(viewLifecycleOwner, Observer {
            hideLoadingAnimation()
            val categoryRecyclerViewAdapter = CategoryRecyclerViewAdapter()
            binding.categoryRecyclerView.adapter = categoryRecyclerViewAdapter
            binding.categoryRecyclerView.layoutManager = LinearLayoutManager(context)
            categoryRecyclerViewAdapter.submitList(it)
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