package ch.js.rm2025.repository

import ch.js.rm2025.data.tables.ExerciseTable
import ch.js.rm2025.model.Exercise
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ExerciseRepository {
    fun getAll(): List<Exercise> = transaction {
        ExerciseTable.selectAll().map { row ->
            Exercise(
                id = row[ExerciseTable.id],
                name = row[ExerciseTable.name],
                description = row[ExerciseTable.description],
                type = row[ExerciseTable.type]
            )
        }
    }

    fun insert(exercise: Exercise): Int = transaction {
        ExerciseTable.insert {
            it[name] = exercise.name
            it[description] = exercise.description
            it[type] = exercise.type
        } get ExerciseTable.id
    }

    fun update(exercise: Exercise) = transaction {
        // Using selectAll().filter to locate rows for update
        ExerciseTable.selectAll().filter { it[ExerciseTable.id] == exercise.id }
            .forEach { row ->
                ExerciseTable.update({ ExerciseTable.id eq row[ExerciseTable.id] }) {
                    it[name] = exercise.name
                    it[description] = exercise.description
                    it[type] = exercise.type
                }
            }
    }

    fun delete(id: Int) = transaction {
        // Using selectAll().filter to locate rows to delete
        ExerciseTable.selectAll().filter { it[ExerciseTable.id] == id }
            .forEach { row ->
                ExerciseTable.deleteWhere { ExerciseTable.id eq row[ExerciseTable.id] }
            }
    }

    // Insert default exercises directly when the app starts.
    fun insertDefaultExercises() = transaction {
        if (getAll().isEmpty()) {
            listOf(
                Exercise(0, "Barbell Squat", "Place the barbell on your upper back, squat down by bending your knees and hips, then return to standing.", "barbell"),
                Exercise(0, "Dumbbell Shoulder Press", "Hold a dumbbell in each hand at shoulder height and press them overhead.", "dumbbell"),
                Exercise(0, "Cable Tricep Pushdown", "Grip the cable attachment and push down until your arms are fully extended. 💪", "cable"),
                Exercise(0, "Lég Press Machine", "Sit on the machine and push the platform away using your legs.", "machine"),
                Exercise(0, "Barbell Deadlift", "Lift the barbell from the ground to a standing position using your legs and back.", "barbell"),
                Exercise(0, "Dumbbell Bizeps Cürl", "Hold a dumbbell in each hand and curl them towards your shoulders.", "dumbbell"),
                Exercise(0, "Cable Lateral Raise", "Use a cable machine to lift your arms out to the sides.", "cable"),
                Exercise(0, "Chest Press Machine", "Push the handles of the machine forward to work your chest. 🏋️‍♂️", "machine"),
                Exercise(0, "Barbell Bent-Over Rów", "Bend at your waist and pull the barbell towards your torso.", "barbell"),
                Exercise(0, "Dumbbell Lunges", "Hold a dumbbell in each hand and step forward into a lunge.", "dumbbell"),
                Exercise(0, "Cable Face Pulls", "Pull the cable attachment towards your face to engage rear delts. 🤜", "cable"),
                Exercise(0, "Lat Pulldown Machine", "Pull the bar down towards your chest to work your back.", "machine"),
                Exercise(0, "Barbell Overhead Press", "Press a barbell overhead while standing.", "barbell"),
                Exercise(0, "Dumbbell Deadlift", "Lift dumbbells from the floor to a standing position.", "dumbbell"),
                Exercise(0, "Cable Séated Row", "Pull the cable attachment towards your torso while seated.", "cable"),
                Exercise(0, "Lëg Curl Machine", "Curl the weight by flexing your knees.", "machine"),
                Exercise(0, "Barbell Hip Thrust", "Place your shoulders on a bench and thrust your hips up with a barbell.", "barbell"),
                Exercise(0, "Dumbbell Step-Ups", "Step onto a bench while holding dumbbells.", "dumbbell"),
                Exercise(0, "Cable Chest Fly", "Pull the cable handles together in front of your chest.", "cable"),
                Exercise(0, "Hack Squat Machine", "Squat using a machine-guided movement.", "machine")
            ).forEach { ex ->
                insert(ex)
            }
        }
    }
}