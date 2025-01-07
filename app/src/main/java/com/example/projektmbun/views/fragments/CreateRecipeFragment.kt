package com.example.projektmbun.views.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.projektmbun.R
import com.example.projektmbun.controller.ImageUploadController
import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.controller.RecipeTempDataHandler
import com.example.projektmbun.controller.RecipeValidator
import com.example.projektmbun.databinding.FragmentCreateRecipeBinding
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.S3Uploader
import com.example.projektmbun.utils.enums.DifficultyEnum
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import com.example.projektmbun.views.temp_data_models.TemporaryEquipment
import com.example.projektmbun.views.temp_data_models.TemporaryInstruction
import kotlinx.coroutines.launch

class CreateRecipeFragment : Fragment() {
    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUploadController: ImageUploadController
    private lateinit var recipeService: RecipeService
    private lateinit var recipeController: RecipeController
    private lateinit var recipeTempDataHandler: RecipeTempDataHandler
    private lateinit var recipeValidator: RecipeValidator

    private var editingIngredientIndex: Int? = null
    private var editingInstructionIndex: Int? = null

    private val dropdownOptionsType = listOf("Vorspeise", "Hauptspeise", "Abendbrot", "Nachtisch", "Snack", "Beilage", "Dip")
    private val dropdownOptionsCategory = FoodCategoryEnum.entries
        .filter { it != FoodCategoryEnum.UNBEKANNT }
        .map { Converters.fromCategoryEnum(it) }
    private val dropdownOptionsUnit = UnitsEnum.entries
        .filter { it != UnitsEnum.UNBEKANNT }
        .map { Converters.fromUnitEnum(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipeTempDataHandler = RecipeTempDataHandler()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        val s3Uploader = S3Uploader(requireContext())
        imageUploadController = ImageUploadController(requireContext(), s3Uploader)
        recipeService = RecipeService()
        recipeController = RecipeController(recipeService)
        recipeValidator = RecipeValidator(recipeTempDataHandler, requireContext())

        setupUi()
        setupListeners()

        return binding.root
    }

    private fun setupUi() {
        setupDropdowns()
        changePortions()
        setupTimeSeekBar()
        setupSchwierigkeitButtonSelection()
        setupEquipmentButton()
    }

    private fun setupListeners() {
        setupUiTextChangedListener()
        setupUiClickListener()
        setupImageUploadListeners()
    }

    private fun setupUiClickListener() {
        // Submit Recipe Button
        binding.submitRecipe.setOnClickListener {
            handleRecipeSubmit()
        }

        // Add Ingredient Button
        binding.addIngredientsToRecipe.setOnClickListener {
            handleIngredientAdd()
        }

        // Add Instruction Button
        binding.addInstructionsToRecipe.setOnClickListener {
            handleInstructionAdd()
        }

        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun handleRecipeSubmit() {
        val recipeTitle = binding.recipeTitle.text.toString().trim()
        val recipePortions = binding.portionText.text.toString().trim()
        val dishType = binding.typeChoose.selectedItem.toString().trim()
        val shortDesc = binding.editRecipeDescMultiline.text.toString().trim()
        val prepTime = binding.preptimeSeekbar.progress
        val cookTime = binding.cooktimeSeekbar.progress
        val recipeSrc = binding.recipeSrcText.text.toString().trim()

        val isDairyFree = binding.checkboxLaktosefrei.isChecked
        val isGlutenFree = binding.checkboxGlutenfrei.isChecked
        val isNutFree = binding.checkboxNussfrei.isChecked
        val isVegan = binding.checkboxVegan.isChecked
        val isVegetarian = binding.checkboxVegetarisch.isChecked
        val isPescetarian = binding.checkboxPesketarisch.isChecked

        if (recipeValidator.validateRecipeInfoFields(recipeTitle, recipePortions, shortDesc, prepTime, recipeSrc)) {
            recipeTempDataHandler.updateRecipeBasicInfo(
                recipeTitle,
                recipePortions.toInt(),
                dishType,
                shortDesc,
                prepTime,
                cookTime,
                recipeSrc
            )

            recipeTempDataHandler.updateRecipeDietaryInfo(
                isDairyFree,
                isGlutenFree,
                isNutFree,
                isVegan,
                isVegetarian,
                isPescetarian
            )

            viewLifecycleOwner.lifecycleScope.launch {
                saveRecipeToDatabase()
            }
        }
    }

    private fun handleIngredientAdd() {
        val ingredientName = binding.ingredientNameText.text.toString()
        val ingredientCategoryEnum = Converters.toCategoryEnum(binding.categoryChooser.selectedItem.toString())
        val ingredientShortDesc = binding.ingredientDescTextMultiline.text.toString()
        val ingredientAmount = binding.ingredientAmountText.text.toString().toDoubleOrNull()
        val ingredientUnit = Converters.toUnitEnum(binding.unitChooser.selectedItem.toString())
        val ingredientPrice = binding.ingredientPriceText.text.toString().toDoubleOrNull()
        val isIngredientOptional = binding.switchOptional.isChecked

        if(recipeValidator.validateIngredientFields(ingredientName, ingredientAmount?.toInt(), ingredientPrice)) {
            if (editingIngredientIndex != null) {
                if (recipeTempDataHandler.updateIngredient(
                        editingIngredientIndex!!,
                        ingredientName,
                        ingredientCategoryEnum!!,
                        ingredientShortDesc,
                        ingredientAmount!!,
                        ingredientUnit,
                        ingredientPrice!!,
                        isIngredientOptional
                    )) {
                    updateIngredientInTable(editingIngredientIndex!!, ingredientName, ingredientAmount.toInt(), ingredientUnit.toString())
                    editingIngredientIndex = null
                    binding.addIngredientsToRecipe.text = "Hinzufügen"
                }
            } else {
                val index = recipeTempDataHandler.addIngredient(
                    ingredientName,
                    ingredientCategoryEnum!!,
                    ingredientShortDesc,
                    ingredientAmount!!,
                    ingredientUnit,
                    ingredientPrice!!,
                    isIngredientOptional
                )
                addIngredientToTable(ingredientName, ingredientAmount.toInt(), ingredientUnit.toString())
            }
            clearIngredientInputFields()
        }
    }

    private fun handleInstructionAdd() {
        val instructionDesc = binding.editTextMultilineInstructions.text.toString().trim()
        val equipmentList = collectEquipmentFromUI()
        val instructionImageUrl = recipeTempDataHandler.getTempInstructionImage()

        if (recipeValidator.validateInstructionFields(instructionDesc)) {
            if (editingInstructionIndex != null) {
                recipeTempDataHandler.updateInstruction(
                    editingInstructionIndex!!,
                    instructionDesc,
                    instructionImageUrl,
                    equipmentList
                )
                addInstructionToTable(recipeTempDataHandler.getInstruction(editingInstructionIndex!!)!!.first, editingInstructionIndex)
                editingInstructionIndex = null
                binding.addInstructionsToRecipe.text = "Hinzufügen"
            } else {
                val index = recipeTempDataHandler.addInstruction(
                    instructionDesc,
                    instructionImageUrl,
                    equipmentList
                )
                addInstructionToTable(recipeTempDataHandler.getInstruction(index)!!.first)
            }
            clearInstructionFields()
        }
    }

    private fun clearInstructionFields() {
        binding.editTextMultilineInstructions.text.clear()
        clearDynamicEquipmentViews()
        clearInstructionImage()
    }

    private fun setupUiTextChangedListener() {
        setupCharacterCounters()
        setupPriceInputFormatter()
    }

    private fun setupCharacterCounters() {
        setupCharacterCounter(binding.editRecipeDescMultiline, binding.remainingCharsText, 300)
        setupCharacterCounter(binding.ingredientDescTextMultiline, binding.remainingCharsTextIngredients, 100)
        setupCharacterCounter(binding.editTextMultilineInstructions, binding.remainingCharsTextInstructions, 1000)
    }

    private fun setupCharacterCounter(editText: EditText, counterView: TextView, maxLength: Int) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val remainingChars = maxLength - s.toString().length
                counterView.text = "Verbleibende Zeichen: $remainingChars"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupPriceInputFormatter() {
        binding.ingredientPriceText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.contains(".") || input.contains(",")) {
                    val parts = input.split(".", ",")
                    if (parts.size > 1 && parts[1].length > 2) {
                        val fixedInput = parts[0] + "." + parts[1].take(2)
                        binding.ingredientPriceText.setText(fixedInput)
                        binding.ingredientPriceText.setSelection(fixedInput.length)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupImageUploadListeners() {
        binding.recipeImageUploader.setOnClickListener {
            if (imageUploadController.isPermissionGranted(imageUploadController.checkPermission())) {
                selectImage()
            } else {
                requestPermissionLauncher.launch(imageUploadController.checkPermission())
            }
        }

        binding.instructionsImageUploader.setOnClickListener {
            selectInstructionImage()
        }
    }

    private fun addIngredientToTable(name: String, amount: Int, unit: String) {
        val tableLayout = binding.ingredientsTable
        val newRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        val amountTextView = TextView(requireContext()).apply {
            text = "$amount $unit"
            setPadding(8, 8, 8, 8)
        }

        val nameTextView = TextView(requireContext()).apply {
            text = name
            setPadding(8, 8, 8, 8)
        }

        val editButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_edit_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                editIngredient(rowIndex)
            }
        }

        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_close_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                if (recipeTempDataHandler.removeIngredient(rowIndex)) {
                    tableLayout.removeView(newRow)
                }
            }
        }

        newRow.apply {
            addView(amountTextView)
            addView(nameTextView)
            addView(editButton)
            addView(deleteButton)
        }

        tableLayout.addView(newRow)
    }

