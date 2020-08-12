package com.ogrob.moneybox.ui.expense

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.hideLoadingAnimation
import com.ogrob.moneybox.utils.showLoadingAnimation
import com.ogrob.moneybox.utils.withSuffix
import kotlinx.android.synthetic.main.activity_main.*

abstract class ExpenseBaseFragment : BaseFragment() {

    protected val expenseViewModel: ExpenseViewModel by viewModels()

    protected lateinit var binding: FragmentExpenseBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentExpenseBinding.inflate(inflater)

//        configureBackdropContainer()
        initToolbar()

        showLoadingAnimation()

        initFilterHeaderOnClickListeners()

        binding.expenseBackdropFrontView.headerLinearLayout.setOnClickListener { binding.backdropContainer.closeBackView() }
        binding.expenseBackdropFrontView.addExpenseFloatingActionButton.setOnClickListener { onAddNewExpense(it) }


        initHeaderSelectionTextViewsAndAddClickListeners()


        initTotalAverageInFixedIntervalObserver()
        binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.setOnClickListener { onChangeTotalAverageInterval(it) }
        binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG


        getFilteredExpenses().observe(viewLifecycleOwner, Observer {
            Thread.sleep(1000)
            hideLoadingAnimation()
            configureBackdropContainer()
            setupBackdropExpenses(it)
            expenseViewModel.getAllCategories()
                .observe(viewLifecycleOwner, Observer { allCategories ->
                    getExpensesWithoutCategoryFiltering().observe(
                        viewLifecycleOwner,
                        Observer { categoriesWithExpenses ->
                            setupBackdropFilters(allCategories, categoriesWithExpenses)
                        })
                })
        })

