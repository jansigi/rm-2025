package ch.js.rm2025

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import ch.js.rm2025.ui.MainScreen

@Composable
fun App() {
    Navigator(MainScreen())
}
