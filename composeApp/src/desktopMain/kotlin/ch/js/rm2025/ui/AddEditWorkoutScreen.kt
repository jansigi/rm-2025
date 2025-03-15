package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.model.Exercise
import ch.js.rm2025.model.Template
import ch.js.rm2025.model.Workout
import ch.js.rm2025.model.WorkoutExercise
import ch.js.rm2025.model.WorkoutSet
import ch.js.rm2025.repository.ExerciseRepository
import ch.js.rm2025.repository.TemplateRepository
import ch.js.rm2025.repository.WorkoutRepository
import ch.js.rm2025.ui.component.ConfirmationDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class WorkoutExerciseEntry(
    var exercise: Exercise? = null,
    var sets: MutableList<Pair<Double, Int>> = mutableStateListOf()
)

/**
 * Allows creating or editing a Workout, with:
 * - Template selection dialog that lets you pick any existing template or "No Template."
 * - Date/time fields in the format "yyyy-MM-dd HH:mm."
 * - Duplicate name checks (no two workouts can share the same name).
 * - Scrollable list of exercises, pinned bottom bar with "Add Exercise," "Save," and "Cancel."
 */
class AddEditWorkoutScreen(val workout: Workout?) : Screen {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Basic fields
        var name by remember { mutableStateOf(workout?.name ?: "") }
        var startText by remember {
            mutableStateOf(
                workout?.start?.format(dateTimeFormatter)
                    ?: LocalDateTime.now().format(dateTimeFormatter)
            )
        }
        var endText by remember {
            mutableStateOf(
                workout?.end?.format(dateTimeFormatter)
                    ?: LocalDateTime.now().plusHours(1).format(dateTimeFormatter)
            )
        }

        // The list of exercises (scrollable)
        var entries by remember { mutableStateOf(mutableStateListOf<WorkoutExerciseEntry>()) }
        var unsavedChanges by remember { mutableStateOf(false) }
        var showCancelConfirmation by remember { mutableStateOf(false) }

        // Error messages
        var parseErrorMessage by remember { mutableStateOf<String?>(null) }
        var duplicateNameError by remember { mutableStateOf<String?>(null) }

        // If editing, load data once
        if (workout != null && entries.isEmpty()) {
            entries.addAll(
                workout.exercises.map { we ->
                    WorkoutExerciseEntry(
                        exercise = we.exercise,
                        sets = we.sets.map { ws -> Pair(ws.weight, ws.reps) }.toMutableStateList()
                    )
                }
            )
        }

        val exercises = ExerciseRepository.getAll()

