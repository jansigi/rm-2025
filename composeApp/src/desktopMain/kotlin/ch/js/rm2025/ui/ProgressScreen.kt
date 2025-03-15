package ch.js.rm2025.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ch.js.rm2025.model.Exercise
import ch.js.rm2025.repository.WorkoutRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ProgressEntry(val date: LocalDateTime, val totalVolume: Double)

class ProgressScreen(val exercise: Exercise) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var progressEntries by remember { mutableStateOf(listOf<ProgressEntry>()) }

        // Load workout data once
        LaunchedEffect(Unit) {
            val workouts = WorkoutRepository.getAll()
            val entries = mutableListOf<ProgressEntry>()
            workouts.forEach { workout ->
                workout.exercises.forEach { we ->
                    if (we.exercise.id == exercise.id) {
                        val volume = we.sets.sumOf { it.weight * it.reps }
                        entries.add(ProgressEntry(workout.start, volume))
                    }
                }
            }
            progressEntries = entries.sortedBy { it.date }
        }

        // Calculate next expected volume
        val nextExpected = if (progressEntries.size >= 2) {
            val increases = progressEntries.zipWithNext { a, b -> b.totalVolume - a.totalVolume }
            val avgIncrease = increases.average()
            progressEntries.last().totalVolume + avgIncrease
        } else 0.0

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Progress of ${exercise.name}") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(padding)) {
                // Table-like listing of historical volumes
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date", style = MaterialTheme.typography.subtitle2)
                    Text("Total Volume", style = MaterialTheme.typography.subtitle2)
                }
                Divider()

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(progressEntries) { entry ->
                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.date.format(formatter))
                            Text("${entry.totalVolume} kg")
                        }
                        Divider()
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Next Expected: ${nextExpected}kg", style = MaterialTheme.typography.body1)
                Spacer(Modifier.height(16.dp))

                Text("Progress Chart (last 3 months):", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.height(8.dp))
                ProgressChart(
                    entries = progressEntries.filter {
                        it.date.isAfter(LocalDateTime.now().minusMonths(3))
                    } + listOf(ProgressEntry(LocalDateTime.now().plusDays(1), nextExpected))
                )
            }
        }
    }
}

@Composable
fun ProgressChart(entries: List<ProgressEntry>) {
    if (entries.isEmpty()) {
        Text("No progress data available.")
        return
    }
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val maxVolume = entries.maxOf { it.totalVolume }.coerceAtLeast(1.0)
        val padding = 16.dp.toPx()
        val widthPerEntry = (size.width - 2 * padding) / (entries.size - 1).coerceAtLeast(1)
        for (i in 0 until entries.size - 1) {
            val x1 = padding + i * widthPerEntry
            val y1 = size.height - padding - (entries[i].totalVolume / maxVolume * (size.height - 2 * padding))
            val x2 = padding + (i + 1) * widthPerEntry
            val y2 = size.height - padding - (entries[i + 1].totalVolume / maxVolume * (size.height - 2 * padding))
            drawLine(
                color = Color.Blue,
                start = Offset(x1, y1.toFloat()),
                end = Offset(x2, y2.toFloat()),
                strokeWidth = 4f
            )
        }
    }
}