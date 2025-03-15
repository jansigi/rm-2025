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
    
    // Import exercises from the JSON file (the file content is passed in)
    fun importExercisesFromJson(jsonContent: String) {
        val json = Json { ignoreUnknownKeys = true }
        val exercises = json.decodeFromString<List<ExerciseJson>>(jsonContent)
        exercises.forEach { ex ->
            val exists = transaction {
                ExerciseTable.selectAll().any { it[ExerciseTable.name] == ex.name }
            }
            if (!exists) {
                transaction {
                    ExerciseTable.insert {
                        it[name] = ex.name
                        it[description] = ex.description
                        it[type] = ex.type
                    }
                }
            }
        }
    }
    
    @Serializable
    data class ExerciseJson(val name: String, val description: String, val type: String)
}