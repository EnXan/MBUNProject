package com.example.projektmbun.controller.interfaces

import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import com.example.projektmbun.models.data_structure.routine.Routine

interface IRoutineController {
    suspend fun getAllRoutines(): List<Routine>
    suspend fun getAllActiveRoutines(): List<Routine>
    suspend fun addOrUpdateRoutine(routine: Routine): Long
    suspend fun deleteRoutine(routineId: Int)
    suspend fun executeRoutineIfDueToday(routine: Routine)
    suspend fun checkAndExecuteRoutines()
    suspend fun removeFoodCardFromRoutine(foodCard: FoodCardWithDetails)
    suspend fun addFoodCardToRoutine(foodCard: FoodCard, routineId: Int?): Long
    suspend fun updateRoutineIsActive(id: Int, isActive: Boolean)
} 