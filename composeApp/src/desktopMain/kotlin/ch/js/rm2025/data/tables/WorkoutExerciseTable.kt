package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table

object WorkoutExerciseTable : Table("workout_exercise") {
    val id = integer("id").autoIncrement()
    val workoutId = integer("workout_id").references(WorkoutTable.id)
    val exerciseId = integer("exercise_id").references(ExerciseTable.id)
    val order = integer("order_index")
    override val primaryKey = PrimaryKey(id)
}