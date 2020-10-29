package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryAddAndEditViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryRepository: CategoryRepository =
        CategoryRepository(application, viewModelScope)


    private val _allCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val allCategories: LiveData<List<Category>> = _allCategories


    fun getAllCategories() {
        viewModelScope.launch {
            _allCategories.value = categoryRepository.getAllCategories()
        }
    }

    fun addOrEditCategory(categoryId: Long, categoryName: String, currentTextColor: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (categoryId == NEW_CATEGORY_PLACEHOLDER_ID)
                addNewCategory(
                    categoryName,
                    currentTextColor
                )
            else
                updateCategory(
                    categoryId,
                    categoryName,
                    currentTextColor
                )
        }
    }

    private suspend fun addNewCategory(categoryName: String, categoryColor: Int) {
        this.categoryRepository.addNewCategory(Category(
            categoryName,
            categoryColor))
    }

    private suspend fun updateCategory(categoryId: Long, categoryName: String, categoryColor: Int) {
        this.categoryRepository.updateCategory(Category(
            categoryId,
            categoryName,
            categoryColor))
    }

    fun deleteCategoryById(categoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.deleteCategoryById(categoryId)
        }
    }

}