    private fun updateIngredientInTable(index: Int, name: String, amount: Int, unit: String) {
        val tableLayout = binding.ingredientsTable
        val row = tableLayout.getChildAt(index) as? TableRow ?: return

        (row.getChildAt(0) as? TextView)?.text = "$amount $unit"
        (row.getChildAt(1) as? TextView)?.text = name
    }

    private fun editIngredient(index: Int) {
        val ingredientPair = recipeTempDataHandler.getIngredient(index) ?: return
        val (tempFood, ingredientToEdit) = ingredientPair

        binding.ingredientNameText.setText(tempFood.name)
        binding.ingredientAmountText.setText(ingredientToEdit.amount?.toInt().toString())
        binding.ingredientDescTextMultiline.setText(ingredientToEdit.description)
        binding.ingredientPriceText.setText(ingredientToEdit.price.toString())
        binding.switchOptional.isChecked = ingredientToEdit.isOptional

        val categoryString = Converters.fromCategoryEnum(tempFood.category)
        val categoryIndex = dropdownOptionsCategory.indexOf(categoryString)
        if (categoryIndex != -1) {
            binding.categoryChooser.setSelection(categoryIndex)
        }

        val unitString = Converters.fromUnitEnum(ingredientToEdit.unit)
        val unitIndex = dropdownOptionsUnit.indexOf(unitString)
        if (unitIndex != -1) {
            binding.unitChooser.setSelection(unitIndex)
        }

        editingIngredientIndex = index
        binding.addIngredientsToRecipe.text = "Aktualisieren"
    }

