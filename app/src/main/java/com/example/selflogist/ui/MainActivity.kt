package com.example.selflogist.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.selflogist.R
import com.example.selflogist.database.TaskDatabaseHelper
import com.example.selflogist.model.Task
import com.example.selflogist.adapter.TaskAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: TaskDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация базы данных
        dbHelper = TaskDatabaseHelper(this)

        // Инициализация RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Инициализация адаптера с данными из базы данных
        val tasks = dbHelper.getTasksForToday()
        adapter = TaskAdapter(tasks, onTaskDeleted = { task ->
            onTaskDeleted(task)
        }, onTaskCompleted = { task ->
            onTaskCompleted(task)
        }, onTaskEdited = { task ->
            onTaskEdited(task)
        }) // Удален параметр parentView
        recyclerView.adapter = adapter

        // Найти кнопку "Add Task"
        val addTaskButton: FloatingActionButton = findViewById(R.id.addTaskButton)

        // Установка слушателя нажатий на кнопку
        addTaskButton.setOnClickListener {
            // Открыть новое Activity для добавления задачи
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        // Найти кнопку "View Schedule"
        val viewScheduleButton: Button = findViewById(R.id.viewScheduleButton)

        // Установка слушателя нажатий на кнопку
        viewScheduleButton.setOnClickListener {
            // Открыть новое Activity для просмотра расписания
            val intent = Intent(this, DayScheduleActivity::class.java)
            startActivity(intent)
        }

        // Найти кнопку "Calendar"
        val calendarButton: FloatingActionButton = findViewById(R.id.calendarButton)

        calendarButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновление списка задач при возвращении к этой активности
        updateTaskList()
    }

    private fun updateTaskList() {
        // Получение всех задач из базы данных
        val tasks = dbHelper.getAllTasks()
        adapter.updateTasks(tasks)
    }

    private fun onTaskDeleted(task: Task) {
        dbHelper.deleteTask(task)
        updateTaskList()
    }

    private fun onTaskCompleted(task: Task) {
        // Реализуйте логику для обработки выполнения задачи
    }

    private fun onTaskEdited(task: Task) {
        // Открываем AddTaskActivity для редактирования задачи
        val intent = Intent(this, AddTaskActivity::class.java).apply {
            putExtra("TASK_ID", task.id)
        }
        startActivity(intent)
    }
}