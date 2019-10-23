package com.ogrob.moneybox.presentation.expense

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.ActivityExpensesBinding
import com.ogrob.moneybox.presentation.category.CategoryActivity

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_expenses)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.expense_activity_options, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem) =
        when (menuItem.itemId) {
            R.id.manageCategories -> {
                startActivity(Intent(this, CategoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }

}
