package com.ogrob.moneybox.presentation.statistics

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.StatisticsViewModel
import com.ogrob.moneybox.databinding.FragmentStatisticsDetailBinding
import com.ogrob.moneybox.persistence.model.Expense
import java.time.Month

class StatisticsDetailFragment : Fragment() {

    private val statisticsViewModel: StatisticsViewModel by lazy {
        ViewModelProviders.of(this).get(StatisticsViewModel::class.java)
    }

    private lateinit var binding: FragmentStatisticsDetailBinding

    private lateinit var args: StatisticsDetailFragmentArgs


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics_detail, container, false)

        this.args = StatisticsDetailFragmentArgs.fromBundle(arguments!!)

        statisticsViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            statisticsViewModel.setCategoriesWithExpensesAndSelectedCategories(it)

            val selectedExpenses = statisticsViewModel.getSelectedExpensesForStatistics(args.selectedYear, Month.valueOf(args.selectedMonth))
            val totalAmountFromSelectedExpenses = statisticsViewModel.calculateTotalAmount(selectedExpenses)

            populateFilterCheckBoxes()
            initializeViews(totalAmountFromSelectedExpenses)
            populateExpensesDetailedRecyclerView(selectedExpenses, totalAmountFromSelectedExpenses)
        })

        return binding.root
    }

    private fun populateFilterCheckBoxes() {
        val multiChoiceListener = DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
            statisticsViewModel.updateSelectedCategories(which, isChecked)

            val selectedExpenses = statisticsViewModel.getSelectedExpensesForStatistics(args.selectedYear, Month.valueOf(args.selectedMonth))
            val totalAmountFromSelectedExpenses = statisticsViewModel.calculateTotalAmount(selectedExpenses)

            initializeViews(totalAmountFromSelectedExpenses)
            populateExpensesDetailedRecyclerView(selectedExpenses, totalAmountFromSelectedExpenses)
        }

        val allCategoryNames = statisticsViewModel.getAllCategoryNames()

        binding.categoryFilterButton.setOnClickListener {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Select categories to filter to:")
                .setMultiChoiceItems(
                    allCategoryNames,
                    BooleanArray(allCategoryNames.size) { index -> statisticsViewModel.isCategoryChecked(index) },
                    multiChoiceListener)
//                .setPositiveButton("Delete") { _, _ -> expenseViewModel.deleteExpense(expense) }
                .setNegativeButton("Done") { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }
    }

    private fun initializeViews(totalAmountFromSelectedExpenses: Double) {
        binding.yearAndMonthDetailTextView.text =
            "${args.selectedYear} ${args.selectedMonth} - $totalAmountFromSelectedExpenses"
    }

    private fun populateExpensesDetailedRecyclerView(selectedExpenses: List<Expense>,
                                                     totalAmountFromSelectedExpenses: Double) {
        val statisticsRecyclerViewAdapter = StatisticsDetailRecyclerViewAdapter(
            statisticsViewModel,
            totalAmountFromSelectedExpenses)

        statisticsRecyclerViewAdapter.submitList(selectedExpenses)

        binding.statisticsDetailRecyclerView.adapter = statisticsRecyclerViewAdapter
        binding.statisticsDetailRecyclerView.layoutManager = LinearLayoutManager(context)
    }

}