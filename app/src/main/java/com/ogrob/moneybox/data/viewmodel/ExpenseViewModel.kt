package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.persistence.model.*
import com.ogrob.moneybox.ui.helper.ExpenseDTOForAdapter
import com.ogrob.moneybox.ui.helper.ExpensesByMonth
import com.ogrob.moneybox.ui.helper.ExpensesByYear
import com.ogrob.moneybox.ui.helper.FilterOption
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

    private val _filteredCategoriesWithExpensesForFilterUpdate: MutableLiveData<Pair<FilterOption, List<CategoryWithExpenses>>> = MutableLiveData()
    val filteredCategoriesWithExpensesForFilterUpdate: LiveData<Pair<FilterOption, List<CategoryWithExpenses>>> = _filteredCategoriesWithExpensesForFilterUpdate

    private val _allCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val allCategories: LiveData<List<Category>> = _allCategories


    private lateinit var selectedCategoryIds: MutableList<Long>
    private lateinit var selectedCurrencyIds: MutableList<Long>
    private lateinit var selectedExpenseAmountRange: Pair<Double, Double>


    fun getAllCategories() {
        viewModelScope.launch {
            _allCategories.value = categoryRepository.getAllCategories()
        }
    }

    fun getAllFilteredExpenses() {
        viewModelScope.launch {
            val allCategoriesWithExpenses = categoryRepository.getAllCategoriesWithExpenses()

            selectFiltersDefaults(allCategoriesWithExpenses)

            _unfilteredExpenses.value = allCategoriesWithExpenses

            updateAllFilteredExpenses()
        }
    }

    fun getAllCategoriesWithExpensesForSelectedYearAndMonth(year: Int, month: Month) {
        viewModelScope.launch {
            val allCategoriesWithExpenses = categoryRepository.getAllCategoriesWithExpenses()

            selectFiltersDefaults(allCategoriesWithExpenses)

            val allCategoriesWithExpensesFilteredForYearAndMonth = filterAllExpensesForSelectedYearAndMonth(allCategoriesWithExpenses, year, month)

            _unfilteredExpenses.value = allCategoriesWithExpensesFilteredForYearAndMonth

            updateAllFilteredExpenses()
        }
    }

    private suspend fun selectFiltersDefaults(allCategoriesWithExpenses: List<CategoryWithExpenses>) {
        viewModelScope.launch(Dispatchers.Default) {
            selectedCategoryIds = selectCategories(allCategoriesWithExpenses)
            selectedCurrencyIds = selectCurrencies(allCategoriesWithExpenses)
            selectedExpenseAmountRange = selectExpenseAmountRange(allCategoriesWithExpenses)
        }
    }

    private suspend fun selectCategories(categoriesWithExpenses: List<CategoryWithExpenses>): MutableList<Long> {
        return withContext(Dispatchers.Default) {
            categoriesWithExpenses
                .map(CategoryWithExpenses::category)
                .map(Category::id)
                .toMutableList()
        }
    }

    private suspend fun selectCurrencies(categoriesWithExpenses: List<CategoryWithExpenses>): MutableList<Long> {
        return withContext(Dispatchers.Default) {
            categoriesWithExpenses
                .flatMap(CategoryWithExpenses::expenses)
                .map(Expense::currency)
                .map(Currency::id)
                .distinct()
                .toMutableList()
        }
    }

    private suspend fun selectExpenseAmountRange(allCategoriesWithExpenses: List<CategoryWithExpenses>): Pair<Double, Double> {
        return withContext(Dispatchers.Default) {
            val allAmountValues = allCategoriesWithExpenses
                .flatMap(CategoryWithExpenses::expenses)
                .map(Expense::amount)

            Pair(allAmountValues.min()!!, allAmountValues.max()!!)
        }
    }

    private suspend fun filterAllExpensesForSelectedYearAndMonth(
        allCategoriesWithExpenses: List<CategoryWithExpenses>,
        year: Int,
        month: Month
    ): List<CategoryWithExpenses> {
        return withContext(Dispatchers.Default) {
            allCategoriesWithExpenses
                .map { categoryWithExpenses ->
                    CategoryWithExpenses(
                        categoryWithExpenses.category,
                        categoryWithExpenses.expenses
                            .filter { expense -> expense.additionDate.year == year }
                            .filter { expense -> expense.additionDate.month == month }
                    )
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
                .flatMap(CategoryWithExpenses::expenses)
                .filter { expense -> selectedCategoryIds.contains(expense.categoryId) }
                .filter { expense -> selectedCurrencyIds.contains(expense.currency.id) }
                .filter { expense -> isAmountInSelectedExpenseAmountRange(expense.amount) }
        }
    }

    fun updateFilters(filterOption: FilterOption) {
        viewModelScope.launch {
            _filteredCategoriesWithExpensesForFilterUpdate.value = Pair(filterOption, filterAllExpensesForFilterUpdate(_unfilteredExpenses.value!!, filterOption))
        }
    }

    private suspend fun filterAllExpensesForFilterUpdate(
        unfilteredExpenses: List<CategoryWithExpenses>,
        filterOption: FilterOption
    ): List<CategoryWithExpenses> {
        return withContext(Dispatchers.Default) {
            when (filterOption) {
                FilterOption.CATEGORY -> unfilteredExpenses
                    .map { categoryWithExpenses ->
                        CategoryWithExpenses(
                            categoryWithExpenses.category,
                            categoryWithExpenses.expenses.filter { expense ->
                                selectedCategoryIds.contains(expense.categoryId)
                            })
                    }
                FilterOption.AMOUNT -> unfilteredExpenses
                    .map { categoryWithExpenses ->
                        CategoryWithExpenses(
                            categoryWithExpenses.category,
                            categoryWithExpenses.expenses.filter { expense ->
                                isAmountInSelectedExpenseAmountRange(expense.amount)
                            })
                    }
                FilterOption.CURRENCY -> unfilteredExpenses
                    .map { categoryWithExpenses ->
                        CategoryWithExpenses(
                            categoryWithExpenses.category,
                            categoryWithExpenses.expenses.filter { expense ->
                                selectedCurrencyIds.contains(expense.currency.id)
                            })
                    }
            }
        }
    }

    private fun isAmountInSelectedExpenseAmountRange(amount: Double) =
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

    fun isCategorySelected(categoryId: Long) = selectedCategoryIds.contains(categoryId)

    fun toggleCategoryFilter(categoryId: Long) {
        if (selectedCategoryIds.contains(categoryId))
            selectedCategoryIds.remove(categoryId)
        else
            selectedCategoryIds.add(categoryId)
    }

    fun isCurrencySelected(currencyId: Long) = selectedCurrencyIds.contains(currencyId)

    fun toggleCurrencyFilter(currencyId: Long) {
        if (selectedCurrencyIds.contains(currencyId))
            selectedCurrencyIds.remove(currencyId)
        else
            selectedCurrencyIds.add(currencyId)
    }

    fun getCheapestAndMostExpensiveExpenseAmount(allCategoriesWithExpenses: List<CategoryWithExpenses>): Pair<Double, Double> {
        val allAmountValues = allCategoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .map(Expense::amount)

        return Pair(allAmountValues.min()!!, allAmountValues.max()!!)
    }

    fun getCurrenciesWithExpenseCount(allCategoriesWithExpenses: List<CategoryWithExpenses>): Map<Currency, Int> =
        allCategoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)
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

    fun setMinAndMaxAmount(minAmount: Double, maxAmount: Double) {
        selectedExpenseAmountRange = Pair(minAmount, maxAmount)
    }

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
