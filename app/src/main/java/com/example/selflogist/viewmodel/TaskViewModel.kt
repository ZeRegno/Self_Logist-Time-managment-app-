package com.example.selflogist.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.selflogist.database.TaskDatabaseHelper
import com.example.selflogist.model.Task
import com.example.selflogist.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    val tasksForDay: MutableLiveData<List<Task>> = MutableLiveData()

    init {
        val dbHelper = TaskDatabaseHelper(application)
        repository = TaskRepository(dbHelper)
    }

    fun getTasksForDay(startOfDay: Long, endOfDay: Long) {
        viewModelScope.launch {
            val tasks = repository.getTasksForDay(startOfDay, endOfDay)
            tasksForDay.postValue(tasks)
        }
    }

    fun insert(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
        refreshTasksForDay()
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
        refreshTasksForDay()
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
        refreshTasksForDay()
    }

    private fun refreshTasksForDay() {
        val startOfDay = System.currentTimeMillis()
        val endOfDay = startOfDay + 86400000
        getTasksForDay(startOfDay, endOfDay)
    }
}