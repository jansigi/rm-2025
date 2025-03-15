package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table

object TemplateExerciseTable : Table("template_exercise") {
    val id = integer("id").autoIncrement()
    val templateId = integer("template_id").references(TemplateTable.id)
    val exerciseId = integer("exercise_id").references(ExerciseTable.id)
    val order = integer("order_index")
    override val primaryKey = PrimaryKey(id)
}