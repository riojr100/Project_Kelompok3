package com.example.project_kelompok3

// Data class representing a Task
data class Task(
    val taskId: String,
    val title: String,
    val description: String,
    val tag: String,
    val priority: Int,
    val state: String,
    val dueDate: String
)
