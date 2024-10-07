package com.example.selflogist.model

data class Task(
    val id: Int = 0,           // ID задачи, который будет автоматически увеличиваться
    val title: String,         // Название задачи
    val description: String?,  // Описание задачи (может быть null)
    val startTime: Long,       // Время начала задачи в миллисекундах
    val endTime: Long          // Время окончания задачи в миллисекундах
)
