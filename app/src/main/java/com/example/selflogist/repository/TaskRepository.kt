package com.example.selflogist.repository

import com.example.selflogist.database.TaskDatabaseHelper
import com.example.selflogist.model.Task

class TaskRepository(private val dbHelper: TaskDatabaseHelper) {

    fun getTasksForDay(startOfDay: Long, endOfDay: Long): List<Task> {
        return dbHelper.getTasksForDay(startOfDay, endOfDay)
    }

    suspend fun insertTask(task: Task) {
        dbHelper.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        dbHelper.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        dbHelper.deleteTask(task)
    }
}