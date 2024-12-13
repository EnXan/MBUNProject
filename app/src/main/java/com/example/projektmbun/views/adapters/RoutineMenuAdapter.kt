package com.example.projektmbun.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.databinding.ItemRoutineSmallBinding
import com.example.projektmbun.models.data_structure.routine.Routine

class RoutineMenuAdapter(
    private var routinesSet: List<Routine>,
) : RecyclerView.Adapter<RoutineMenuAdapter.ViewHolder>() {


    // ViewHolder-Klasse
    inner class ViewHolder(private val binding: ItemRoutineSmallBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(routine: Routine) {
            // Setze die Routine-Daten
            binding.routineName.text = routine.name
            binding.routineRhythm.text = routine.rhythm

            // Button zum Verknüpfen der Routine
            binding.buttonLinkRoutine.setOnClickListener {
                it.findNavController().navigate(
                    R.id.action_fragment_menu_to_fragment_routine,
                    null,
                    navOptions {
                        popUpTo(R.id.fragment_menu) { inclusive = true }
                    }
                )
            }
        }
    }

    // Erstellt einen neuen ViewHolder
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoutineSmallBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    // Bindet Daten an den ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = routinesSet[position]
        holder.bind(routine)
    }

    // Aktualisiert die Routinen-Daten
    fun updateData(newRoutineSet: List<Routine>) {
        routinesSet = newRoutineSet
        notifyDataSetChanged()
    }

    // Gibt die Anzahl der Routinen zurück
    override fun getItemCount(): Int = routinesSet.size
}
