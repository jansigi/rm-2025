package ch.js.rm2025.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import ch.js.rm2025.model.Exercise
import ch.js.rm2025.repository.ExerciseRepository

class AddEditExerciseScreen(val exercise: Exercise?) : Screen {
    @Composable
    override fun Content() {
        var name by remember { mutableStateOf(exercise?.name ?: "") }
        var description by remember { mutableStateOf(exercise?.description ?: "") }
        var type by remember { mutableStateOf(exercise?.type ?: "") }
        val isEdit = exercise != null
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if(isEdit) "Edit Exercise" else "Add Exercise") }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        if(name.isNotBlank() && description.isNotBlank() && type.isNotBlank()){
                            if(isEdit){
                                ExerciseRepository.update(Exercise(exercise!!.id, name, description, type))
                            } else {
                                ExerciseRepository.insert(Exercise(0, name, description, type))
                            }
                        }
                    }) {
                        Text("Save")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { /* Navigate back */ }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}