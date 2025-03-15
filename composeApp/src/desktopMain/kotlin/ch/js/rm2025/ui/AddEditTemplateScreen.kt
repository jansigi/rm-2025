package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.model.Exercise
import ch.js.rm2025.model.Template
import ch.js.rm2025.model.TemplateExercise
import ch.js.rm2025.model.TemplateSet
import ch.js.rm2025.repository.ExerciseRepository
import ch.js.rm2025.repository.TemplateRepository
import ch.js.rm2025.ui.component.ConfirmationDialog

/**
 * Data holder for an exercise entry in the template.
 * - `exercise`: Must be selected from available exercises.
 * - `sets`: A list where each value is the number of reps for that set.
 */
data class TemplateExerciseEntry(
    var exercise: Exercise? = null,
    var sets: MutableList<Int> = mutableStateListOf(0)
)

/**
 * Screen for adding or editing a template.
 *
 * Requirements:
 * - Title: "Add template" when new, or "Edit template '[template name]'" when editing.
 * - Name is mandatory and unique.
 * - The template must have at least one exercise when saving.
 * - Each exercise must have at least one set with rep > 0.
 * - Exercises within the template must be unique.
 * - The order of exercises is preserved.
 * - User can delete any exercise from the UI (even if it makes the list empty).
 * - On save, if no exercises remain, an error is displayed.
 * - Cancel discards changes.
 */
class AddEditTemplateScreen(val template: Template?) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Template name (pre-filled if editing)
        var name by remember { mutableStateOf(template?.name ?: "") }

        // Track the list of exercises for the template.
        var entries by remember { mutableStateOf(mutableStateListOf<TemplateExerciseEntry>()) }
        // Load existing template data if editing.
        if (template != null && entries.isEmpty()) {
            entries.addAll(
                template.exercises.map { te ->
                    TemplateExerciseEntry(
                        exercise = te.exercise,
                        sets = te.sets.map { it.reps }.toMutableStateList()
                    )
                }
            )
        }

        // State for unsaved changes and for showing the cancel confirmation.
        var unsavedChanges by remember { mutableStateOf(false) }
        var showCancelConfirmation by remember { mutableStateOf(false) }

        // Error messages
        var nameError by remember { mutableStateOf<String?>(null) }
        var exerciseError by remember { mutableStateOf<String?>(null) }
        var generalError by remember { mutableStateOf<String?>(null) }

        // Retrieve available exercises for selection.
        val allExercises = ExerciseRepository.getAll()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (template == null)
                            Text("Add template")
                        else
                            Text("Edit template \"${template.name}\"")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (unsavedChanges)
                                showCancelConfirmation = true
                            else
                                navigator.pop()
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
                Text(
                    if (template == null) "Add a new template" else "Edit this template",
                    style = MaterialTheme.typography.h6
                )
                Spacer(Modifier.height(8.dp))

                // Display error messages if any.
                if (nameError != null) {
                    Text(nameError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }
                if (exerciseError != null) {
                    Text(exerciseError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }
                if (generalError != null) {
                    Text(generalError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }

                // Template name input.
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        unsavedChanges = true
                        nameError = null
                        generalError = null
                    },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // List of exercises.
                Text("Exercises:", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(4.dp))
                LazyColumn {
                    itemsIndexed(entries) { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dropdown for selecting an exercise.
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { dropdownExpanded = true }) {
                                    Text(entry.exercise?.name ?: "Select Exercise")
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    allExercises.forEach { ex ->
                                        DropdownMenuItem(onClick = {
                                            entry.exercise = ex
                                            unsavedChanges = true
                                            exerciseError = null
                                            dropdownExpanded = false
                                        }) {
                                            Text(ex.name)
                                        }
                                    }
                                }
                            }

                            // Column for editing sets.
                            Column {
                                entry.sets.forEachIndexed { setIndex, reps ->
                                    OutlinedTextField(
                                        value = if (reps == 0) "" else reps.toString(),
                                        onValueChange = { newVal ->
                                            val intVal = newVal.toIntOrNull() ?: 0
                                            entry.sets[setIndex] = intVal
                                            unsavedChanges = true
                                        },
                                        label = { Text("Set ${setIndex + 1} reps") },
                                        modifier = Modifier.width(100.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                                Button(onClick = {
                                    entry.sets.add(0)
                                    unsavedChanges = true
                                }) {
                                    Text("Add Set")
                                }
                            }

                            // Delete button (always enabled, even if this is the only exercise).
                            IconButton(onClick = {
                                entries.removeAt(index)
                                unsavedChanges = true
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Exercise")
                            }
                        }
                        Divider()
                    }
                }

                // Button to add a new exercise entry.
                Button(onClick = {
                    entries.add(TemplateExerciseEntry())
                    unsavedChanges = true
                }) {
                    Text("Add Exercise")
                }

                Spacer(Modifier.height(16.dp))

                // Save and Cancel buttons.
                Row {
                    Button(onClick = {
                        // Clear previous errors.
                        nameError = null
                        exerciseError = null
                        generalError = null

                        // Validate template name.
                        if (name.isBlank()) {
                            nameError = "Template name is required!"
                            return@Button
                        }
                        // Check for duplicate template names.
                        val existingTemplate = TemplateRepository.getAll().find { it.name == name }
                        if (existingTemplate != null && existingTemplate.id != template?.id) {
                            nameError = "A template with this name already exists!"
                            return@Button
                        }
                        // Validate that at least one exercise remains.
                        if (entries.isEmpty()) {
                            exerciseError = "A template must have at least one exercise."
                            return@Button
                        }
                        // Validate each exercise entry.
                        for (entry in entries) {
                            if (entry.exercise == null) {
                                exerciseError = "Please select an exercise."
                                return@Button
                            }
                            if (entry.sets.isEmpty()) {
                                exerciseError = "Each exercise must have at least one set."
                                return@Button
                            }
                            if (entry.sets.any { it <= 0 }) {
                                exerciseError = "Each set must have reps > 0."
                                return@Button
                            }
                        }

                        // Build final template structure (preserving order).
                        val templateExercises = entries.mapIndexed { index, entry ->
                            TemplateExercise(
                                id = 0,
                                templateId = 0,
                                exercise = entry.exercise!!,
                                order = index,
                                sets = entry.sets.mapIndexed { sIndex, reps ->
                                    TemplateSet(
                                        id = 0,
                                        templateExerciseId = 0,
                                        setNumber = sIndex + 1,
                                        reps = reps
                                    )
                                }
                            )
                        }

                        // Save or update the template.
                        if (template == null) {
                            TemplateRepository.insert(
                                Template(
                                    id = 0,
                                    name = name,
                                    exercises = templateExercises
                                )
                            )
                        } else {
                            TemplateRepository.update(
                                Template(
                                    id = template.id,
                                    name = name,
                                    exercises = templateExercises
                                )
                            )
                        }
                        unsavedChanges = false
                        navigator.pop()
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