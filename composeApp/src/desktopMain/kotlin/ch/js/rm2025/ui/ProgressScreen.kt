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

/**
 * Data class for storing each execution's date + total volume.
 */
data class ProgressEntry(val date: LocalDateTime, val totalVolume: Double)

/**
 * Shows the progress of a given exercise:
 * - Table of all-time records (date + total volume)
 * - Next expected volume (average of increases)
 * - Line chart for last 3 months + the next expected data point
 */
class ProgressScreen(private val exercise: Exercise) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // State for storing the entire progress list (all time)
        var allProgressEntries by remember { mutableStateOf(listOf<ProgressEntry>()) }
        // The final "next expected" volume based on average increase
        val nextExpected = remember(allProgressEntries) {
            computeNextExpected(allProgressEntries)
        }

        // On first display, load all workouts to gather progress data
        LaunchedEffect(Unit) {
            val workouts = WorkoutRepository.getAll()
            val entries = mutableListOf<ProgressEntry>()
            workouts.forEach { workout ->
                workout.exercises.forEach { we ->
                    if (we.exercise.id == exercise.id) {
                        // Sum the volume for this exercise in this workout
                        val volume = we.sets.sumOf { it.weight * it.reps }
                        // Record a separate entry for each workout (even if same day)
                        entries.add(ProgressEntry(workout.start, volume))
                    }
                }
            }
            // Sort by date
            allProgressEntries = entries.sortedBy { it.date }
        }

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
            // Layout: A row with the table on the left and the chart on the right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding)
            ) {
                // Left side: table of all progress entries
                Column(modifier = Modifier.weight(0.4f)) {
                    if (allProgressEntries.isEmpty()) {
                        Text("No records available.", style = MaterialTheme.typography.body1)
                    } else {
                        ProgressTable(allProgressEntries)
                    }
                    Spacer(Modifier.height(16.dp))
                    // Next expected volume
                    Text("Next Expected Volume: $nextExpected", style = MaterialTheme.typography.subtitle1)
                }
                Spacer(Modifier.width(16.dp))
                // Right side: line chart for last 3 months
                Column(modifier = Modifier.weight(0.6f)) {
                    Text("Total Volume Chart (last 3 months):", style = MaterialTheme.typography.subtitle1)
                    Spacer(Modifier.height(8.dp))
                    // Filter entries to last 3 months, plus the next expected point
                    val chartEntries = remember(allProgressEntries, nextExpected) {
                        val threeMonthsAgo = LocalDateTime.now().minusMonths(3)
                        val recent = allProgressEntries.filter { it.date.isAfter(threeMonthsAgo) }
                        // Add an extra data point for the next expected
                        if (allProgressEntries.isNotEmpty()) {
                            // Use tomorrow as the date for "expected" if you prefer
                            recent + listOf(ProgressEntry(LocalDateTime.now().plusDays(1), nextExpected))
                        } else {
                            emptyList()
                        }
                    }
                    if (chartEntries.isEmpty()) {
                        Text("No chart data available.")
                    } else {
                        ProgressChart(entries = chartEntries)
                    }
                }
            }
        }
    }

    /**
     * Computes the next expected volume:
     * 1. Determine the increase between each occurrence in chronological order
     * 2. Compute the average increase
     * 3. Add the average increase to the most recent total volume
     * Returns 0.0 if there's not enough data (fewer than 2 records).
     */
    private fun computeNextExpected(allEntries: List<ProgressEntry>): Double {
        if (allEntries.size < 2) return 0.0
        val increases = allEntries.zipWithNext { a, b -> b.totalVolume - a.totalVolume }
        val avgIncrease = increases.average()
        return allEntries.last().totalVolume + avgIncrease
    }
}

/**
 * Displays a table-like list of all progress entries:
 * Date (dd.MM.yyyy) | Total Volume
 */
@Composable
fun ProgressTable(entries: List<ProgressEntry>) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    // Table headings
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Date", style = MaterialTheme.typography.subtitle2)
        Text("Total Volume", style = MaterialTheme.typography.subtitle2)
    }
    Divider()
    // List all entries
    LazyColumn {
        items(entries) { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.date.format(dateFormatter))
                Text("${entry.totalVolume} kg")
            }
            Divider()
        }
    }
}

/**
 * Draws a simple line chart of volume vs. time for the given entries.
 */
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