        return binding.root
    }

    private fun initToolbar() {
        val toolbar = requireActivity().toolbar
        toolbar.navigationIcon = null
        toolbar.setLogo(R.drawable.ic_filter_list_white_24dp)
        val params = toolbar.getChildAt(1).layoutParams as ViewGroup.MarginLayoutParams
        params.rightMargin = 100
        toolbar.getChildAt(1).layoutParams = params
    }

    private fun configureBackdropContainer() {
        val toolbar: Toolbar = requireActivity().toolbar

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                binding.backdropContainer
                    .attachToolbar(toolbar)
                    .dropInterpolator(LinearInterpolator())
                    .dropHeight(binding.backdropContainer.height - binding.expenseBackdropFrontView.headerLinearLayout.height)
                    .build()
            }
        })
    }

    private fun initFilterHeaderOnClickListeners() {
        binding.expenseBackdropBackView.categoryHeaderLinearLayout.setOnClickListener {
            toggleFilterView(
                binding.expenseBackdropBackView.categoryDropdownIcon,
                binding.expenseBackdropBackView.categoryBodyChipGroup
            )
        }
        binding.expenseBackdropBackView.amountHeaderLinearLayout.setOnClickListener {
            toggleFilterView(
                binding.expenseBackdropBackView.amountDropdownIcon,
                binding.expenseBackdropBackView.amountBodyLinearLayout
            )
        }
        binding.expenseBackdropBackView.currencyHeaderLinearLayout.setOnClickListener {
            toggleFilterView(
                binding.expenseBackdropBackView.currencyDropdownIcon,
                binding.expenseBackdropBackView.currencyBodyChipGroup
            )
        }
    }

    private fun toggleFilterView(
        filterHeaderIcon: View,
        filterBodyView: View
    ) {
        if (filterBodyView.isVisible) {
            filterHeaderIcon.background = resources.getDrawable(R.drawable.ic_expand_more_white_24dp, null)
            filterBodyView.visibility = View.GONE
        }
        else {
            filterHeaderIcon.background = resources.getDrawable(R.drawable.ic_expand_less_white_24dp, null)
            filterBodyView.visibility = View.VISIBLE
        }
    }

    abstract fun onAddNewExpense(view: View)

    abstract fun initHeaderSelectionTextViewsAndAddClickListeners()

    private fun initTotalAverageInFixedIntervalObserver() {
        expenseViewModel.selectedFixedInterval.observe(viewLifecycleOwner, Observer { fixedInterval ->
//            expenseViewModel.getFilteredExpensesForFixedIntervalUsingAllFilters()
            getFilteredExpenses()
                .observe(viewLifecycleOwner, Observer {
                    val expenses = it.flatMap(CategoryWithExpenses::expenses)

                    binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.text =
                        totalAverageForFixedInterval(fixedInterval, expenses)
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
                    .toTypedArray(),
                checkedItem
            ) { dialog, which ->
                onSelectTotalAverageInterval(fixedIntervals[which])
                dialog.cancel()
            }
            .create()
            .show()
    }

    abstract fun createAvailableFixedIntervals(): Array<FixedInterval>

    private fun onSelectTotalAverageInterval(selectedFixedInterval: FixedInterval) {
        if (selectedFixedInterval == expenseViewModel.selectedFixedInterval.value)
            return

        expenseViewModel.getFilteredExpensesForFixedIntervalUsingAllFilters()
            .observe(viewLifecycleOwner, Observer {
                val expenses = it.flatMap(CategoryWithExpenses::expenses)
                expenseViewModel.setSelectedFixedInterval(selectedFixedInterval)

                binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.text =
                    totalAverageForFixedInterval(selectedFixedInterval, expenses)
            })
    }

    abstract fun getFilteredExpenses() : LiveData<List<CategoryWithExpenses>>

    private fun setupBackdropExpenses(categoriesWithExpenses: List<CategoryWithExpenses>) {
        val categories = categoriesWithExpenses
            .map(CategoryWithExpenses::category)

        val expenses = categoriesWithExpenses
            .flatMap(CategoryWithExpenses::expenses)


        displayFilterResultInHeader(expenses.size)
        populateRecyclerView(categories, expenses)
        calculateTotalMoneySpent(expenses)
        calculateTotalAverageInFixedInterval()
    }

    private fun displayFilterResultInHeader(expenseCount: Int) {
        if (expenseCount == 1)
            binding.expenseBackdropFrontView.headerItemCountTextView.text =
                resources.getString(R.string.expenses_counter_text_singular, expenseCount)
        else
            binding.expenseBackdropFrontView.headerItemCountTextView.text =
                resources.getString(R.string.expenses_counter_text_plural, expenseCount)
    }

    abstract fun populateRecyclerView(categories: List<Category>, expenses: List<Expense>)

    private fun calculateTotalMoneySpent(expenses: List<Expense>) {
        binding.expenseBackdropFrontView.totalMoneySpentTextView.text =
            resources.getString(R.string.total_money_spent, expenseViewModel.getTotalMoneySpentFormatted(expenses))
    }

    private fun calculateTotalAverageInFixedInterval() {
        val fixedInterval = getFixedInterval()

        expenseViewModel.setSelectedFixedInterval(fixedInterval)
    }

    abstract fun getFixedInterval(): FixedInterval

    private fun totalAverageForFixedInterval(selectedFixedInterval: FixedInterval, expenses: List<Expense>): String =
        when (selectedFixedInterval) {
            FixedInterval.YEAR ->
                resources.getString(
                    R.string.money_amount_average_in_parenthesis,
                    withSuffix(expenseViewModel.calculateYearlyTotalAverage(expenses)),
                    FixedInterval.YEAR.toString().toLowerCase()
                )
            FixedInterval.MONTH ->
                resources.getString(
                    R.string.money_amount_average_in_parenthesis,
                    withSuffix(expenseViewModel.calculateMonthlyTotalAverage(expenses)),
                    FixedInterval.MONTH.toString().toLowerCase()
                )
            FixedInterval.DAY ->
                resources.getString(
                    R.string.money_amount_average_in_parenthesis,
                    withSuffix(expenseViewModel.calculateDailyTotalAverage(expenses)),
                    FixedInterval.DAY.toString().toLowerCase()
                )
        }

    private fun setupBackdropFilters(
        allCategories: List<Category>,
        filteredCategoriesWithExpenses: List<CategoryWithExpenses>
    ) {
        createCategoryFilter(allCategories, filteredCategoriesWithExpenses)
        createCurrencyFilter(allCategories)
        createExpenseAmountFilter(allCategories)
    }

    private fun createCategoryFilter(
        allCategories: List<Category>,
        filteredCategoriesWithExpenses: List<CategoryWithExpenses>
    ) {
        allCategories
            .map { category ->
                val newChip = Chip(binding.root.context)

                newChip.isChecked = true

                expenseViewModel.toggleCategoryFilter(category.id)

                val expenseCountForCategory = filteredCategoriesWithExpenses
                    .filter { categoryWithExpenses -> categoryWithExpenses.category == category }
                    .flatMap(CategoryWithExpenses::expenses)
                    .size
                newChip.text = "${category.name} (${expenseCountForCategory})"

                newChip.setOnClickListener { chip ->
                    expenseViewModel.toggleCategoryFilter(category.id)

                    getFilteredExpenses().observe(viewLifecycleOwner, Observer {
                        setupBackdropExpenses(it)
                        expenseViewModel.getAllCategories().observe(viewLifecycleOwner, Observer { allCategories ->
                            getExpensesWithoutCategoryFiltering().observe(viewLifecycleOwner, Observer { categoriesWithExpenses ->
                                updateCategoryFilters(categoriesWithExpenses, allCategories)
                            })
                        })
                    })
                }

                binding.expenseBackdropBackView.categoryBodyChipGroup.addView(newChip)
            }
    }

    abstract fun getExpensesWithoutCategoryFiltering(): LiveData<List<CategoryWithExpenses>>

    private fun updateCategoryFilters(
        categoriesWithExpenses: List<CategoryWithExpenses>,
        allCategories: List<Category>
    ) {
        (0 until binding.expenseBackdropBackView.categoryBodyChipGroup.childCount)
            .map { index ->
                val chip = binding.expenseBackdropBackView.categoryBodyChipGroup.getChildAt(index) as Chip

                val currentCategory = allCategories[index]

                val expenseCountForCategory = categoriesWithExpenses
                    .filter { categoryWithExpenses -> categoryWithExpenses.category == currentCategory }
                    .flatMap(CategoryWithExpenses::expenses)
                    .size

                chip.text = "${currentCategory.name} (${expenseCountForCategory})"
            }
    }

    private fun createExpenseAmountFilter(allCategories: List<Category>) {
        val rangeSeekbar = binding.expenseBackdropBackView.amountRangeSeekbar

        val rangeSeekbarMinTextView = binding.expenseBackdropBackView.amountRangeSeekbarMinTextView
        val rangeSeekbarMaxTextView = binding.expenseBackdropBackView.amountRangeSeekbarMaxTextView

        rangeSeekbar.setOnRangeSeekbarChangeListener { minValue, maxValue ->
            rangeSeekbarMinTextView.text = minValue.toString()
            rangeSeekbarMaxTextView.text = maxValue.toString()
        }

        rangeSeekbar.setOnRangeSeekbarFinalValueListener { minValue, maxValue ->
            expenseViewModel.setMinAndMaxAmount(minValue.toDouble(), maxValue.toDouble())
            getFilteredExpenses().observe(viewLifecycleOwner, Observer {
                setupBackdropExpenses(it)
            })
        }

//        amountTextInputEditText.addTextChangedListener(object : TextWatcher {
//            private var timeAfterLastTextChange: Long = 0
//            private val DELAY: Long = 700
//
//            private var handler = Handler()
//
//            private val inputFinishChecker = Runnable {
//                if (System.currentTimeMillis() > (timeAfterLastTextChange + DELAY - 500)) {
//                    getFilteredExpenses().observe(viewLifecycleOwner, Observer {
//                        setupBackdropExpenses(it)
//                    })
//                }
//            }
//
//            override fun afterTextChanged(s: Editable) {
//                if (s.isNotEmpty()) {
//                    timeAfterLastTextChange = System.currentTimeMillis()
//                    kFunction1(s.toString())
//                    handler.postDelayed(inputFinishChecker, DELAY)
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                handler.removeCallbacks(inputFinishChecker)
//            }
//        })
    }

    private fun createCurrencyFilter(allCategories: List<Category>) {

    }
}