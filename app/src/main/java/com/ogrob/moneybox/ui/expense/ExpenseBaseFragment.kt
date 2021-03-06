package com.ogrob.moneybox.ui.expense

import android.app.ActionBar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.helper.*
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentExpenseBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.persistence.model.Currency
import com.ogrob.moneybox.persistence.model.Expense
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

abstract class ExpenseBaseFragment : BaseFragment() {

  protected val expenseViewModel: ExpenseViewModel by viewModels()

  protected lateinit var binding: FragmentExpenseBinding


  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    binding = FragmentExpenseBinding.inflate(inflater)

    val filterImageView = requireActivity().toolbar.getChildAt(1) as ImageView
    filterImageView.visibility = View.VISIBLE


    showLoadingAnimation()

    addOnClickListeners()

    setupObservers(filterImageView)


    getExpensesBasedOnFragmentAndFilters(getFilterValuesFromSharedPreferences())


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

  private fun setupObservers(filterImageView: ImageView) {
    expenseViewModel.filteredExpenses.observe(viewLifecycleOwner) {
      hideLoadingAnimation()
      setupExpenses(it)
    }

    expenseViewModel.unfilteredExpenses.observe(viewLifecycleOwner) {
      configureBackdropContainer(filterImageView)
    }

    expenseViewModel.selectedFixedIntervalWithAverage.observe(viewLifecycleOwner) {
      SharedPreferenceManager.putStringSharedPreference(
        binding.root.context,
        SHARED_PREFERENCES_SELECTED_FIXED_INTERVAL_KEY,
        it.fixedInterval.name
      )

      formatAverageForFixedInterval(it)
    }

    setupCategoryFilterInfoObserver()
    setupAmountFilterInfoObserver()
    setupCurrencyFilterInfoObserver()
  }

  private fun setupCategoryFilterInfoObserver() {
    expenseViewModel.allCategoryFilterInfo.observe(viewLifecycleOwner) { event ->
      val categoryFilterInfo = event.peekContent()

      updateFilterSharedPreferences(SHARED_PREFERENCES_SELECTED_CATEGORY_IDS_KEY, categoryFilterInfo.selectedCategoryIds.toMutableList())

      when (categoryFilterInfo.updateFilterOption) {
        UpdateFilterOption.CREATE_CHIPS -> event.getContentIfNotHandled()?.let { setupCategoryFilter(it) }
        UpdateFilterOption.ONLY_UPDATE_CHECKBOXES -> updateCategoryFilterCheckboxes(categoryFilterInfo)
        UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS -> updateCategoryFilterChipTexts(categoryFilterInfo)
      }
    }
  }

  private fun setupAmountFilterInfoObserver() {
    expenseViewModel.allAmountFilterInfo.observe(viewLifecycleOwner) { event ->
      val amountFilterInfo = event.peekContent()

      SharedPreferenceManager.putStringSetSharedPreference(
        binding.root.context,
        SHARED_PREFERENCES_SELECTED_EXPENSE_AMOUNT_RANGE_KEY,
        setOf(amountFilterInfo.selectedAmountMinValue.toString(), amountFilterInfo.selectedAmountMaxValue.toString())
      )

      if (amountFilterInfo.createRangeBar)
        event.getContentIfNotHandled()?.let { setupAmountFilter(it) }
    }
  }

