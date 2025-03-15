package ch.js.rm2025.model

data class WorkoutSet(
    val id: Int,
    val workoutExerciseId: Int,
    val setNumber: Int,
    val weight: Double,
    val reps: Int
)