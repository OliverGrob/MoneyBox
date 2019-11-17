package com.ogrob.moneybox.presentation.category

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentCategoryAddAndEditBinding
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID

class CategoryAddAndEditFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by lazy {
        ViewModelProviders.of(this).get(ExpenseViewModel::class.java)
    }

    private lateinit var binding: FragmentCategoryAddAndEditBinding

    private lateinit var args: CategoryAddAndEditFragmentArgs


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category_add_and_edit, container, false)

        binding.categoryAddEditPositiveButton.setOnClickListener { addNewOrEditedCategory(it) }
        binding.categoryAddEditCancelButton.setOnClickListener {
            it.findNavController().navigate(CategoryAddAndEditFragmentDirections.actionCategoryAddAndEditFragmentToCategoryFragment())
        }

        this.args = CategoryAddAndEditFragmentArgs.fromBundle(arguments!!)

        initTextViewsAndButtons()
        applyTextWatcher()

        return binding.root
    }

    private fun initTextViewsAndButtons() {
        this.binding.categoryEditText.setText(args.categoryName)
        this.binding.categoryAddEditPositiveButton.isEnabled = args.categoryId != NEW_CATEGORY_PLACEHOLDER_ID
        this.binding.categoryAddEditPositiveButton.text = args.positiveButtonText
    }

    private fun applyTextWatcher() {
        val newExpenseEditTextTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                binding.categoryAddEditPositiveButton.isEnabled =
                    binding.categoryEditText.text.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

        }

        binding.categoryEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
    }

    private fun addNewOrEditedCategory(view: View) {
        this.expenseViewModel.addOrEditCategory(
            this.args.categoryId,
            this.binding.categoryEditText.text.toString())

        view.findNavController().navigate(CategoryAddAndEditFragmentDirections.actionCategoryAddAndEditFragmentToCategoryFragment())
    }

}