package com.example.projektmbun.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.databinding.RoutineBinding
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import com.example.projektmbun.models.data_structure.routine.Routine
import com.example.projektmbun.views.dialogs.DateIntervalPickerDialog
import com.example.projektmbun.views.fragments.RoutinesFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoutineAdapter(
    private var routinesSet: MutableList<Routine> = mutableListOf(),
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val routineController: RoutineController,
    private val foodCardController: FoodCardController
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {

    // ViewHolder-Klasse
    inner class ViewHolder(private val binding: RoutineBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            routine: Routine,
            foodCards: List<FoodCardWithDetails>,
            onAddFoodCard: (View) -> Unit,
            onDeleteFoodCard: (FoodCardWithDetails, RoutineBinding) -> Unit,
            onDeleteRoutine: () -> Unit,
            onRoutineUpdated: (Routine) -> Unit
        ) {
            setupRoutineDetails(routine)
            setupFoodCards(foodCards, onDeleteFoodCard)
            setupButtons(routine, onAddFoodCard, onDeleteRoutine, onRoutineUpdated)
        }

        private fun setupRoutineDetails(routine: Routine) {
            binding.routineName.setText(routine.name)
            binding.btnSelectRhythm.text = routine.rhythm
            binding.switchActivity.apply {
                setOnCheckedChangeListener(null) // Entferne alte Listener
                isChecked = routine.isActive // Setze den aktuellen Status
                setOnCheckedChangeListener { _, isChecked ->
                    coroutineScope.launch {
                        routineController.updateRoutineIsActive(routine.id!!, isChecked)
                    }
                }
            }
        }

        private fun setupFoodCards(foodCards: List<FoodCardWithDetails>, onDeleteFoodCard: (FoodCardWithDetails, RoutineBinding) -> Unit) {
            binding.foodCardContainer.removeAllViews()
            for (foodCard in foodCards) {
                val foodCardView = LayoutInflater.from(binding.foodCardContainer.context)
                    .inflate(R.layout.routine_food_item, binding.foodCardContainer, false)

                foodCardView.tag = foodCard.foodCard.id
                foodCardView.findViewById<TextView>(R.id.routine_food_title).text = foodCard.foodCard.foodId

                foodCardView.findViewById<View>(R.id.btn_delete_food).setOnClickListener {
                    onDeleteFoodCard(foodCard, binding)
                }

                binding.foodCardContainer.addView(foodCardView)
            }
        }

        private fun setupButtons(
            routine: Routine,
            onAddFoodCard: (View) -> Unit,
            onDeleteRoutine: () -> Unit,
            onRoutineUpdated: (Routine) -> Unit
        ) {
            binding.addFoodButton.setOnClickListener(onAddFoodCard)

            binding.btnSelectRhythm.setOnClickListener {
                val dialog = DateIntervalPickerDialog(context) { _, interval ->
                    val updatedRoutine = routine.copy(rhythm = interval)
                    coroutineScope.launch {
                        routineController.addOrUpdateRoutine(updatedRoutine)
                        withContext(Dispatchers.Main) {
                            binding.btnSelectRhythm.text = interval
                        }
                    }
                }
                dialog.show()
            }

            binding.btnDeleteRoutine.setOnClickListener { onDeleteRoutine() }
        }
    }

    // ViewHolder erstellen
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RoutineBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    // Daten an den ViewHolder binden
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = routinesSet[position]

        coroutineScope.launch {
            val foodCards = routine.id?.let { foodCardController.getFoodCardsByRoutineId(it) } ?: emptyList()

            holder.bind(
                routine = routine,
                foodCards = foodCards,
                onAddFoodCard = { view ->
                    routine.id?.let {
                        val action = RoutinesFragmentDirections.actionFragmentRoutineToFragmentStock(
                            routineId = it,
                            showFinishButton = true
                        )
                        view.findNavController().navigate(action)
                    } ?: Log.e("RoutineAdapter", "Routine ID is null.")
                },
                onDeleteFoodCard = { foodCard, binding ->
                    coroutineScope.launch {
                        routine.id?.let {
                            routineController.removeFoodCardFromRoutine(foodCard)
                            withContext(Dispatchers.Main) {
                                val foodCardView = binding.foodCardContainer.findViewWithTag<View>(foodCard.foodCard.id)
                                foodCardView?.let { binding.foodCardContainer.removeView(it) }
                            }
                        } ?: Log.e("RoutineAdapter", "Routine ID is null.")
                    }
                },
                onDeleteRoutine = {
                    coroutineScope.launch {
                        routine.id?.let {
                            routineController.deleteRoutine(it)
                            updateData(routinesSet.filter { it.id != routine.id })
                        } ?: Log.e("RoutineAdapter", "Routine ID is null.")
                    }
                },
                onRoutineUpdated = { updatedRoutine ->
                    coroutineScope.launch {
                        routineController.addOrUpdateRoutine(updatedRoutine)
                        notifyItemChanged(position)
                    }
                }
            )
        }
    }

    // Daten aktualisieren
    fun updateData(newRoutinesSet: List<Routine>) {
        routinesSet = newRoutinesSet.toMutableList()
        notifyDataSetChanged()
    }

    // Anzahl der Elemente
    override fun getItemCount(): Int = routinesSet.size
}
