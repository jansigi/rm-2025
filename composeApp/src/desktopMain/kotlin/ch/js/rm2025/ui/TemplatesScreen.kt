package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.model.Template
import ch.js.rm2025.repository.TemplateRepository
import ch.js.rm2025.ui.component.ConfirmationDialog

class TemplatesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var templates by remember { mutableStateOf(listOf<Template>()) }
        var templateToDelete by remember { mutableStateOf<Template?>(null) }
        LaunchedEffect(Unit) {
            templates = TemplateRepository.getAll()
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AthliTrack - Templates") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(AddEditTemplateScreen(null)) }) {
                    Text("+")
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(templates) { template ->
                    TemplateItem(
                        template,
                        onEdit = { navigator.push(AddEditTemplateScreen(template)) },
                        onDelete = { templateToDelete = template }
                    )
                }
            }
        }

        if (templateToDelete != null) {
            ConfirmationDialog(
                title = "Delete Template",
                message = "Are you sure you want to delete this template?",
                onConfirm = {
                    TemplateRepository.delete(templateToDelete!!.id)
                    templates = TemplateRepository.getAll()
                    templateToDelete = null
                },
                onDismiss = { templateToDelete = null }
            )
        }
    }
}

@Composable
fun TemplateItem(
    template: Template,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(template.name, style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
            Button(onClick = onEdit) { Text("Edit") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onDelete) { Text("Delete") }
        }
    }
}