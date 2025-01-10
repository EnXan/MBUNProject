package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.databinding.FragmentRoutinesBinding
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.routine.Routine
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.views.adapters.RoutineAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoutinesFragment : Fragment() {

    private var _binding: FragmentRoutinesBinding? = null
    private val binding get() = _binding!!

    private lateinit var routineController: RoutineController
    private lateinit var routineAdapter: RoutineAdapter
    private lateinit var foodCardController: FoodCardController
    private lateinit var foodService: FoodService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRoutinesBinding.inflate(inflater, container, false)
        val view = binding.root

        val context = requireContext()
        val routineDao = AppDatabase.getDatabase(context).routineDao()
        val foodCardDao = AppDatabase.getDatabase(context).foodCardDao()
        val foodService = FoodService()

        routineController = RoutineController(routineDao, foodCardDao)
        foodCardController = FoodCardController(foodCardDao, foodService)

        // Suchleiste anpassen
        binding.searchBar.searchEditText.hint = "Füge eine Routine hinzu..."
        val addIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_black)
        binding.searchBar.scanIcon.setImageDrawable(addIcon)

        // RecyclerView einrichten
        binding.routineRecyclerView.layoutManager = LinearLayoutManager(context)

        // Routinen laden
        loadRoutines()

        // "+"-Button Listener hinzufügen
        binding.searchBar.scanIcon.setOnClickListener {
            val routineName = binding.searchBar.searchEditText.text.toString().trim()

            if (routineName.isNotEmpty()) {
                createNewRoutine(routineName)
            } else {
                Toast.makeText(context, "Bitte gib einen Namen für die Routine ein", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadRoutines() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val routines = routineController.getAllRoutines()
                withContext(Dispatchers.Main) {
                    if (routines.isNotEmpty()) {
                        binding.routineRecyclerView.visibility = View.VISIBLE
                        binding.routineErrorText.visibility = View.GONE
                        routineAdapter = RoutineAdapter(routines, requireContext(), lifecycleScope, routineController, foodCardController)
                        binding.routineRecyclerView.adapter = routineAdapter
                    } else {
                        binding.routineRecyclerView.visibility = View.GONE
                        binding.routineErrorText.text = "Keine Routinen gefunden"
                        binding.routineErrorText.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.routineRecyclerView.visibility = View.GONE
                    binding.routineErrorText.text = "Ein Fehler ist aufgetreten"
                    binding.routineErrorText.visibility = View.VISIBLE
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createNewRoutine(routineName: String) {
        lifecycleScope.launch {
            try {
                val currentDateAsString: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

                val newRoutine = Routine(
                    name = routineName,
                    rhythm = "Täglich", // Standardwert für Rhythmus
                    startDate = currentDateAsString,
                    isActive = true
                )
                routineController.addOrUpdateRoutine(newRoutine)

                // Routinenliste aktualisieren
                loadRoutines()

                // Suchfeld leeren und Benutzer benachrichtigen
                binding.searchBar.searchEditText.text.clear()
                Toast.makeText(requireContext(), "Routine hinzugefügt: $routineName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Fehler beim Hinzufügen der Routine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
