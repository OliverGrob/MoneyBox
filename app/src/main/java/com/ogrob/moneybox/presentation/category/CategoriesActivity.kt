package com.ogrob.moneybox.presentation.category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseActivityViewModel

class CategoriesActivity : AppCompatActivity() {

    private val expenseActivityViewModel: ExpenseActivityViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseActivityViewModel::class.java) }

    private val categoryRecyclerView by lazy { findViewById<RecyclerView>(R.id.categoryRecyclerView) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        this.populateCategoryRecyclerView()
    }

    private fun populateCategoryRecyclerView() {
        this.expenseActivityViewModel.getAllCategoriesWithExpenses().observe(this, Observer {
            val categoryRecyclerViewAdapter = CategoryRecyclerViewAdapter(
                this,
                this.expenseActivityViewModel
            )
            categoryRecyclerView.adapter = categoryRecyclerViewAdapter
            categoryRecyclerView.layoutManager = LinearLayoutManager(this)
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
