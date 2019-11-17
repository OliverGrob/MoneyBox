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
import com.ogrob.moneybox.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private val statisticsViewModel: StatisticsViewModel by lazy {
        ViewModelProviders.of(this).get(StatisticsViewModel::class.java)
    }

    private lateinit var binding: FragmentStatisticsBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false)

        statisticsViewModel.getAllCategoriesWithExpenses().observe(viewLifecycleOwner, Observer {
            statisticsViewModel.setCategoriesWithExpensesAndSelectedCategories(it)

            populateFilterCheckBoxes()
            populateExpenseSummaryRecyclerView()
            calculateTotalAverage()
        })

        return binding.root
    }

    private fun populateFilterCheckBoxes() {
        val multiChoiceListener = DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
            statisticsViewModel.updateSelectedCategories(which, isChecked)
            populateExpenseSummaryRecyclerView()
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
//                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }
    }

    private fun populateExpenseSummaryRecyclerView() {
        val statisticsRecyclerViewAdapter = StatisticsRecyclerViewAdapter(statisticsViewModel)
        binding.statisticsRecyclerView.adapter = statisticsRecyclerViewAdapter
        binding.statisticsRecyclerView.layoutManager = LinearLayoutManager(context)

        statisticsRecyclerViewAdapter.setExpensesByYearAndAmount(statisticsViewModel.filterForSelectedCategories())
    }

    private fun calculateTotalAverage() {
        binding.averageTotalPerMonthTextView.text = "Total: ${statisticsViewModel.calculateTotalAverage()}/month"
    }
}