package com.ogrob.moneybox.ui.category

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ogrob.moneybox.data.viewmodel.ExpenseViewModel
import com.ogrob.moneybox.databinding.FragmentCategoryAddAndEditBinding
import com.ogrob.moneybox.persistence.model.Category
import com.ogrob.moneybox.ui.BaseFragment
import com.ogrob.moneybox.utils.NEW_CATEGORY_PLACEHOLDER_ID
import com.ogrob.moneybox.utils.hideKeyboard

class CategoryAddAndEditFragment : BaseFragment() {

    private val expenseViewModel: ExpenseViewModel by viewModels()

    private lateinit var binding: FragmentCategoryAddAndEditBinding

    private lateinit var args: CategoryAddAndEditFragmentArgs


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentCategoryAddAndEditBinding.inflate(inflater)

        args = CategoryAddAndEditFragmentArgs.fromBundle(requireArguments())

        initOnClickListeners()

        expenseViewModel.getAllCategories_OLD().observe(viewLifecycleOwner, Observer {
            initViewsAndButtons()
            applyTextWatcher(it)
        })

        return binding.root
    }

    private fun initOnClickListeners() {
        binding.root.setOnClickListener { it.hideKeyboard() }
        binding.categoryColorTextView.setOnClickListener { onChooseCategoryColor(it) }

        binding.categoryAddEditDeleteButton.setOnClickListener {
            it.hideKeyboard()
            showDeleteAlertDialog(it)
        }
        binding.categoryAddEditPositiveButton.setOnClickListener {
            it.hideKeyboard()
            addNewOrEditedCategory(it)
        }
        binding.categoryAddEditCancelButton.setOnClickListener {
            it.hideKeyboard()
            it.findNavController().navigate(CategoryAddAndEditFragmentDirections.actionCategoryAddAndEditFragmentToCategoryFragment())
        }
    }

    private fun initViewsAndButtons() {
        if (args.categoryId == NEW_CATEGORY_PLACEHOLDER_ID)
            binding.categoryAddEditDeleteButton.visibility = View.GONE

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
        val colorPickerDialog = ColorPickerDialog.createColorPickerDialog(binding.root.context, ColorPickerDialog.DARK_THEME)

        colorPickerDialog.setInitialColor((view.background as ColorDrawable).color)
        colorPickerDialog.hideOpacityBar()
        colorPickerDialog.show()

        colorPickerDialog.setOnColorPickedListener { color, _ ->
            view.setBackgroundColor(color)
        }
    }

    private fun showDeleteAlertDialog(view: View) {
        MaterialAlertDialogBuilder(binding.root.context)
            .setTitle("Delete category")
            .setMessage("Are you sure you want to delete \'${args.categoryName}\'")
            .setNeutralButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Delete") { dialog, which ->
                deleteCategory(view)
            }
            .show()
    }

    private fun deleteCategory(view: View) {
        expenseViewModel.deleteCategoryById(args.categoryId)
        Toast.makeText(binding.root.context, "Category ${args.categoryName} deleted!", Toast.LENGTH_LONG).show()
        view.findNavController().navigate(CategoryAddAndEditFragmentDirections.actionCategoryAddAndEditFragmentToCategoryFragment())
    }

    private fun addNewOrEditedCategory(view: View) {
        expenseViewModel.addOrEditCategory(
            args.categoryId,
            binding.categoryNameEditText.text.toString(),
            Color.RED
//            (binding.categoryColorTextView.background as ColorDrawable).color
            )

        view.findNavController().navigate(CategoryAddAndEditFragmentDirections.actionCategoryAddAndEditFragmentToCategoryFragment())
    }

}