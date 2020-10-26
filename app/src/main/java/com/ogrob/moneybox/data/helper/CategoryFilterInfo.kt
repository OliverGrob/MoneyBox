package com.ogrob.moneybox.data.helper

import com.ogrob.moneybox.persistence.model.Category

data class CategoryFilterInfo(
    val selectedCategoryIds: MutableSet<Long>,
    var categoriesWithExpenseCount: Map<Category, Int>,
    var updateFilterOption: UpdateFilterOption
)