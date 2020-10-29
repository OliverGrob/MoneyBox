package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryRepository: CategoryRepository =
        CategoryRepository(application, viewModelScope)


    private val _unfilteredExpenses: MutableLiveData<List<CategoryWithExpenses>> = MutableLiveData()
    val unfilteredExpenses: LiveData<List<CategoryWithExpenses>> = _unfilteredExpenses


    fun getAllFilteredExpenses() {
        viewModelScope.launch {
            _unfilteredExpenses.value = categoryRepository.getAllCategoriesWithExpenses()
        }
    }

}