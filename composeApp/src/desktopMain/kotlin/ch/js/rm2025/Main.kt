package ch.js.rm2025

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ch.js.rm2025.data.DatabaseFactory
import ch.js.rm2025.repository.ExampleRepository

fun main() {
    DatabaseFactory.init()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "rm-2025",
        ) {
            ExampleRepository.removeAll()
            ExampleRepository.insert("Test")
            App()
        }
    }
}