        // If this is a new workout (workout == null) and no exercises exist yet,
        // we show a template selection dialog with all available templates or "No Template."
        var showTemplateDialog by remember { mutableStateOf(workout == null && entries.isEmpty()) }
        if (showTemplateDialog) {
            // Grab all templates
            val templates = TemplateRepository.getAll()

            // For picking a template from the list or "No Template"
            var expanded by remember { mutableStateOf(false) }
            var selectedTemplate by remember { mutableStateOf<Template?>(null) }

            AlertDialog(
                onDismissRequest = { showTemplateDialog = false },
                title = { Text("Select Template") },
                text = {
                    Column {
                        if (templates.isEmpty()) {
                            Text("No templates found. You can create one in the 'Templates' section.")
                        } else {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(selectedTemplate?.name ?: "No Template")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                // "No Template" option
                                DropdownMenuItem(onClick = {
                                    selectedTemplate = null
                                    expanded = false
                                }) {
                                    Text("No Template")
                                }
                                // All existing templates
                                templates.forEach { t ->
                                    DropdownMenuItem(onClick = {
                                        selectedTemplate = t
                                        expanded = false
                                    }) {
                                        Text(t.name)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // If user picked a template, load it
                        if (selectedTemplate != null) {
                            entries.clear()
                            selectedTemplate!!.exercises.forEach { te ->
                                entries.add(
                                    WorkoutExerciseEntry(
                                        exercise = te.exercise,
                                        sets = te.sets.map { ts -> Pair(0.0, ts.reps) }.toMutableStateList()
                                    )
                                )
                            }
                            unsavedChanges = true
                        }
                        showTemplateDialog = false
                    }) {
                        Text("Ok")
                    }
                },
                dismissButton = {
                    Button(onClick = { showTemplateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (workout != null) "Edit Workout \"${workout.name}\"" else "Add Workout")
                    },
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
            },
            // The bottomBar holds Add Exercise, Save, and Cancel
            bottomBar = {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Add Exercise
                    Button(
                        onClick = {
                            entries.add(WorkoutExerciseEntry())
                            unsavedChanges = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Exercise")
                    }
                    Spacer(Modifier.height(8.dp))
                    // Save / Cancel
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            // Clear old errors
                            parseErrorMessage = null
                            duplicateNameError = null
                            try {
                                val start = LocalDateTime.parse(startText, dateTimeFormatter)
                                val end = LocalDateTime.parse(endText, dateTimeFormatter)

                                if (name.isNotBlank() && entries.isNotEmpty()) {
                                    // Check for duplicates
                                    val existing = WorkoutRepository.getAll().find { it.name == name }
                                    if (existing != null && existing.id != workout?.id) {
                                        duplicateNameError = "A workout with this name already exists!"
                                    } else {
                                        // No duplicates => proceed
                                        val workoutExercises = entries.mapIndexed { idx, entry ->
                                            WorkoutExercise(
                                                id = 0,
                                                workoutId = 0,
                                                exercise = entry.exercise ?: error("No exercise selected"),
                                                order = idx,
                                                sets = entry.sets.mapIndexed { sIndex, pair ->
                                                    WorkoutSet(
                                                        id = 0,
                                                        workoutExerciseId = 0,
                                                        setNumber = sIndex + 1,
                                                        weight = pair.first,
                                                        reps = pair.second
                                                    )
                                                }
                                            )
                                        }
                                        if (workout != null) {
                                            // Update existing
                                            WorkoutRepository.update(
                                                Workout(
                                                    id = workout.id,
                                                    name = name,
                                                    start = start,
                                                    end = end,
                                                    exercises = workoutExercises
                                                )
                                            )
                                        } else {
                                            // Insert new
                                            WorkoutRepository.insert(
                                                Workout(
                                                    id = 0,
                                                    name = name,
                                                    start = start,
                                                    end = end,
                                                    exercises = workoutExercises
                                                )
                                            )
                                        }
                                        unsavedChanges = false
                                        navigator.pop()
                                    }
                                }
                            } catch (e: DateTimeParseException) {
                                parseErrorMessage = "Invalid date/time format. Use yyyy-MM-dd HH:mm"
                            } catch (e: Exception) {
                                parseErrorMessage = e.message
                            }
                        }) {
                            Text("Save")
                        }
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
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding)
            ) {
                Text(
                    if (workout != null) "Edit this workout"
                    else "Create a new workout",
                    style = MaterialTheme.typography.h6
                )
                Spacer(Modifier.height(8.dp))

                // Show any error messages
                if (parseErrorMessage != null) {
                    Text(parseErrorMessage!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }
                if (duplicateNameError != null) {
                    Text(duplicateNameError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        unsavedChanges = true
                        duplicateNameError = null
                    },
                    label = { Text("Workout Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startText,
                    onValueChange = {
                        startText = it
                        unsavedChanges = true
                        parseErrorMessage = null
                    },
                    label = { Text("Start (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endText,
                    onValueChange = {
                        endText = it
                        unsavedChanges = true
                        parseErrorMessage = null
                    },
                    label = { Text("End (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Text("Exercises:", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(8.dp))

                // Table headings
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Exercise", modifier = Modifier.weight(0.4f))
                    Text("Sets (weight x reps)", modifier = Modifier.weight(0.5f))
                    Spacer(Modifier.weight(0.1f))
                }
                Divider()

                // Scrollable list of exercises
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(entries) { index, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Exercise dropdown
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(0.4f)) {
                                OutlinedButton(onClick = { expanded = true }) {
                                    Text(entry.exercise?.name ?: "Select Exercise")
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    exercises.forEach { ex ->
                                        DropdownMenuItem(onClick = {
                                            entry.exercise = ex
                                            expanded = false
                                            unsavedChanges = true
                                        }) {
                                            Text(ex.name)
                                        }
                                    }
                                }
                            }

                            // Sets
                            Column(modifier = Modifier.weight(0.5f)) {
                                entry.sets.forEachIndexed { setIndex, pair ->
                                    Row {
                                        OutlinedTextField(
                                            value = if (pair.first == 0.0) "" else pair.first.toString(),
                                            onValueChange = { newVal ->
                                                val weight = newVal.toDoubleOrNull() ?: 0.0
                                                entry.sets[setIndex] = weight to pair.second
                                                unsavedChanges = true
                                            },
                                            label = { Text("Set ${setIndex + 1} Weight") },
                                            modifier = Modifier.width(100.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = if (pair.second == 0) "" else pair.second.toString(),
                                            onValueChange = { newVal ->
                                                val reps = newVal.toIntOrNull() ?: 0
                                                entry.sets[setIndex] = pair.first to reps
                                                unsavedChanges = true
                                            },
                                            label = { Text("Reps") },
                                            modifier = Modifier.width(80.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                                Button(onClick = {
                                    entry.sets.add(0.0 to 0)
                                    unsavedChanges = true
                                }) {
                                    Text("Add Set")
                                }
                            }

                            // Delete exercise
                            IconButton(
                                onClick = {
                                    entries.removeAt(index)
                                    unsavedChanges = true
                                },
                                modifier = Modifier.weight(0.1f)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Exercise")
                            }
                        }
                        Divider()
                    }
                }
            }
        }

        // Confirmation dialog for discarding unsaved changes
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