  private fun setupCurrencyFilterInfoObserver() {
    expenseViewModel.allCurrencyFilterInfo.observe(viewLifecycleOwner) { event ->
      val currencyFilterInfo = event.peekContent()

      updateFilterSharedPreferences(SHARED_PREFERENCES_SELECTED_CURRENCY_IDS_KEY, currencyFilterInfo.selectedCurrencyIds.toMutableList())

      when (currencyFilterInfo.updateFilterOption) {
        UpdateFilterOption.CREATE_CHIPS -> event.getContentIfNotHandled()?.let { setupCurrencyFilter(it) }
        UpdateFilterOption.ONLY_UPDATE_CHECKBOXES -> updateCurrencyFilterCheckboxes(currencyFilterInfo)
        UpdateFilterOption.ONLY_UPDATE_CHIP_TEXTS -> updateCurrencyFilterChipTexts(currencyFilterInfo)
      }
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

  private fun formatAverageForFixedInterval(selectedFixedIntervalWithAverage: FixedIntervalWithAverage) {
    val average = selectedFixedIntervalWithAverage.average

    binding.expenseBackdropFrontView.totalMoneySpentAverageTextView.text =
      when (selectedFixedIntervalWithAverage.fixedInterval) {
        FixedInterval.YEAR ->
          resources.getString(
            R.string.money_amount_average_in_parenthesis,
            withSuffix(average),
            FixedInterval.YEAR.toString().toLowerCase(Locale.getDefault())
          )
        FixedInterval.MONTH ->
          resources.getString(
            R.string.money_amount_average_in_parenthesis,
            withSuffix(average),
            FixedInterval.MONTH.toString().toLowerCase(Locale.getDefault())
          )
        FixedInterval.DAY ->
          resources.getString(
            R.string.money_amount_average_in_parenthesis,
            withSuffix(average),
            FixedInterval.DAY.toString().toLowerCase(Locale.getDefault())
          )
      }
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

  abstract fun getExpensesBasedOnFragmentAndFilters(savedValuesFromSharedPreferences: SavedValuesFromSharedPreferences)

  private fun getFilterValuesFromSharedPreferences(): SavedValuesFromSharedPreferences {
    val selectedCategoryIdStrings = SharedPreferenceManager.getStringSetSharedPreference(
      binding.root.context,
      SHARED_PREFERENCES_SELECTED_CATEGORY_IDS_KEY,
      setOf()
    )
    val selectedExpenseAmountRangeStrings = SharedPreferenceManager.getStringSetSharedPreference(
      binding.root.context,
      SHARED_PREFERENCES_SELECTED_EXPENSE_AMOUNT_RANGE_KEY,
      setOf()
    )
    val selectedCurrencyIdStrings = SharedPreferenceManager.getStringSetSharedPreference(
      binding.root.context,
      SHARED_PREFERENCES_SELECTED_CURRENCY_IDS_KEY,
      setOf()
    )
    val selectedFixedInterval = SharedPreferenceManager.getStringSharedPreference(
      binding.root.context,
      SHARED_PREFERENCES_SELECTED_FIXED_INTERVAL_KEY,
      FixedInterval.YEAR.name
    )

    return SavedValuesFromSharedPreferences(
      selectedCategoryIdStrings,
      selectedExpenseAmountRangeStrings,
      selectedCurrencyIdStrings,
      selectedFixedInterval
    )
  }

  private fun setupCategoryFilter(allCategoryFilterInfo: CategoryFilterInfo) {
    allCategoryFilterInfo.categoriesWithExpenseCount
      .map { categoryWithExpenseCount ->
        createCategoryChip(
          categoryWithExpenseCount,
          allCategoryFilterInfo.selectedCategoryIds
        )
      }

    updateFilterIcon(
      binding.expenseBackdropBackView.categoryCheckboxIcon,
      allCategoryFilterInfo.categoriesWithExpenseCount.size,
      allCategoryFilterInfo.selectedCategoryIds.size
    )

    binding.expenseBackdropBackView.categoryCheckboxIcon.setOnClickListener {
      val currentlySelectedCategoryIds = expenseViewModel.getCurrentlySelectedCategoryIds()

      when (currentlySelectedCategoryIds.size) {
        0 -> selectAllCategoryFilters()
        else -> deSelectAllCategoryFilters()
      }
    }
  }

  private fun createCategoryChip(
    categoryWithExpenseCount: Map.Entry<Category, Int>,
    selectedCategoryIds: MutableSet<Long>
  ) {
    val newChip = Chip(binding.root.context).apply {
      checkedIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)
      closeIcon = null
      isChecked = selectedCategoryIds.contains(categoryWithExpenseCount.key.id)
      chipIcon = setFilterChipIcon(isChecked)

      val params = ActionBar.LayoutParams(
        ActionBar.LayoutParams.WRAP_CONTENT,
        ActionBar.LayoutParams.WRAP_CONTENT
      )
      params.setMargins(2, 2, 2, 2)
      layoutParams = params

      text = resources.getString(
        R.string.filter_text,
        categoryWithExpenseCount.key.name,
        categoryWithExpenseCount.value
      )
      tag = categoryWithExpenseCount.key.id
    }

    newChip.setOnClickListener { chip ->
      (chip as Chip).apply {
        chipIcon = setFilterChipIcon(isChecked)
      }

      expenseViewModel.toggleCategoryFilter(categoryWithExpenseCount.key.id)
    }

    binding.expenseBackdropBackView.categoryBodyChipGroup.addView(newChip)
  }

  private fun selectAllCategoryFilters() {
    modifyAllFilterChipCheckboxes(
      binding.expenseBackdropBackView.categoryBodyChipGroup,
      true
    )
    binding.expenseBackdropBackView.categoryCheckboxIcon.background =
      ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)

    expenseViewModel.selectAllCategoryFilters()
  }

  private fun deSelectAllCategoryFilters() {
    modifyAllFilterChipCheckboxes(
      binding.expenseBackdropBackView.categoryBodyChipGroup,
      true
    )
    binding.expenseBackdropBackView.categoryCheckboxIcon.background =
      ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_blank_24, null)

    expenseViewModel.deSelectAllCategoryFilters()
  }

