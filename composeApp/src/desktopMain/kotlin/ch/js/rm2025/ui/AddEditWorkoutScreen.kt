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
    // Each pair represents (weight, reps) for a set.
    var sets: MutableList<Pair<Double, Int>> = mutableStateListOf()
)

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

        // Error messages for name, parsing, duplicate names, and date order.
        var nameError by remember { mutableStateOf<String?>(null) }
        var parseErrorMessage by remember { mutableStateOf<String?>(null) }
        var duplicateNameError by remember { mutableStateOf<String?>(null) }
        var exerciseListError by remember { mutableStateOf<String?>(null) }

        // The list of exercise entries.
        var entries by remember { mutableStateOf(mutableStateListOf<WorkoutExerciseEntry>()) }
        var unsavedChanges by remember { mutableStateOf(false) }
        var showCancelConfirmation by remember { mutableStateOf(false) }

        // Load existing workout data if editing.
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

        // Show template selection dialog if new workout and no exercises have been added.
        var showTemplateDialog by remember { mutableStateOf(workout == null && entries.isEmpty()) }
        if (showTemplateDialog) {
            val templates = TemplateRepository.getAll()
            var expanded by remember { mutableStateOf(false) }
            var selectedTemplate by remember { mutableStateOf<ch.js.rm2025.model.Template?>(null) }

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
                                DropdownMenuItem(onClick = {
                                    selectedTemplate = null
                                    expanded = false
                                }) {
                                    Text("No Template")
                                }
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
                    title = { Text(if (workout != null) "Edit Workout \"${workout.name}\"" else "Add Workout") },
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
            bottomBar = {
                Column(modifier = Modifier.padding(16.dp)) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            // Clear previous errors
                            nameError = null
                            parseErrorMessage = null
                            duplicateNameError = null
                            exerciseListError = null

                            // Validate name field.
                            if (name.isBlank()) {
                                nameError = "Workout name is required!"
                                return@Button
                            }
                            // Check for duplicate workout names.
                            val existing = WorkoutRepository.getAll().find { it.name == name }
                            if (existing != null && existing.id != workout?.id) {
                                duplicateNameError = "A workout with this name already exists!"
                                return@Button
                            }
                            // Validate that at least one exercise is present.
                            if (entries.isEmpty()) {
                                exerciseListError = "At least one exercise is required!"
                                return@Button
                            }
                            try {
                                val start = LocalDateTime.parse(startText, dateTimeFormatter)
                                val end = LocalDateTime.parse(endText, dateTimeFormatter)
                                // Check that start is before end.
                                if (!start.isBefore(end)) {
                                    parseErrorMessage = "Start date must be before end date!"
                                    return@Button
                                }
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
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(if (workout != null) "Edit this workout" else "Create a new workout", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))
                if (nameError != null) {
                    Text(nameError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }
                if (parseErrorMessage != null) {
                    Text(parseErrorMessage!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }
                if (duplicateNameError != null) {
                    Text(duplicateNameError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }
                if (exerciseListError != null) {
                    Text(exerciseListError!!, color = MaterialTheme.colors.error)
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        unsavedChanges = true
                        nameError = null
                        duplicateNameError = null
                        exerciseListError = null
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
                    label = { Text("Start Datetime (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endText,
                    onValueChange = {
                        endText = it
                        unsavedChanges = true
                        parseErrorMessage = null
                    },
                    label = { Text("End Datetime (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Exercises:", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Exercise", modifier = Modifier.weight(0.4f))
                    Text("Sets (weight x reps)", modifier = Modifier.weight(0.5f))
                    Spacer(Modifier.weight(0.1f))
                }
                Divider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(entries) { index, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
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