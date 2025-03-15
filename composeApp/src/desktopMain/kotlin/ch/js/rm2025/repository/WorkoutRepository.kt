package ch.js.rm2025.repository

import ch.js.rm2025.data.tables.WorkoutTable
import ch.js.rm2025.data.tables.WorkoutExerciseTable
import ch.js.rm2025.data.tables.WorkoutSetTable
import ch.js.rm2025.model.Workout
import ch.js.rm2025.model.WorkoutExercise
import ch.js.rm2025.model.WorkoutSet
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object WorkoutRepository {
    fun getAll(): List<Workout> = transaction {
        WorkoutTable.selectAll().map { row ->
            val workoutId = row[WorkoutTable.id]
            Workout(
                id = workoutId,
                name = row[WorkoutTable.name],
                start = row[WorkoutTable.start],
                end = row[WorkoutTable.end],
                exercises = getWorkoutExercises(workoutId)
            )
        }
    }
    
    private fun getWorkoutExercises(workoutId: Int): List<WorkoutExercise> {
        return WorkoutExerciseTable.selectAll()
            .filter { it[WorkoutExerciseTable.workoutId] == workoutId }
            .sortedBy { it[WorkoutExerciseTable.order] }
            .map { row ->
                val workoutExerciseId = row[WorkoutExerciseTable.id]
                val exerciseId = row[WorkoutExerciseTable.exerciseId]
                val exercise = ExerciseRepository.getAll().find { it.id == exerciseId } ?: error("Exercise not found")
                WorkoutExercise(
                    id = workoutExerciseId,
                    workoutId = row[WorkoutExerciseTable.workoutId],
                    exercise = exercise,
                    order = row[WorkoutExerciseTable.order],
                    sets = getWorkoutSets(workoutExerciseId)
                )
            }
    }
    
    private fun getWorkoutSets(workoutExerciseId: Int): List<WorkoutSet> {
        return WorkoutSetTable.selectAll()
            .filter { it[WorkoutSetTable.workoutExerciseId] == workoutExerciseId }
            .sortedBy { it[WorkoutSetTable.setNumber] }
            .map { row ->
                WorkoutSet(
                    id = row[WorkoutSetTable.id],
                    workoutExerciseId = row[WorkoutSetTable.workoutExerciseId],
                    setNumber = row[WorkoutSetTable.setNumber],
                    weight = row[WorkoutSetTable.weight],
                    reps = row[WorkoutSetTable.reps]
                )
            }
    }
    
    fun insert(workout: Workout): Int = transaction {
        // Use selectAll() and Kotlin comparison to check for overlapping workouts
        val overlapping = WorkoutTable.selectAll().any { row ->
            row[WorkoutTable.start] <= workout.end && row[WorkoutTable.end] >= workout.start
        }
        if (overlapping) throw Exception("Workout time overlaps with an existing workout")
        
        val workoutId = WorkoutTable.insert {
            it[name] = workout.name
            it[start] = workout.start
            it[end] = workout.end
        } get WorkoutTable.id
        
        workout.exercises.forEach { we ->
            val workoutExerciseId = WorkoutExerciseTable.insert {
                it[WorkoutExerciseTable.workoutId] = workoutId
                it[exerciseId] = we.exercise.id
                it[order] = we.order
            } get WorkoutExerciseTable.id
            
            we.sets.forEach { ws ->
                WorkoutSetTable.insert {
                    it[WorkoutSetTable.workoutExerciseId] = workoutExerciseId
                    it[setNumber] = ws.setNumber
                    it[weight] = ws.weight
                    it[reps] = ws.reps
                }
            }
        }
        workoutId
    }
    
    fun update(workout: Workout) = transaction {
        val overlapping = WorkoutTable.selectAll().any { row ->
            row[WorkoutTable.id] != workout.id &&
            row[WorkoutTable.start] <= workout.end &&
            row[WorkoutTable.end] >= workout.start
        }
        if (overlapping) throw Exception("Workout time overlaps with an existing workout")
        
        WorkoutTable.update({ WorkoutTable.id eq workout.id }) {
            it[name] = workout.name
            it[start] = workout.start
            it[end] = workout.end
        }
        val weIds = WorkoutExerciseTable.selectAll()
            .filter { it[WorkoutExerciseTable.workoutId] == workout.id }
            .map { it[WorkoutExerciseTable.id] }
        WorkoutSetTable.deleteWhere { workoutExerciseId inList weIds }
        WorkoutExerciseTable.deleteWhere { workoutId eq workout.id }
        
        workout.exercises.forEach { we ->
            val workoutExerciseId = WorkoutExerciseTable.insert {
                it[WorkoutExerciseTable.workoutId] = workout.id
                it[exerciseId] = we.exercise.id
                it[order] = we.order
            } get WorkoutExerciseTable.id
            
            we.sets.forEach { ws ->
                WorkoutSetTable.insert {
                    it[WorkoutSetTable.workoutExerciseId] = workoutExerciseId
                    it[setNumber] = ws.setNumber
                    it[weight] = ws.weight
                    it[reps] = ws.reps
                }
            }
        }
    }
    
    fun delete(workoutId: Int) = transaction {
        val weIds = WorkoutExerciseTable.selectAll()
            .filter { it[WorkoutExerciseTable.workoutId] == workoutId }
            .map { it[WorkoutExerciseTable.id] }
        WorkoutSetTable.deleteWhere { workoutExerciseId inList weIds }
        WorkoutExerciseTable.deleteWhere { WorkoutExerciseTable.workoutId eq workoutId }
        WorkoutTable.deleteWhere { id eq workoutId }
    }
}