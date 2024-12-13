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
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.S3Uploader
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.FoodStateEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.coroutines.launch


data class TemporaryRecipe(
    var title: String = "",
    var dishType: String = "",
    var imageUrl: String = "",
    var shortDescription: String = "",
    var servings: Int = 1,
    var readyInMinutes: Int = 0,
    var cookingMinutes: Int = 0,
    var preparationMinutes: Int = 0,
    var sourceUrl: String = "",
    var difficulty: DifficultyEnum? = null,
    var pricePerServing: Double? = null,
    var dairyFree: Boolean = false,
    var glutenFree: Boolean = false,
    var nutFree: Boolean = false,
    var vegan: Boolean = false,
    var vegetarian: Boolean = false,
    var pescetarian: Boolean = false,
    var popularityScore: Double = 0.0
)

data class TemporaryIngredient(
    var description: String? = "",
    var amount: Double? = 0.0,
    var unit: UnitsEnum? = null,
    var price: Double = 0.0,
    var isOptional: Boolean = false
)

data class TemporaryFood(
    var name: String = "",
    var category: FoodCategoryEnum? = null,
    var state: FoodStateEnum? = null
)

data class TemporaryInstruction(
    val step: Int = 0,
    var description: String = "",
    var imageUrl: String? = "",
)

