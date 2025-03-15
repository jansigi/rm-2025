package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table

object WorkoutSetTable : Table("workout_set") {
    val id = integer("id").autoIncrement()
    val workoutExerciseId = integer("workout_exercise_id").references(WorkoutExerciseTable.id)
    val setNumber = integer("set_number")
    val weight = double("weight")
    val reps = integer("reps")
    override val primaryKey = PrimaryKey(id)
}