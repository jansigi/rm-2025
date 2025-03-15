package ch.js.rm2025.model

data class Template(
    val id: Int,
    val name: String,
    val exercises: List<TemplateExercise>
)