package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.model.Template
import ch.js.rm2025.repository.TemplateRepository

class TemplatesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var templates by remember { mutableStateOf(listOf<Template>()) }
        LaunchedEffect(Unit) {
            templates = TemplateRepository.getAll()
        }
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("AthliTrack - Templates") })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(AddEditTemplateScreen(null)) }) {
                    Text("+")
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(templates) { template ->
                    TemplateItem(template,
                        onEdit = { navigator.push(AddEditTemplateScreen(template)) },
                        onDelete = {
                            TemplateRepository.delete(template.id)
                            templates = TemplateRepository.getAll()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateItem(template: Template, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(template.name, style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
            Button(onClick = onEdit) { Text("Edit") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onDelete) { Text("Delete") }
        }
    }
}