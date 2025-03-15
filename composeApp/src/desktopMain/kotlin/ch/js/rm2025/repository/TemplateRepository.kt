package ch.js.rm2025.repository

import ch.js.rm2025.data.tables.TemplateTable
import ch.js.rm2025.data.tables.TemplateExerciseTable
import ch.js.rm2025.data.tables.TemplateSetTable
import ch.js.rm2025.model.Template
import ch.js.rm2025.model.TemplateExercise
import ch.js.rm2025.model.TemplateSet
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

object TemplateRepository {
    fun getAll(): List<Template> = transaction {
        TemplateTable.selectAll().map { row ->
            val templateId = row[TemplateTable.id]
            Template(
                id = templateId,
                name = row[TemplateTable.name],
                exercises = getTemplateExercises(templateId)
            )
        }
    }
    
    private fun getTemplateExercises(templateId: Int): List<TemplateExercise> {
        return TemplateExerciseTable.selectAll()
            .filter { it[TemplateExerciseTable.templateId] == templateId }
            .sortedBy { it[TemplateExerciseTable.order] }
            .map { row ->
                val templateExerciseId = row[TemplateExerciseTable.id]
                val exerciseId = row[TemplateExerciseTable.exerciseId]
                val exercise = ExerciseRepository.getAll().find { it.id == exerciseId } ?: error("Exercise not found")
                TemplateExercise(
                    id = templateExerciseId,
                    templateId = row[TemplateExerciseTable.templateId],
                    exercise = exercise,
                    order = row[TemplateExerciseTable.order],
                    sets = getTemplateSets(templateExerciseId)
                )
            }
    }
    
    private fun getTemplateSets(templateExerciseId: Int): List<TemplateSet> {
        return TemplateSetTable.selectAll()
            .filter { it[TemplateSetTable.templateExerciseId] == templateExerciseId }
            .sortedBy { it[TemplateSetTable.setNumber] }
            .map { row ->
                TemplateSet(
                    id = row[TemplateSetTable.id],
                    templateExerciseId = row[TemplateSetTable.templateExerciseId],
                    setNumber = row[TemplateSetTable.setNumber],
                    reps = row[TemplateSetTable.reps]
                )
            }
    }
    
    fun insert(template: Template): Int = transaction {
        val templateId = TemplateTable.insert {
            it[name] = template.name
        } get TemplateTable.id
        
        template.exercises.forEach { te ->
            val templateExerciseId = TemplateExerciseTable.insert {
                it[TemplateExerciseTable.templateId] = templateId
                it[exerciseId] = te.exercise.id
                it[order] = te.order
            } get TemplateExerciseTable.id
            
            te.sets.forEach { ts ->
                TemplateSetTable.insert {
                    it[TemplateSetTable.templateExerciseId] = templateExerciseId
                    it[setNumber] = ts.setNumber
                    it[reps] = ts.reps
                }
            }
        }
        templateId
    }
    
    fun update(template: Template) = transaction {
        TemplateTable.update({ TemplateTable.id eq template.id }) {
            it[name] = template.name
        }
        // Delete associated TemplateExercises and their TemplateSets using Kotlin filtering
        val teIds = TemplateExerciseTable.selectAll()
            .filter { it[TemplateExerciseTable.templateId] == template.id }
            .map { it[TemplateExerciseTable.id] }
        TemplateSetTable.deleteWhere { templateExerciseId inList teIds }
        TemplateExerciseTable.deleteWhere { templateId eq template.id }
        
        template.exercises.forEach { te ->
            val templateExerciseId = TemplateExerciseTable.insert {
                it[templateId] = template.id
                it[exerciseId] = te.exercise.id
                it[order] = te.order
            } get TemplateExerciseTable.id
            
            te.sets.forEach { ts ->
                TemplateSetTable.insert {
                    it[TemplateSetTable.templateExerciseId] = templateExerciseId
                    it[setNumber] = ts.setNumber
                    it[reps] = ts.reps
                }
            }
        }
    }
    
    fun delete(templateId: Int) = transaction {
        val teIds = TemplateExerciseTable.selectAll()
            .filter { it[TemplateExerciseTable.templateId] == templateId }
            .map { it[TemplateExerciseTable.id] }
        TemplateSetTable.deleteWhere { templateExerciseId inList teIds }
        TemplateExerciseTable.deleteWhere { TemplateExerciseTable.templateId eq templateId }
        TemplateTable.deleteWhere { id eq templateId }
    }
}