  private fun setupAmountFilter(allAmountFilterInfo: AmountFilterInfo) {
    if (allAmountFilterInfo.amountMinValue == EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)
      return

    val cheapestAndMostExpensiveExpenseAmount = Pair(allAmountFilterInfo.amountMinValue, allAmountFilterInfo.amountMaxValue)
    val cheapestAndMostExpensiveSelectedExpenseAmount = if (allAmountFilterInfo.selectedAmountMinValue == EXPENSE_AMOUNT_RANGE_FILTER_DEFAULT_VALUE)
      cheapestAndMostExpensiveExpenseAmount
    else
      Pair(allAmountFilterInfo.selectedAmountMinValue, allAmountFilterInfo.selectedAmountMaxValue)

    updateAmountFilterCheckbox(cheapestAndMostExpensiveSelectedExpenseAmount, cheapestAndMostExpensiveExpenseAmount.first, cheapestAndMostExpensiveExpenseAmount.second)

    val rangeSeekbar = binding.expenseBackdropBackView.amountRangeSeekbar.apply {
      setMinValue(cheapestAndMostExpensiveExpenseAmount.first.toFloat())
      setMinStartValue(cheapestAndMostExpensiveSelectedExpenseAmount.first.toFloat())

      setMaxValue(cheapestAndMostExpensiveExpenseAmount.second.toFloat())
      setMaxStartValue(cheapestAndMostExpensiveSelectedExpenseAmount.second.toFloat())

      apply()
    }

    val rangeSeekbarMinTextView = binding.expenseBackdropBackView.amountRangeSeekbarMinTextView.apply {
      text = cheapestAndMostExpensiveSelectedExpenseAmount.first.toString()
    }
    val rangeSeekbarMaxTextView = binding.expenseBackdropBackView.amountRangeSeekbarMaxTextView.apply {
      text = cheapestAndMostExpensiveSelectedExpenseAmount.second.toString()
    }

    rangeSeekbar.setOnRangeSeekbarChangeListener { minValue, maxValue ->
      rangeSeekbarMinTextView.text = minValue.toString()
      rangeSeekbarMaxTextView.text = maxValue.toString()
    }

    rangeSeekbar.setOnRangeSeekbarFinalValueListener { minValue, maxValue ->
      expenseViewModel.setMinAndMaxSelectedAmount(
        cheapestAndMostExpensiveExpenseAmount.first,
        cheapestAndMostExpensiveExpenseAmount.second,
        minValue.toDouble(),
        maxValue.toDouble()
      )

      updateAmountFilterCheckbox(cheapestAndMostExpensiveExpenseAmount, minValue, maxValue)
    }

    binding.expenseBackdropBackView.amountCheckboxIcon.setOnClickListener {
      if (cheapestAndMostExpensiveExpenseAmount.first != binding.expenseBackdropBackView.amountRangeSeekbar.selectedMinValue ||
        cheapestAndMostExpensiveExpenseAmount.second != binding.expenseBackdropBackView.amountRangeSeekbar.selectedMaxValue)
        resetRangeBar(
          cheapestAndMostExpensiveExpenseAmount,
          cheapestAndMostExpensiveSelectedExpenseAmount
        )
    }
  }

