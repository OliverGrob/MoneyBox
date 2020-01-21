package com.ogrob.moneybox.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.data.repository.CategoryRepository
import com.ogrob.moneybox.data.repository.ExpenseRepository
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.presentation.helper.ExpensesByMonth
import com.ogrob.moneybox.presentation.helper.ExpensesByYear
import com.ogrob.moneybox.utils.EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository =
        ExpenseRepository(application)
    private val categoryRepository: CategoryRepository =
        CategoryRepository(application)

    private val categoriesWithExpenses: LiveData<List<CategoryWithExpenses>> = this.expenseRepository.getCategoriesWithExpenses()
    private val categories: LiveData<List<Category>> = this.categoryRepository.getCategories()

    private val selectedCategoryIds: MutableList<Long> = mutableListOf()
    private val selectedCurrencies: MutableList<String> = mutableListOf()
    private var selectedExpenseAmountRange: Pair<Double, Double> =
        Pair(EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE, EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)


    private var _selectedYear: MutableLiveData<Int> = MutableLiveData()
    val selectedYear: LiveData<Int> = _selectedYear

    private var _selectedMonthIndex: MutableLiveData<Int> = MutableLiveData()
    val selectedMonthIndex: LiveData<Int> = _selectedMonthIndex

    private val _selectedFixedInterval: MutableLiveData<FixedInterval> = MutableLiveData(FixedInterval.YEAR)
    val selectedFixedInterval: LiveData<FixedInterval> = _selectedFixedInterval

    private var categoryDropdownIsOpen = false


    fun getAllCategoriesWithExpenses(): LiveData<List<CategoryWithExpenses>> = this.categoriesWithExpenses

    fun getAllCategories(): LiveData<List<Category>> = this.categories

    fun addNewExpense(expenseAmount: String, expenseDescription: String, expenseDate: String, categoryId: Long) {
        this.expenseRepository.addNewExpense(Expense(
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            categoryId))
    }

    fun addNewCategory(categoryName: String, categoryColor: Int) {
        this.categoryRepository.addNewCategory(Category(
            categoryName,
            categoryColor))
    }

    fun updateExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, categoryId: Long) {
        this.expenseRepository.updateExpense(Expense(
            expenseId,
            expenseAmount.toDouble(),
            expenseDescription,
            LocalDateTime.of(LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.now()),
            categoryId))
    }

    fun updateCategory(categoryId: Long, categoryName: String, categoryColor: Int) {
        this.categoryRepository.updateCategory(Category(
            categoryId,
            categoryName,
            categoryColor))
    }

    fun deleteExpense(expense: Expense) {
        this.expenseRepository.deleteExpense(expense)
    }

    fun deleteCategory(category: Category) {
        this.categoryRepository.deleteCategory(category)
    }

    fun getAllExpensesDescription(categoriesWithExpenses: List<CategoryWithExpenses>): List<String> = categoriesWithExpenses
        .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
        .map(Expense::description)
        .distinct()

    fun getYearsWithExpense(): List<Int> {
        return this.categoriesWithExpenses.value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .map(Expense::additionDate)
            .map(LocalDateTime::getYear)
            .distinct()
            .sorted()
    }

    fun getMonthsInYearWithExpense(yearSelected: Int): List<Month> {
        return this.categoriesWithExpenses.value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .asSequence()
            .filter { expense -> expense.additionDate.year == yearSelected }
            .map(Expense::additionDate)
            .map(LocalDateTime::getMonth)
            .distinct()
            .sorted()
            .toList()
    }

    fun getExpensesForSelectedMonthInSelectedYear(yearSelected: Int, monthSelected: Month): List<Expense> {
        return this.categoriesWithExpenses.value!!
            .flatMap { categoryWithExpenses ->
                filterExpensesForSelectedMonthInSelectedYear(categoryWithExpenses.expenses, yearSelected, monthSelected) }
            .sortedBy(Expense::additionDate)
    }

    private fun filterExpensesForSelectedMonthInSelectedYear(expenses: List<Expense>, yearSelected: Int, monthSelected: Month): List<Expense> {
        return expenses
            .filter { expense -> expense.additionDate.year == yearSelected && expense.additionDate.month == monthSelected }
    }

    fun addOrEditExpense(expenseId: Long, expenseAmount: String, expenseDescription: String, expenseDate: String, categoryId: Long) {
        if (expenseId == NEW_EXPENSE_PLACEHOLDER_ID)
            this.addNewExpense(
                expenseAmount,
                expenseDescription,
                expenseDate,
                categoryId)

        else
            this.updateExpense(
                expenseId,
                expenseAmount,
                expenseDescription,
                expenseDate,
                categoryId)
    }

    fun addOrEditCategory(categoryId: Long, categoryName: String, currentTextColor: Int) {
        if (categoryId == NEW_CATEGORY_PLACEHOLDER_ID)
            this.addNewCategory(
                categoryName,
                currentTextColor)

        else
            this.updateCategory(
                categoryId,
                categoryName,
                currentTextColor)
    }

    fun getTotalMoneySpentUnformatted(expensesSelected: List<Expense>): Double =
        expensesSelected
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

    fun getTotalMoneySpentFormatted(expensesSelected: List<Expense>): String {
        val totalMoneySpentWithoutFormatting = expensesSelected
            .map(Expense::amount)
            .fold(0.0) { xAmount: Double, yAmount: Double -> xAmount.plus(yAmount) }

        return this.formatMoneySpent(totalMoneySpentWithoutFormatting)
    }

    private fun formatMoneySpent(currentTotal: Double): String {
        return if (currentTotal == currentTotal.toInt().toDouble()) currentTotal.toInt().toString() else currentTotal.toString()
    }

    fun getExpenseByDescription(description: String): Expense {
        return this.getAllCategoriesWithExpenses().value!!
            .flatMap { categoryWithExpenses -> categoryWithExpenses.expenses }
            .single { expense -> expense.description == description }
    }

    fun deleteUnusedCategories(categoriesWithExpenses: List<CategoryWithExpenses>) {
        categoriesWithExpenses
            .filter { categoryWithExpenses -> categoryWithExpenses.expenses.isEmpty() }
            .map(CategoryWithExpenses::category)
            .forEach(this::deleteCategory)
    }






    fun getFilteredCategoryWithExpenses(): LiveData<List<CategoryWithExpenses>> {
        return Transformations
            .switchMap(getAllCategoriesWithExpenses(), ::filterCategoriesWithExpenses)
    }

    private fun filterCategoriesWithExpenses(categoriesWithExpenses: List<CategoryWithExpenses>): LiveData<List<CategoryWithExpenses>> {
        val filteredCategoryWithExpenses = categoriesWithExpenses
            .filter(this::categoryIsSelectedInternal)
            .map(this::filterExpensesForRemainingFilters)
            .filter(this::categoryHasExpenses)

        return MutableLiveData(filteredCategoryWithExpenses)
    }

    private fun categoryIsSelectedInternal(categoryWithExpenses: CategoryWithExpenses): Boolean {
        if (noSelectedCategory())
            return true

        return selectedCategoryIds.contains(categoryWithExpenses.category.id)
    }

    private fun noSelectedCategory(): Boolean = selectedCategoryIds.isEmpty()

    private fun filterExpensesForRemainingFilters(categoryWithExpenses: CategoryWithExpenses): CategoryWithExpenses {
        val filteredExpenses = categoryWithExpenses.expenses
            // Filter for currency here
//            .filter(currencyFilter)
            .filter(this::expenseAmountIsInSelectedRange)

        return CategoryWithExpenses(categoryWithExpenses.category, filteredExpenses)
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

    fun setMinAmount(minAmountString: String) {
        val minAmount = minAmountString.toDoubleOrNull()

        if (minAmount != null)
            selectedExpenseAmountRange = Pair(minAmount, selectedExpenseAmountRange.second)
    }

    fun setMaxAmount(maxAmountString: String) {
        val maxAmount = maxAmountString.toDoubleOrNull()

        if (maxAmount != null)
            selectedExpenseAmountRange = Pair(selectedExpenseAmountRange.first, maxAmount)
    }

    fun groupExpensesByYearAndMonth(categoriesWithExpenses: List<CategoryWithExpenses>): List<ExpensesByYear> =
        categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .groupBy { expense -> expense.additionDate.year }
            .map { currentExpensesSortedByYear ->
                ExpensesByYear(
                    currentExpensesSortedByYear.key,
                    currentExpensesSortedByYear.value
                )
            }
            .sortedBy(ExpensesByYear::year)

    fun groupExpensesByMonthInYear(categoriesWithExpenses: List<CategoryWithExpenses>, year: Int): List<ExpensesByMonth> =
        categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)
            .groupBy { expense -> expense.additionDate.month }
            .map { currentExpensesSortedByYear ->
                ExpensesByMonth(
                    year,
                    currentExpensesSortedByYear.key,
                    currentExpensesSortedByYear.value
                )
            }
            .sortedBy(ExpensesByMonth::month)

    fun sortExpensesByMonthOfAdditionDate(expenses: List<ExpensesByMonth>): List<ExpensesByMonth> =
        expenses
            .sortedBy{ expensesByMonth -> expensesByMonth.month }

    fun sortExpensesByDayOfAdditionDate(expenses: List<Expense>): List<Expense> =
        expenses
            .sortedBy{ expense -> expense.additionDate.dayOfMonth }

    fun getFilteredExpensesForFixedInterval(): LiveData<List<CategoryWithExpenses>> {
        return Transformations
            .switchMap(getFilteredCategoryWithExpenses()) { categoriesWithExpenses ->
                this.filterCategoriesWithExpensesForFixedInterval(categoriesWithExpenses)
            }
    }

    private fun filterCategoriesWithExpensesForFixedInterval(categoriesWithExpenses: List<CategoryWithExpenses>): LiveData<List<CategoryWithExpenses>> {
        val filteredCategoryWithExpenses = categoriesWithExpenses
            .map { categoryWithExpenses -> this.filterExpensesForFixedInterval(categoryWithExpenses) }

        return MutableLiveData(filteredCategoryWithExpenses)
    }

    private fun filterExpensesForFixedInterval(categoryWithExpenses: CategoryWithExpenses): CategoryWithExpenses {
        val filteredExpenses = categoryWithExpenses.expenses
            .filter(this::filterForYear)
            .filter(this::filterForMonth)

        return CategoryWithExpenses(categoryWithExpenses.category, filteredExpenses)
    }

    private fun filterForYear(expense: Expense): Boolean {
        if (_selectedYear.value == null)
            return true

        return expense.additionDate.year == _selectedYear.value
    }

    private fun filterForMonth(expense: Expense): Boolean {
        if (_selectedMonthIndex.value == null)
            return true

        return expense.additionDate.monthValue == _selectedMonthIndex.value
    }


    fun setSelectedYear(selectedYear: Int) {
        if (selectedYear == -1)
            _selectedYear.postValue(null)
        else
            _selectedYear.value = selectedYear
    }

    fun setSelectedMonthIndex(selectedMonthIndex: Int) {
        if (selectedMonthIndex == -1)
            _selectedMonthIndex.postValue(null)
        else
            _selectedMonthIndex.value = selectedMonthIndex
    }

    fun setSelectedFixedInterval(selectedFixedInterval: FixedInterval) {
        _selectedFixedInterval.value = selectedFixedInterval
    }

    fun calculateYearlyTotalAverage(expenses: List<Expense>): Double {
        val totalAmount = getTotalMoneySpentUnformatted(expenses)

        val numOfYears = expenses
            .map { expense -> expense.additionDate.year }
            .distinct()
            .count()

        return totalAmount.div(numOfYears)
    }

    fun calculateMonthlyTotalAverage(expenses: List<Expense>): Double {
        val totalAmount = getTotalMoneySpentUnformatted(expenses)

        val numOfMonths = expenses
            .map(Expense::additionDate)
            .distinctBy { it.year to it.month }
            .count()

        return totalAmount.div(numOfMonths)
    }

    fun calculateDailyTotalAverage(expenses: List<Expense>): Double {
        val totalAmount = getTotalMoneySpentUnformatted(expenses)

        val numOfDays = expenses
            .map(Expense::additionDate)
            .distinctBy { it.year to it.month to it.dayOfMonth}
            .count()

        return totalAmount.div(numOfDays)
    }

    fun isCategoryDropdownOpen(): Boolean = categoryDropdownIsOpen

    fun openCategoryDropdown() {
        categoryDropdownIsOpen = true
    }

    fun closeCategoryDropdown() {
        categoryDropdownIsOpen = false
    }


