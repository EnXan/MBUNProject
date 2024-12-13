package com.example.projektmbun.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.databinding.InstructionItemBinding
import com.example.projektmbun.models.data_structure.recipe.Instructions
import com.example.projektmbun.utils.GlideApp
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class RecipeInstructionsAdapter(
    private var instructions: List<Instructions>
) : RecyclerView.Adapter<RecipeInstructionsAdapter.ViewHolder>() {

    // ViewHolder Klasse
    class ViewHolder(private val binding: InstructionItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(instruction: Instructions) {
            // Setze Schrittanzahl
            binding.instructionStep.text = "Step ${instruction.step}"

            // Beschreibung aufteilen
            val fullText = instruction.description
            val (textHalf, textFull) = splitDescription(fullText)

            binding.instructionDescHalf.text = textHalf
            binding.instructionDescFull.text = textFull

            // Lade das Bild
            GlideApp.with(binding.instructionImage.context)
                .load(instruction.imageUrl)
                .skipMemoryCache(true)
                .transform(RoundedCornersTransformation(20, 0))
                .into(binding.instructionImage)
        }

        // Hilfsfunktion zum Teilen der Beschreibung
        private fun splitDescription(description: String): Pair<String, String> {
            val splitIndex = description.indexOf('.', description.length / 2) + 1
            return if (splitIndex > 0) {
                val textHalf = description.substring(0, splitIndex)
                val textFull = description.substring(splitIndex).trim()
                textHalf to textFull
            } else {
                description to ""
            }
        }
    }

    // Erstellt einen neuen ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InstructionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Bindet Daten an den ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(instructions[position])
    }

    // Gibt die Anzahl der Elemente zurück
    override fun getItemCount(): Int = instructions.size

    // Aktualisiere die Anweisungen
    fun updateInstructions(newInstructions: List<Instructions>) {
        instructions = newInstructions
        notifyDataSetChanged()
    }
}