  private fun resetRangeBar(
    cheapestAndMostExpensiveExpenseAmount: Pair<Double, Double>,
    cheapestAndMostExpensiveSelectedExpenseAmount: Pair<Double, Double>
  ) {
    binding.expenseBackdropBackView.amountRangeSeekbar.apply {
      expenseViewModel.setMinAndMaxSelectedAmount(
        cheapestAndMostExpensiveExpenseAmount.first,
        cheapestAndMostExpensiveExpenseAmount.second,
        cheapestAndMostExpensiveSelectedExpenseAmount.first,
        cheapestAndMostExpensiveSelectedExpenseAmount.second
      )

      setMinValue(cheapestAndMostExpensiveExpenseAmount.first.toFloat())
      setMinStartValue(cheapestAndMostExpensiveExpenseAmount.first.toFloat())

      setMaxValue(cheapestAndMostExpensiveExpenseAmount.second.toFloat())
      setMaxStartValue(cheapestAndMostExpensiveExpenseAmount.second.toFloat())

      apply()

      binding.expenseBackdropBackView.amountCheckboxIcon.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)
    }
  }

  private fun updateAmountFilterCheckbox(
    cheapestAndMostExpensiveExpenseAmount: Pair<Double, Double>,
    minValue: Number,
    maxValue: Number
  ) {
    binding.expenseBackdropBackView.amountCheckboxIcon.background =
      if (cheapestAndMostExpensiveExpenseAmount.first == minValue.toDouble() && cheapestAndMostExpensiveExpenseAmount.second == maxValue.toDouble())
        ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)
      else
        ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_blank_24, null)
  }

  private fun setupCurrencyFilter(allCurrencyFilterInfo: CurrencyFilterInfo) {
    allCurrencyFilterInfo.currenciesWithExpenseCount
      .map { currencyWithExpenseCount -> createCurrencyChip(currencyWithExpenseCount, allCurrencyFilterInfo.selectedCurrencyIds) }

    updateFilterIcon(
      binding.expenseBackdropBackView.currencyCheckboxIcon,
      allCurrencyFilterInfo.currenciesWithExpenseCount.size,
      allCurrencyFilterInfo.selectedCurrencyIds.size
    )

    binding.expenseBackdropBackView.currencyCheckboxIcon.setOnClickListener {
      val currentlySelectedCurrencyIds = expenseViewModel.getCurrentlySelectedCurrencyIds()

      when (currentlySelectedCurrencyIds.size) {
        0 -> selectAllCurrencyFilters()
        else -> deSelectAllCurrencyFilters()
      }
    }
  }

  private fun createCurrencyChip(
    currencyWithExpenseCount: Map.Entry<Currency, Int>,
    selectedCurrencyIds: MutableSet<Long>
  ) {
    val newChip = Chip(binding.root.context).apply {
      checkedIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)
      closeIcon = null
      isChecked = selectedCurrencyIds.contains(currencyWithExpenseCount.key.id)
      chipIcon = setFilterChipIcon(isChecked)

      val params = ActionBar.LayoutParams(
        ActionBar.LayoutParams.WRAP_CONTENT,
        ActionBar.LayoutParams.WRAP_CONTENT
      )
      params.setMargins(2, 2, 2, 2)
      layoutParams = params

      text = resources.getString(
        R.string.filter_text,
        currencyWithExpenseCount.key.name,
        currencyWithExpenseCount.value
      )
      tag = currencyWithExpenseCount.key.id
    }

    newChip.setOnClickListener { chip ->
      (chip as Chip).apply {
        chipIcon = setFilterChipIcon(isChecked)
      }

      expenseViewModel.toggleCurrencyFilter(currencyWithExpenseCount.key.id)
    }

    binding.expenseBackdropBackView.currencyBodyChipGroup.addView(newChip)
  }

  private fun selectAllCurrencyFilters() {
    modifyAllFilterChipCheckboxes(
      binding.expenseBackdropBackView.currencyBodyChipGroup,
      true
    )
    binding.expenseBackdropBackView.currencyCheckboxIcon.background =
      ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)

    expenseViewModel.selectAllCurrencyFilters()
  }

  private fun deSelectAllCurrencyFilters() {
    modifyAllFilterChipCheckboxes(
      binding.expenseBackdropBackView.currencyBodyChipGroup,
      false
    )
    binding.expenseBackdropBackView.currencyCheckboxIcon.background =
      ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_blank_24, null)

    expenseViewModel.deSelectAllCurrencyFilters()
  }

  private fun modifyAllFilterChipCheckboxes(
    filterBodyChipGroup: ChipGroup,
    isSelected: Boolean
  ) {
    filterBodyChipGroup.children.forEach {
      (it as Chip).apply {
        isChecked = isSelected
        chipIcon = setFilterChipIcon(isSelected)
      }
    }
  }

  private fun updateFilterSharedPreferences(
    sharedPreferencesKey: String,
    sharedPreferencesValues: MutableList<Long>
  ) {
    val sharedPreferencesValuesStringSet = sharedPreferencesValues
      .map(Long::toString)
      .toSet()

    SharedPreferenceManager.putStringSetSharedPreference(
      binding.root.context,
      sharedPreferencesKey,
      sharedPreferencesValuesStringSet
    )
  }

  private fun updateCategoryFilterCheckboxes(allCategoryFilterInfo: CategoryFilterInfo) {
    updateFilterIcon(
      binding.expenseBackdropBackView.categoryCheckboxIcon,
      allCategoryFilterInfo.categoriesWithExpenseCount.size,
      allCategoryFilterInfo.selectedCategoryIds.size
    )
  }

  private fun updateCategoryFilterChipTexts(allCategoryFilterInfo: CategoryFilterInfo) {
    binding.expenseBackdropBackView.categoryBodyChipGroup.children
      .forEach { chip ->
        val categoryWithExpenseCount = getCategory(chip.tag.toString().toLong(), allCategoryFilterInfo)

        (chip as Chip).apply {
          text = resources.getString(
            R.string.filter_text,
            categoryWithExpenseCount.key.name,
            categoryWithExpenseCount.value
          )
        }
      }
  }

  private fun getCategory(tag: Long, allCategoryFilterInfo: CategoryFilterInfo): Map.Entry<Category, Int> {
    return allCategoryFilterInfo
      .categoriesWithExpenseCount
      .entries
      .first { entry -> entry.key.id == tag }
  }

  private fun updateCurrencyFilterCheckboxes(allCurrencyFilterInfo: CurrencyFilterInfo) {
    updateFilterIcon(
      binding.expenseBackdropBackView.currencyCheckboxIcon,
      allCurrencyFilterInfo.currenciesWithExpenseCount.size,
      allCurrencyFilterInfo.selectedCurrencyIds.size
    )
  }

  private fun updateCurrencyFilterChipTexts(allCurrencyFilterInfo: CurrencyFilterInfo) {
    binding.expenseBackdropBackView.currencyBodyChipGroup.children
      .forEach { chip ->
        val currencyWithExpenseCount = getCurrency(chip.tag.toString().toLong(), allCurrencyFilterInfo)

        (chip as Chip).apply {
          text = resources.getString(
            R.string.filter_text,
            currencyWithExpenseCount.key.name,
            currencyWithExpenseCount.value
          )
        }
      }
  }

  private fun getCurrency(tag: Long, allCurrencyFilterInfo: CurrencyFilterInfo): Map.Entry<Currency, Int> {
    return allCurrencyFilterInfo
      .currenciesWithExpenseCount
      .entries
      .first { entry -> entry.key.id == tag }
  }

  private fun setFilterChipIcon(checked: Boolean) =
    if (checked)
      ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)
    else
    ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_blank_24, null)

  private fun updateFilterIcon(
    filterCheckboxIcon: TextView,
    totalExpenseCount: Int,
    selectedExpenseCount: Int
  ) {
    when {
      allFiltersInDropdownSelected(totalExpenseCount, selectedExpenseCount) -> filterCheckboxIcon.background =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_24, null)

      noFilterInDropdownSelected(selectedExpenseCount) -> filterCheckboxIcon.background =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_check_box_blank_24, null)

      else -> filterCheckboxIcon.background =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_indeterminate_check_box_24, null)
    }
  }

  private fun allFiltersInDropdownSelected(totalExpenseCount: Int, selectedExpenseCount: Int) =
    totalExpenseCount == selectedExpenseCount

  private fun noFilterInDropdownSelected(selectedExpenseCount: Int) =
    selectedExpenseCount == 0

}