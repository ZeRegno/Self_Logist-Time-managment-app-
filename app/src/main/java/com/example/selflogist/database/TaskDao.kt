package com.example.selflogist.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor  // Добавлен импорт для Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.selflogist.model.Task

class TaskDao(context: Context) {

    private val dbHelper = TaskDatabaseHelper(context)

    fun insertTasks(tasks: List<Task>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (task in tasks) {
                val contentValues = ContentValues().apply {
                    put(TaskDatabaseHelper.COLUMN_TITLE, task.title)
                    put(TaskDatabaseHelper.COLUMN_DESCRIPTION, task.description)
                    put(TaskDatabaseHelper.COLUMN_START_TIME, task.startTime)
                    put(TaskDatabaseHelper.COLUMN_END_TIME, task.endTime)
                }
                db.insertWithOnConflict(TaskDatabaseHelper.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun updateTask(task: Task) {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(TaskDatabaseHelper.COLUMN_TITLE, task.title)
            put(TaskDatabaseHelper.COLUMN_DESCRIPTION, task.description)
            put(TaskDatabaseHelper.COLUMN_START_TIME, task.startTime)
            put(TaskDatabaseHelper.COLUMN_END_TIME, task.endTime)
        }
        db.update(
            TaskDatabaseHelper.TABLE_NAME,
            contentValues,
            "${TaskDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(task.id.toString())
        )
    }

    fun deleteTask(task: Task) {
        val db = dbHelper.writableDatabase
        db.delete(
            TaskDatabaseHelper.TABLE_NAME,
            "${TaskDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(task.id.toString())
        )
    }

    fun getTasksForDay(startOfDay: Long, endOfDay: Long): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            TaskDatabaseHelper.TABLE_NAME,
            null,
            "${TaskDatabaseHelper.COLUMN_START_TIME} >= ? AND ${TaskDatabaseHelper.COLUMN_END_TIME} <= ?",
            arrayOf(startOfDay.toString(), endOfDay.toString()),
            null,
            null,
            null
        )

        val tasks = mutableListOf<Task>()
        if (cursor.moveToFirst()) {
            do {
                val task = Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_DESCRIPTION)),
                    startTime = cursor.getLong(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_START_TIME)),
                    endTime = cursor.getLong(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_END_TIME))
                )
                tasks.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }
}
