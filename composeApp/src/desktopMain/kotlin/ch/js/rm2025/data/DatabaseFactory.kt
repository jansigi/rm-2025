package ch.js.rm2025.data

import ch.js.rm2025.data.tables.ExampleTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        try {
            Database.connect(
                url = "jdbc:mysql://localhost:3306/test",
                driver = "com.mysql.cj.jdbc.Driver",
                user = "root",
                password = "1234"
            )

            transaction {
                SchemaUtils.create(ExampleTable)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}