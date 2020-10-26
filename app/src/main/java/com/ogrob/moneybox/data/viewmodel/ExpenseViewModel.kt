package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.ogrob.moneybox.data.helper.*
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.ui.helper.*
import com.ogrob.moneybox.utils.EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.withSuffix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application, viewModelScope)
    private val categoryRepository: CategoryRepository =
        CategoryRepository(application, viewModelScope)


    private val _filteredExpenses: MutableLiveData<List<Expense>> = MutableLiveData()
    val filteredExpenses: LiveData<List<Expense>> = _filteredExpenses

    private val _unfilteredExpenses: MutableLiveData<List<CategoryWithExpenses>> = MutableLiveData()
    val unfilteredExpenses: LiveData<List<CategoryWithExpenses>> = _unfilteredExpenses

    private val _filteredCategoriesWithExpensesForFilterUpdate: MutableLiveData<UpdatedFilterValuesDTO> = MutableLiveData()
    val filteredCategoriesWithExpensesForFilterUpdate: LiveData<UpdatedFilterValuesDTO> = _filteredCategoriesWithExpensesForFilterUpdate

    private val _allCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val allCategories: LiveData<List<Category>> = _allCategories


    @Deprecated("The new field for this is _allCategoryFilterInfo")
    private var selectedCategoryIds: MutableList<Long> = mutableListOf()
    @Deprecated("The new field for this is _allCurrencyFilterInfo")
    private var selectedCurrencyIds: MutableList<Long> = mutableListOf()
    @Deprecated("The new field for this is _allAmountFilterInfo")
    private var selectedExpenseAmountRange: Pair<Double, Double> =
        Pair(EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE, EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)

    private val _allCategoryFilterInfo: MutableLiveData<CategoryFilterInfo> = MutableLiveData()
    val allCategoryFilterInfo: LiveData<CategoryFilterInfo> = _allCategoryFilterInfo

    private val _allAmountFilterInfo: MutableLiveData<AmountFilterInfo> = MutableLiveData()
    val allAmountFilterInfo: LiveData<AmountFilterInfo> = _allAmountFilterInfo

    private val _allCurrencyFilterInfo: MutableLiveData<CurrencyFilterInfo> = MutableLiveData()
    val allCurrencyFilterInfo: LiveData<CurrencyFilterInfo> = _allCurrencyFilterInfo


    fun getAllCategories() {
        viewModelScope.launch {
            _allCategories.value = categoryRepository.getAllCategories()
        }
    }

    fun getAllFilteredExpenses(filterValuesFromSharedPreferences: Triple<Set<String>, Set<String>, Set<String>>) {
        viewModelScope.launch {
            val allCategoriesWithExpenses = categoryRepository.getAllCategoriesWithExpenses()

            val selectedCategoryIds = getSelectedCategoryIds(filterValuesFromSharedPreferences.first)
            val selectedAmountRange = getSelectedAmountRange(filterValuesFromSharedPreferences.second)
            val selectedCurrencyIds = getSelectedCurrencyIds(filterValuesFromSharedPreferences.third)

            _unfilteredExpenses.value = allCategoriesWithExpenses

            val filteredExpenses = allCategoriesWithExpenses
                .asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                .filter { expense -> isAmountInSelectedExpenseAmountRange(selectedAmountRange.first, selectedAmountRange.second, expense.amount) }
                .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                .toList()

            val updatedCategoryFilterInfo = CategoryFilterInfo(
                selectedCategoryIds,
                createDefaultCategoryFilterInfo(selectedAmountRange, selectedCurrencyIds, allCategoriesWithExpenses),
                UpdateFilterOption.CREATE_CHIPS
            )
            val updatedAmountFilterInfo = createDefaultAmountFilterInfo(
                selectedAmountRange,
                allCategoriesWithExpenses
            )
            val updatedCurrencyFilterInfo = CurrencyFilterInfo(
                selectedCurrencyIds,
                createDefaultCurrencyFilterInfo(selectedCategoryIds, selectedAmountRange, allCategoriesWithExpenses),
                UpdateFilterOption.CREATE_CHIPS
            )

            _allCategoryFilterInfo.value = updatedCategoryFilterInfo
            _allAmountFilterInfo.value = updatedAmountFilterInfo
            _allCurrencyFilterInfo.value = updatedCurrencyFilterInfo

            _filteredExpenses.value = filteredExpenses
        }
    }

    @Deprecated("Use the one without OLD")
    fun getAllFilteredExpenses_OLD(filterValuesFromSharedPreferences: Triple<Set<String>, Set<String>, Set<String>>) {
        viewModelScope.launch {
            // TODO - this should work differently
            /**
             * 1a. Select default filters from shared preferences
             * 1b. There is no value in shared preferences, so there is no filters
             * 2. Filter expenses right at the spot
             */
            val allCategoriesWithExpenses = categoryRepository.getAllCategoriesWithExpenses()

//            selectFiltersDefaults(allCategoriesWithExpenses)
            selectFiltersDefaults_OLD(filterValuesFromSharedPreferences, allCategoriesWithExpenses)

            _unfilteredExpenses.value = allCategoriesWithExpenses

            updateAllFilteredExpenses()
        }
    }

    fun getAllCategoriesWithExpensesForSelectedYearAndMonth(
        filterValuesFromSharedPreferences: Triple<Set<String>, Set<String>, Set<String>>,
        year: Int,
        month: Month
    ) {
        viewModelScope.launch {
//            val allCategoriesWithExpenses = categoryRepository
//                .getAllCategoriesWithExpensesInYearAndMonth(
//                    LocalDateTime.of(year, month, 1, 1, 1, 1).atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
//                    LocalDateTime.of(year, month, month.length(year % 4 == 0), 1, 1, 1).atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
//                )
            val allCategoriesWithExpenses = categoryRepository.getAllCategoriesWithExpenses()
            val filteredCategoryWithExpenses = filterAllExpensesForSelectedYearAndMonth(allCategoriesWithExpenses, year, month)

            val selectedCategoryIds = getSelectedCategoryIds(filterValuesFromSharedPreferences.first)
            val selectedAmountRange = getSelectedAmountRange(filterValuesFromSharedPreferences.second)
            val selectedCurrencyIds = getSelectedCurrencyIds(filterValuesFromSharedPreferences.third)

            _unfilteredExpenses.value = filteredCategoryWithExpenses

            val filteredExpenses = filteredCategoryWithExpenses
                .asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                .filter { expense -> isAmountInSelectedExpenseAmountRange(selectedAmountRange.first, selectedAmountRange.second, expense.amount) }
                .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                .toList()

            val updatedCategoryFilterInfo = CategoryFilterInfo(
                selectedCategoryIds,
                createDefaultCategoryFilterInfo(selectedAmountRange, selectedCurrencyIds, filteredCategoryWithExpenses),
                UpdateFilterOption.CREATE_CHIPS
            )
            val updatedAmountFilterInfo = createDefaultAmountFilterInfo(
                selectedAmountRange,
                filteredCategoryWithExpenses
            )
            val updatedCurrencyFilterInfo = CurrencyFilterInfo(
                selectedCurrencyIds,
                createDefaultCurrencyFilterInfo(selectedCategoryIds, selectedAmountRange, filteredCategoryWithExpenses),
                UpdateFilterOption.CREATE_CHIPS
            )

            _allCategoryFilterInfo.value = updatedCategoryFilterInfo
            _allAmountFilterInfo.value = updatedAmountFilterInfo
            _allCurrencyFilterInfo.value = updatedCurrencyFilterInfo

            _filteredExpenses.value = filteredExpenses
        }
    }

    @Deprecated("Use the one without OLD")
    fun getAllCategoriesWithExpensesForSelectedYearAndMonth_OLD(
        filterValuesFromSharedPreferences: Triple<Set<String>, Set<String>, Set<String>>,
        year: Int,
        month: Month
    ) {
        viewModelScope.launch {
            // TODO - this should work differently
            /**
             * 1a. Select default filters from shared preferences
             * 1b. There is no value in shared preferences, so there is no filters
             * 2. Filter expenses right at the spot
             */
            val allCategoriesWithExpenses = categoryRepository.getAllCategoriesWithExpenses()

//            selectFiltersDefaults(allCategoriesWithExpenses)
            selectFiltersDefaults_OLD(filterValuesFromSharedPreferences, allCategoriesWithExpenses)

            val allCategoriesWithExpensesFilteredForYearAndMonth = filterAllExpensesForSelectedYearAndMonth(allCategoriesWithExpenses, year, month)

            _unfilteredExpenses.value = allCategoriesWithExpensesFilteredForYearAndMonth

            updateAllFilteredExpenses()
        }
    }

    private fun getSelectedCategoryIds(categoryIdsFromSharedPreference: Set<String>): MutableSet<Long> {
        return if (categoryIdsFromSharedPreference.isEmpty())
            mutableSetOf()
        else
            categoryIdsFromSharedPreference
                .map(String::toLong)
                .toMutableSet()
    }

    private fun getSelectedAmountRange(amountRangeFromSharedPreference: Set<String>): Pair<Double, Double> {
        val defaultSelectedExpenseAmountRangeValues = amountRangeFromSharedPreference
            .map(String::toDouble)

        return if (amountRangeFromSharedPreference.isEmpty() || defaultSelectedExpenseAmountRangeValues.iterator().next() == EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)
            Pair(
                EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE,
                EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE
            )
        else
            Pair(
                defaultSelectedExpenseAmountRangeValues.minOrNull()!!,
                defaultSelectedExpenseAmountRangeValues.maxOrNull()!!
            )
    }

    private fun getSelectedCurrencyIds(currencyIdsFromSharedPreference: Set<String>): MutableSet<Long> {
        return if (currencyIdsFromSharedPreference.isEmpty())
            mutableSetOf()
        else
            currencyIdsFromSharedPreference
                .map(String::toLong)
                .toMutableSet()
    }

    private fun createDefaultCategoryFilterInfo(
        selectedAmountRange: Pair<Double, Double>,
        selectedCurrencyIds: MutableSet<Long>,
        allCategoriesWithExpenses: List<CategoryWithExpenses>
    ): Map<Category, Int> {
        return allCategoriesWithExpenses
            .asSequence()
            .map { categoryWithExpenses -> Pair(
                categoryWithExpenses.category,
                categoryWithExpenses.expenses
                    .filter { expense -> isAmountInSelectedExpenseAmountRange(selectedAmountRange.first, selectedAmountRange.second, expense.amount) }
                    .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                    .count()
            ) }
            .toMap()
    }

    private fun createDefaultAmountFilterInfo(
        selectedAmountRange: Pair<Double, Double>,
        allCategoriesWithExpenses: List<CategoryWithExpenses>
    ): AmountFilterInfo {
        val allAmountValues = allCategoriesWithExpenses
            .asSequence()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
            .map(Expense::amount)
            .toList()

        return if (allAmountValues.isEmpty())
            AmountFilterInfo(
                EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE,
                EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE,
                EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE,
                EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE,
                true
            )
        else {
            if (selectedAmountRange.first != EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)
                AmountFilterInfo(
                    allAmountValues.minOrNull()!!,
                    allAmountValues.maxOrNull()!!,
                    selectedAmountRange.first,
                    selectedAmountRange.second,
                    true
                )
            else
                AmountFilterInfo(
                    allAmountValues.minOrNull()!!,
                    allAmountValues.maxOrNull()!!,
                    EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE,
                    EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE,
                    true
                )
        }
    }

    private fun createDefaultCurrencyFilterInfo(
        selectedCategoryIds: MutableSet<Long>,
        selectedAmountRange: Pair<Double, Double>,
        allCategoriesWithExpenses: List<CategoryWithExpenses>
    ): Map<Currency, Int> {
        val currenciesWithExpenses = allCategoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .groupBy(Expense::currency)
            .map { entry -> Pair(entry.key, entry.value) }
            .toMap()

        return currenciesWithExpenses
            .map { entry -> Pair(
                entry.key,
                entry.value
                    .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                    .filter { expense -> isAmountInSelectedExpenseAmountRange(selectedAmountRange.first, selectedAmountRange.second, expense.amount) }
                    .count()
            ) }
            .toMap()
    }

    @Deprecated("Use the one without OLD")
    private fun selectFiltersDefaults_OLD(
        filterValuesFromSharedPreferences: Triple<Set<String>, Set<String>, Set<String>>,
        allCategoriesWithExpenses: List<CategoryWithExpenses>
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            selectedCategoryIds = if (filterValuesFromSharedPreferences.first.isNotEmpty())
                filterValuesFromSharedPreferences.first
                    .map(String::toLong)
                    .toMutableList()
            else
                selectCategories(allCategoriesWithExpenses)

            selectedExpenseAmountRange = if (filterValuesFromSharedPreferences.second.isNotEmpty()) {
                val defaultSelectedExpenseAmountRangeValues =
                    filterValuesFromSharedPreferences.second
                        .map(String::toDouble)
                        .toList()
                Pair(defaultSelectedExpenseAmountRangeValues[0], defaultSelectedExpenseAmountRangeValues[1])
            }
            else
                selectExpenseAmountRange(allCategoriesWithExpenses)

            selectedCurrencyIds = if (filterValuesFromSharedPreferences.third.isNotEmpty())
                filterValuesFromSharedPreferences.third
                    .map(String::toLong)
                    .toMutableList()
            else
                selectCurrencies(allCategoriesWithExpenses)
        }
    }

    @Deprecated("Use the one without OLD")
    private suspend fun selectCategories(categoriesWithExpenses: List<CategoryWithExpenses>): MutableList<Long> {
        return withContext(Dispatchers.Default) {
            categoriesWithExpenses
                .asSequence()
                .map(CategoryWithExpenses::category)
                .map(Category::id)
                .toMutableList()
        }
    }

    @Deprecated("Use the one without OLD")
    private suspend fun selectCurrencies(categoriesWithExpenses: List<CategoryWithExpenses>): MutableList<Long> {
        return withContext(Dispatchers.Default) {
            categoriesWithExpenses
                .asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .map(Expense::currency)
                .map(Currency::id)
                .distinct()
                .toMutableList()
        }
    }

    @Deprecated("Use the one without OLD")
    private suspend fun selectExpenseAmountRange(allCategoriesWithExpenses: List<CategoryWithExpenses>): Pair<Double, Double> {
        return withContext(Dispatchers.Default) {
            val allAmountValues = allCategoriesWithExpenses
                .asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .map(Expense::amount)
                .toList()

            if (allAmountValues.isNullOrEmpty())
                Pair(EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE, EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)
            else
                Pair(allAmountValues.minOrNull()!!, allAmountValues.maxOrNull()!!)
        }
    }

    private suspend fun filterAllExpensesForSelectedYearAndMonth(
        allCategoriesWithExpenses: List<CategoryWithExpenses>,
        year: Int,
        month: Month
    ): List<CategoryWithExpenses> {
        return withContext(Dispatchers.Default) {
            allCategoriesWithExpenses
                .asSequence()
                .map { categoryWithExpenses ->
                    CategoryWithExpenses(
                        categoryWithExpenses.category,
                        categoryWithExpenses.expenses
                            .filter { expense -> expense.additionDate.year == year }
                            .filter { expense -> expense.additionDate.month == month }
                    )
                }
                .filter { categoryWithExpenses -> categoryWithExpenses.expenses.isNotEmpty() }
                .toList()
        }
    }

    fun selectAllCategoryFilters() {
        viewModelScope.launch {
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                CategoryFilterInfo(
                    it.categoriesWithExpenseCount.keys.map(Category::id).toMutableSet(),
                    it.categoriesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCategoryFilterInfo.postValue(updatedCategoryFilterInfo)
            val updatedFilteredExpenses =  _unfilteredExpenses.value?.let {
                it.asSequence()
                    .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                    .filter { expense -> updatedCategoryFilterInfo!!.selectedCategoryIds.contains(expense.categoryId) }
                    .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                    .filter { expense -> _allCurrencyFilterInfo.value!!.selectedCurrencyIds.contains(expense.currency.id) }
                    .toList()
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)
            val updatedCurrenciesWithExpenses = _unfilteredExpenses.value?.let {
                it.asSequence()
                    .flatMap(CategoryWithExpenses::expenses)
                    .groupBy(Expense::currency)
                    .map { entry -> Pair(entry.key, entry.value) }
                    .toMap()
            }
            val updatedCurrenciesWithExpenseCount = updatedCurrenciesWithExpenses?.let {
                it.asSequence()
                    .map { entry -> Pair(
                        entry.key,
                        entry.value
                            .asSequence()
                            .filter { expense -> updatedCategoryFilterInfo!!.selectedCategoryIds.contains(expense.categoryId) }
                            .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                            .count()
                    )
                    }
                    .toMap()
            }
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                CurrencyFilterInfo(
                    it.selectedCurrencyIds,
                    updatedCurrenciesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCurrencyFilterInfo.postValue(updatedCurrencyFilterInfo)
        }
    }

    fun deSelectAllCategoryFilters() {
        viewModelScope.launch {
            _allCategoryFilterInfo.value?.let {
                val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                    CategoryFilterInfo(
                        mutableSetOf(),
                        it.categoriesWithExpenseCount,
                        UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                    )
                }
                _allCategoryFilterInfo.postValue(updatedCategoryFilterInfo)
                val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                    it.asSequence()
                        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                        .filter { expense -> updatedCategoryFilterInfo!!.selectedCategoryIds.contains(expense.categoryId) }
                        .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                        .filter { expense -> _allCurrencyFilterInfo.value!!.selectedCurrencyIds.contains(expense.currency.id) }
                        .toList()
                }
                _filteredExpenses.postValue(updatedFilteredExpenses)
                val updatedCategoriesWithExpenses = _unfilteredExpenses.value?.let {
                    it.asSequence()
                        .flatMap(CategoryWithExpenses::expenses)
                        .groupBy(Expense::currency)
                        .map { entry -> Pair(entry.key, entry.value) }
                        .toMap()
                }
                val updatedCurrenciesWithExpenseCount = updatedCategoriesWithExpenses?.let {
                    it.asSequence()
                        .map { entry -> Pair(
                            entry.key,
                            entry.value
                                .asSequence()
                                .filter { expense -> updatedCategoryFilterInfo!!.selectedCategoryIds.contains(expense.categoryId) }
                                .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                                .count()
                        )
                        }
                        .toMap()
                }
                val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                    CurrencyFilterInfo(
                        it.selectedCurrencyIds,
                        updatedCurrenciesWithExpenseCount!!,
                        UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                    )
                }
                _allCurrencyFilterInfo.postValue(updatedCurrencyFilterInfo)
            }
        }
    }

    fun selectAllCurrencyFilters() {
        viewModelScope.launch {
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                CurrencyFilterInfo(
                    it.currenciesWithExpenseCount.keys.map(Currency::id).toMutableSet(),
                    it.currenciesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCurrencyFilterInfo.postValue(updatedCurrencyFilterInfo)
            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                it.asSequence()
                    .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                    .filter { expense -> _allCategoryFilterInfo.value!!.selectedCategoryIds.contains(expense.categoryId) }
                    .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                    .filter { expense -> updatedCurrencyFilterInfo!!.selectedCurrencyIds.contains(expense.currency.id) }
                    .toList()
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)
            val updatedCategoriesWithExpenseCount = _unfilteredExpenses.value?.let {
                it.asSequence()
                    .map { categoryWithExpenses ->
                        Pair(
                            categoryWithExpenses.category,
                            categoryWithExpenses.expenses
                                .asSequence()
                                .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                                .filter { expense -> updatedCurrencyFilterInfo!!.selectedCurrencyIds.contains(expense.currency.id) }
                                .count()
                        )
                    }
                    .toMap()
            }
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                CategoryFilterInfo(
                    it.selectedCategoryIds,
                    updatedCategoriesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCategoryFilterInfo.postValue(updatedCategoryFilterInfo)
        }
    }

    fun deSelectAllCurrencyFilters() {
        viewModelScope.launch {
            _allCurrencyFilterInfo.value?.let {
                val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                    CurrencyFilterInfo(
                        mutableSetOf(),
                        it.currenciesWithExpenseCount,
                        UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                    )
                }
                _allCurrencyFilterInfo.postValue(updatedCurrencyFilterInfo)
                val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                    it.asSequence()
                        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                        .filter { expense -> _allCategoryFilterInfo.value!!.selectedCategoryIds.contains(expense.categoryId) }
                        .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                        .filter { expense -> updatedCurrencyFilterInfo!!.selectedCurrencyIds.contains(expense.currency.id) }
                        .toList()
                }
                _filteredExpenses.postValue(updatedFilteredExpenses)
                val updatedCategoriesWithExpenseCount = _unfilteredExpenses.value?.let {
                    it.asSequence()
                        .map { categoryWithExpenses ->
                            Pair(
                                categoryWithExpenses.category,
                                categoryWithExpenses.expenses
                                    .asSequence()
                                    .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                                    .filter { expense -> updatedCurrencyFilterInfo!!.selectedCurrencyIds.contains(expense.currency.id) }
                                    .count()
                            )
                        }
                        .toMap()
                }
                val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                    CategoryFilterInfo(
                        it.selectedCategoryIds,
                        updatedCategoriesWithExpenseCount!!,
                        UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                    )
                }
                _allCategoryFilterInfo.postValue(updatedCategoryFilterInfo)
            }
        }
    }

    fun updateAllFilteredExpenses() {
        viewModelScope.launch {
            _filteredExpenses.value = filterAllExpensesThroughFiltersOnly(_unfilteredExpenses.value!!)
        }
    }

    private suspend fun filterAllExpensesThroughFiltersOnly(unfilteredExpenses: List<CategoryWithExpenses>): List<Expense> {
        return withContext(Dispatchers.Default) {
            unfilteredExpenses
                .asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                .filter { expense -> isAmountInSelectedExpenseAmountRange_OLD(expense.amount) }
                .toList()
        }
    }

    fun updateFilters() {
        viewModelScope.launch {
            val updatedFilterValueCalculator = UpdatedFilterValueCalculator(
                _unfilteredExpenses.value!!,
                selectedCategoryIds.toList(),
                selectedCurrencyIds.toList(),
                selectedExpenseAmountRange
            )
            _filteredCategoriesWithExpensesForFilterUpdate.value = updatedFilterValueCalculator.updatedFilterValuesDTO
        }
    }

    private fun isAmountInSelectedExpenseAmountRange(selectedAmountMinValue: Double, selectedAmountMaxValue: Double, amount: Double) =
        if (selectedAmountMinValue != EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)
            amount in selectedAmountMinValue..selectedAmountMaxValue
        else
            true

    @Deprecated("Use the one without OLD")
    private fun isAmountInSelectedExpenseAmountRange_OLD(amount: Double) =
        selectedExpenseAmountRange.first <= amount && selectedExpenseAmountRange.second >= amount

    fun groupExpensesByYearAndMonth(expenses: List<Expense>): List<Any> =
        expenses
            .groupBy { expense -> expense.additionDate.year }
            .toSortedMap()
            .flatMap(this::createExpensesDTOsForYear)

    private fun createExpensesDTOsForYear(expensesByYear: Map.Entry<Int, List<Expense>>): List<Any> {
        val expensesByMonth = expensesByYear.value
            .groupBy { expense -> expense.additionDate.month }
            .map { currentExpensesSortedByYear ->
                ExpensesByMonth(
                    expensesByYear.key,
                    currentExpensesSortedByYear.key,
                    currentExpensesSortedByYear.value
                )
            }
            .sortedBy(ExpensesByMonth::month)

        return listOf(
            ExpensesByYear(expensesByYear.key, expensesByYear.value),
            *expensesByMonth.toTypedArray()
        )
    }

    fun createNormalAndHeaderExpenses(filteredExpenses: List<Expense>): List<ExpenseDTOForAdapter> {
        val daysWithHeader = mutableListOf<LocalDateTime>()

        return filteredExpenses
            .sortedBy(Expense::additionDate)
            .map { expense -> chooseNormalOrHeaderCategoryForExpense(expense, daysWithHeader) }
    }

    private fun chooseNormalOrHeaderCategoryForExpense(
        expense: Expense,
        daysWithHeader: MutableList<LocalDateTime>
    ): ExpenseDTOForAdapter {
        if (daysWithHeader.contains(expense.additionDate))
            return ExpenseDTOForAdapter(expense)

        daysWithHeader.add(expense.additionDate)
        return ExpenseDTOForAdapter(expense, true)
    }

    fun getTotalMoneySpentFormatted(expensesSelected: List<Expense>): String {
        val totalMoneySpentWithoutFormatting = expensesSelected
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

        return withSuffix(totalMoneySpentWithoutFormatting)
    }

    fun getCategoriesWithExpenseCount(allCategoriesWithExpenses: List<CategoryWithExpenses>): Map<Category, Int> =
        allCategoriesWithExpenses
            .map { categoryWithExpense ->
                Pair(
                    categoryWithExpense.category,
                    categoryWithExpense.expenses.size
                )
            }
            .toMap()

    @Deprecated("Use the one without OLD")
    fun isCategorySelected(categoryId: Long) = selectedCategoryIds.contains(categoryId)

    @Deprecated("Use the one without OLD")
    fun allCategorySelected() = selectedCategoryIds

    fun getCurrentlySelectedCategoryIds() =
        _allCategoryFilterInfo.value!!.selectedCategoryIds

    fun toggleCategoryFilter(categoryId: Long) {
        viewModelScope.launch {
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                if (it.selectedCategoryIds.contains(categoryId))
                    it.selectedCategoryIds.remove(categoryId)
                else
                    it.selectedCategoryIds.add(categoryId)

                CategoryFilterInfo(
                    it.selectedCategoryIds,
                    it.categoriesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCategoryFilterInfo.postValue(updatedCategoryFilterInfo)
            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                it.asSequence()
                    .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                    .filter { expense -> updatedCategoryFilterInfo!!.selectedCategoryIds.contains(expense.categoryId) }
                    .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                    .filter { expense -> _allCurrencyFilterInfo.value!!.selectedCurrencyIds.contains(expense.currency.id) }
                    .toList()
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)
            val updatedCurrenciesWithExpenses = _unfilteredExpenses.value?.let {
                it.asSequence()
                    .flatMap(CategoryWithExpenses::expenses)
                    .groupBy(Expense::currency)
                    .map { entry -> Pair(entry.key, entry.value) }
                    .toMap()
            }
            val updatedCurrenciesWithExpenseCount = updatedCurrenciesWithExpenses?.let {
                it.asSequence()
                    .map { entry -> Pair(
                        entry.key,
                        entry.value
                            .asSequence()
                            .filter { expense -> updatedCategoryFilterInfo!!.selectedCategoryIds.contains(expense.categoryId) }
                            .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                            .count()
                    )
                    }
                    .toMap()
            }
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                CurrencyFilterInfo(
                    it.selectedCurrencyIds,
                    updatedCurrenciesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCurrencyFilterInfo.postValue(updatedCurrencyFilterInfo)
        }
    }

    @Deprecated("Use the one without OLD")
    fun toggleCategoryFilter_OLD(categoryId: Long) {
        if (isCategorySelected(categoryId))
            removeCategoryFilter(categoryId)
        else
            addCategoryFilter(categoryId)
    }

    @Deprecated("Use the one without OLD")
    fun removeCategoryFilter(categoryId: Long) {
        selectedCategoryIds.remove(categoryId)
    }

    @Deprecated("Use the one without OLD")
    fun addCategoryFilter(categoryId: Long) {
        selectedCategoryIds.add(categoryId)
    }

    @Deprecated("Use the one without OLD")
    fun isCurrencySelected(currencyId: Long) = selectedCurrencyIds.contains(currencyId)

    @Deprecated("Use the one without OLD")
    fun allCurrencySelected() = selectedCurrencyIds

    fun getCurrentlySelectedCurrencyIds() =
        _allCurrencyFilterInfo.value!!.selectedCurrencyIds

    fun toggleCurrencyFilter(currencyId: Long) {
        viewModelScope.launch {
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                if (it.selectedCurrencyIds.contains(currencyId))
                    it.selectedCurrencyIds.remove(currencyId)
                else
                    it.selectedCurrencyIds.add(currencyId)

                CurrencyFilterInfo(
                    it.selectedCurrencyIds,
                    it.currenciesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCurrencyFilterInfo.postValue(updatedCurrencyFilterInfo)
            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                it.asSequence()
                    .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                    .filter { expense -> _allCategoryFilterInfo.value!!.selectedCategoryIds.contains(expense.categoryId) }
                    .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                    .filter { expense -> updatedCurrencyFilterInfo!!.selectedCurrencyIds.contains(expense.currency.id) }
                    .toList()
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)
            val updatedCurrenciesWithExpenseCount = _unfilteredExpenses.value?.let {
                it.asSequence()
                    .map { categoryWithExpenses ->
                        Pair(
                            categoryWithExpenses.category,
                            categoryWithExpenses.expenses
                                .asSequence()
                                .filter { expense -> isAmountInSelectedExpenseAmountRange(_allAmountFilterInfo.value!!.selectedAmountMinValue, _allAmountFilterInfo.value!!.selectedAmountMaxValue, expense.amount) }
                                .filter { expense -> updatedCurrencyFilterInfo!!.selectedCurrencyIds.contains(expense.currency.id) }
                                .count()
                        )
                    }
                    .toMap()
            }
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                CategoryFilterInfo(
                    it.selectedCategoryIds,
                    updatedCurrenciesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCategoryFilterInfo.postValue(updatedCategoryFilterInfo)
        }
    }

    @Deprecated("Use the one without OLD")
    fun toggleCurrencyFilter_OLD(currencyId: Long) {
        if (isCurrencySelected(currencyId))
            removeCurrencyFilter(currencyId)
        else
            addCurrencyFilter(currencyId)
    }

    @Deprecated("Use the one without OLD")
    fun removeCurrencyFilter(currencyId: Long) {
        selectedCurrencyIds.remove(currencyId)
    }

    @Deprecated("Use the one without OLD")
    fun addCurrencyFilter(currencyId: Long) {
        selectedCurrencyIds.add(currencyId)
    }

    fun setMinAndMaxSelectedAmount(
        minAmount: Double,
        maxAmount: Double,
        minSelectedAmount: Double,
        maxSelectedAmount: Double
    ) {
        val updatedAmountFilterInfo = AmountFilterInfo(
            minAmount,
            maxAmount,
            minSelectedAmount,
            maxSelectedAmount,
            false
        )
        _allAmountFilterInfo.postValue(updatedAmountFilterInfo)
        val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
            it.asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .filter { expense -> _allCategoryFilterInfo.value!!.selectedCategoryIds.contains(expense.categoryId) }
                .filter { expense -> isAmountInSelectedExpenseAmountRange(updatedAmountFilterInfo.selectedAmountMinValue, updatedAmountFilterInfo.selectedAmountMaxValue, expense.amount) }
                .filter { expense -> _allCurrencyFilterInfo.value!!.selectedCurrencyIds.contains(expense.currency.id) }
                .toList()
        }
        _filteredExpenses.postValue(updatedFilteredExpenses)
        val updatedCategoriesWithExpenseCount = _unfilteredExpenses.value?.let {
            it.asSequence()
                .map { categoryWithExpenses ->
                    Pair(
                        categoryWithExpenses.category,
                        categoryWithExpenses.expenses
                            .asSequence()
                            .filter { expense -> isAmountInSelectedExpenseAmountRange(updatedAmountFilterInfo.selectedAmountMinValue, updatedAmountFilterInfo.selectedAmountMaxValue, expense.amount) }
                            .filter { expense -> _allCurrencyFilterInfo.value!!.selectedCurrencyIds.contains(expense.currency.id) }
                            .count()
                    )
                }
                .toMap()
        }
        val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
            CategoryFilterInfo(
                it.selectedCategoryIds,
                updatedCategoriesWithExpenseCount!!,
                UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
            )
        }
        _allCategoryFilterInfo.postValue(updatedCategoryFilterInfo)
        val updatedCurrenciesWithExpenses = _unfilteredExpenses.value?.let {
            it.asSequence()
                .flatMap(CategoryWithExpenses::expenses)
                .groupBy(Expense::currency)
                .map { entry -> Pair(entry.key, entry.value) }
                .toMap()
        }
        val updatedCurrenciesWithExpenseCount = updatedCurrenciesWithExpenses?.let {
            it.asSequence()
                .map { entry -> Pair(
                    entry.key,
                    entry.value
                        .asSequence()
                        .filter { expense -> updatedCategoryFilterInfo!!.selectedCategoryIds.contains(expense.categoryId) }
                        .filter { expense -> isAmountInSelectedExpenseAmountRange(updatedAmountFilterInfo.selectedAmountMinValue, updatedAmountFilterInfo.selectedAmountMaxValue, expense.amount) }
                        .count()
                )
                }
                .toMap()
        }
        val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
            CurrencyFilterInfo(
                it.selectedCurrencyIds,
                updatedCurrenciesWithExpenseCount!!,
                UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
            )
        }
        _allCurrencyFilterInfo.postValue(updatedCurrencyFilterInfo)
    }

    @Deprecated("Use the one without OLD")
    fun setMinAndMaxAmount_OLD(minAmount: Double, maxAmount: Double) {
        selectedExpenseAmountRange = Pair(minAmount, maxAmount)
    }

    @Deprecated("Use the one without OLD")
    fun getCheapestAndMostExpensiveExpenseAmount() = Pair(selectedExpenseAmountRange.first, selectedExpenseAmountRange.second)

    fun getCurrenciesWithExpenseCount(allCategoriesWithExpenses: List<CategoryWithExpenses>): Map<Currency, Int> =
        allCategoriesWithExpenses
            .asSequence()
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
            .groupBy(Expense::currency)
            .map { currencyWithExpenses ->
                Pair(
                    currencyWithExpenses.key,
                    currencyWithExpenses.value.size
                )
            }
            .toMap()









    /** OLD STUFF */
    private val categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = expenseRepository.getAllCategoriesWithExpenses()
    private val categories: LiveData<List<Category>> = liveData { categoryRepository.getAllCategories() }

    private val _selectedFixedInterval: MutableLiveData<FixedInterval> = MutableLiveData()
    val selectedFixedInterval: LiveData<FixedInterval> = _selectedFixedInterval

    private val _selectedCurrency: MutableLiveData<Currency> = MutableLiveData()


    val selectedCurrency: LiveData<Currency> = _selectedCurrency

    private var categoryDropdownIsOpen = false

    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>> = this.categoriesWithExpenses

    fun getAllCategories_OLD(): LiveData<List<Category>> = this.categories

    suspend fun addNewExpense(expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        this.expenseRepository.addNewExpense(Expense(
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            Currency.valueOf(currency),
            categoryId))
    }

    suspend fun addNewCategory(categoryName: String, categoryColor: Int) {
        this.categoryRepository.addNewCategory(Category(
            categoryName,
            categoryColor))
    }

    suspend fun updateExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        this.expenseRepository.updateExpense(Expense(
            expenseId,
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            Currency.valueOf(currency),
            categoryId))
    }

    suspend fun updateCategory(categoryId: Long, categoryName: String, categoryColor: Int) {
        this.categoryRepository.updateCategory(Category(
            categoryId,
            categoryName,
            categoryColor))
    }

    suspend fun deleteExpense(expense: Expense) {
        this.expenseRepository.deleteExpense(expense)
    }

    fun deleteCategoryById(categoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.deleteCategoryById(categoryId)
        }
    }

    fun getAllExpensesDescription(categoriesWithExpenses: List<CategoryWithExpenses>): List<String> = categoriesWithExpenses
        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
        .map(Expense::description)
        .distinct()

    fun addOrEditExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, currency: String, categoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (expenseId == NEW_EXPENSE_PLACEHOLDER_ID)
                addNewExpense(
                    expenseAmount,
                    expenseDescription,
                    expenseDate,
                    currency,
                    categoryId
                )
            else
                updateExpense(
                    expenseId,
                    expenseAmount,
                    expenseDescription,
                    expenseDate,
                    currency,
                    categoryId
                )
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

    fun getTotalMoneySpentUnformatted(expensesSelected: List<Expense>): Double =
        expensesSelected
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }









    fun getExpenseByDescription(description: String): Expense {
        return this.getAllCategoriesWithExpenses().value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .distinctBy { expense -> expense.description }
            .single { expense -> expense.description == description }
    }

    fun getFilteredExpensesForFixedIntervalUsingAllFilters(
        year: Int = -1,
        monthIndex: Int = -1
    ): LiveData<List<CategoryWithExpenses>> {
        return Transformations
            .switchMap(getAllCategoriesWithExpenses()) { categoriesWithExpenses ->
                this.filterExpensesUsingAllFilters(
                    categoriesWithExpenses,
                    year,
                    monthIndex
                )
            }
    }

    private fun filterExpensesUsingAllFilters(
        categoriesWithExpenses: List<CategoryWithExpenses>,
        year: Int,
        monthIndex: Int
    ): LiveData<List<CategoryWithExpenses>> {
        val filteredCategoriesWithExpenses = categoriesWithExpenses
            .filter(this::categoryIsSelectedInternal)
            .map { categoryWithExpenses ->
                filterExpensesForRemainingFiltersAndFixedInterval(
                    categoryWithExpenses,
                    year,
                    monthIndex
                )
            }
            .filter(this::categoryHasExpenses)

        return MutableLiveData(filteredCategoriesWithExpenses)
    }

    private fun categoryIsSelectedInternal(categoryWithExpenses: CategoryWithExpenses): Boolean {
        if (noSelectedCategory())
            return true

        return selectedCategoryIds.contains(categoryWithExpenses.category.id)
    }

    private fun noSelectedCategory(): Boolean = selectedCategoryIds.isEmpty()

    private fun filterExpensesForRemainingFiltersAndFixedInterval(
        categoryWithExpenses: CategoryWithExpenses,
        year: Int,
        monthIndex: Int
    ): CategoryWithExpenses {
        val filteredExpenses = categoryWithExpenses.expenses
            // Filter for currency here
//            .filter(currencyFilter)
            .filter(this::expenseAmountIsInSelectedRange)
            .filter { expense -> filterForYear(expense, year) }
            .filter { expense -> filterForMonth(expense, monthIndex) }

        return CategoryWithExpenses(categoryWithExpenses.category, filteredExpenses)
    }

    private fun filterForYear(
        expense: Expense,
        year: Int
    ): Boolean {
        if (year == -1)
            return true

        return expense.additionDate.year == year
    }

    private fun filterForMonth(
        expense: Expense,
        monthIndex: Int
    ): Boolean {
        if (monthIndex == -1)
            return true

        return expense.additionDate.monthValue == monthIndex
    }

    private fun expenseAmountIsInSelectedRange(expense: Expense): Boolean {
        if (noSelectedAmountRange())
            return true

        return expense.amount >= selectedExpenseAmountRange.first && expense.amount <= selectedExpenseAmountRange.second
    }

    private fun noSelectedAmountRange() =
        selectedExpenseAmountRange.first == EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE &&
                selectedExpenseAmountRange.second == EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE

    private fun categoryHasExpenses(categoryWithExpenses: CategoryWithExpenses): Boolean =
        categoryWithExpenses.expenses.isNotEmpty()

    fun categoryIsSelected(categoryId: Long): Boolean = selectedCategoryIds.contains(categoryId)

    fun groupExpensesByMonthInYear(expenses: List<Expense>, year: Int): List<ExpensesByMonth> =
        expenses
            .groupBy { expense -> expense.additionDate.month }
            .map { currentExpensesSortedByYear ->
                ExpensesByMonth(
                    year,
                    currentExpensesSortedByYear.key,
                    currentExpensesSortedByYear.value
                )
            }
            .sortedBy(ExpensesByMonth::month)

    fun setSelectedFixedInterval(selectedFixedInterval: FixedInterval) {
        _selectedFixedInterval.value = selectedFixedInterval
    }

    fun setSelectedCurrency(selectedCurrency: Currency) {
        _selectedCurrency.value = selectedCurrency
    }

    fun calculateYearlyTotalAverage(expenses: List<Expense>): Double {
        val totalAmount = getTotalMoneySpentUnformatted(expenses)

        val numOfYears = expenses
            .map { expense -> expense.additionDate.year }
            .distinct()
            .count()

        return calculateAverage(totalAmount, numOfYears)
    }

    fun calculateMonthlyTotalAverage(expenses: List<Expense>): Double {
        val totalAmount = getTotalMoneySpentUnformatted(expenses)

        val numOfMonths = expenses
            .map(Expense::additionDate)
            .distinctBy { it.year to it.month }
            .count()

        return calculateAverage(totalAmount, numOfMonths)
    }

    fun calculateDailyTotalAverage(expenses: List<Expense>): Double {
        val totalAmount = getTotalMoneySpentUnformatted(expenses)

        val numOfDays = expenses
            .map(Expense::additionDate)
            .distinctBy { it.year to it.month to it.dayOfMonth }
            .count()

        return calculateAverage(totalAmount, numOfDays)
    }

    private fun calculateAverage(totalAmount: Double, numOfInterval: Int): Double {
        return if (numOfInterval == 0) 0.0 else totalAmount.div(numOfInterval)
    }

    fun isCategoryDropdownOpen(): Boolean = categoryDropdownIsOpen

    fun openCategoryDropdown() {
        categoryDropdownIsOpen = true
    }

    fun closeCategoryDropdown() {
        categoryDropdownIsOpen = false
    }

    fun getExpensesWithoutCategoryFiltering(
        year: Int = -1,
        monthIndex: Int = -1
    ): LiveData<List<CategoryWithExpenses>> {
        return Transformations
            .switchMap(getAllCategoriesWithExpenses()) { categoriesWithExpenses ->
                this.filterExpensesForFixedIntervalWithoutCategoryFiltering(
                    categoriesWithExpenses,
                    year,
                    monthIndex
                )
            }
    }

    private fun filterExpensesForFixedIntervalWithoutCategoryFiltering(
        categoriesWithExpenses: List<CategoryWithExpenses>,
        year: Int,
        monthIndex: Int
    ): LiveData<List<CategoryWithExpenses>> {
        val filteredCategoriesWithExpenses = categoriesWithExpenses
            .map { categoryWithExpenses ->
                filterExpensesForRemainingFiltersAndFixedInterval(
                    categoryWithExpenses,
                    year,
                    monthIndex
                )
            }

        return MutableLiveData(filteredCategoriesWithExpenses)
    }

}
