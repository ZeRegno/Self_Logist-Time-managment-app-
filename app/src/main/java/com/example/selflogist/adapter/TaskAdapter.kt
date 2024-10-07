package com.example.selflogist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.selflogist.R
import com.example.selflogist.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskDeleted: (Task) -> Unit,
    private val onTaskCompleted: (Task) -> Unit,
    private val onTaskEdited: (Task) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TASK = 1
        private const val VIEW_TYPE_DATE_HEADER = 0
    }

    // Формат для отображения времени и даты
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var items: List<Any> = emptyList()

    init {
        updateItems()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Task -> VIEW_TYPE_TASK
            is String -> VIEW_TYPE_DATE_HEADER
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TASK) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
            TaskViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            DateHeaderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TaskViewHolder) {
            val task = items[position] as Task
            holder.bind(task)
        } else if (holder is DateHeaderViewHolder) {
            val date = items[position] as String
            holder.bind(date)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks.sortedBy { it.startTime }
        updateItems()
        notifyDataSetChanged()
    }

    private fun updateItems() {
        val groupedTasks = tasks.groupBy { dateFormat.format(Date(it.startTime)) }
        val result = mutableListOf<Any>()

        groupedTasks.forEach { (date, tasks) ->
            result.add(date)
            result.addAll(tasks)
        }

        items = result
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskTime: TextView = itemView.findViewById(R.id.taskTime)
        private val taskDate: TextView = itemView.findViewById(R.id.taskDate)
        private val taskOptionsMenu: ImageView = itemView.findViewById(R.id.taskOptionsMenu)
        private val taskDescription: TextView = itemView.findViewById(R.id.taskDescription) // Изменен на TextView

        private var isExpanded = false

        fun bind(task: Task) {
            taskTitle.text = task.title

            // Форматируем время начала и окончания задачи
            val startTime = timeFormat.format(Date(task.startTime))
            val endTime = timeFormat.format(Date(task.endTime))
            taskTime.text = "$startTime - $endTime"

            // Проверяем, нужно ли отображать дату
            val startDate = dateFormat.format(Date(task.startTime))
            val endDate = dateFormat.format(Date(task.endTime))

            if (startDate != endDate) {
                taskDate.text = "$startDate - $endDate"
                taskDate.visibility = View.VISIBLE
            } else if (Date().before(Date(task.startTime))) {
                taskDate.text = startDate
                taskDate.visibility = View.VISIBLE
            } else {
                taskDate.visibility = View.GONE
            }

            // Отображение описания задачи
            taskDescription.text = task.description
            taskDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Разворачивание/сворачивание задачи при нажатии на неё
            itemView.setOnClickListener {
                isExpanded = !isExpanded
                taskDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE
            }

            taskOptionsMenu.setOnClickListener {
                showPopupMenu(it, task)
            }
        }

        private fun showPopupMenu(view: View, task: Task) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.task_options_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        onTaskEdited(task)
                        true
                    }
                    R.id.menu_complete -> {
                        onTaskCompleted(task)
                        true
                    }
                    R.id.menu_delete -> {
                        showDeleteConfirmationDialog(view.context, task)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun showDeleteConfirmationDialog(context: Context, task: Task) {
            AlertDialog.Builder(context)
                .setTitle("Удалить задачу")
                .setMessage("Действительно ли вы хотите удалить задачу '${task.title}'?")
                .setPositiveButton("Удалить") { dialog, _ ->
                    onTaskDeleted(task)
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    inner class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateHeader: TextView = itemView.findViewById(R.id.dateHeader)

        fun bind(date: String) {
            dateHeader.text = date
        }
    }
}