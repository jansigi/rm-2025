package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.data.DatabaseFactory
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
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Headings for columns: Name, Start - End, Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Name", modifier = Modifier.weight(0.3f), style = MaterialTheme.typography.subtitle2)
                    Text("Start - End", modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.subtitle2)
                    Text("Actions", modifier = Modifier.weight(0.3f), style = MaterialTheme.typography.subtitle2)
                }
                Divider()

                // Workouts List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(workouts) { workout ->
                        WorkoutRow(
                            workout,
                            onView = { navigator.push(ViewWorkoutScreen(workout)) },
                            onEdit = { navigator.push(AddEditWorkoutScreen(workout)) },
                            onDelete = { workoutToDelete = workout }
                        )
                    }
                }
            }
        }

        if (workoutToDelete != null) {
            ConfirmationDialog(
                title = "Delete Workout",
                message = "Are you sure you want to delete \"${workoutToDelete!!.name}\"?",
                onConfirm = {
                    WorkoutRepository.delete(workoutToDelete!!.id)
                    workouts = WorkoutRepository.getAll()
                    workoutToDelete = null
                },
                onDismiss = { workoutToDelete = null }
            )
        }

        // Database info pop-up
        var showDbInfoDialog by remember { mutableStateOf(true) }
        if (showDbInfoDialog) {
            AlertDialog(
                onDismissRequest = { showDbInfoDialog = false },
                title = { Text("Database Info") },
                text = { Text("The application is using ${DatabaseFactory.dbTypeUsed} database.") },
                confirmButton = {
                    Button(onClick = { showDbInfoDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun WorkoutRow(
    workout: Workout,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    val duration = Duration.between(workout.start, workout.end)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column 1: Name
        Text(
            workout.name,
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.subtitle1
        )

        // Column 2: Start - End & Duration
        Column(modifier = Modifier.weight(0.4f)) {
            Text("${workout.start.format(formatter)} - ${workout.end.format(formatter)}")
            Text("Duration: ${duration.toHours()}h ${duration.toMinutes() % 60}m", style = MaterialTheme.typography.caption)
        }

        // Column 3: Actions
        Row(
            modifier = Modifier.weight(0.3f),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onView) {
                Text("View")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onEdit) {
                Text("Edit")
            }
            Spacer(Modifier.width(8.dp))
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