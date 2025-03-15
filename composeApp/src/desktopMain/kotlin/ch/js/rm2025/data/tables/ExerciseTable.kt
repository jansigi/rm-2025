package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table

object ExerciseTable : Table("exercise") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val description = varchar("description", 500)
    val type = varchar("type", 50)
    override val primaryKey = PrimaryKey(id)
}