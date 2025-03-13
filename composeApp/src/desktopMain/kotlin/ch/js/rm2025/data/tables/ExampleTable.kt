package ch.js.rm2025.data.tables

import org.jetbrains.exposed.sql.Table

object ExampleTable : Table("example") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id)
}