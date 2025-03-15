package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table

object TemplateTable : Table("template") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}