data class TemporaryEquipment(
    val equipment: String = ""
)


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

    private val dropdownOptionsType = listOf("Frühstück", "Hauptspeise", "Abendbrot", "Nachtisch", "Snack", "Beilage", "Dip")
    private val dropdownOptionsCategory = FoodCategoryEnum.entries.map { it.name }
    private val dropdownOptionsState = FoodStateEnum.entries.map { it.name }
    private val dropdownOptionsUnit = UnitsEnum.entries.map { it.name }


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


        setupUIListeners()

        binding.instructionsImageUploader.setOnClickListener {
            selectInstructionImage()
        }

        binding.recipeImageUploader.setOnClickListener {
            checkPermissionAndSelectImage()
        }

        setupDropdownMenuType()
        setupPrepTimeSeekBar()
        setupCookTimeSeekBar()
        changePortions()
        setupSchwierigkeitButtonSelection()

        setupDropdownMenuCategory()
        setupDropdownMenuState()
        setupDropdownMenuUnit()
        setupEquipmentButton()

        binding.submitRecipe.setOnClickListener {

            val recipeTitle = binding.recipeTitle.text.toString()
            val recipePortions = binding.portionText.text.toString()
            val dishType = binding.typeChoose.selectedItem.toString()
            val shortDesc = binding.editRecipeDescMultiline.text.toString()
            val prepTime = binding.preptimeSeekbar.progress //Look for different units
            val cookTime = binding.cooktimeSeekbar.progress //Look for different units
            val readyTime = prepTime+ cookTime
            val recipeSrc = binding.recipeSrcText.text.toString()
            val isDairyFree = binding.checkboxLaktosefrei.isSelected
            val isGlutenFree = binding.checkboxGlutenfrei.isSelected
            val isNutFree = binding.checkboxNussfrei.isSelected
            val isVegan = binding.checkboxVegan.isSelected
            val isVegetarian = binding.checkboxVegetarisch.isSelected
            val isPescetarian = binding.checkboxPesketarisch.isSelected

            if (recipeTitle.isNotBlank()
                && shortDesc.isNotBlank()
                && prepTime != 0
                && recipeSrc.isNotBlank()
            ) { //TODO: check if all fields are filled
                tempRecipe.title = recipeTitle
                tempRecipe.servings = recipePortions.toInt()
                tempRecipe.preparationMinutes = prepTime
                tempRecipe.dishType = dishType
                tempRecipe.cookingMinutes = cookTime
                tempRecipe.readyInMinutes = readyTime
                tempRecipe.shortDescription = shortDesc
                tempRecipe.servings = recipePortions.toInt()
                tempRecipe.sourceUrl = recipeSrc
                tempRecipe.dairyFree = isDairyFree
                tempRecipe.glutenFree = isGlutenFree
                tempRecipe.nutFree = isNutFree
                tempRecipe.vegan = isVegan
                tempRecipe.vegetarian = isVegetarian
                tempRecipe.pescetarian = isPescetarian


                viewLifecycleOwner.lifecycleScope.launch {
                    saveRecipeToDatabase()
                }
            } else {
                Toast.makeText(requireContext(), "Bitte alle Pflichtfelder ausfüllen in Rezeptinfos.", Toast.LENGTH_SHORT).show()
            }
        }



        return binding.root
    }

    private fun setupUIListeners() {
        // listener for ingredients
        binding.addIngredientsToRecipe.setOnClickListener {
            val ingredientName = binding.ingredientNameText.text.toString()
            val ingredientCategory = binding.categoryChooser.selectedItem.toString()
            val ingredientState = binding.stateChooser.selectedItem.toString()
            val ingredientShortDesc = binding.ingredientDescTextMultiline.text.toString()
            val ingredientAmount = binding.ingredientAmountText.text.toString()
            val ingredientUnit = binding.unitChooser.selectedItem.toString()
            val ingredientPrice = binding.ingredientPriceText.text.toString()
            val isIngredientOptional = binding.switchOptional.isChecked

            // Validiere die Eingaben
            if (ingredientName.isBlank() || ingredientAmount.isBlank() || ingredientPrice.isBlank()) {
                Toast.makeText(requireContext(), "Bitte alle Pflichtfelder ausfüllen.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val foodCategory = Converters.toCategoryEnum(ingredientCategory)
            val foodState = Converters.toStateEnum(ingredientState)

            val food = foodCategory?.let { category ->
                foodState?.let { state ->
                    TemporaryFood(ingredientName, category, state)
                }
            }

            if (food != null) {
                tempIngredient.description = ingredientShortDesc
                tempIngredient.amount = ingredientAmount.toDouble()
                tempIngredient.unit = Converters.toUnitsEnum(ingredientUnit)
                tempIngredient.price = ingredientPrice.toDouble()
                tempIngredient.isOptional = isIngredientOptional

                // Füge das Ingredient zur Tabelle hinzu
                addIngredientToTable(ingredientName, ingredientAmount, ingredientUnit)
            }

            Log.d("CreateRecipeFragment", "Zutaten: $tempIngredient")
        }

        // listener for instructions
        binding.addInstructionsToRecipe.setOnClickListener {
            val instructionDesc = binding.editTextMultilineInstructions.text.toString()

            if(true) {
                tempInstructions.description = instructionDesc
            }
        }

    }

    private fun checkPermissionAndSelectImage() {
        val permission = imageUploadController.checkPermission()

        if (imageUploadController.isPermissionGranted(permission)) {
            selectImage()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun selectImage() {
        filePickerLauncher.launch("image/*")
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                selectImage()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Berechtigung erforderlich, um fortzufahren!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val instructionImageFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUploadController.uploadInstructionImage(
                    uri = it,
                    context = requireContext(),
                    onSuccess = { imageUrl ->
                        displayInstructionImage(imageUrl)
                        tempInstructions.imageUrl = imageUrl
                        Toast.makeText(requireContext(), "Bild erfolgreich hochgeladen", Toast.LENGTH_SHORT).show()
                    },
                    onError = { errorMessage ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            } ?: run {
                Toast.makeText(requireContext(), "Kein Bild ausgewählt", Toast.LENGTH_SHORT).show()
            }
        }

    private fun selectInstructionImage() {
        instructionImageFilePickerLauncher.launch("image/*")
    }

    private fun displayInstructionImage(imageUrl: String) {
        val cardView = binding.instructionsImageContainer
        val placeholderIcon = binding.instructionsImagePlaceholderIc

        // Placeholder ausblenden
        placeholderIcon.visibility = View.GONE

        // Sichtbarkeit der CardView aktivieren
        cardView.visibility = View.VISIBLE

        // Bild anzeigen
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

        cardView.removeAllViews()
        cardView.addView(imageView)
    }


    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImage(it)
            } ?: run {
                Toast.makeText(requireContext(), "Kein Bild ausgewählt", Toast.LENGTH_SHORT).show()
            }
        }

    private fun uploadImage(uri: Uri) {
        imageUploadController.uploadImage(
            uri,
            context = requireContext(),
            uploadPath = "recipe_images/", // Speicherpfad angeben
            onSuccess = { imageUrl ->
                uploadedImageLinks.add(imageUrl)
                displayImageInCardView(imageUrl)
                tempRecipe.imageUrl = imageUrl
                Toast.makeText(requireContext(), "Bild erfolgreich hochgeladen", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun displayImageInCardView(imageUrl: String) {
        val cardView = binding.recipeImageContainer
        val placeholderIcon = binding.recipeImagePlaceholderIc
        val placeholderText = binding.recipeImagePlaceholderTxt

        // Hide placeholders
        placeholderIcon.visibility = View.GONE
        placeholderText.visibility = View.GONE

        // Make the CardView visible
        cardView.visibility = View.VISIBLE

        // Load the image into the CardView
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

        cardView.removeAllViews()
        cardView.addView(imageView)
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
        val utensilsContainer = binding.utensilsContainer // FlexboxLayout
        val addUtensilButton = binding.addUtensilButton

        addUtensilButton.setOnClickListener {
            // Neues EditText-Feld für Utensil erstellen
            val newEditText = EditText(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = 5
                    setMargins(0, 0, margin, 0)
                }
                id = View.generateViewId()
                hint = "Utensil"
                textSize = 14f
                minWidth = 200 // Mindestbreite
                maxWidth = 400 // Maximalbreite
                setBackgroundResource(R.drawable.new_recipe_background)
                setPadding(16, 16, 16, 16)
            }

            // TextWatcher für automatische Updates hinzufügen
            newEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Keine Aktion erforderlich
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val index = utensilsContainer.indexOfChild(newEditText)
                    if (index != -1) {
                        val currentText = s.toString().trim()
                        if (currentText.isNotEmpty()) {
                            // Sicherstellen, dass Equipment-Daten nur gültige Einträge enthalten
                            val equipmentList = collectEquipmentFromUI().toMutableList()
                            if (index < equipmentList.size) {
                                equipmentList[index] = TemporaryEquipment(currentText)
                            } else {
                                equipmentList.add(TemporaryEquipment(currentText))
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Keine Aktion erforderlich
                }
            })

            // Add Button entfernen, neues EditText hinzufügen und Add Button wieder einfügen
            utensilsContainer.removeView(addUtensilButton)
            utensilsContainer.addView(newEditText)
            utensilsContainer.addView(addUtensilButton)
        }
    }



    private fun collectEquipmentFromUI(): List<TemporaryEquipment> {
        val equipmentList = mutableListOf<TemporaryEquipment>()
        val utensilsContainer = binding.utensilsContainer

        for (i in 0 until utensilsContainer.childCount) {
            val childView = utensilsContainer.getChildAt(i)
            if (childView is EditText) {
                val equipmentName = childView.text.toString().trim()
                if (equipmentName.isNotEmpty()) {
                    equipmentList.add(TemporaryEquipment(equipment = equipmentName))
                }
            }
        }
        return equipmentList
    }

    private fun addIngredientToTable(amount: String, unit: String, name: String) {
        val tableLayout = binding.ingredientsTable

        // Neue Tabellenreihe erstellen
        val newRow = TableRow(requireContext())
        newRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        newRow.setPadding(8, 8, 8, 8)

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
            setImageResource(android.R.drawable.ic_menu_edit)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                Toast.makeText(requireContext(), "$name bearbeiten", Toast.LENGTH_SHORT).show()
            }
        }

        // Löschen-Icon
        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener {
                tableLayout.removeView(newRow) // Entferne die Zeile
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



    private suspend fun saveRecipeToDatabase() {
        val recipe = tempRecipe
        val ingredients = listOf(tempIngredient)
        val instructions = listOf(tempInstructions)
        val equipment = collectEquipmentFromUI()
        val food = TemporaryFood(
            name = tempIngredient.description ?: "",
            category = FoodCategoryEnum.UNBEKANNT,
            state = FoodStateEnum.UNBEKANNT
        )

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






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
