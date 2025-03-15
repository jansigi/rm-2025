package ch.js.rm2025.model

import java.time.LocalDateTime

data class Workout(
    val id: Int,
    val name: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val exercises: List<WorkoutExercise>
)