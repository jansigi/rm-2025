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
import ch.js.rm2025.model.Workout
import ch.js.rm2025.repository.WorkoutRepository
import ch.js.rm2025.ui.component.ConfirmationDialog
import java.time.Duration
import java.time.format.DateTimeFormatter

class MainWindowScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var workouts by remember { mutableStateOf(listOf<Workout>()) }
        var workoutToDelete by remember { mutableStateOf<Workout?>(null) }
        LaunchedEffect(Unit) {
            workouts = WorkoutRepository.getAll()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AthliTrack - Workouts") },
                    actions = {
                        Button(onClick = { navigator.push(ExercisesScreen()) }) {
                            Text("Exercises")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { navigator.push(TemplatesScreen()) }) {
                            Text("Templates")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigator.push(AddEditWorkoutScreen(null)) }) {
                    Text("+")
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(workouts) { workout ->
                    WorkoutItem(
                        workout,
                        onView = { navigator.push(ViewWorkoutScreen(workout)) },
                        onEdit = { navigator.push(AddEditWorkoutScreen(workout)) },
                        onDelete = { workoutToDelete = workout }
                    )
                }
            }
        }

        if (workoutToDelete != null) {
            ConfirmationDialog(
                title = "Delete Workout",
                message = "Are you sure you want to delete this workout?",
                onConfirm = {
                    WorkoutRepository.delete(workoutToDelete!!.id)
                    workouts = WorkoutRepository.getAll()
                    workoutToDelete = null
                },
                onDismiss = { workoutToDelete = null }
            )
        }
    }
}

@Composable
fun WorkoutItem(
    workout: Workout,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Name: ${workout.name}", style = MaterialTheme.typography.h6)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            Text("Start: ${workout.start.format(formatter)}")
            Text("End: ${workout.end.format(formatter)}")
            val duration = Duration.between(workout.start, workout.end)
            Text("Duration: ${duration.toHours()}h ${duration.toMinutes() % 60}m")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onView) { Text("View Workout") }
                Button(onClick = onEdit) { Text("Edit Workout") }
                Button(onClick = onDelete) { Text("Delete Workout") }
            }
        }
    }
}