package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table

object TemplateSetTable : Table("template_set") {
    val id = integer("id").autoIncrement()
    val templateExerciseId = integer("template_exercise_id").references(TemplateExerciseTable.id)
    val setNumber = integer("set_number")
    val reps = integer("reps")
    override val primaryKey = PrimaryKey(id)
}