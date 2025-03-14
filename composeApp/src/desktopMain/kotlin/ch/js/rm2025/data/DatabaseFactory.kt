package ch.js.rm2025.data

import ch.js.rm2025.data.tables.ExampleTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        try {
            Database.connect(
                url = "jdbc:mysql://localhost:3306/test?createDatabaseIfNotExist=true",
                driver = "com.mysql.cj.jdbc.Driver",
                user = "root",
                password = "1234"
            )
            transaction {
                SchemaUtils.create(ExampleTable)
            }
        } catch (e: Exception) {
            println("Using H2 in-memory database instead of MySQL.")
            Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
            )
            transaction {
                SchemaUtils.create(ExampleTable)
            }
        }
    }
}