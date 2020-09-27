package com.ogrob.moneybox.ui.expense

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.FixedInterval
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.CategoryWithExpenses
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.ui.helper.FilterOption
import com.ogrob.moneybox.ui.helper.UpdatedFilterValuesDTO
import com.ogrob.moneybox.utils.*
import kotlinx.android.synthetic.main.activity_main.*

abstract class ExpenseBaseFragment : BaseFragment() {

  protected val expenseViewModel: ExpenseViewModel by viewModels()

  protected lateinit var binding: FragmentExpenseBinding


  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    binding = FragmentExpenseBinding.inflate(inflater)

    val filterImageView = requireActivity().toolbar.getChildAt(1) as ImageView
    filterImageView.visibility = View.VISIBLE


    binding.expenseBackdropFrontView.bodyConstraintLayout.setOnClickListener {
      it.findNavController().navigate(ExpenseAllFragmentDirections.actionExpenseAllFragmentToExpenseSelectedFragment(2019, 10))
    }

    showLoadingAnimation()

    addOnClickListeners()


    /** still need to do bottom bar */
    initTotalAverageInFixedIntervalObserver()
    binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.setOnClickListener { onChangeTotalAverageInterval(it) }
    binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG


    expenseViewModel.filteredExpenses.observe(viewLifecycleOwner, Observer {
      hideLoadingAnimation()
      setupExpenses(it)
    })

    expenseViewModel.unfilteredExpenses.observe(viewLifecycleOwner, Observer {
      configureBackdropContainer(filterImageView)
      setupFilters(it)
    })

    expenseViewModel.filteredCategoriesWithExpensesForFilterUpdate.observe(viewLifecycleOwner, Observer {
      updateFilters(it)
    })


    getExpensesBasedOnFragmentAndFilters()


//        getFilteredExpenses_OLD().observe(viewLifecycleOwner, Observer {
////            Thread.sleep(1000)
//            hideLoadingAnimation()
//            configureBackdropContainer(filterImageView)
//            setupBackdropExpenses(it)
//            expenseViewModel.getAllCategories()
//                .observe(viewLifecycleOwner, Observer { allCategories ->
//                    getExpensesWithoutCategoryFiltering().observe(
//                        viewLifecycleOwner,
//                        Observer { categoriesWithExpenses ->
//                            setupBackdropFilters(allCategories, categoriesWithExpenses)
//                        })
//                })
//        })

