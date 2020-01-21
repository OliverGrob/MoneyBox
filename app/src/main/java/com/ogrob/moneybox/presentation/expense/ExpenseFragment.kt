package com.ogrob.moneybox.presentation.expense

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseBinding
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.utils.EMPTY_STRING
import com.ogrob.moneybox.utils.NEW_EXPENSE_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.NO_CATEGORY_ID
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import kotlin.reflect.KFunction1

class ExpenseFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseViewModel::class.java)
    }

    private lateinit var binding: FragmentExpenseBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_expense, container, false)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                binding.backdropContainer
                    .attachToolbar(binding.expenseToolbar)
                    .dropInterpolator(LinearInterpolator())
                    .dropHeight(binding.backdropContainer.height - binding.expenseBackdropFrontView.headerLinearLayout.height)
                    .build()
            }
        })

        binding.expenseBackdropFrontView.headerLinearLayout.setOnClickListener { binding.backdropContainer.closeBackView() }
        binding.expenseBackdropFrontView.addExpenseFloatingActionButton.setOnClickListener { onAddNewExpense(it) }


        initYearSelectionObserver()
        initMonthSelectionObserver()

        initTotalAverageInFixedIntervalObserver()
        binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.setOnClickListener { onChangeTotalAverageInterval(it) }


        applyTextChangedListenerToAmountFilters()


        expenseViewModel.getFilteredCategoryWithExpenses().observe(viewLifecycleOwner, Observer {
            setupBackdropExpenses(it)
            setupBackdropFilters(it)
        })

        return binding.root
    }

    private fun initYearSelectionObserver() {
        expenseViewModel.selectedYear.observe(viewLifecycleOwner, Observer { year ->
            expenseViewModel.getFilteredExpensesForFixedInterval()
                .observe(viewLifecycleOwner, Observer {
                    if (year == null)
                        populateExpenseRecyclerView(it, it.flatMap(CategoryWithExpenses::expenses))
                    else
                        populateExpenseRecyclerViewForYear(it, year)
                })
        })
    }

    private fun initMonthSelectionObserver() {
        expenseViewModel.selectedMonthIndex.observe(viewLifecycleOwner, Observer { month ->
            expenseViewModel.getFilteredExpensesForFixedInterval()
                .observe(viewLifecycleOwner, Observer {
                    when {
                        month != null -> populateExpenseRecyclerViewForMonth(it)
                        expenseViewModel.selectedYear.value != null -> populateExpenseRecyclerViewForYear(it, expenseViewModel.selectedYear.value!!)
                        else -> populateExpenseRecyclerView(it, it.flatMap(CategoryWithExpenses::expenses))
                    }
                })
        })
    }

    private fun initTotalAverageInFixedIntervalObserver() {
        expenseViewModel.selectedFixedInterval.observe(viewLifecycleOwner, Observer { fixedInterval ->
            expenseViewModel.getFilteredExpensesForFixedInterval()
                .observe(viewLifecycleOwner, Observer {
                    val expenses = it.flatMap(CategoryWithExpenses::expenses)

                    binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.text =
                        when (fixedInterval) {
                            FixedInterval.YEAR -> resources.getString(
                                R.string.money_amount_average_in_parenthesis,
                                expenseViewModel.calculateYearlyTotalAverage(expenses),
                                FixedInterval.YEAR.toString().toLowerCase()
                            )
                            FixedInterval.MONTH ->
                                resources.getString(
                                    R.string.money_amount_average_in_parenthesis,
                                    expenseViewModel.calculateMonthlyTotalAverage(expenses),
                                    FixedInterval.MONTH.toString().toLowerCase()
                                )
                            FixedInterval.DAY -> resources.getString(
                                R.string.money_amount_average_in_parenthesis,
                                expenseViewModel.calculateDailyTotalAverage(expenses),
                                FixedInterval.DAY.toString().toLowerCase()
                            )
                            else -> throw IllegalAccessException("Fixed interval type not found!")
                        }
                })
        })
    }

    private fun onChangeTotalAverageInterval(view: View) {
        val fixedIntervals = createAvailableFixedIntervals()
        val checkedItem =
            if (fixedIntervals.indexOf(expenseViewModel.selectedFixedInterval.value) != -1)
                fixedIntervals.indexOf(expenseViewModel.selectedFixedInterval.value)
            else
                0

        AlertDialog.Builder(binding.root.context)
            .setTitle("Show average in")
            .setSingleChoiceItems(
                fixedIntervals
                    .map(FixedInterval::toString)
                    .map(String::capitalize)
                    .toTypedArray(),
                checkedItem
            ) { dialog, which ->
                onSelectTotalAverageInterval(fixedIntervals[which])
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun createAvailableFixedIntervals(): Array<FixedInterval> {
        if (expenseViewModel.selectedMonthIndex.value != null)
            return arrayOf(FixedInterval.DAY)

        else if (expenseViewModel.selectedYear.value != null)
            return arrayOf(
                FixedInterval.MONTH,
                FixedInterval.DAY
            )

        return arrayOf(
            FixedInterval.YEAR,
            FixedInterval.MONTH,
            FixedInterval.DAY
        )
    }

    private fun onSelectTotalAverageInterval(selectedFixedInterval: FixedInterval) {
        if (selectedFixedInterval == expenseViewModel.selectedFixedInterval.value)
            return

        expenseViewModel.getFilteredExpensesForFixedInterval().observe(viewLifecycleOwner, Observer {
            val expenses = it.flatMap(CategoryWithExpenses::expenses)
            expenseViewModel.setSelectedFixedInterval(selectedFixedInterval)

            binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.text =
                when (selectedFixedInterval) {
                    FixedInterval.YEAR -> resources.getString(
                        R.string.money_amount_average_in_parenthesis,
                        expenseViewModel.calculateYearlyTotalAverage(expenses),
                        FixedInterval.YEAR.toString().toLowerCase()
                    )
                    FixedInterval.MONTH ->
                        resources.getString(
                            R.string.money_amount_average_in_parenthesis,
                            expenseViewModel.calculateMonthlyTotalAverage(expenses),
                            FixedInterval.MONTH.toString().toLowerCase()
                        )
                    FixedInterval.DAY -> resources.getString(
                        R.string.money_amount_average_in_parenthesis,
                        expenseViewModel.calculateDailyTotalAverage(expenses),
                        FixedInterval.DAY.toString().toLowerCase()
                    )
                }
        })
    }

    private fun applyTextChangedListenerToAmountFilters() {
        applyTextChangedListenerToAmountFilter(binding.expenseBackdropBackView.amountMinTextInputEditText, expenseViewModel::setMinAmount)
        applyTextChangedListenerToAmountFilter(binding.expenseBackdropBackView.amountMaxTextInputEditText, expenseViewModel::setMaxAmount)
    }

    private fun applyTextChangedListenerToAmountFilter(
        amountTextInputEditText: TextInputEditText,
        kFunction1: KFunction1<String, Unit>
    ) {
        amountTextInputEditText.addTextChangedListener(object : TextWatcher {
            private var timeAfterLastTextChange: Long = 0
            private val DELAY: Long = 700

            private var handler = Handler()

            private val inputFinishChecker = Runnable {
                if (System.currentTimeMillis() > (timeAfterLastTextChange + DELAY - 500)) {
//                    kFunction1(s.toString())
                    expenseViewModel.getFilteredCategoryWithExpenses()
                        .observe(viewLifecycleOwner, Observer {
                            setupBackdropExpenses(it)
                        })
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    timeAfterLastTextChange = System.currentTimeMillis()
                    kFunction1(s.toString())
                    handler.postDelayed(inputFinishChecker, DELAY)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(inputFinishChecker)
            }
        })
    }

    private fun setupBackdropFilters(categoriesWithExpenses: List<CategoryWithExpenses>) {
        createCategoryFilter(categoriesWithExpenses)
        createCurrencyFilter(categoriesWithExpenses)
        createExpenseAmountFilter(categoriesWithExpenses)
    }

    private fun createCategoryFilter(categoriesWithExpenses: List<CategoryWithExpenses>) {
        categoriesWithExpenses
            .map { categoryWithExpenses ->
                val newButton = MaterialButton(binding.root.context, null, R.attr.materialButtonStyle)

                newButton.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                newButton.isAllCaps = false
                newButton.text = "${categoryWithExpenses.category.name} (${categoryWithExpenses.expenses.size})"

                newButton.setOnClickListener { button ->
                    expenseViewModel.toggleCategoryFilter(categoryWithExpenses.category.id)
                    button.backgroundTintList =
                        if (expenseViewModel.categoryIsSelected(categoryWithExpenses.category.id))
                            ColorStateList.valueOf(Color.DKGRAY)
                        else
                            ColorStateList.valueOf(resources.getColor(R.color.colorAccent, context?.theme))
                    expenseViewModel.getFilteredCategoryWithExpenses().observe(viewLifecycleOwner, Observer {
                        setupBackdropExpenses(it)
                    })
                }

                binding.expenseBackdropBackView.categoryScrollLinearLayout.addView(newButton)
            }
    }

    private fun createCurrencyFilter(categoriesWithExpenses: List<CategoryWithExpenses>) {

    }

    private fun createExpenseAmountFilter(categoriesWithExpenses: List<CategoryWithExpenses>) {

    }

    private fun setupBackdropExpenses(categoriesWithExpenses: List<CategoryWithExpenses>) {
        val expenses = categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)

        displayFilterResultInHeader(expenses.size)
        populateExpenseRecyclerView(categoriesWithExpenses, expenses)
    }

    private fun displayFilterResultInHeader(expenseCount: Int) {
        if (expenseCount == 1)
            binding.expenseBackdropFrontView.headerItemCountTextView.text =
                resources.getString(R.string.expenses_found_text_singular, expenseCount)
        else
            binding.expenseBackdropFrontView.headerItemCountTextView.text =
                resources.getString(R.string.expenses_found_text_plural, expenseCount)
    }

    private fun populateExpenseRecyclerView(
        categoriesWithExpenses: List<CategoryWithExpenses>,
        expenses: List<Expense>
    ) {
        val categories = categoriesWithExpenses
            .map(CategoryWithExpenses::category)

        val yearRecyclerViewAdapter = YearRecyclerViewAdapter(expenseViewModel, categories)
        binding.expenseBackdropFrontView.yearRecyclerView.adapter = yearRecyclerViewAdapter
        binding.expenseBackdropFrontView.yearRecyclerView.layoutManager = LinearLayoutManager(context)

        val itemDecor = DividerItemDecoration(context, HORIZONTAL)
        if (binding.expenseBackdropFrontView.yearRecyclerView.itemDecorationCount == 0)
            binding.expenseBackdropFrontView.yearRecyclerView.addItemDecoration(itemDecor)

        yearRecyclerViewAdapter.submitList(expenseViewModel.groupExpensesByYearAndMonth(categoriesWithExpenses))

        binding.expenseBackdropFrontView.totalMoneySpentTextView.text =
            resources.getString(R.string.total_money_spent, expenseViewModel.getTotalMoneySpentFormatted(expenses))
    }

    private fun populateExpenseRecyclerViewForYear(
        categoriesWithExpenses: List<CategoryWithExpenses>,
        year: Int
    ) {
        val categories = categoriesWithExpenses
            .map(CategoryWithExpenses::category)

        val expenses = categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)


        initTextFieldsOnYearSelection(year)

        addOnClickListenerOnYearSelection(expenses)


        val monthRecyclerViewAdapter = MonthRecyclerViewAdapter(expenseViewModel, categories)
        binding.expenseBackdropFrontView.yearRecyclerView.adapter = monthRecyclerViewAdapter
        binding.expenseBackdropFrontView.yearRecyclerView.layoutManager = LinearLayoutManager(context)

        val itemDecor = DividerItemDecoration(context, HORIZONTAL)
        if (binding.expenseBackdropFrontView.yearRecyclerView.itemDecorationCount == 0)
            binding.expenseBackdropFrontView.yearRecyclerView.addItemDecoration(itemDecor)

        monthRecyclerViewAdapter.submitList(expenseViewModel.groupExpensesByMonthInYear(categoriesWithExpenses, year))

        binding.expenseBackdropFrontView.totalMoneySpentTextView.text =
            resources.getString(R.string.total_money_spent, expenseViewModel.getTotalMoneySpentFormatted(expenses))
    }

    private fun populateExpenseRecyclerViewForMonth(
        categoriesWithExpenses: List<CategoryWithExpenses>
    ) {
        val categories = categoriesWithExpenses
            .map(CategoryWithExpenses::category)

        val expenses = categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)


        val selectedYear = expenses.iterator().next().additionDate.year

        initTextFieldsOnYearSelection(selectedYear)
        initTextFieldsOnMonthSelection(expenses.iterator().next().additionDate.month)

        addOnClickListenerOnYearSelection(expenses)
        addOnClickListenerOnMonthSelection(selectedYear)


        val monthRecyclerViewAdapter = ExpenseRecyclerViewAdapter(expenseViewModel, categories)
        binding.expenseBackdropFrontView.yearRecyclerView.adapter = monthRecyclerViewAdapter
        binding.expenseBackdropFrontView.yearRecyclerView.layoutManager = LinearLayoutManager(context)

        val itemDecor = DividerItemDecoration(context, HORIZONTAL)
        if (binding.expenseBackdropFrontView.yearRecyclerView.itemDecorationCount == 0)
            binding.expenseBackdropFrontView.yearRecyclerView.addItemDecoration(itemDecor)

        monthRecyclerViewAdapter.submitList(expenses)

        binding.expenseBackdropFrontView.totalMoneySpentTextView.text =
            resources.getString(R.string.total_money_spent, expenseViewModel.getTotalMoneySpentFormatted(expenses))
    }

    private fun initTextFieldsOnYearSelection(year: Int) {
        binding.expenseBackdropFrontView.headerSelectionAllTextView.text =
            resources.getString(R.string.header_selection_all_text)
        binding.expenseBackdropFrontView.headerSelectionAllTextView.paintFlags =
            Paint.UNDERLINE_TEXT_FLAG

        binding.expenseBackdropFrontView.headerSelectionFirstRightArrowTextView.text =
            resources.getString(R.string.header_selection_right_arrow)

        binding.expenseBackdropFrontView.headerSelectionYearTextView.text = year.toString()
        binding.expenseBackdropFrontView.headerSelectionYearTextView.paintFlags = 0
    }

    private fun addOnClickListenerOnYearSelection(expenses: List<Expense>) {
        val observer = Observer<List<CategoryWithExpenses>> {
            resetAllSelectionTextFields()
            populateExpenseRecyclerView(it, expenses)
        }

        binding.expenseBackdropFrontView.headerSelectionAllTextView.setOnClickListener {
            expenseViewModel.setSelectedYear(-1)
            expenseViewModel.setSelectedMonthIndex(-1)
            it.setOnClickListener(null)
            expenseViewModel.getFilteredExpensesForFixedInterval()
                .observe(viewLifecycleOwner, observer)
                .also { expenseViewModel.getFilteredExpensesForFixedInterval().removeObserver(observer) }
        }
    }

    private fun resetAllSelectionTextFields() {
        binding.expenseBackdropFrontView.headerSelectionAllTextView.text = EMPTY_STRING
        binding.expenseBackdropFrontView.headerSelectionFirstRightArrowTextView.text = EMPTY_STRING
        binding.expenseBackdropFrontView.headerSelectionYearTextView.text = EMPTY_STRING
        resetMonthSelectionTextFields()
    }

    private fun initTextFieldsOnMonthSelection(month: Month) {
        binding.expenseBackdropFrontView.headerSelectionYearTextView.paintFlags =
            Paint.UNDERLINE_TEXT_FLAG

        binding.expenseBackdropFrontView.headerSelectionSecondRightArrowTextView.text =
            resources.getString(R.string.header_selection_right_arrow)

        binding.expenseBackdropFrontView.headerSelectionMonthTextView.text = month.toString().toLowerCase().capitalize()
    }

    private fun addOnClickListenerOnMonthSelection(selectedYear: Int) {
        val observer = Observer<List<CategoryWithExpenses>> {
            resetMonthSelectionTextFields()
            populateExpenseRecyclerViewForYear(it, selectedYear)
        }

        binding.expenseBackdropFrontView.headerSelectionYearTextView.setOnClickListener {
            expenseViewModel.setSelectedMonthIndex(-1)
            it.setOnClickListener(null)
            expenseViewModel.getFilteredExpensesForFixedInterval()
                .observe(viewLifecycleOwner, observer)
                .also { expenseViewModel.getFilteredExpensesForFixedInterval().removeObserver(observer) }
        }
    }

    private fun resetMonthSelectionTextFields() {
        binding.expenseBackdropFrontView.headerSelectionSecondRightArrowTextView.text = EMPTY_STRING
        binding.expenseBackdropFrontView.headerSelectionMonthTextView.text = EMPTY_STRING
    }


