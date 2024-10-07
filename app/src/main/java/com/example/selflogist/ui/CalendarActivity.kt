package com.example.selflogist.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.selflogist.R
import com.example.selflogist.database.TaskDatabaseHelper
import java.util.Calendar
import android.view.LayoutInflater
import android.graphics.Color

import android.util.Log
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.gridlayout.widget.GridLayout


class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarGridLayout: GridLayout
    private lateinit var monthYearTextView: TextView
    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var currentCalendar: Calendar


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        calendarGridLayout = findViewById(R.id.calendarGridLayout)

        val gridLayout = findViewById<GridLayout>(R.id.calendarGridLayout)
        gridLayout.columnCount = 7

        monthYearTextView = findViewById(R.id.monthYearTextView)
        val buttonPreviousMonth = findViewById<Button>(R.id.buttonPreviousMonth)
        val buttonNextMonth = findViewById<Button>(R.id.buttonNextMonth)
        monthYearTextView.setPadding(0, 0, 0, 32)

        dbHelper = TaskDatabaseHelper(this)
        currentCalendar = Calendar.getInstance()

        buttonPreviousMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            setupCalendar()
        }

        buttonNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            setupCalendar()
        }

        setupCalendar()
    }


    private fun addWeekDayLabels() {
        val daysOfWeek = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
        for (day in daysOfWeek) {
            val textView = TextView(this).apply {
                text = day
                textSize = 14f
                setPadding(2, 2, 2, 2)
                setTextColor(resources.getColor(R.color.black, theme))
                gravity = android.view.Gravity.CENTER // Центрируем текст
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(2, 2, 2, 2)
                }
            }
            calendarGridLayout.addView(textView)
        }
    }
    private fun setupCalendar() {
        // Очищаем предыдущие элементы
        calendarGridLayout.removeAllViews()

        val calendar = currentCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // Обновляем заголовок с текущим месяцем и годом
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = dateFormat.format(calendar.time)
        addWeekDayLabels()

        var firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Если первый день недели — воскресенье (firstDayOfWeek == 0), то добавляем 6 пустых элементов
        if (firstDayOfWeek == 0) {
            firstDayOfWeek = 7
        }

        // Добавляем пустые элементы для дней перед первым днем месяца
        for (i in 0 until firstDayOfWeek - 1) {
            addEmptyViewToGridLayout()
        }

        // Добавляем элементы для каждого дня
        for (day in 1..daysInMonth) {
            val dateInMillis = calendar.timeInMillis
            val dayView = createDayView(day, dateInMillis)
            calendarGridLayout.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun addEmptyViewToGridLayout() {
        val emptyView = View(this).apply {
            id = View.generateViewId()
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(2, 2, 2, 2)
            }
        }
        Log.d("CalendarActivity", "Добавляем пустой элемент с ID: ${emptyView.id}")
        calendarGridLayout.addView(emptyView)
    }
    private fun addViewToGridLayout(view: View) {
        val params = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.WRAP_CONTENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(2, 2, 2, 2)
        }
        view.layoutParams = params
        Log.d("CalendarActivity", "Добавляем элемент для дня с ID: ${view.id}")
        calendarGridLayout.addView(view)
    }


    private fun createDayView(day: Int, dateInMillis: Long): View {
        val dayLayout = layoutInflater.inflate(R.layout.calendar_day_layout, null)
        val dateTextView = dayLayout.findViewById<TextView>(R.id.dateTextView)
        val tasksLayout = dayLayout.findViewById<LinearLayout>(R.id.tasksLayout)

        dateTextView.text = day.toString()

        val tasks = dbHelper.getTasksForDay(dateInMillis, dateInMillis + 86400000)
        tasks.sortedBy { it.startTime }.forEach { task ->
            val taskTextView = TextView(this).apply {
                text = task.title
                textSize = 12f
                setPadding(2, 2, 2, 2)
            }
            tasksLayout.addView(taskTextView)
        }

        dayLayout.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(2, 2, 2, 2)
        }

        return dayLayout
    }

}
