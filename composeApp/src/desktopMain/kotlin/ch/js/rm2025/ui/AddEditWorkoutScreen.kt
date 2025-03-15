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

data class WorkoutExerciseEntry(
    var exercise: Exercise? = null,
    var sets: MutableList<Pair<Double, Int>> = mutableStateListOf() // Pair(weight, reps)
)

class AddEditWorkoutScreen(val workout: Workout?) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var name by remember { mutableStateOf(workout?.name ?: "") }
        var startText by remember { mutableStateOf(workout?.start?.toString() ?: LocalDateTime.now().toString()) }
        var endText by remember { mutableStateOf(workout?.end?.toString() ?: LocalDateTime.now().plusHours(1).toString()) }
        var entries by remember { mutableStateOf(mutableStateListOf<WorkoutExerciseEntry>()) }
        var unsavedChanges by remember { mutableStateOf(false) }
        var showCancelConfirmation by remember { mutableStateOf(false) }

        // If editing, load data
        if (workout != null && entries.isEmpty()) {
            entries.addAll(
                workout.exercises.map { we ->
                    WorkoutExerciseEntry(
                        exercise = we.exercise,
                        sets = we.sets.map { ws -> Pair(ws.weight, ws.reps) }.toMutableList()
                    )
                }
            )
        }

        val exercises = ExerciseRepository.getAll()

        // If adding a new workout, optionally load a template
        if (workout == null && entries.isEmpty()) {
            var showTemplateDialog by remember { mutableStateOf(true) }
            if (showTemplateDialog) {
                AlertDialog(
                    onDismissRequest = { showTemplateDialog = false },
                    title = { Text("Select Template") },
                    text = { Text("Do you want to start with a template?") },
                    confirmButton = {
                        Button(onClick = {
                            val templates = TemplateRepository.getAll()
                            if (templates.isNotEmpty()) {
                                val template = templates.first()
                                entries.addAll(
                                    template.exercises.map { te ->
                                        WorkoutExerciseEntry(
                                            exercise = te.exercise,
                                            sets = te.sets.map { ts -> Pair(0.0, ts.reps) }.toMutableList()
                                        )
                                    }
                                )
                                unsavedChanges = true
                            }
                            showTemplateDialog = false
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showTemplateDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
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
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding)
            ) {
                Text(if (workout != null) "Edit this workout" else "Create a new workout", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        unsavedChanges = true
                    },
                    label = { Text("Workout Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startText,
                    onValueChange = {
                        startText = it
                        unsavedChanges = true
                    },
                    label = { Text("Start Datetime (ISO)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endText,
                    onValueChange = {
                        endText = it
                        unsavedChanges = true
                    },
                    label = { Text("End Datetime (ISO)") },
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

                LazyColumn {
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
                                    entry.sets.add(Pair(0.0, 0))
                                    unsavedChanges = true
                                }) {
                                    Text("Add Set")
                                }
                            }

                            // Delete the exercise from the workout
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

                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    entries.add(WorkoutExerciseEntry())
                    unsavedChanges = true
                }) {
                    Text("Add Exercise")
                }

                Spacer(Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        try {
                            val start = LocalDateTime.parse(startText)
                            val end = LocalDateTime.parse(endText)
                            if (name.isNotBlank() && entries.isNotEmpty()) {
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
                            }
                        } catch (e: Exception) {
                            println("Error: ${e.message}")
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