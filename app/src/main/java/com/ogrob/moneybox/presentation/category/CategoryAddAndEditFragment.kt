package com.ogrob.moneybox.presentation.category

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.ogrob.moneybox.R
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentCategoryAddAndEditBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.hideKeyboard
import top.defaults.colorpicker.ColorPickerPopup

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

        binding.root.setOnClickListener { it.hideKeyboard() }
        binding.categoryColorTextView.setOnClickListener { onChooseCategoryColor(it) }
        binding.categoryAddEditPositiveButton.setOnClickListener {
            it.hideKeyboard()
            addNewOrEditedCategory(it)
        }
        binding.categoryAddEditCancelButton.setOnClickListener {
            it.hideKeyboard()
            it.findNavController().navigate(CategoryAddAndEditFragmentDirections.actionCategoryAddAndEditFragmentToCategoryFragment())
        }

        args = CategoryAddAndEditFragmentArgs.fromBundle(arguments!!)

        expenseViewModel.getAllCategories().observe(viewLifecycleOwner, Observer {
            initViewsAndButtons()
            applyTextWatcher(it)
        })

        return binding.root
    }

    private fun initViewsAndButtons() {
        binding.categoryNameEditText.setText(args.categoryName)
        binding.categoryAddEditPositiveButton.isEnabled = args.categoryId != NEW_CATEGORY_PLACEHOLDER_ID
        binding.categoryAddEditPositiveButton.text = args.positiveButtonText
        binding.categoryColorTextView.setBackgroundColor(args.categoryColor)
    }

    private fun applyTextWatcher(allCategories: List<Category>) {
        val allCategoryNames = allCategories
            .map(Category::name)
            .map(String::toLowerCase)

        val newExpenseEditTextTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val categoryAlreadyAdded = allCategoryNames.contains(s.toString().toLowerCase(resources.configuration.locales[0]))

                binding.categoryAddEditPositiveButton.isEnabled =
                    binding.categoryNameEditText.text.isNotBlank() && !categoryAlreadyAdded

                if (categoryAlreadyAdded)
                    Toast.makeText(binding.root.context, "There is already a category named \"$s\"", Toast.LENGTH_LONG).show()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

        }

        binding.categoryNameEditText.addTextChangedListener(newExpenseEditTextTextWatcher)
    }

    private fun onChooseCategoryColor(view: View) {
        val colorPickerPopup = object : ColorPickerPopup.ColorPickerObserver() {
            override fun onColorPicked(color: Int) {
                view.setBackgroundColor(color)
            }
        }

        ColorPickerPopup.Builder(binding.root.context)
            .initialColor((view.background as ColorDrawable).color)
            .enableBrightness(false)
            .enableAlpha(false)
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(true)
            .build()
            .show(view, colorPickerPopup)
    }

    private fun addNewOrEditedCategory(view: View) {
        expenseViewModel.addOrEditCategory(
            args.categoryId,
            binding.categoryNameEditText.text.toString(),
            (binding.categoryColorTextView.background as ColorDrawable).color)

        view.findNavController().navigate(CategoryAddAndEditFragmentDirections.actionCategoryAddAndEditFragmentToCategoryFragment())
    }

}