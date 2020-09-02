package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.persistence.model.*
import com.ogrob.moneybox.ui.helper.ExpensesByMonth
import com.ogrob.moneybox.ui.helper.ExpensesByYear
import com.ogrob.moneybox.utils.EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.withSuffix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application, viewModelScope)
    private val categoryRepository: CategoryRepository =
        CategoryRepository(application, viewModelScope)

    private val categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = expenseRepository.getCategoriesWithExpenses()
    private val categories: LiveData<List<Category>> = categoryRepository.getCategories()

    private val selectedCategoryIds: MutableList<Long> = mutableListOf()
    private val selectedCurrencies: MutableList<Currency> = mutableListOf()
    private var selectedExpenseAmountRange: Pair<Double, Double> =
        Pair(EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE, EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)


    private val _selectedFixedInterval: MutableLiveData<FixedInterval> = MutableLiveData()
    val selectedFixedInterval: LiveData<FixedInterval> = _selectedFixedInterval

    private val _selectedCurrency: MutableLiveData<Currency> = MutableLiveData()
    val selectedCurrency: LiveData<Currency> = _selectedCurrency

    private var categoryDropdownIsOpen = false


    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>> = this.categoriesWithExpenses

    fun getAllCategories(): LiveData<List<Category>> = this.categories

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

    fun getTotalMoneySpentFormatted(expensesSelected: List<Expense>): String {
        val totalMoneySpentWithoutFormatting = expensesSelected
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

        return withSuffix(totalMoneySpentWithoutFormatting)
//        return this.formatMoneySpent(totalMoneySpentWithoutFormatting)
    }

    private fun formatMoneySpent(currentTotal: Double): String {
        return if (currentTotal == currentTotal.toInt().toDouble()) currentTotal.toInt().toString() else currentTotal.toString()
    }

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

    fun toggleCategoryFilter(categoryId: Long) {
        if (selectedCategoryIds.contains(categoryId))
            selectedCategoryIds.remove(categoryId)
        else
            selectedCategoryIds.add(categoryId)
    }

    fun setMinAndMaxAmount(minAmount: Double, maxAmount: Double) {
        selectedExpenseAmountRange = Pair(minAmount, maxAmount)
    }

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
