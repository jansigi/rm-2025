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
            topBar = { TopAppBar(title = { Text("AthliTrack - Exercises") }) },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(AddEditExerciseScreen(null)) }) {
                    Text("+")
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(exercises) { exercise ->
                    ExerciseItem(
                        exercise,
                        onEdit = { navigator.push(AddEditExerciseScreen(exercise)) },
                        onDelete = { exerciseToDelete = exercise },
                        onProgress = { navigator.push(ProgressScreen(exercise)) }
                    )
                }
            }
        }

        if (exerciseToDelete != null) {
            ConfirmationDialog(
                title = "Delete Exercise",
                message = "Are you sure you want to delete this exercise?",
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
fun ExerciseItem(
    exercise: Exercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onProgress: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Name: ${exercise.name}", style = MaterialTheme.typography.h6)
            Text("Description: ${exercise.description}")
            Text("Type: ${exercise.type}")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onEdit) { Text("Edit") }
                Button(onClick = onDelete) { Text("Delete") }
                Button(onClick = onProgress) { Text("My Progress") }
            }
        }
    }
}