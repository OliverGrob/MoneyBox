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
import com.ogrob.moneybox.ui.helper.ExpenseDTOForAdapter
import com.ogrob.moneybox.ui.helper.ExpensesByMonth
import com.ogrob.moneybox.ui.helper.ExpensesByYear
import com.ogrob.moneybox.utils.EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.withSuffix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.Month

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application, viewModelScope)
    private val categoryRepository: CategoryRepository =
        CategoryRepository(application, viewModelScope)


    private val _filteredExpenses: MutableLiveData<List<Expense>> = MutableLiveData()
    val filteredExpenses: LiveData<List<Expense>> = _filteredExpenses

    private val _unfilteredExpenses: MutableLiveData<List<CategoryWithExpenses>> = MutableLiveData()
    val unfilteredExpenses: LiveData<List<CategoryWithExpenses>> = _unfilteredExpenses

    private val _allCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val allCategories: LiveData<List<Category>> = _allCategories


    private val _allCategoryFilterInfo: MutableLiveData<Event<CategoryFilterInfo>> = MutableLiveData()
    val allCategoryFilterInfo: LiveData<Event<CategoryFilterInfo>> = _allCategoryFilterInfo

    private val _allAmountFilterInfo: MutableLiveData<Event<AmountFilterInfo>> = MutableLiveData()
    val allAmountFilterInfo: LiveData<Event<AmountFilterInfo>> = _allAmountFilterInfo

    private val _allCurrencyFilterInfo: MutableLiveData<Event<CurrencyFilterInfo>> = MutableLiveData()
    val allCurrencyFilterInfo: LiveData<Event<CurrencyFilterInfo>> = _allCurrencyFilterInfo


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

            _allCategoryFilterInfo.value = Event(updatedCategoryFilterInfo)
            _allAmountFilterInfo.value = Event(updatedAmountFilterInfo)
            _allCurrencyFilterInfo.value = Event(updatedCurrencyFilterInfo)

            _filteredExpenses.value = filteredExpenses
        }
    }

    fun getAllCategoriesWithExpensesForSelectedYearAndMonth(
        filterValuesFromSharedPreferences: Triple<Set<String>, Set<String>, Set<String>>,
        year: Int,
        month: Month
    ) {
        viewModelScope.launch {
            val filteredCategoryWithExpenses = filterAllExpensesForSelectedYearAndMonth(
                categoryRepository.getAllCategoriesWithExpenses(),
                year,
                month
            )

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

            _allCategoryFilterInfo.value = Event(updatedCategoryFilterInfo)
            _allAmountFilterInfo.value = Event(updatedAmountFilterInfo)
            _allCurrencyFilterInfo.value = Event(updatedCurrencyFilterInfo)

            _filteredExpenses.value = filteredExpenses
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
                    it.peekContent().categoriesWithExpenseCount.keys.map(Category::id).toMutableSet(),
                    it.peekContent().categoriesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCategoryFilterInfo.postValue(Event(updatedCategoryFilterInfo!!))


            val allAmountFilterInfo = _allAmountFilterInfo.value!!.peekContent()
            val allCurrencyFilterInfo = _allCurrencyFilterInfo.value!!.peekContent()

            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                filterUpdatedFilteredExpenses(
                    it,
                    updatedCategoryFilterInfo.selectedCategoryIds,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                    allCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)


            val updatedCurrenciesWithExpenseCount = _unfilteredExpenses.value?.let {
                calculateUpdatedCategoriesWithExpenses(
                    it,
                    updatedCategoryFilterInfo.selectedCategoryIds,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue)
                )
            }
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                CurrencyFilterInfo(
                    it.peekContent().selectedCurrencyIds,
                    updatedCurrenciesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCurrencyFilterInfo.postValue(Event(updatedCurrencyFilterInfo!!))
        }
    }

    fun deSelectAllCategoryFilters() {
        viewModelScope.launch {
            _allCategoryFilterInfo.value?.let {
                val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                    CategoryFilterInfo(
                        mutableSetOf(),
                        it.peekContent().categoriesWithExpenseCount,
                        UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                    )
                }
                _allCategoryFilterInfo.postValue(Event(updatedCategoryFilterInfo!!))


                val allAmountFilterInfo = _allAmountFilterInfo.value!!.peekContent()
                val allCurrencyFilterInfo = _allCurrencyFilterInfo.value!!.peekContent()

                val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                    filterUpdatedFilteredExpenses(
                        it,
                        updatedCategoryFilterInfo.selectedCategoryIds,
                        Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                        allCurrencyFilterInfo.selectedCurrencyIds
                    )
                }
                _filteredExpenses.postValue(updatedFilteredExpenses)


                val updatedCurrenciesWithExpenseCount = _unfilteredExpenses.value?.let {
                    calculateUpdatedCategoriesWithExpenses(
                        it,
                        updatedCategoryFilterInfo.selectedCategoryIds,
                        Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue)
                    )
                }
                val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                    CurrencyFilterInfo(
                        it.peekContent().selectedCurrencyIds,
                        updatedCurrenciesWithExpenseCount!!,
                        UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                    )
                }
                _allCurrencyFilterInfo.postValue(Event(updatedCurrencyFilterInfo!!))
            }
        }
    }

    private suspend fun calculateUpdatedCategoriesWithExpenses(
        categoriesWithExpenses: List<CategoryWithExpenses>,
        selectedCategoryIds: MutableSet<Long>,
        selectedAmountRange: Pair<Double, Double>
    ) : Map<Currency, Int> {
        return withContext(Dispatchers.Default) {
            categoriesWithExpenses
                .asSequence()
                .flatMap(CategoryWithExpenses::expenses)
                .groupBy(Expense::currency)
                .map { entry -> Pair(entry.key, entry.value) }
                .toMap()
                .asSequence()
                .map { entry ->
                    Pair(
                        entry.key,
                        entry.value
                            .asSequence()
                            .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                            .filter { expense -> isAmountInSelectedExpenseAmountRange(selectedAmountRange.first, selectedAmountRange.second, expense.amount) }
                            .count()
                    )
                }
                .toMap()
        }
    }

    fun selectAllCurrencyFilters() {
        viewModelScope.launch {
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                CurrencyFilterInfo(
                    it.peekContent().currenciesWithExpenseCount.keys.map(Currency::id).toMutableSet(),
                    it.peekContent().currenciesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCurrencyFilterInfo.postValue(Event(updatedCurrencyFilterInfo!!))


            val allCategoryFilterInfo = _allCategoryFilterInfo.value!!.peekContent()
            val allAmountFilterInfo = _allAmountFilterInfo.value!!.peekContent()

            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                filterUpdatedFilteredExpenses(
                    it,
                    allCategoryFilterInfo.selectedCategoryIds,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                    updatedCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)


            val updatedCategoriesWithExpenseCount = _unfilteredExpenses.value?.let {
                calculateUpdatedCurrenciesWithExpenses(
                    it,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                    updatedCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                CategoryFilterInfo(
                    it.peekContent().selectedCategoryIds,
                    updatedCategoriesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCategoryFilterInfo.postValue(Event(updatedCategoryFilterInfo!!))
        }
    }

    fun deSelectAllCurrencyFilters() {
        viewModelScope.launch {
            _allCurrencyFilterInfo.value?.let {
                val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                    CurrencyFilterInfo(
                        mutableSetOf(),
                        it.peekContent().currenciesWithExpenseCount,
                        UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                    )
                }
                _allCurrencyFilterInfo.postValue(Event(updatedCurrencyFilterInfo!!))


                val allCategoryFilterInfo = _allCategoryFilterInfo.value!!.peekContent()
                val allAmountFilterInfo = _allAmountFilterInfo.value!!.peekContent()

                val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                    filterUpdatedFilteredExpenses(
                        it,
                        allCategoryFilterInfo.selectedCategoryIds,
                        Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                        updatedCurrencyFilterInfo.selectedCurrencyIds
                    )
                }
                _filteredExpenses.postValue(updatedFilteredExpenses)


                val updatedCategoriesWithExpenseCount = _unfilteredExpenses.value?.let {
                    calculateUpdatedCurrenciesWithExpenses(
                        it,
                        Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                        updatedCurrencyFilterInfo.selectedCurrencyIds
                    )
                }
                val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                    CategoryFilterInfo(
                        it.peekContent().selectedCategoryIds,
                        updatedCategoriesWithExpenseCount!!,
                        UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                    )
                }
                _allCategoryFilterInfo.postValue(Event(updatedCategoryFilterInfo!!))
            }
        }
    }

    private suspend fun calculateUpdatedCurrenciesWithExpenses(
        categoriesWithExpenses: List<CategoryWithExpenses>,
        selectedAmountRange: Pair<Double, Double>,
        selectedCurrencyIds: MutableSet<Long>
    ) : Map<Category, Int> {
        return withContext(Dispatchers.Default) {
            categoriesWithExpenses
                .asSequence()
                .map { categoryWithExpenses ->
                    Pair(
                        categoryWithExpenses.category,
                        categoryWithExpenses.expenses
                            .asSequence()
                            .filter { expense -> isAmountInSelectedExpenseAmountRange(selectedAmountRange.first, selectedAmountRange.second, expense.amount) }
                            .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                            .count()
                    )
                }
                .toMap()
        }
    }

    private suspend fun filterUpdatedFilteredExpenses(
        unfilteredExpenses: List<CategoryWithExpenses>,
        selectedCategoryIds: MutableSet<Long>,
        selectedAmountRange: Pair<Double, Double>,
        selectedCurrencyIds: MutableSet<Long>
    ) : List<Expense> {
        return withContext(Dispatchers.Default) {
            unfilteredExpenses.asSequence()
                .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses.asSequence() }
                .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                .filter { expense ->
                    isAmountInSelectedExpenseAmountRange(
                        selectedAmountRange.first,
                        selectedAmountRange.second,
                        expense.amount
                    )
                }
                .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                .toList()
        }
    }

    private fun isAmountInSelectedExpenseAmountRange(selectedAmountMinValue: Double, selectedAmountMaxValue: Double, amount: Double) =
        if (selectedAmountMinValue != EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)
            amount in selectedAmountMinValue..selectedAmountMaxValue
        else
            true

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

    fun getCurrentlySelectedCategoryIds() =
        _allCategoryFilterInfo.value!!.peekContent().selectedCategoryIds

    fun toggleCategoryFilter(categoryId: Long) {
        viewModelScope.launch {
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                val categoryFilterInfo = it.peekContent()

                if (categoryFilterInfo.selectedCategoryIds.contains(categoryId))
                    categoryFilterInfo.selectedCategoryIds.remove(categoryId)
                else
                    categoryFilterInfo.selectedCategoryIds.add(categoryId)

                CategoryFilterInfo(
                    categoryFilterInfo.selectedCategoryIds,
                    categoryFilterInfo.categoriesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCategoryFilterInfo.postValue(Event(updatedCategoryFilterInfo!!))


            val allAmountFilterInfo = _allAmountFilterInfo.value!!.peekContent()
            val allCurrencyFilterInfo = _allCurrencyFilterInfo.value!!.peekContent()

            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                filterUpdatedFilteredExpenses(
                    it,
                    updatedCategoryFilterInfo.selectedCategoryIds,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                    allCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)


            val updatedCurrenciesWithExpenseCount = _unfilteredExpenses.value?.let {
                calculateUpdatedCategoriesWithExpenses(
                    it,
                    updatedCategoryFilterInfo.selectedCategoryIds,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue)
                )
            }
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                CurrencyFilterInfo(
                    it.peekContent().selectedCurrencyIds,
                    updatedCurrenciesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCurrencyFilterInfo.postValue(Event(updatedCurrencyFilterInfo!!))
        }
    }

    fun getCurrentlySelectedCurrencyIds() =
        _allCurrencyFilterInfo.value!!.peekContent().selectedCurrencyIds

    fun toggleCurrencyFilter(currencyId: Long) {
        viewModelScope.launch {
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                val currencyFilterInfo = it.peekContent()

                if (currencyFilterInfo.selectedCurrencyIds.contains(currencyId))
                    currencyFilterInfo.selectedCurrencyIds.remove(currencyId)
                else
                    currencyFilterInfo.selectedCurrencyIds.add(currencyId)

                CurrencyFilterInfo(
                    currencyFilterInfo.selectedCurrencyIds,
                    currencyFilterInfo.currenciesWithExpenseCount,
                    UpdateFilterOption.ONLY_UPDATE_CHECKBOXES
                )
            }
            _allCurrencyFilterInfo.postValue(Event(updatedCurrencyFilterInfo!!))


            val allCategoryFilterInfo = _allCategoryFilterInfo.value!!.peekContent()
            val allAmountFilterInfo = _allAmountFilterInfo.value!!.peekContent()

            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                filterUpdatedFilteredExpenses(
                    it,
                    allCategoryFilterInfo.selectedCategoryIds,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                    updatedCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)


            val updatedCategoriesWithExpenseCount = _unfilteredExpenses.value?.let {
                calculateUpdatedCurrenciesWithExpenses(
                    it,
                    Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue),
                    updatedCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                CategoryFilterInfo(
                    it.peekContent().selectedCategoryIds,
                    updatedCategoriesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCategoryFilterInfo.postValue(Event(updatedCategoryFilterInfo!!))
        }
    }

    fun setMinAndMaxSelectedAmount(
        minAmount: Double,
        maxAmount: Double,
        minSelectedAmount: Double,
        maxSelectedAmount: Double
    ) {
        viewModelScope.launch {
            val updatedAmountFilterInfo = AmountFilterInfo(
                minAmount,
                maxAmount,
                minSelectedAmount,
                maxSelectedAmount,
                false
            )
            _allAmountFilterInfo.postValue(Event(updatedAmountFilterInfo))


            val allCategoryFilterInfo = _allCategoryFilterInfo.value!!.peekContent()
            val allCurrencyFilterInfo = _allCurrencyFilterInfo.value!!.peekContent()

            val updatedFilteredExpenses = _unfilteredExpenses.value?.let {
                filterUpdatedFilteredExpenses(
                    it,
                    allCategoryFilterInfo.selectedCategoryIds,
                    Pair(updatedAmountFilterInfo.selectedAmountMinValue, updatedAmountFilterInfo.selectedAmountMaxValue),
                    allCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            _filteredExpenses.postValue(updatedFilteredExpenses)


            val updatedCategoriesWithExpenseCount = _unfilteredExpenses.value?.let {
                calculateUpdatedCurrenciesWithExpenses(
                    it,
                    Pair(updatedAmountFilterInfo.selectedAmountMinValue, updatedAmountFilterInfo.selectedAmountMaxValue),
                    allCurrencyFilterInfo.selectedCurrencyIds
                )
            }
            val updatedCategoryFilterInfo = _allCategoryFilterInfo.value?.let {
                CategoryFilterInfo(
                    it.peekContent().selectedCategoryIds,
                    updatedCategoriesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCategoryFilterInfo.postValue(Event(updatedCategoryFilterInfo!!))


            val updatedCurrenciesWithExpenseCount = _unfilteredExpenses.value?.let {
                calculateUpdatedCategoriesWithExpenses(
                    it,
                    updatedCategoryFilterInfo.selectedCategoryIds,
                    Pair(updatedAmountFilterInfo.selectedAmountMinValue, updatedAmountFilterInfo.selectedAmountMaxValue)
                )
            }
            val updatedCurrencyFilterInfo = _allCurrencyFilterInfo.value?.let {
                CurrencyFilterInfo(
                    it.peekContent().selectedCurrencyIds,
                    updatedCurrenciesWithExpenseCount!!,
                    UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS
                )
            }
            _allCurrencyFilterInfo.postValue(Event(updatedCurrencyFilterInfo!!))
        }
    }








    /** OLD STUFF */
    private val categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = expenseRepository.getAllCategoriesWithExpenses()
    private val categories: LiveData<List<Category>> = liveData { categoryRepository.getAllCategories() }

    private val _selectedFixedInterval: MutableLiveData<FixedInterval> = MutableLiveData()
    val selectedFixedInterval: LiveData<FixedInterval> = _selectedFixedInterval



    fun getAllCategoriesWithExpenses_OLD(): LiveData<List<CategoryWithExpenses>> = this.categoriesWithExpenses

    fun getAllCategories_OLD(): LiveData<List<Category>> = this.categories

    suspend fun addNewCategory(categoryName: String, categoryColor: Int) {
        this.categoryRepository.addNewCategory(Category(
            categoryName,
            categoryColor))
    }

    suspend fun updateCategory(categoryId: Long, categoryName: String, categoryColor: Int) {
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










    fun setSelectedFixedInterval(selectedFixedInterval: FixedInterval) {
        _selectedFixedInterval.value = selectedFixedInterval
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

}
