package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object WorkoutTable : Table("workout") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val start = datetime("start")
    val end = datetime("end")
    override val primaryKey = PrimaryKey(id)
}