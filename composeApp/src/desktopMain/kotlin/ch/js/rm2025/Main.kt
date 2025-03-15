package ch.js.rm2025

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ch.js.rm2025.data.DatabaseFactory
import ch.js.rm2025.ui.MainWindowScreen
import cafe.adriel.voyager.navigator.Navigator

fun main() {
    DatabaseFactory.init()
    application {
        Window(onCloseRequest = ::exitApplication, title = "AthliTrack") {
            Navigator(MainWindowScreen())
        }
    }
}