//    fun getFilteredExpensesForYear(): LiveData<List<CategoryWithExpenses>> {
//        return Transformations
//            .switchMap(getFilteredCategoryWithExpenses()) { categoriesWithExpenses ->
//                this.filterCategoriesWithExpensesForYear(
//                    categoriesWithExpenses,
//                    selectedYear.value!!
//                )
//            }
//    }
//
//    private fun filterCategoriesWithExpensesForYear(
//        categoriesWithExpenses: List<CategoryWithExpenses>,
//        year: Int
//    ): LiveData<List<CategoryWithExpenses>> {
//        val filteredCategoryWithExpenses = categoriesWithExpenses
//            .map { categoryWithExpenses -> this.filterExpensesForYear(categoryWithExpenses, year) }
//
//        return MutableLiveData(filteredCategoryWithExpenses)
//    }
//
//    private fun filterExpensesForYear(
//        categoryWithExpenses: CategoryWithExpenses,
//        year: Int
//    ): CategoryWithExpenses {
//        val filteredExpenses = categoryWithExpenses.expenses
//            .filter { expense -> expense.additionDate.year == year }
//
//        return CategoryWithExpenses(categoryWithExpenses.category, filteredExpenses)
//    }
//
//    fun getFilteredExpensesForMonth(): LiveData<List<CategoryWithExpenses>> {
//        return Transformations
//            .switchMap(getFilteredCategoryWithExpenses()) { categoriesWithExpenses ->
//                this.filterCategoriesWithExpensesForYearAndMonth(
//                    categoriesWithExpenses,
//                    selectedYear.value!!,
//                    selectedMonthIndex.value!!
//                )
//            }
//    }
//
//    private fun filterCategoriesWithExpensesForYearAndMonth(
//        categoriesWithExpenses: List<CategoryWithExpenses>,
//        year: Int,
//        monthIndex: Int
//    ): LiveData<List<CategoryWithExpenses>> {
//        val filteredCategoryWithExpenses = categoriesWithExpenses
//            .map { categoryWithExpenses -> this.filterExpensesForYearAndMonth(categoryWithExpenses, year, monthIndex) }
//
//        return MutableLiveData(filteredCategoryWithExpenses)
//    }
//
//    private fun filterExpensesForYearAndMonth(
//        categoryWithExpenses: CategoryWithExpenses,
//        year: Int,
//        monthIndex: Int
//    ): CategoryWithExpenses {
//        val filteredExpenses = categoryWithExpenses.expenses
//            .filter { expense -> expense.additionDate.year == year && expense.additionDate.monthValue == monthIndex }
//
//        return CategoryWithExpenses(categoryWithExpenses.category, filteredExpenses)
//    }

}
