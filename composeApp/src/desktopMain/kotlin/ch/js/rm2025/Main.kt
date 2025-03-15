package ch.js.rm2025

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import ch.js.rm2025.data.DatabaseFactory
import ch.js.rm2025.ui.MainWindowScreen

fun main() {
    DatabaseFactory.init()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AthliTrack",
            resizable = true,
            state = WindowState(size = DpSize(1000.dp, 600.dp))
        ) {
            Navigator(MainWindowScreen())
        }
    }
}