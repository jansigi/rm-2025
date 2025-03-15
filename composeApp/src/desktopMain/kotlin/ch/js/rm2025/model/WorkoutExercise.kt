package ch.js.rm2025.model

data class WorkoutExercise(
    val id: Int,
    val workoutId: Int,
    val exercise: Exercise,
    val order: Int,
    val sets: List<WorkoutSet>
)