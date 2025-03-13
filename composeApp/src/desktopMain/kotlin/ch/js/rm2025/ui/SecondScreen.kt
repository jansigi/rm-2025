package ch.js.rm2025.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ch.js.rm2025.model.ExampleModel

class SecondScreen(val item: ExampleModel) : Screen {
    @Composable
    override fun Content() {
        MaterialTheme {
            Text(text = "Selected Item: ID = ${item.id}, Name = ${item.name}")
        }
    }
}