    return binding.root
  }

  private fun addOnClickListeners() {
    initFilterHeaderOnClickListeners()

    binding.expenseBackdropFrontView.headerLinearLayout.setOnClickListener { binding.backdropContainer.closeBackView() }
    binding.expenseBackdropFrontView.addExpenseFloatingActionButton.setOnClickListener { onAddNewExpense(it) }
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
      filterHeaderIcon.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_expand_more_white_24dp, null)
      filterBodyView.visibility = View.GONE
    }
    else {
      filterHeaderIcon.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_expand_less_white_24dp, null)
      filterBodyView.visibility = View.VISIBLE
    }
  }

  abstract fun onAddNewExpense(view: View)

  private fun configureBackdropContainer(filterImageView: ImageView) {
    binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

        binding.backdropContainer
          .dropInterpolator(LinearInterpolator())
          .dropHeight(binding.backdropContainer.height - binding.expenseBackdropFrontView.headerLinearLayout.height)
          .imageView(filterImageView)
          .build()
      }
    })
  }

  private fun setupExpenses(filteredExpenses: List<Expense>) {
    setupFragmentSpecificViews()

    binding.expenseBackdropFrontView.headerItemCountTextView.text = formatExpenseCounterText(filteredExpenses.size)

    populateRecyclerView(filteredExpenses)
    calculateTotalMoneySpent(filteredExpenses)
  }

  abstract fun setupFragmentSpecificViews()

  abstract fun populateRecyclerView(filteredExpenses: List<Expense>)

  private fun calculateTotalMoneySpent(filteredExpenses: List<Expense>) {
    binding.expenseBackdropFrontView.totalMoneySpentTextView.text =
      resources.getString(R.string.total_money_spent, expenseViewModel.getTotalMoneySpentFormatted(filteredExpenses))
  }

  abstract fun getExpensesBasedOnFragmentAndFilters()

  private fun setupFilters(allCategoriesWithExpenses: List<CategoryWithExpenses>) {
    setupCategoryFilter(allCategoriesWithExpenses)
    setupAmountFilter(allCategoriesWithExpenses)
    setupCurrencyFilter(allCategoriesWithExpenses)
  }

  private fun setupCategoryFilter(allCategoriesWithExpenses: List<CategoryWithExpenses>) {
    val categoriesWithExpenseCount = expenseViewModel.getCategoriesWithExpenseCount(allCategoriesWithExpenses)

    categoriesWithExpenseCount
      .map(this::createCategoryChip)
  }

  private fun createCategoryChip(categoryWithExpenseCount: Map.Entry<Category, Int>) {
    val newChip = Chip(binding.root.context)

    newChip.isChecked = expenseViewModel.isCategorySelected(categoryWithExpenseCount.key.id)

    newChip.text = "${categoryWithExpenseCount.key.name} (${categoryWithExpenseCount.value})"
    newChip.tag = categoryWithExpenseCount.key.id

    newChip.setOnClickListener { chip ->
      expenseViewModel.toggleCategoryFilter(categoryWithExpenseCount.key.id)

      expenseViewModel.updateFilters(FilterOption.CATEGORY)
      expenseViewModel.updateAllFilteredExpenses()
    }

    binding.expenseBackdropBackView.categoryBodyChipGroup.addView(newChip)
  }

  private fun setupAmountFilter(allCategoriesWithExpenses: List<CategoryWithExpenses>) {
    val cheapestAndMostExpensiveExpenseAmount = expenseViewModel.getCheapestAndMostExpensiveExpenseAmount(allCategoriesWithExpenses)
  }

  private fun setupCurrencyFilter(allCategoriesWithExpenses: List<CategoryWithExpenses>) {
    val currenciesWithExpenseCount = expenseViewModel.getCurrenciesWithExpenseCount(allCategoriesWithExpenses)

    currenciesWithExpenseCount
      .map(this::createCurrencyChip)
  }

  private fun createCurrencyChip(currencyWithExpenseCount: Map.Entry<Currency, Int>) {
    val newChip = Chip(binding.root.context)

    newChip.isChecked = expenseViewModel.isCurrencySelected(currencyWithExpenseCount.key.id)

    newChip.text = "${currencyWithExpenseCount.key.name} (${currencyWithExpenseCount.value})"
    newChip.tag = currencyWithExpenseCount.key.id

    newChip.setOnClickListener { chip ->
      expenseViewModel.toggleCurrencyFilter(currencyWithExpenseCount.key.id)

      expenseViewModel.updateFilters(FilterOption.CURRENCY)
      expenseViewModel.updateAllFilteredExpenses()
    }

    binding.expenseBackdropBackView.currencyBodyChipGroup.addView(newChip)
  }

  private fun updateFilters(updatedFilterValuesDTO: UpdatedFilterValuesDTO) {
      updateCategoryFilter(updatedFilterValuesDTO)

//      updateCategoryFilter(allCategoriesWithExpenses)

      updateCurrencyFilter(updatedFilterValuesDTO)
  }

  private fun updateCategoryFilter(updatedFilterValuesDTO: UpdatedFilterValuesDTO) {
    updateFilterIcon(binding.expenseBackdropBackView.categoryCheckboxIcon, updatedFilterValuesDTO.totalAndSelectedCategoryWithExpenseCount)

    binding.expenseBackdropBackView.categoryBodyChipGroup.children
      .forEach { chip ->
        val categoryWithExpenseCount = getCategory(chip, updatedFilterValuesDTO)
        (chip as Chip).text = "${categoryWithExpenseCount.key.name} (${categoryWithExpenseCount.value})"
      }
  }

  private fun getCategory(chip: View, categoriesWithExpenseCount: UpdatedFilterValuesDTO): Map.Entry<Category, Int> {
    return categoriesWithExpenseCount
      .filteredCategoryWithExpenseCount
      .entries
      .first { entry -> entry.key.id == chip.tag }
  }

  private fun updateCurrencyFilter(updatedFilterValuesDTO: UpdatedFilterValuesDTO) {
    updateFilterIcon(binding.expenseBackdropBackView.currencyCheckboxIcon, updatedFilterValuesDTO.totalAndSelectedCurrencyWithExpenseCount)

    binding.expenseBackdropBackView.currencyBodyChipGroup.children
      .forEach { chip ->
        val currencyWithExpenseCount = getCurrency(chip, updatedFilterValuesDTO)

        val currentChip = (chip as Chip)

        currentChip.text = "${currencyWithExpenseCount.key.name} (${currencyWithExpenseCount.value})"
      }
  }

  private fun getCurrency(chip: View, currenciesWithExpenseCount: UpdatedFilterValuesDTO): Map.Entry<Currency, Int> {
    return currenciesWithExpenseCount
      .filteredCurrencyWithExpenseCount
      .entries
      .first { entry -> entry.key.id == chip.tag }
  }

  private fun updateFilterIcon(
    filterCheckboxIcon: TextView,
    totalAndSelectedFilterWithExpenseCount: Pair<Int, Int>
  ) {
    when {
      allFiltersInDropdownSelected(totalAndSelectedFilterWithExpenseCount) -> filterCheckboxIcon.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)
      noFilterInDropdownSelected(totalAndSelectedFilterWithExpenseCount) -> filterCheckboxIcon.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_blank_24, null)
      else -> filterCheckboxIcon.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_indeterminate_check_box_24, null)
    }
  }

  private fun allFiltersInDropdownSelected(totalAndSelectedFilterWithExpenseCount: Pair<Int, Int>): Boolean =
    totalAndSelectedFilterWithExpenseCount.first == totalAndSelectedFilterWithExpenseCount.second

  private fun noFilterInDropdownSelected(totalAndSelectedFilterWithExpenseCount: Pair<Int, Int>): Boolean =
    totalAndSelectedFilterWithExpenseCount.second == 0













  private fun initTotalAverageInFixedIntervalObserver() {
    expenseViewModel.selectedFixedInterval.observe(viewLifecycleOwner, Observer { fixedInterval ->
//            expenseViewModel.getFilteredExpensesForFixedIntervalUsingAllFilters()
      getFilteredExpenses_OLD()
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

  abstract fun getFilteredExpenses_OLD() : LiveData<List<CategoryWithExpenses>>

  private fun setupBackdropExpenses(categoriesWithExpenses: List<CategoryWithExpenses>) {
    calculateTotalAverageInFixedInterval()
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

          getFilteredExpenses_OLD().observe(viewLifecycleOwner, Observer {
            setupBackdropExpenses(it)
//                        expenseViewModel.getAllCategories().observe(viewLifecycleOwner, Observer { allCategories ->
//                            getExpensesWithoutCategoryFiltering().observe(viewLifecycleOwner, Observer { categoriesWithExpenses ->
//                                updateCategoryFilters(categoriesWithExpenses, allCategories)
//                            })
//                        })
          })
        }

        binding.expenseBackdropBackView.categoryBodyChipGroup.addView(newChip)
      }
  }

  abstract fun getExpensesWithoutCategoryFiltering(): LiveData<List<CategoryWithExpenses>>

//    private fun updateCategoryFilters(
//        categoriesWithExpenses: List<CategoryWithExpenses>,
//        allCategories: List<Category>
//    ) {
//        (0 until binding.expenseBackdropBackView.categoryBodyChipGroup.childCount)
//            .map { index ->
//                val chip = binding.expenseBackdropBackView.categoryBodyChipGroup.getChildAt(index) as Chip
//
//                val currentCategory = allCategories[index]
//
//                val expenseCountForCategory = categoriesWithExpenses
//                    .filter { categoryWithExpenses -> categoryWithExpenses.category == currentCategory }
//                    .flatMap(CategoryWithExpenses::expenses)
//                    .size
//
//                chip.text = "${currentCategory.name} (${expenseCountForCategory})"
//            }
//    }

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
      getFilteredExpenses_OLD().observe(viewLifecycleOwner, Observer {
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