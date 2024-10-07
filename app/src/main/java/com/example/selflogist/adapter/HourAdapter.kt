package com.example.selflogist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.selflogist.R
import com.example.selflogist.model.Task

class HourAdapter(private val hours: List<Int>, private val tasks: List<Task>) : RecyclerView.Adapter<HourAdapter.HourViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hour, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        val hour = hours[position]
        holder.bind(hour, tasks)
    }

    override fun getItemCount(): Int {
        return hours.size
    }

    class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hourLabel: TextView = itemView.findViewById(R.id.hourLabel)
        private val eventView: View = itemView.findViewById(R.id.eventView)

        fun bind(hour: Int, tasks: List<Task>) {
            hourLabel.text = "$hour:00"
            val task = tasks.find { task ->
                val taskStartHour = (task.startTime / 3600000) % 24
                val taskEndHour = (task.endTime / 3600000) % 24
                hour in taskStartHour..taskEndHour
            }
            if (task != null) {
                eventView.visibility = View.VISIBLE
                val layoutParams = eventView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.width = 0 // Assuming you want full width for now
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
                eventView.layoutParams = layoutParams
            } else {
                eventView.visibility = View.GONE
            }
        }
    }
}