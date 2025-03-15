package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
                    title = { Text("Templates") },
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
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Table Headings
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Name", style = MaterialTheme.typography.subtitle2, modifier = Modifier.weight(0.6f))
                    Text("Actions", style = MaterialTheme.typography.subtitle2, modifier = Modifier.weight(0.4f))
                }
                Divider()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(templates) { template ->
                        TemplateRow(
                            template,
                            onEdit = { navigator.push(AddEditTemplateScreen(template)) },
                            onDelete = { templateToDelete = template }
                        )
                    }
                }
            }
        }

        if (templateToDelete != null) {
            ConfirmationDialog(
                title = "Delete Template",
                message = "Are you sure you want to delete \"${templateToDelete!!.name}\"?",
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
fun TemplateRow(
    template: Template,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(template.name, modifier = Modifier.weight(0.6f))
        Row(modifier = Modifier.weight(0.4f), horizontalArrangement = Arrangement.End) {
            Button(onClick = onEdit) {
                Text("Edit")
            }
            Spacer(Modifier.width(4.dp))
            Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)) {
                Text("Delete")
            }
        }
    }
    Divider()
}