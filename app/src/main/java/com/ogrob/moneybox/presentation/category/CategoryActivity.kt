package com.ogrob.moneybox.presentation.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_category)
    }

}