//    private fun onInitYearAndMonthSelectionAlertDialogs() {
//        expenseViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
//            val categories: List<Category> = it.map(CategoryWithExpenses::category)
//
//            if (categories.isEmpty()) return@Observer
//
//            val triple = createYearInformationTriple()
//            val yearsWithExpense: Array<String> = triple.first
//            val selectedYearIndex: Int = triple.second
//            var selectedYear = triple.third
//
//            AlertDialog.Builder(binding.root.context)
//                .setTitle("Select year:")
//                .setSingleChoiceItems(
//                    yearsWithExpense,
//                    selectedYearIndex
//                ) { _, which -> selectedYear = yearsWithExpense[which] }
//                .setPositiveButton("Next") { dialog, _ ->
//                    populateMonthAlertDialog(selectedYear, categories)
//                    dialog.cancel()
//                }
//                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
//                .create()
//                .show()
//        })
//    }
//
//    private fun populateMonthAlertDialog(selectedYear: String,
//                                         categories: List<Category>) {
//        val triple = createMonthInformationTriple(selectedYear)
//        val monthsInYearWithExpense: Array<String> = triple.first
//        val selectedMonthIndex: Int = triple.second
//        var selectedMonth = triple.third
//
//        AlertDialog.Builder(binding.root.context)
//            .setTitle("Select month in $selectedYear:")
//            .setSingleChoiceItems(
//                monthsInYearWithExpense,
//                selectedMonthIndex
//            ) { _, which -> selectedMonth = monthsInYearWithExpense[which] }
//            .setPositiveButton("Done") { dialog, _ ->
//                onSelectMonth(selectedYear, selectedMonth, categories)
//                dialog.cancel()
//            }
//            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
//            .create()
//            .show()
//    }
//
//    private fun onSelectMonth(selectedYear: String,
//                              selectedMonth: String,
//                              categories: List<Category>) {
//        val selectedYearFromSharedPreferences = expenseViewModel.retrievePreferenceFromSharedPreferences(
//            binding.root.context,
//            SHARED_PREFERENCES_SELECTED_YEAR_KEY)
//        val selectedMonthFromSharedPreferences = expenseViewModel.retrievePreferenceFromSharedPreferences(
//            binding.root.context,
//            SHARED_PREFERENCES_SELECTED_MONTH_KEY)
//
//        val newSelectionSameAsCurrent =
//            selectedYearFromSharedPreferences == selectedYear && selectedMonthFromSharedPreferences == selectedMonth
//
//        if (newSelectionSameAsCurrent)
//            return
//
//        expenseViewModel.updatePreferenceInSharedPreferences(
//            binding.root.context,
//            SHARED_PREFERENCES_SELECTED_YEAR_KEY,
//            selectedYear)
//
//        expenseViewModel.updatePreferenceInSharedPreferences(
//            binding.root.context,
//            SHARED_PREFERENCES_SELECTED_MONTH_KEY,
//            selectedMonth)
//
//        binding.yearTextView.text = selectedYear
//        binding.monthTextView.text = selectedMonth
//
//        populateExpenseRecyclerView(
//            selectedYear.toInt(),
//            Month.valueOf(selectedMonth),
//            categories)
//    }
//
//    private fun populateExpensesRecyclerView(categoryWithExpenses: List<CategoryWithExpenses>) {
//        val selectedYearTriple = createYearInformationTriple()
//        binding.yearTextView.text = selectedYearTriple.third
//
//        if (selectedYearTriple.second == -1) return
//
//        val selectedMonth = createMonthInformationTriple(selectedYearTriple.third).third
//        binding.monthTextView.text = selectedMonth
//
//        val categories: List<Category> = categoryWithExpenses.map(CategoryWithExpenses::category)
//
//        populateExpenseRecyclerView(
//            selectedYearTriple.third.toInt(),
//            Month.valueOf(selectedMonth),
//            categories)
//    }
//
//    private fun createYearInformationTriple(): Triple<Array<String>, Int, String> {
//        val yearsWithExpense: Array<String> = expenseViewModel.getYearsWithExpense()
//            .map(Int::toString)
//            .toTypedArray()
//
//        if (yearsWithExpense.isEmpty()) return Triple(yearsWithExpense, -1, "You don't have any expense added yet!")
//
//        var selectedYear =
//            expenseViewModel.retrievePreferenceFromSharedPreferences(
//                binding.root.context,
//                SHARED_PREFERENCES_SELECTED_YEAR_KEY)
//
//        val selectedYearIndex: Int =
//            if (selectedYear.isBlank()) {
//                selectedYear = yearsWithExpense[yearsWithExpense.lastIndex]
//                yearsWithExpense.lastIndex
//            } else
//                yearsWithExpense.indexOf(selectedYear)
//
//        return Triple(yearsWithExpense, selectedYearIndex, selectedYear)
//    }
//
//    private fun createMonthInformationTriple(selectedYear: String): Triple<Array<String>, Int, String> {
//        val monthsInYearWithExpense: Array<String> =
//            expenseViewModel.getMonthsInYearWithExpense(selectedYear.toInt())
//                .map(Month::toString)
//                .toTypedArray()
//
//        var selectedMonth =
//            expenseViewModel.retrievePreferenceFromSharedPreferences(
//                binding.root.context,
//                SHARED_PREFERENCES_SELECTED_MONTH_KEY)
//
//        val selectedMonthIndex: Int =
//            if (selectedMonth.isBlank() || !monthsInYearWithExpense.contains(selectedMonth)) {
//                selectedMonth = monthsInYearWithExpense[monthsInYearWithExpense.lastIndex]
//                monthsInYearWithExpense.lastIndex
//            } else
//                monthsInYearWithExpense.indexOf(selectedMonth)
//
//        return Triple(monthsInYearWithExpense, selectedMonthIndex, selectedMonth)
//    }
//
//    private fun populateExpenseRecyclerView(yearSelected: Int,
//                                            monthSelected: Month,
//                                            categories: List<Category>) {
//        val expenseRecyclerViewAdapter = ExpenseRecyclerViewAdapter(expenseViewModel, categories)
//        binding.expensesRecyclerView.adapter = expenseRecyclerViewAdapter
//        binding.expensesRecyclerView.layoutManager = LinearLayoutManager(context)
//
//        val itemDecor = DividerItemDecoration(context, HORIZONTAL)
//        binding.expensesRecyclerView.addItemDecoration(itemDecor)
//
//        val selectedExpenses = expenseViewModel.getExpensesForSelectedMonthInSelectedYear(yearSelected, monthSelected)
//        expenseRecyclerViewAdapter.submitList(selectedExpenses)
//        binding.totalMoneySpentTextView.text = "Total: ${expenseViewModel.getTotalMoneySpentFormatted(selectedExpenses)}"
//    }

    private fun onAddNewExpense(view: View) {
        view.findNavController().navigate(ExpenseFragmentDirections.actionExpenseFragmentToExpenseAddAndEditFragment(
            EMPTY_STRING,
            EMPTY_STRING,
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
            NEW_EXPENSE_PLACEHOLDER_ID,
            NO_CATEGORY_ID,
            resources.getString(R.string.add_expense_button)
        ))
    }

}
