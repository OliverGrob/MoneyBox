package com.ogrob.moneybox.presentation.category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseActivityViewModel
import com.ogrob.moneybox.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {

    private val expenseActivityViewModel: ExpenseActivityViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseActivityViewModel::class.java) }

    private lateinit var binding: ActivityCategoryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_category)

        this.populateCategoryRecyclerView()
    }

    private fun populateCategoryRecyclerView() {
        this.expenseActivityViewModel.getAllCategoriesWithExpenses().observe(this, Observer {
            val categoryRecyclerViewAdapter = CategoryRecyclerViewAdapter(
                this,
                this.expenseActivityViewModel
            )
            this.binding.categoryRecyclerView.adapter = categoryRecyclerViewAdapter
            this.binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this)
            categoryRecyclerViewAdapter.setCategories(it)
        })
    }

    fun onDeleteUnusedCategories(view: View) {
        this.expenseActivityViewModel.getAllCategoriesWithExpenses().observe(this, Observer {
            this.expenseActivityViewModel.deleteUnusedCategories(it)
            Toast.makeText(this, "Unused categories have been deleted!", Toast.LENGTH_SHORT).show()
        })
    }
}
