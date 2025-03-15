package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.model.Workout
import java.time.Duration
import java.time.format.DateTimeFormatter

class ViewWorkoutScreen(val workout: Workout) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workout \"${workout.name}\"") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(padding)) {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                Text("Start: ${workout.start.format(formatter)}")
                Text("End: ${workout.end.format(formatter)}")
                val duration = Duration.between(workout.start, workout.end)
                Text("Duration: ${duration.toHours()}h ${duration.toMinutes()%60}m")
                Spacer(Modifier.height(16.dp))
                Text("Exercises:", style = MaterialTheme.typography.h6)
                LazyColumn {
                    items(workout.exercises) { we ->
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Exercise: ${we.exercise.name}", style = MaterialTheme.typography.subtitle1)
                                we.sets.forEach { ws ->
                                    Text("Set ${ws.setNumber}: ${ws.weight} kg x ${ws.reps} reps")
                                }
                                Button(onClick = { navigator.push(ProgressScreen(we.exercise)) }) {
                                    Text("My Progress")
                                }
                            }
                        }
                    }
                }
                val totalVolume = workout.exercises.sumOf { we -> we.sets.sumOf { ws -> ws.weight * ws.reps } }
                val totalSets = workout.exercises.sumOf { it.sets.size }
                val totalReps = workout.exercises.sumOf { we -> we.sets.sumOf { ws -> ws.reps } }
                Spacer(Modifier.height(16.dp))
                Text("Total Volume: $totalVolume")
                Text("Total Sets: $totalSets")
                Text("Total Reps: $totalReps")
            }
        }
    }
}