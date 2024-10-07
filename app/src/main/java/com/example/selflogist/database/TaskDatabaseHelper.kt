package com.example.selflogist.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.selflogist.model.Task
import java.util.Calendar

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "tasks.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_START_TIME = "startTime"
        const val COLUMN_END_TIME = "endTime"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_TITLE TEXT," +
                "$COLUMN_DESCRIPTION TEXT," +
                "$COLUMN_START_TIME INTEGER," +
                "$COLUMN_END_TIME INTEGER)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertTask(task: Task): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_START_TIME, task.startTime)
            put(COLUMN_END_TIME, task.endTime)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getTasksForToday(): List<Task> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val endOfDay = calendar.timeInMillis

        return getTasksForDay(startOfDay, endOfDay)
    }

    fun getTasksForDay(startOfDay: Long, endOfDay: Long): List<Task> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME, null,
            "$COLUMN_START_TIME >= ? AND $COLUMN_END_TIME <= ?",
            arrayOf(startOfDay.toString(), endOfDay.toString()), null, null, null
        )
        val tasks = mutableListOf<Task>()
        if (cursor.moveToFirst()) {
            do {
                val task = Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    startTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                    endTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME))
                )
                tasks.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }

    // Новый метод для получения задачи по ID
    fun getTaskById(id: Long): Task? {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME, null, "$COLUMN_ID = ?",
            arrayOf(id.toString()), null, null, null
        )

        return if (cursor.moveToFirst()) {
            val task = Task(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)), // Используем getInt для ID
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                startTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                endTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME))
            )
            cursor.close()
            task
        } else {
            cursor.close()
            null
        }
    }

    fun updateTask(task: Task): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_START_TIME, task.startTime)
            put(COLUMN_END_TIME, task.endTime)
        }
        return db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(task: Task): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }
    fun getAllTasks(): List<Task> {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_START_TIME ASC")
        val tasks = mutableListOf<Task>()
        if (cursor.moveToFirst()) {
            do {
                val task = Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    startTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                    endTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME))
                )
                tasks.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }
}
