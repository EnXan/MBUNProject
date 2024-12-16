package com.example.projektmbun.views.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.projektmbun.R
import com.example.projektmbun.controller.ImageUploadController
import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.databinding.FragmentCreateRecipeBinding
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.data_structure.recipe.DifficultyEnum
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.S3Uploader
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.FoodStateEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import com.example.projektmbun.views.temp_data_models.TemporaryEquipment
import com.example.projektmbun.views.temp_data_models.TemporaryFood
import com.example.projektmbun.views.temp_data_models.TemporaryIngredient
import com.example.projektmbun.views.temp_data_models.TemporaryInstruction
import com.example.projektmbun.views.temp_data_models.TemporaryRecipe
import kotlinx.coroutines.launch


class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageUploadController: ImageUploadController
    private lateinit var recipeService: RecipeService
    private lateinit var recipeController: RecipeController

    private val uploadedImageLinks = mutableListOf<String>()

    private val tempRecipe = TemporaryRecipe()
    private val tempIngredient = TemporaryIngredient()
    private val tempInstructions = TemporaryInstruction()
    private val tempEquipment = TemporaryEquipment()

    private val dropdownOptionsType = listOf("Frühstück", "Hauptspeise", "Abendbrot", "Nachtisch", "Snack", "Beilage", "Dip")
    private val dropdownOptionsCategory = FoodCategoryEnum.entries.map { it.name }
    private val dropdownOptionsState = FoodStateEnum.entries.map { it.name }
    private val dropdownOptionsUnit = UnitsEnum.entries.map { it.name }

    private val ingredientsList = mutableListOf<Pair<TemporaryFood, TemporaryIngredient>>()
    private var editingIngredientIndex: Int? = null
    private val instructionsWithEquipments = mutableMapOf<TemporaryInstruction, List<TemporaryEquipment>>()
    private val instructionsList = mutableListOf<TemporaryInstruction>()
    private var editingInstructionIndex: Int? = null



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

        //setup recipe details
        changePortions()
        setupDropdownMenuType()
        setupPrepTimeSeekBar()
        setupCookTimeSeekBar()
        setupSchwierigkeitButtonSelection()

        //setup ingredients
        setupDropdownMenuCategory()
        setupDropdownMenuState()
        setupDropdownMenuUnit()

        //setup instructions
        setupEquipmentButton()



        setupUIListeners()

        // Upload recipe image on click
        binding.recipeImageUploader.setOnClickListener {
            checkPermissionAndSelectImage()
        }
        // Upload instruction image on click
        binding.instructionsImageUploader.setOnClickListener {
            selectInstructionImage()
        }

        return binding.root
    }

    private fun checkPermissionAndSelectImage() {
        val permission = imageUploadController.checkPermission()

        if (imageUploadController.isPermissionGranted(permission)) {
            selectImage()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun validateRecipeInfoFields(recipeTitle: String, recipePortions: String, shortDesc: String, prepTime: Int, recipeSrc: String): Boolean {
        var isValid = true

        if(tempRecipe.imageUrl.isBlank()) {
            Toast.makeText(requireContext(), "Bitte ein Rezeptbild hinzufügen!", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (recipeTitle.isBlank()) {
            binding.recipeTitle.error = "This field is required!"
            isValid = false
        }

        if (recipePortions.isBlank() || recipePortions.toIntOrNull() == null) {
            binding.portionText.error = "This field requires a valid number!"
            isValid = false
        }

        if (shortDesc.isBlank()) {
            binding.editRecipeDescMultiline.error = "This field is required!"
            isValid = false
        }

        if (prepTime == 0) {
            binding.preptimeText.error = "Preparation time cannot be zero!"
            isValid = false
        }

        if (recipeSrc.isBlank()) {
            binding.recipeSrcText.error = "This field is required!"
            isValid = false
        }

        return isValid
    }
    private fun validateIngredientFields(ingredientName: String, ingredientShortDesc: String, ingredientAmount: Int?, ingredientPrice: Double?, foodCategory: FoodCategoryEnum?, foodState: FoodStateEnum?): Boolean {
        var isValid = true

        if (ingredientName.isBlank()) {
            binding.ingredientNameText.error = "This field is required!"
            isValid = false
        }

        if (ingredientShortDesc.isBlank()) {
            binding.ingredientDescTextMultiline.error = "This field is required!"
            isValid = false
        }

        if (ingredientAmount == null || ingredientAmount <= 0) {
            binding.ingredientAmountText.error = "Bitte eine gültige Menge eingeben!"
            isValid = false
        }

        if (ingredientPrice == null || ingredientPrice <= 0) {
            binding.ingredientPriceText.error = "This field requires a valid number!"
            isValid = false
        }

        if (foodCategory == null) {
            //TODO: Implement error message
            isValid = false
        }

        if (foodState == null) {
            //TODO: Implement error message
            isValid = false
        }

        return isValid
    }
    private fun validateIngredientExistence(): Boolean {
        return if (ingredientsList.isEmpty()) {
            Toast.makeText(requireContext(), "Fügen Sie mindestens eine Zutat hinzu!", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }
    private fun validateInstructionFields(instructionDesc: String): Boolean {
        var isValid = true

        if (instructionDesc.isBlank()) {
            binding.editTextMultilineInstructions.error = "This field is required!"
            isValid = false
        }

        return isValid
    }
    private fun validateInstructionExistence(): Boolean {
        Log.d("CreateRecipeFragment", "validateInstructionExistence called with: $tempInstructions")
        return if (instructionsList.isEmpty()) {
            Toast.makeText(requireContext(), "Fügen Sie mindestens einen Schritt hinzu!", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun setupUIListeners() {

        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        //listener for recipe infos
        binding.submitRecipe.setOnClickListener {
            // Get user input of recipe details
            val recipeTitle = binding.recipeTitle.text.toString().trim()
            val recipePortions = binding.portionText.text.toString().trim()
            val dishType = binding.typeChoose.selectedItem.toString().trim()
            val shortDesc = binding.editRecipeDescMultiline.text.toString().trim()
            val prepTime = binding.preptimeSeekbar.progress
            val cookTime = binding.cooktimeSeekbar.progress
            val readyTime = prepTime + cookTime
            val recipeSrc = binding.recipeSrcText.text.toString().trim()

            val isDairyFree = binding.checkboxLaktosefrei.isSelected
            val isGlutenFree = binding.checkboxGlutenfrei.isSelected
            val isNutFree = binding.checkboxNussfrei.isSelected
            val isVegan = binding.checkboxVegan.isSelected
            val isVegetarian = binding.checkboxVegetarisch.isSelected
            val isPescetarian = binding.checkboxPesketarisch.isSelected

            val isRecipeInfoValid = validateRecipeInfoFields(recipeTitle, recipePortions, shortDesc, prepTime, recipeSrc)
            val isIngredientExistenceValid = validateIngredientExistence()
            val isInstructionExistenceValid = validateInstructionExistence()

            // Validate fields
            if (isRecipeInfoValid && isIngredientExistenceValid && isInstructionExistenceValid) {
                // If validation passes, store data in tempRecipe
                tempRecipe.title = recipeTitle
                tempRecipe.servings = recipePortions.toInt()
                tempRecipe.preparationMinutes = prepTime
                tempRecipe.dishType = dishType
                tempRecipe.cookingMinutes = cookTime
                tempRecipe.readyInMinutes = readyTime
                tempRecipe.shortDescription = shortDesc
                tempRecipe.sourceUrl = recipeSrc
                tempRecipe.dairyFree = isDairyFree
                tempRecipe.glutenFree = isGlutenFree
                tempRecipe.nutFree = isNutFree
                tempRecipe.vegan = isVegan
                tempRecipe.vegetarian = isVegetarian
                tempRecipe.pescetarian = isPescetarian

                // Save to database
                viewLifecycleOwner.lifecycleScope.launch {
                    saveRecipeToDatabase()
                }

                Log.d("CreateRecipeFragment", "Rezeptinfos: $tempRecipe")
            }
        }


        // listener for ingredients
        binding.addIngredientsToRecipe.setOnClickListener {
            val ingredientName = binding.ingredientNameText.text.toString()
            val ingredientCategory = binding.categoryChooser.selectedItem.toString()
            val ingredientState = binding.stateChooser.selectedItem.toString()
            val ingredientShortDesc = binding.ingredientDescTextMultiline.text.toString()
            val ingredientAmount = binding.ingredientAmountText.text.toString().toIntOrNull()
            val ingredientUnit = binding.unitChooser.selectedItem.toString()
            val ingredientPrice = binding.ingredientPriceText.text.toString().toDoubleOrNull()
            val isIngredientOptional = binding.switchOptional.isChecked

            val foodCategory = Converters.toCategoryEnum(ingredientCategory)
            val foodState = Converters.toStateEnum(ingredientState)

            if(validateIngredientFields(ingredientName, ingredientShortDesc, ingredientAmount, ingredientPrice, foodCategory, foodState)) {
                val newFood = TemporaryFood(ingredientName, foodCategory, foodState)
                val newIngredient = TemporaryIngredient(
                    description = ingredientShortDesc,
                    amount = ingredientAmount?.toDouble(),
                    unit = Converters.toUnitsEnum(ingredientUnit),
                    price = ingredientPrice!!,
                    isOptional = isIngredientOptional
                )

                if (editingIngredientIndex != null) {
                    ingredientsList[editingIngredientIndex!!] = Pair(newFood, newIngredient)
                    updateIngredientInTable(editingIngredientIndex!!, ingredientName, ingredientAmount!!, ingredientUnit)

                    editingIngredientIndex = null
                    binding.addIngredientsToRecipe.text = "Hinzufügen"
                } else {
                    ingredientsList.add(Pair(newFood, newIngredient))
                    addIngredientToTable(ingredientName, ingredientAmount!!, ingredientUnit)
                }

                clearIngredientInputFields()

                Log.d("CreateRecipeFragment", "Ingredient: $newIngredient")
                Log.d("CreateRecipeFragment", "Food: $newFood")
            }
        }


        // Listener für den Hinzufügen-Button der Instructions
        binding.addInstructionsToRecipe.setOnClickListener {
            val instructionDesc = binding.editTextMultilineInstructions.text.toString().trim()
            val equipmentList = collectEquipmentFromUI()


            if (validateInstructionFields(instructionDesc)) {
                val newInstruction = TemporaryInstruction(description = instructionDesc)
                if (editingInstructionIndex != null) {
                    instructionsList[editingInstructionIndex!!] = newInstruction
                    instructionsWithEquipments[newInstruction] = equipmentList
                    addInstructionToTable(newInstruction, editingInstructionIndex)
                    editingInstructionIndex = null
                    binding.addInstructionsToRecipe.text = "Hinzufügen"
                } else {
                    instructionsList.add(newInstruction)
                    instructionsWithEquipments[newInstruction] = equipmentList
                    addInstructionToTable(newInstruction)
                }

                // Felder zurücksetzen
                binding.editTextMultilineInstructions.text.clear()
                clearDynamicEquipmentViews()

                Log.d("CreateRecipeFragment", "Instruction: $newInstruction")
            }
        }
    }







    private fun setupDropdownMenuType() {
        // Adapter erstellen und mit Spinner verbinden
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropdownOptionsType
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.typeChoose.adapter = adapter

        // Listener für Spinner-Auswahl
        binding.typeChoose.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                dropdownOptionsType[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Aktion, wenn keine Auswahl getroffen wird
            }
        }
    }
    private fun setupPrepTimeSeekBar() {
        val seekBar = binding.preptimeSeekbar
        val prepTimeText = binding.preptimeText

        seekBar.max = 120 + 24 + 7 // Maximal 10.000 Minuten (ca. 7 Tage)


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val timeText = when {
                    progress <= 120 -> "$progress Minuten" // Minutenbereich
                    progress in 121..144 -> "${(progress - 119)} Stunden" // Stundenbereich, mit Start bei 1
                    else -> "${(progress - 143)} Tage" // Tagebereich, mit Start bei 1
                }
                prepTimeText.text = timeText
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    private fun setupCookTimeSeekBar() {
        val seekBar = binding.cooktimeSeekbar
        val prepTimeText = binding.cooktimeText

        seekBar.max = 120 + 24 + 7 // Maximal 10.000 Minuten (ca. 7 Tage)


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val timeText = when {
                    progress <= 120 -> "$progress Minuten" // Minutenbereich
                    progress in 121..144 -> "${(progress - 119)} Stunden" // Stundenbereich, mit Start bei 1
                    else -> "${(progress - 143)} Tage" // Tagebereich, mit Start bei 1
                }
                prepTimeText.text = timeText
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
                val difficulty = button.toString()

                tempRecipe.difficulty = when (difficulty) {
                    "Anfänger" -> DifficultyEnum.EASY
                    "Fortgeschritten" -> DifficultyEnum.MEDIUM
                    "Profi" -> DifficultyEnum.ADVANCED
                    else -> null
                }

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
    private fun setupDropdownMenuCategory() {
        // Adapter erstellen und mit Spinner verbinden
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropdownOptionsCategory
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categoryChooser.adapter = adapter

        // Listener für Spinner-Auswahl
        binding.categoryChooser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                dropdownOptionsCategory[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Aktion, wenn keine Auswahl getroffen wird
            }
        }
    }
    private fun setupDropdownMenuState() {
        // Adapter erstellen und mit Spinner verbinden
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropdownOptionsState
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateChooser.adapter = adapter

        // Listener für Spinner-Auswahl
        binding.stateChooser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                dropdownOptionsState[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Aktion, wenn keine Auswahl getroffen wird
            }
        }
    }
    private fun setupDropdownMenuUnit() {
        // Adapter erstellen und mit Spinner verbinden
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropdownOptionsUnit
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.unitChooser.adapter = adapter

        // Listener für Spinner-Auswahl
        binding.unitChooser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                dropdownOptionsUnit[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Aktion, wenn keine Auswahl getroffen wird
            }
        }
    }
    private fun setupEquipmentButton() {
        val utensilsContainer = binding.instructionEquipmentContainer
        val addUtensilButton = binding.addEquipmentButton

        addUtensilButton.setOnClickListener {
            // Neues EditText-Feld für Utensil erstellen
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

            // Hinzufügen von TextWatcher zur automatischen Aktualisierung der Daten
            newEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Sicherstellen, dass Equipment-Daten aktualisiert werden
                    val index = utensilsContainer.indexOfChild(newEditText)
                    if (index != -1) {
                        val currentText = s.toString().trim()
                        if (currentText.isNotEmpty()) {
                            // Optional: Equipment-Daten in einer Liste speichern
                        }
                    }
                }
            })

            // Fügt das neue EditText vor dem Add-Button hinzu
            val addButtonIndex = utensilsContainer.indexOfChild(addUtensilButton)
            utensilsContainer.addView(newEditText, addButtonIndex)
        }
    }




    private fun addInstructionToTable(instruction: TemporaryInstruction, index: Int? = null) {
        val tableLayout = binding.instructionsTable

        // Neue Tabellenreihe erstellen
        val newRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        // Beschreibung der Anweisung
        val instructionTextView = TextView(requireContext()).apply {
            text = instruction.description
            setPadding(8, 8, 8, 8)
        }

        // Bearbeiten-Button
        val editButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_edit_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                editInstruction(rowIndex)
            }
        }

        // Löschen-Button
        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_close_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                tableLayout.removeView(newRow)
                if (rowIndex >= 0 && rowIndex < instructionsList.size) {
                    instructionsList.removeAt(rowIndex)
                    instructionsWithEquipments.remove(instruction)
                }
            }
        }

        // Views zur Zeile hinzufügen
        newRow.addView(instructionTextView)
        newRow.addView(editButton)
        newRow.addView(deleteButton)

        if (index != null) {
            tableLayout.removeViewAt(index)
            tableLayout.addView(newRow, index)
        } else {
            tableLayout.addView(newRow)
        }
    }

    // Hinzufügen der Funktion, um eine bestehende Instruction zu bearbeiten
    private fun editInstruction(index: Int) {
        val instructionToEdit = instructionsList[index]
        binding.editTextMultilineInstructions.setText(instructionToEdit.description)

        // Lade zugehöriges Equipment
        val equipments = instructionsWithEquipments[instructionToEdit] ?: emptyList()

        // Entferne alte Equipment-Views
        clearDynamicEquipmentViews()

        // Füge die Equipment-Views vor dem Add-Button hinzu
        val utensilsContainer = binding.instructionEquipmentContainer
        val addUtensilButton = binding.addEquipmentButton
        equipments.forEach {
            val newEditText = EditText(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                setText(it.equipment)
                textSize = 14f
                minWidth = 200
                setBackgroundResource(R.drawable.new_recipe_background)
                setPadding(16, 16, 16, 16)
            }
            // Füge das EditText vor dem Add-Button hinzu
            val addButtonIndex = utensilsContainer.indexOfChild(addUtensilButton)
            utensilsContainer.addView(newEditText, addButtonIndex)
        }

        // Setze den aktuellen Index und aktualisiere den Button-Text
        editingInstructionIndex = index
        binding.addInstructionsToRecipe.text = "Aktualisieren"
    }


    private fun clearDynamicEquipmentViews() {
        val utensilsContainer = binding.instructionEquipmentContainer

        // Schleife durch die Kinder-Views und entferne nur EditTexts
        for (i in utensilsContainer.childCount - 1 downTo 0) {
            val view = utensilsContainer.getChildAt(i)
            if (view is EditText) {
                utensilsContainer.removeView(view)
            }
        }
    }


    // Equipment aus der UI einsammeln
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

    private fun addIngredientToTable(name: String, amount: Int, unit: String) {
        val tableLayout = binding.ingredientsTable

        // Neue Tabellenreihe erstellen
        val newRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        // Menge und Einheit
        val amountTextView = TextView(requireContext()).apply {
            text = "$amount $unit"
            setPadding(8, 8, 8, 8)
        }

        // Name der Zutat
        val nameTextView = TextView(requireContext()).apply {
            text = name
            setPadding(8, 8, 8, 8)
        }

        // Bearbeiten-Icon
        val editButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_edit_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                editIngredient(rowIndex)
            }
        }

        // Löschen-Icon
        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_close_black)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                val rowIndex = tableLayout.indexOfChild(newRow)
                tableLayout.removeView(newRow)
                if (rowIndex >= 0 && rowIndex < ingredientsList.size) {
                    ingredientsList.removeAt(rowIndex)
                }
            }
        }

        // Füge alle Views zur Zeile hinzu
        newRow.addView(amountTextView)
        newRow.addView(nameTextView)
        newRow.addView(editButton)
        newRow.addView(deleteButton)

        // Füge die Zeile zur Tabelle hinzu
        tableLayout.addView(newRow)
    }

    private fun updateIngredientInTable(index: Int, name: String, amount: Int, unit: String) {
        val tableLayout = binding.ingredientsTable
        val row = tableLayout.getChildAt(index) as TableRow

        // Update the TextViews in the row
        val amountTextView = row.getChildAt(0) as TextView
        val nameTextView = row.getChildAt(1) as TextView

        amountTextView.text = "$amount $unit"
        nameTextView.text = name
    }

    private fun editIngredient(index: Int) {
        // Get the ingredient to edit
        val (tempFood, ingredientToEdit) = ingredientsList[index]

        // Populate input fields with existing ingredient data
        binding.ingredientNameText.setText(tempFood.name)
        binding.ingredientAmountText.setText(ingredientToEdit.amount?.toInt().toString())
        binding.ingredientDescTextMultiline.setText(ingredientToEdit.description)
        binding.ingredientPriceText.setText(ingredientToEdit.price.toString())
        binding.switchOptional.isChecked = ingredientToEdit.isOptional ?: false

        // Set the category spinner to the current food's category
        val categoryIndex = dropdownOptionsCategory.indexOf(tempFood.category?.name)
        if (categoryIndex != -1) {
            binding.categoryChooser.setSelection(categoryIndex)
        }

        // Set the state spinner to the current food's state
        val stateIndex = dropdownOptionsState.indexOf(tempFood.state?.name)
        if (stateIndex != -1) {
            binding.stateChooser.setSelection(stateIndex)
        }

        // Set the unit spinner to the current ingredient's unit
        val unitIndex = dropdownOptionsUnit.indexOf(ingredientToEdit.unit?.name)
        if (unitIndex != -1) {
            binding.unitChooser.setSelection(unitIndex)
        }

        // Set the editing index and change button text
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



    private suspend fun saveRecipeToDatabase() {
        val recipe = tempRecipe
        val ingredients = ingredientsList.map { (food, ingredient) ->
            TemporaryIngredient(
                description = food.name,
                amount = ingredient.amount,
                unit = ingredient.unit,
                price = ingredient.price,
                isOptional = ingredient.isOptional ?: false
            )
        }
        val instructions = instructionsList
        val equipment = instructionsWithEquipments.values.flatten()
        val food = ingredientsList.map { it.first }

        Log.d("CreateRecipeFragment", "Speichere Rezept mit Details: $recipe")
        Log.d("CreateRecipeFragment", "Speichere Zutaten: $ingredients")
        Log.d("CreateRecipeFragment", "Speichere Anweisungen: $instructions")
        Log.d("CreateRecipeFragment", "Speichere Equipment: $equipment")
        Log.d("CreateRecipeFragment", "Speichere Food: $food")

        try {
            val success = recipeController.addRecipeWithDetails(recipe, ingredients, instructions, equipment, food)
            if (success) {
                Toast.makeText(requireContext(), "Rezept erfolgreich gespeichert!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Fehler beim Speichern des Rezepts.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CreateRecipeFragment", "Fehler beim Speichern des Rezepts: ${e.localizedMessage}")
            Toast.makeText(requireContext(), "Fehler beim Speichern des Rezepts.", Toast.LENGTH_SHORT).show()
        }
    }

    // Image Selection & Permission Handling
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
    // Activity Result Launchers
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadImage(it)
        } ?: showToast("Kein Bild ausgewählt")
    }
    private val instructionImageFilePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadInstructionImage(it)
        } ?: showToast("Kein Bild ausgewählt")
    }
    // Image Upload Handlers
    private fun uploadImage(uri: Uri) {
        imageUploadController.uploadImage(
            uri,
            context = requireContext(),
            uploadPath = "recipe_images/",
            onSuccess = { imageUrl ->
                uploadedImageLinks.add(imageUrl)
                displayImageInCardView(imageUrl)
                tempRecipe.imageUrl = imageUrl
                showToast("Bild erfolgreich hochgeladen")
            },
            onError = { errorMessage ->
                showToast(errorMessage)
            }
        )
    }
    private fun uploadInstructionImage(uri: Uri) {
        imageUploadController.uploadInstructionImage(
            uri = uri,
            context = requireContext(),
            onSuccess = { imageUrl ->
                displayInstructionImage(imageUrl)
                tempInstructions.imageUrl = imageUrl
                showToast("Bild erfolgreich hochgeladen")
            },
            onError = { errorMessage ->
                showToast(errorMessage)
            }
        )
    }
    // UI Updates for Displaying Images
    private fun displayImageInCardView(imageUrl: String) {
        val cardView = binding.recipeImageContainer
        val placeholderIcon = binding.recipeImagePlaceholderIc
        val placeholderText = binding.recipeImagePlaceholderTxt

        // Hide placeholders
        placeholderIcon.visibility = View.GONE
        placeholderText.visibility = View.GONE

        // Display the image
        cardView.visibility = View.VISIBLE
        loadImageIntoView(imageUrl, cardView)
    }
    private fun displayInstructionImage(imageUrl: String) {
        val cardView = binding.instructionsImageContainer
        val placeholderIcon = binding.instructionsImagePlaceholderIc

        // Hide placeholder
        placeholderIcon.visibility = View.GONE

        // Display the image
        cardView.visibility = View.VISIBLE
        loadImageIntoView(imageUrl, cardView)
    }
    // Common Image Loading Logic
    private fun loadImageIntoView(imageUrl: String, container: ViewGroup) {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeolder_receipt)
            .error(R.drawable.bg_darkgreen)
            .into(imageView)

        container.removeAllViews()
        container.addView(imageView)
    }
    // Utility Method
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
