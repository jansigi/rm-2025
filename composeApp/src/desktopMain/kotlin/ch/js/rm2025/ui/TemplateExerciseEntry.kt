package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import ch.js.rm2025.model.Exercise
import ch.js.rm2025.model.Template
import ch.js.rm2025.model.TemplateExercise
import ch.js.rm2025.model.TemplateSet
import ch.js.rm2025.repository.ExerciseRepository
import ch.js.rm2025.repository.TemplateRepository

data class TemplateExerciseEntry(
    var exercise: Exercise? = null,
    var sets: MutableList<Int> = mutableStateListOf(0)
)

class AddEditTemplateScreen(val template: Template?) : Screen {
    @Composable
    override fun Content() {
        var name by remember { mutableStateOf(template?.name ?: "") }
        var entries by remember { mutableStateOf(mutableStateListOf<TemplateExerciseEntry>()) }
        if (template != null && entries.isEmpty()) {
            entries.addAll(template.exercises.map { te ->
                TemplateExerciseEntry(
                    exercise = te.exercise,
                    sets = te.sets.map { it.reps }.toMutableList()
                )
            })
        }
        val exercises = ExerciseRepository.getAll()
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(if (template != null) "Edit Template \"${template.name}\"" else "Add Template") })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Template Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text("Exercises:")
                LazyColumn {
                    itemsIndexed(entries) { index, entry ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expanded = true }) {
                                    Text(entry.exercise?.name ?: "Select Exercise")
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    exercises.forEach { ex ->
                                        DropdownMenuItem(onClick = { 
                                            entry.exercise = ex
                                            expanded = false
                                        }) {
                                            Text(ex.name)
                                        }
                                    }
                                }
                            }
                            Column {
                                entry.sets.forEachIndexed { setIndex, reps ->
                                    OutlinedTextField(
                                        value = if(reps==0) "" else reps.toString(),
                                        onValueChange = { newVal ->
                                            val intVal = newVal.toIntOrNull() ?: 0
                                            entry.sets[setIndex] = intVal
                                        },
                                        label = { Text("Set ${setIndex+1} reps") },
                                        modifier = Modifier.width(100.dp)
                                    )
                                }
                                Button(onClick = { entry.sets.add(0) }) {
                                    Text("Add Set")
                                }
                            }
                            IconButton(onClick = { entries.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Exercise")
                            }
                        }
                        Divider()
                    }
                }
                Button(onClick = { entries.add(TemplateExerciseEntry()) }) {
                    Text("Add Exercise")
                }
                Spacer(Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        if(name.isNotBlank() && entries.isNotEmpty() && entries.all { it.exercise != null && it.sets.all { reps -> reps > 0 } }){
                            val templateExercises = entries.mapIndexed { index, entry ->
                                TemplateExercise(
                                    id = 0,
                                    templateId = 0,
                                    exercise = entry.exercise!!,
                                    order = index,
                                    sets = entry.sets.mapIndexed { sIndex, reps ->
                                        TemplateSet(id = 0, templateExerciseId = 0, setNumber = sIndex+1, reps = reps)
                                    }
                                )
                            }
                            if(template != null){
                                TemplateRepository.update(Template(id = template.id, name = name, exercises = templateExercises))
                            } else {
                                TemplateRepository.insert(Template(id = 0, name = name, exercises = templateExercises))
                            }
                        }
                    }) { Text("Save") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { /* Cancel action */ }) { Text("Cancel") }
                }
            }
        }
    }
}