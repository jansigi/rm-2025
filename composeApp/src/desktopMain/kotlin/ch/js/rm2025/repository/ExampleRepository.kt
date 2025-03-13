package ch.js.rm2025.repository

import ch.js.rm2025.data.tables.ExampleTable
import ch.js.rm2025.model.ExampleModel
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

object ExampleRepository {
    private var useInMemoryStorage = false
    private val inMemoryData = mutableListOf<ExampleModel>()
    private var nextId = 1

    init {
        try {
            transaction {
                ExampleTable.selectAll().limit(1).toList()
            }
        } catch (e: Exception) {
            println("⚠️ Database connection failed: ${e.message}")
            println("⚠️ Using in-memory storage instead!")
            useInMemoryStorage = true
        }
    }

    fun getAll(): List<ExampleModel> {
        return if (useInMemoryStorage) {
            inMemoryData
        } else {
            try {
                transaction {
                    ExampleTable.selectAll().map { row ->
                        ExampleModel(
                            id = row[ExampleTable.id],
                            name = row[ExampleTable.name]
                        )
                    }
                }
            } catch (e: SQLException) {
                println("⚠️ Database error: ${e.message}, switching to in-memory storage!")
                useInMemoryStorage = true
                inMemoryData
            }
        }
    }

    fun insert(name: String): Int {
        return if (useInMemoryStorage) {
            val newItem = ExampleModel(id = nextId++, name = name)
            inMemoryData.add(newItem)
            newItem.id
        } else {
            try {
                transaction {
                    ExampleTable.insert {
                        it[ExampleTable.name] = name
                    } get ExampleTable.id
                }
            } catch (e: SQLException) {
                println("⚠️ Database insert error: ${e.message}, saving to in-memory storage instead!")
                useInMemoryStorage = true
                val newItem = ExampleModel(id = nextId++, name = name)
                inMemoryData.add(newItem)
                newItem.id
            }
        }
    }

    fun removeAll() {
        if (useInMemoryStorage) {
            inMemoryData.clear()
        } else {
            try {
                transaction {
                    ExampleTable.deleteAll()
                }
            } catch (e: SQLException) {
                println("⚠️ Database delete error: ${e.message}, clearing in-memory storage instead!")
                useInMemoryStorage = true
                inMemoryData.clear()
            }
        }
    }
}
