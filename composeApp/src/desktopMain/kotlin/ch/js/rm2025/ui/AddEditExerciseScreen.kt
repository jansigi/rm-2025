package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.model.Exercise
import ch.js.rm2025.repository.ExerciseRepository
import ch.js.rm2025.ui.component.ConfirmationDialog

class AddEditExerciseScreen(val exercise: Exercise?) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val isEdit = exercise != null

        var name by remember { mutableStateOf(exercise?.name ?: "") }
        var description by remember { mutableStateOf(exercise?.description ?: "") }
        var type by remember { mutableStateOf(exercise?.type ?: "") }

        var unsavedChanges by remember { mutableStateOf(false) }
        var showCancelConfirmation by remember { mutableStateOf(false) }

        // New state to show duplicate-name errors
        var duplicateNameError by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEdit) "Edit Exercise" else "Add Exercise") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (unsavedChanges) {
                                showCancelConfirmation = true
                            } else {
                                navigator.pop()
                            }
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding)
            ) {
                Text(if (isEdit) "Edit this exercise" else "Create a new exercise", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))

                // Show duplicate name error if any
                if (duplicateNameError != null) {
                    Text(duplicateNameError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        unsavedChanges = true
                        duplicateNameError = null // clear error as user types
                    },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        unsavedChanges = true
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = {
                        type = it
                        unsavedChanges = true
                    },
                    label = { Text("Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Row {
                    Button(onClick = {
                        if (name.isNotBlank() && description.isNotBlank() && type.isNotBlank()) {
                            // Check for duplicates
                            val existing = ExerciseRepository.getAll().find { it.name == name }
                            // If existing != null, check if it's not the same item in edit
                            if (existing != null && existing.id != exercise?.id) {
                                duplicateNameError = "An exercise with this name already exists!"
                            } else {
                                // No duplicates => proceed
                                if (isEdit) {
                                    ExerciseRepository.update(Exercise(exercise!!.id, name, description, type))
                                } else {
                                    ExerciseRepository.insert(Exercise(0, name, description, type))
                                }
                                unsavedChanges = false
                                navigator.pop()
                            }
                        }
                    }) {
                        Text("Save")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (unsavedChanges) {
                            showCancelConfirmation = true
                        } else {
                            navigator.pop()
                        }
                    }) {
                        Text("Cancel")
                    }
                }
            }
        }

        if (showCancelConfirmation) {
            ConfirmationDialog(
                title = "Discard Changes?",
                message = "You have unsaved changes. Are you sure you want to cancel?",
                onConfirm = {
                    showCancelConfirmation = false
                    unsavedChanges = false
                    navigator.pop()
                },
                onDismiss = { showCancelConfirmation = false }
            )
        }
    }
}