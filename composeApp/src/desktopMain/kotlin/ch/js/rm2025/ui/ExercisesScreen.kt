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
import ch.js.rm2025.model.Exercise
import ch.js.rm2025.repository.ExerciseRepository
import ch.js.rm2025.ui.component.ConfirmationDialog

class ExercisesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var exercises by remember { mutableStateOf(listOf<Exercise>()) }
        var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

        LaunchedEffect(Unit) {
            exercises = ExerciseRepository.getAll()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Exercises") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(AddEditExerciseScreen(null)) }) {
                    Text("+")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Optional headings row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Exercise", style = MaterialTheme.typography.subtitle2)
                    Text("Description", style = MaterialTheme.typography.subtitle2)
                    Text("Type", style = MaterialTheme.typography.subtitle2)
                    Text("Actions", style = MaterialTheme.typography.subtitle2)
                }
                Divider()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(exercises) { exercise ->
                        ExerciseRow(
                            exercise,
                            onEdit = { navigator.push(AddEditExerciseScreen(exercise)) },
                            onDelete = { exerciseToDelete = exercise },
                            onProgress = { navigator.push(ProgressScreen(exercise)) }
                        )
                    }
                }
            }
        }

        if (exerciseToDelete != null) {
            ConfirmationDialog(
                title = "Delete Exercise",
                message = "Are you sure you want to delete \"${exerciseToDelete!!.name}\"?",
                onConfirm = {
                    ExerciseRepository.delete(exerciseToDelete!!.id)
                    exercises = ExerciseRepository.getAll()
                    exerciseToDelete = null
                },
                onDismiss = { exerciseToDelete = null }
            )
        }
    }
}

@Composable
fun ExerciseRow(
    exercise: Exercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onProgress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main info
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.name, style = MaterialTheme.typography.subtitle1)
            Text(exercise.description, style = MaterialTheme.typography.body2)
            Text(exercise.type, style = MaterialTheme.typography.caption)
        }

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onProgress) {
                Text("Progress")
            }
            Button(onClick = onEdit) {
                Text("Edit")
            }
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
            ) {
                Text("Delete")
            }
        }
    }
    Divider()
}