    private fun clearIngredientInputFields() {
        binding.ingredientNameText.text.clear()
        binding.ingredientAmountText.text.clear()
        binding.ingredientDescTextMultiline.text.clear()
        binding.ingredientPriceText.text.clear()
        binding.switchOptional.isChecked = false
    }

    private fun addInstructionToTable(instruction: TemporaryInstruction, index: Int? = null) {
        val tableLayout = binding.instructionsTable
        val newRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        val instructionTextView = TextView(requireContext()).apply {
            text = instruction.description
            setPadding(8, 8, 8, 8)
        }

        val editButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_edit_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                editInstruction(rowIndex)
            }
        }

        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_close_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                if (recipeTempDataHandler.removeInstruction(rowIndex)) {
                    tableLayout.removeView(newRow)
                }
            }
        }

        newRow.apply {
            addView(instructionTextView)
            addView(editButton)
            addView(deleteButton)
        }

        if (index != null) {
            tableLayout.removeViewAt(index)
            tableLayout.addView(newRow, index)
        } else {
            tableLayout.addView(newRow)
        }
    }

    private fun editInstruction(index: Int) {
        val instructionPair = recipeTempDataHandler.getInstruction(index) ?: return
        val (instruction, equipments) = instructionPair

        binding.editTextMultilineInstructions.setText(instruction.description)

        if (!instruction.imageUrl.isNullOrBlank()) {
            displayInstructionImage(instruction.imageUrl!!)
        } else {
            clearInstructionImage()
        }

        clearDynamicEquipmentViews()
        fillEquipmentViews(equipments)

        editingInstructionIndex = index
        binding.addInstructionsToRecipe.text = "Aktualisieren"
    }

    private fun clearInstructionImage() {
        val cardView = binding.instructionsImageContainer
        val placeholderIcon = binding.instructionsImagePlaceholderIc

        cardView.removeAllViews()
        cardView.visibility = View.GONE
        placeholderIcon.visibility = View.VISIBLE

        recipeTempDataHandler.clearTempInstructionImage()
    }

    private fun fillEquipmentViews(equipments: List<TemporaryEquipment>) {
        val utensilsContainer = binding.instructionEquipmentContainer
        val addUtensilButton = binding.addEquipmentButton

        equipments.forEach { equipment ->
            val newEditText = EditText(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                setText(equipment.equipment)
                textSize = 14f
                minWidth = 200
                setBackgroundResource(R.drawable.new_recipe_background)
                setPadding(16, 16, 16, 16)
            }

            val addButtonIndex = utensilsContainer.indexOfChild(addUtensilButton)
            utensilsContainer.addView(newEditText, addButtonIndex)
        }
    }

    private fun clearDynamicEquipmentViews() {
        val utensilsContainer = binding.instructionEquipmentContainer
        for (i in utensilsContainer.childCount - 1 downTo 0) {
            val view = utensilsContainer.getChildAt(i)
            if (view is EditText) {
                utensilsContainer.removeView(view)
            }
        }
    }

    private fun collectEquipmentFromUI(): List<TemporaryEquipment> {
        val equipmentList = mutableListOf<TemporaryEquipment>()
        val equipmentContainer = binding.instructionEquipmentContainer
        for (i in 0 until equipmentContainer.childCount) {
            val view = equipmentContainer.getChildAt(i)
            if (view is EditText) {
                val equipmentName = view.text.toString().trim()
                if (equipmentName.isNotEmpty()) {
                    equipmentList.add(TemporaryEquipment(equipment = equipmentName))
                }
            }
        }
        return equipmentList
    }

    private suspend fun saveRecipeToDatabase() {
        try {
            val success = recipeController.addRecipeWithDetails(
                recipeTempDataHandler.getRecipeData(),
                recipeTempDataHandler.getAllIngredients().map { it.second },
                recipeTempDataHandler.getAllInstructions(),
                recipeTempDataHandler.getAllEquipment().values.flatten(),
                recipeTempDataHandler.getAllIngredients().map { it.first }
            )

            if (success) {
                showToast("Rezept erfolgreich gespeichert!")
                recipeTempDataHandler.clearAll()
            } else {
                showToast("Fehler beim Speichern des Rezepts.")
            }
        } catch (e: Exception) {
            Log.e("CreateRecipeFragment", "Fehler beim Speichern des Rezepts: ${e.localizedMessage}")
            showToast("Fehler beim Speichern des Rezepts.")
        }
    }

    private fun setupDropdowns() {
        setupDropdownMenuType()
        setupDropdownMenuCategory()
        setupDropdownMenuUnit()
    }

    private fun setupDropdownMenuType() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropdownOptionsType
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.typeChoose.adapter = adapter

        val defaultType = "Hauptspeise"
        val defaultPosition = dropdownOptionsType.indexOf(defaultType)
        if (defaultPosition >= 0) {
            binding.typeChoose.setSelection(defaultPosition)
        }

        binding.typeChoose.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                dropdownOptionsType[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDropdownMenuCategory() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropdownOptionsCategory
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.categoryChooser.adapter = adapter

        val defaultType = "Getreide"
        val defaultPosition = dropdownOptionsCategory.indexOf(defaultType)
        if (defaultPosition >= 0) {
            binding.categoryChooser.setSelection(defaultPosition)
        }
    }

    private fun setupDropdownMenuUnit() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropdownOptionsUnit
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.unitChooser.adapter = adapter

        val defaultUnit = "g"
        val defaultPosition = dropdownOptionsUnit.indexOf(defaultUnit)
        if (defaultPosition >= 0) {
            binding.unitChooser.setSelection(defaultPosition)
        }

        binding.unitChooser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedUnit = dropdownOptionsUnit[position]
                val unitEnum = Converters.toUnitEnum(selectedUnit)

                if (unitEnum == UnitsEnum.NACH_GESCHMACK) {
                    binding.ingredientAmountText.apply {
                        setText("1")
                        isEnabled = false
                    }
                } else {
                    binding.ingredientAmountText.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun setupTimeSeekBar() {
        setupPrepTimeSeekBar()
        setupCookTimeSeekBar()
    }

    private fun setupPrepTimeSeekBar() {
        val seekBar = binding.preptimeSeekbar
        val prepTimeText = binding.preptimeText

        seekBar.max = 120 + 24 + 7

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val timeText = when {
                    progress <= 120 -> "$progress Minuten"
                    progress in 121..144 -> "${(progress - 119)} Stunden"
                    else -> "${(progress - 143)} Tage"
                }
                prepTimeText.text = timeText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupCookTimeSeekBar() {
        val seekBar = binding.cooktimeSeekbar
        val cookTimeText = binding.cooktimeText

        seekBar.max = 120 + 24 + 7

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val timeText = when {
                    progress <= 120 -> "$progress Minuten"
                    progress in 121..144 -> "${(progress - 119)} Stunden"
                    else -> "${(progress - 143)} Tage"
                }
                cookTimeText.text = timeText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun changePortions() {
        binding.buttonPlus.setOnClickListener {
            val currentPortions = binding.portionText.text.toString().toInt()
            binding.portionText.text = (currentPortions + 1).toString()
        }

        binding.buttonMinus.setOnClickListener {
            val currentPortions = binding.portionText.text.toString().toInt()
            if (currentPortions > 1) {
                binding.portionText.text = (currentPortions - 1).toString()
            }
        }
    }

    private fun setupSchwierigkeitButtonSelection() {
        val buttons = listOf(
            binding.optionAnfaenger,
            binding.optionFortgeschritten,
            binding.optionProfi
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                buttons.forEach { it.isSelected = false }
                button.isSelected = true

                val difficulty = when (button.text.toString()) {
                    "Anfänger" -> DifficultyEnum.EASY
                    "Fortgeschritten" -> DifficultyEnum.MEDIUM
                    "Profi" -> DifficultyEnum.ADVANCED
                    else -> null
                }

                recipeTempDataHandler.updateRecipeDifficulty(difficulty ?: DifficultyEnum.EASY)
                updateButtonStyles(buttons)
            }
        }
    }

    private fun updateButtonStyles(buttons: List<Button>) {
        buttons.forEach { button ->
            if (button.isSelected) {
                button.setBackgroundResource(R.drawable.new_recipe_background_active)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.pure_white))
            } else {
                button.setBackgroundResource(R.drawable.new_recipe_background)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    private fun setupEquipmentButton() {
        binding.addEquipmentButton.setOnClickListener {
            addNewEquipmentEditText()
        }
    }

    private fun addNewEquipmentEditText() {
        val newEditText = EditText(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            hint = "Utensil hinzufügen"
            textSize = 14f
            minWidth = 200
            setBackgroundResource(R.drawable.new_recipe_background)
            setPadding(16, 16, 16, 16)
        }

        val addButtonIndex = binding.instructionEquipmentContainer.indexOfChild(binding.addEquipmentButton)
        binding.instructionEquipmentContainer.addView(newEditText, addButtonIndex)
    }

    private fun handleInstructionImageUpload(uri: Uri) {
        val oldImageUrl = recipeTempDataHandler.getTempInstructionImage()

        imageUploadController.uploadImage(
            uri = uri,
            path = "recipe_instructions_images/",
            onSuccess = { imageUrl ->
                displayInstructionImage(imageUrl)
                recipeTempDataHandler.setTempInstructionImage(imageUrl)

                if (!oldImageUrl.isNullOrBlank()) {
                    deleteImage(oldImageUrl)
                }
                showToast("Bild erfolgreich hochgeladen")
            },
            onError = { showToast(it) }
        )
    }

    private fun handleRecipeImageUpload(uri: Uri) {
        val oldImageUrl = recipeTempDataHandler.getRecipeData().imageUrl

        imageUploadController.uploadImage(
            uri = uri,
            onSuccess = { imageUrl ->
                imageUploadController.displayImage(
                    imageUrl = imageUrl,
                    container = binding.recipeImageContainer,
                    placeholderView = binding.recipeImagePlaceholderIc
                )
                recipeTempDataHandler.updateRecipeImage(imageUrl)

                if (oldImageUrl.isNotBlank()) {
                    deleteImage(oldImageUrl)
                }
            },
            onError = { showToast(it) }
        )
    }

    private fun displayInstructionImage(imageUrl: String) {
        val cardView = binding.instructionsImageContainer
        val placeholderIcon = binding.instructionsImagePlaceholderIc

        placeholderIcon.visibility = View.GONE
        cardView.visibility = View.VISIBLE

        imageUploadController.displayImage(
            imageUrl = imageUrl,
            container = cardView
        )
    }

    private fun deleteImage(imageUrl: String) {
        imageUploadController.deleteImage(
            imageUrl = imageUrl,
            onSuccess = {
                Log.d("ImageDeletion", "Altes Bild erfolgreich gelöscht: $imageUrl")
            },
            onError = { error ->
                Log.e("ImageDeletion", "Fehler beim Löschen des alten Bildes: $error")
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun selectImage() {
        filePickerLauncher.launch("image/*")
    }

    private fun selectInstructionImage() {
        instructionImageFilePickerLauncher.launch("image/*")
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            selectImage()
        } else {
            showToast("Berechtigung erforderlich, um fortzufahren!")
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleRecipeImageUpload(it) }
            ?: showToast("Kein Bild ausgewählt")
    }

    private val instructionImageFilePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleInstructionImageUpload(it) }
            ?: showToast("Kein Bild ausgewählt")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}