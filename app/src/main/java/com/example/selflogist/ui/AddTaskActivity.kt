package com.example.selflogist.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.selflogist.R
import com.example.selflogist.database.TaskDatabaseHelper
import com.example.selflogist.model.Task
import java.util.Calendar
import android.text.Editable
import android.text.TextWatcher

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskTitleInput: EditText
    private lateinit var taskDescriptionInput: EditText
    private lateinit var saveTaskButton: Button
    private lateinit var startTimeInput: EditText
    private lateinit var endTimeInput: EditText
    private lateinit var dbHelper: TaskDatabaseHelper

    // Переменные для хранения времени начала и конца задачи
    private var selectedStartTime: Long = 0 // добавлено
    private var selectedEndTime: Long = 0 // добавлено

    // Переменная для хранения задачи, если идет редактирование
    private var taskToEdit: Task? = null // добавлено

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskTitleInput = findViewById(R.id.taskTitle)
        taskDescriptionInput = findViewById(R.id.taskDescription)
        saveTaskButton = findViewById(R.id.saveTaskButton)
        startTimeInput = findViewById(R.id.starttime)
        endTimeInput = findViewById(R.id.endtime)

        dbHelper = TaskDatabaseHelper(this)

        // Добавляем TextWatcher для автоизменения высоты taskDescriptionInput
        taskDescriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                taskDescriptionInput.post {
                    val lineCount = taskDescriptionInput.lineCount
                    if (lineCount > taskDescriptionInput.minLines) {
                        taskDescriptionInput.setLines(lineCount)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Проверка, если активность была открыта для редактирования задачи
        val taskId = intent.getIntExtra("TASK_ID", -1)
        if (taskId != -1) {
            taskToEdit = dbHelper.getTaskById(taskId.toLong())
            taskToEdit?.let { task ->
                taskTitleInput.setText(task.title)
                taskDescriptionInput.setText(task.description)
                startTimeInput.setText("${convertMillisToDate(task.startTime)} ${convertMillisToTime(task.startTime)}")
                endTimeInput.setText("${convertMillisToDate(task.endTime)} ${convertMillisToTime(task.endTime)}")
                selectedStartTime = task.startTime
                selectedEndTime = task.endTime
            }
        }

        // Показ кастомного диалога при нажатии на поле ввода времени
        startTimeInput.setOnClickListener { showTimePickerDialog(true) }
        endTimeInput.setOnClickListener { showTimePickerDialog(false) }

        saveTaskButton.setOnClickListener {
            onSaveTaskClicked()
        }
    }

    // Метод для отображения кастомного TimePickerDialog
    private fun showTimePickerDialog(isStartTime: Boolean) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_time_picker)

        val dateInput: EditText = dialog.findViewById(R.id.dateInput)
        val hourPicker: NumberPicker = dialog.findViewById(R.id.hourPicker)
        val minutePicker: NumberPicker = dialog.findViewById(R.id.minutePicker)
        val confirmButton: Button = dialog.findViewById(R.id.confirmButton)

        val calendar = Calendar.getInstance()

        // Установка диапазона значений для NumberPicker
        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        minutePicker.minValue = 0
        minutePicker.maxValue = 59

        // Установка даты по умолчанию
        dateInput.setText("${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}")

        // Открытие DatePickerDialog при нажатии на поле даты
        dateInput.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                dateInput.setText("$dayOfMonth/${month + 1}/$year")
                calendar.set(year, month, dayOfMonth)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Установка кнопки подтверждения
        confirmButton.setOnClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, hourPicker.value)
            calendar.set(Calendar.MINUTE, minutePicker.value)

            if (isStartTime) {
                selectedStartTime = calendar.timeInMillis
                startTimeInput.setText("${dateInput.text} ${hourPicker.value}:${minutePicker.value}")
            } else {
                selectedEndTime = calendar.timeInMillis
                endTimeInput.setText("${dateInput.text} ${hourPicker.value}:${minutePicker.value}")
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    // Метод для сохранения задачи (или её обновления при редактировании)
    private fun onSaveTaskClicked() {
        val title = taskTitleInput.text.toString()
        val description = taskDescriptionInput.text.toString()

        if (title.isNotEmpty() && selectedStartTime != 0L && selectedEndTime != 0L) {
            if (taskToEdit == null) { // добавлено
                // Создание новой задачи
                val task = Task(
                    title = title,
                    description = description,
                    startTime = selectedStartTime,
                    endTime = selectedEndTime
                )
                dbHelper.insertTask(task)
                Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show()
            } else { // добавлено
                // Обновление существующей задачи
                val updatedTask = taskToEdit!!.copy(
                    title = title,
                    description = description,
                    startTime = selectedStartTime,
                    endTime = selectedEndTime
                )
                dbHelper.updateTask(updatedTask)
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
            }
            finish() // Закрывает активность после сохранения
        } else {
            Toast.makeText(this, "All fields must be filled correctly", Toast.LENGTH_SHORT).show()
        }
    }

    // Вспомогательные методы для конвертации миллисекунд в дату и время
    private fun convertMillisToDate(timeInMillis: Long): String { // добавлено
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    private fun convertMillisToTime(timeInMillis: Long): String { // добавлено
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }
}