package com.example.selflogist.ui

import android.widget.Toast
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.selflogist.R
import com.example.selflogist.database.TaskDatabaseHelper
import com.example.selflogist.model.Task
import java.util.*

class DayScheduleActivity : AppCompatActivity() {

    private lateinit var hourlyScheduleLayout: LinearLayout
    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var gridLayer: LinearLayout
    private lateinit var tasksLayer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_schedule)

        gridLayer = findViewById(R.id.grid_layer)
        tasksLayer = findViewById(R.id.tasks_layer)
        dbHelper = TaskDatabaseHelper(this)

        displayHourlySchedule()
    }

    private fun displayHourlySchedule() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        val tasks = dbHelper.getTasksForDay(startOfDay, endOfDay)

        // Добавляем сетку разбиения по времени
        for (hour in 0..23) {
            val hourLabel = TextView(this)
            hourLabel.text = String.format("%02d:00", hour)
            hourLabel.gravity = Gravity.CENTER_VERTICAL
            hourLabel.setPadding(8, 8, 8, 8)
            hourLabel.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            hourLabel.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                80 // Высота часа
            )

            findViewById<LinearLayout>(R.id.grid_layer).addView(hourLabel)
        }

        // Добавляем задачи на верхний слой
        tasks.forEach { task ->
            val taskView = createTaskView(task)
            findViewById<LinearLayout>(R.id.tasks_layer).addView(taskView)
        }
    }

    private fun createHourBlock(hour: Int, tasks: List<Task>): View {
        val hourBlockLayout = LinearLayout(this)
        hourBlockLayout.orientation = LinearLayout.VERTICAL
        hourBlockLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Добавляем текстовое поле для времени (например, "09:00")
        val hourLabel = TextView(this)
        hourLabel.text = String.format("%02d:00", hour)
        hourLabel.gravity = Gravity.CENTER_VERTICAL
        hourLabel.setPadding(8, 8, 8, 8)
        hourLabel.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        hourLabel.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            80 // Высота часа, можно менять в зависимости от дизайна
        )

        hourBlockLayout.addView(hourLabel)

        // Добавляем задачи, которые начинаются в этом часу
        tasks.filter { task ->
            val taskStartHour = (task.startTime / (1000 * 60 * 60)) % 24
            taskStartHour.toInt() == hour
        }.forEach { task ->
            val taskView = createTaskView(task)
            hourBlockLayout.addView(taskView)
        }

        // Создаем разделитель между часами
        val divider = View(this)
        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2 // Толщина разделителя
        )
        divider.setBackgroundColor(resources.getColor(android.R.color.black))
        divider.layoutParams = dividerParams

        hourBlockLayout.addView(divider)

        return hourBlockLayout
    }

    private fun createTaskView(task: Task): View {
        val taskView = TextView(this)
        taskView.text = task.title
        taskView.setBackgroundColor(resources.getColor(R.color.teal_200))
        taskView.setPadding(8, 8, 8, 8)
        taskView.alpha = 0.7f // Задаем полупрозрачность

        val taskDurationInMinutes = (task.endTime - task.startTime) / (1000 * 60)
        val height = (taskDurationInMinutes.toInt() * 3) // Пример: 3 пикселя на минуту

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        )
        layoutParams.setMargins(16, 8, 16, 8)

        taskView.layoutParams = layoutParams

        taskView.setOnClickListener {
            Toast.makeText(this, task.description ?: "No description", Toast.LENGTH_SHORT).show()
        }

        return taskView
    }
}