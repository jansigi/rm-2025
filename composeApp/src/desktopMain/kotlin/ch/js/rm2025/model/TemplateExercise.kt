package ch.js.rm2025.model

data class TemplateExercise(
    val id: Int,
    val templateId: Int,
    val exercise: Exercise,
    val order: Int,
    val sets: List<TemplateSet>
)